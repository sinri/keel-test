# KeelInstantRunner

`io.github.sinri.keel.tesuto.KeelInstantRunner` 是即时运行类，提供了一个可在 IDE 中直接运行的快速可执行程序基础实现。

与 `KeelJUnit5Test` 不同，`KeelInstantRunner` 不依赖 JUnit 框架。它自带
`main` 方法，适合在开发过程中快速验证某段 Vert.x 异步逻辑，而不必编写完整的测试用例。

## 类定义

```java
public abstract class KeelInstantRunner { ...
}
```

## 运行方式

`KeelInstantRunner` 的子类可以直接在 IDE 中运行——右键点击类名，选择 "Run" 即可。框架会自动通过
`ProcessHandle` API 识别当前运行的子类，并通过反射实例化执行。

## 生命周期

```
main(args)
  └─ 通过 ProcessHandle 识别调用类
  └─ 反射创建子类实例
  └─ 调用 launch(args)
      │
      ▼
launch(args)
  ├─ 保存命令行参数
  ├─ 调用 loadLocalConfiguration() 加载配置
  ├─ 创建 Vertx 实例（使用 buildVertxOptions()）
  ├─ 注册全局 Keel 实例
  ├─ 初始化 LoggerFactory 和 Logger
  │
  ├─ beforeRun()         ← 准备工作
  │
  ├─ 部署 Verticle
  │   └─ run()           ← 正式逻辑（在 Verticle 中执行）
  │       └─ afterRun()  ← 清理工作（无论成功或失败都会执行）
  │
  ├─ 等待 CountDownLatch
  │
  └─ 关闭 Keel 和 Vertx，调用 System.exit()
```

### 关键流程说明

1. `run()` 方法在一个临时部署的 `KeelVerticleBase` 中执行，确保异步逻辑运行在 Vert.x 事件循环中。
2. `afterRun()` 在 `run()` 完成后（无论成功或失败）总是会被调用，适合用于资源清理。
3. 整个执行流程结束后，程序会自动关闭 Vertx 实例并退出进程。

## API 参考

### 抽象方法（必须实现）

#### run()

```java
private abstract Future<Void> run() throws Exception;
```

正式逻辑入口。实现此方法以编写需要验证的异步逻辑。

返回的 `Future<Void>` 完成后，框架会自动调用 `afterRun()` 并随后关闭 Vertx 实例。

```java

@Override
private Future<Void> run() throws Exception {
    getLogger().info("开始执行");
    return getKeel().asyncSleep(2000L)
                    .compose(v -> {
                        getLogger().info("执行完毕");
                        return Future.succeededFuture();
                    });
}
```

### 实例方法

#### getKeel()

```java
public Keel getKeel()
```

返回全局共享的 `Keel` 实例。

#### getLogger()

```java
public Logger getLogger()
```

返回当前运行实例的 `Logger`，日志主题为当前类的全限定名。该 Logger 在 `launch()` 方法中初始化，请勿在构造方法中调用。

#### getArgs()

```java
public List<String> getArgs()
```

返回 `main` 方法接收到的命令行参数列表。

### 可重写方法

#### loadLocalConfiguration()

```java
private void loadLocalConfiguration() throws IOException
```

加载本地配置。默认从 classpath 加载 `config.properties` 到 `ConfigElement.root()`。

如不需要配置文件：

```java

@Override
private void loadLocalConfiguration() throws IOException {
    // 不加载任何配置
}
```

#### buildVertxOptions()

```java
public VertxOptions buildVertxOptions()
```

构建创建 Vertx 实例时使用的选项。默认返回 `new VertxOptions()`。

重写此方法以自定义 Vert.x 行为，例如设置事件循环线程数：

```java

@Override
public VertxOptions buildVertxOptions() {
    return new VertxOptions().setEventLoopPoolSize(4);
}
```

#### buildLoggerFactory()

```java
private LoggerFactory buildLoggerFactory()
```

构建日志工厂。默认返回 `StdoutLoggerFactory.getInstance()`。

#### buildVisibleLogLevel()

```java
private LogLevel buildVisibleLogLevel()
```

设置日志的可见级别。默认为 `LogLevel.DEBUG`，即所有级别的日志都会输出。

如只需查看 INFO 及以上级别日志：

```java

@Override
private LogLevel buildVisibleLogLevel() {
    return LogLevel.INFO;
}
```

#### buildDeploymentOptions()

```java
private DeploymentOptions buildDeploymentOptions()
```

构建运行 `run()` 方法的 Verticle 部署选项。默认返回 `new DeploymentOptions()`。

#### beforeRun()

```java
private Future<Void> beforeRun()
```

在 `run()` 之前执行的准备工作。默认实现只打印一条调试日志。

```java

@Override
private Future<Void> beforeRun() {
    getLogger().info("初始化数据库连接...");
    return initDatabase();
}
```

#### afterRun()

```java
private Future<Void> afterRun()
```

在 `run()` 之后执行的清理工作（无论 `run()` 成功或失败）。默认实现只打印一条调试日志。

```java

@Override
private Future<Void> afterRun() {
    getLogger().info("关闭数据库连接...");
    return closeDatabase();
}
```

## 使用模式

### 基础用法

最简单的使用方式——只需实现 `run()` 方法：

```java
public class SimpleRunner extends KeelInstantRunner {

    @Override
    protected Future<Void> run() throws Exception {
        getLogger().info("Hello from KeelInstantRunner!");
        return Future.succeededFuture();
    }
}
```

### 多步骤异步逻辑

利用 Keel 提供的异步工具方法：

```java
public class StepwiseRunner extends KeelInstantRunner {

    @Override
    protected Future<Void> run() throws Exception {
        getLogger().info("开始");
        return getKeel().asyncCallStepwise(5, i -> {
            getLogger().info("步骤 " + i);
            return getKeel().asyncSleep(1000L);
        }).compose(v -> {
            getLogger().info("全部步骤完成");
            return Future.succeededFuture();
        });
    }
}
```

### 带前置/后置处理

```java
public class FullLifecycleRunner extends KeelInstantRunner {

    @Override
    protected Future<Void> beforeRun() {
        getLogger().info("准备测试环境...");
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> run() throws Exception {
        getLogger().info("执行核心逻辑");
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> afterRun() {
        getLogger().info("清理测试环境...");
        return Future.succeededFuture();
    }
}
```

### 处理失败场景

当 `run()` 返回失败的 Future 时，框架会自动记录错误日志：

```java
public class FailureRunner extends KeelInstantRunner {

    @Override
    protected Future<Void> run() throws Exception {
        return Future.failedFuture("模拟失败场景");
    }
}
```

输出类似：

```
FATAL - RUN FAILED
  Exception: 模拟失败场景
```

### 使用命令行参数

```java
public class ArgsRunner extends KeelInstantRunner {

    @Override
    protected Future<Void> run() throws Exception {
        List<String> args = getArgs();
        getLogger().info("接收到 " + args.size() + " 个参数");
        for (int i = 0; i < args.size(); i++) {
            getLogger().info("参数[" + i + "] = " + args.get(i));
        }
        return Future.succeededFuture();
    }
}
```

### 自定义 Vertx 和部署选项

```java
public class CustomOptionsRunner extends KeelInstantRunner {

    @Override
    public VertxOptions buildVertxOptions() {
        return new VertxOptions()
                .setEventLoopPoolSize(2)
                .setWorkerPoolSize(4);
    }

    @Override
    protected DeploymentOptions buildDeploymentOptions() {
        return new DeploymentOptions().setWorkerPoolSize(2);
    }

    @Override
    protected Future<Void> run() throws Exception {
        getLogger().info("使用自定义选项运行");
        return Future.succeededFuture();
    }
}
```

## 注意事项

- `KeelInstantRunner` 的 `main` 方法使用 `ProcessHandle` API（JDK 9+）获取调用类名，因此**必须使用 Java 9 或以上版本**。
- `getLogger()` 在 `launch()` 方法内初始化，**不可在构造方法中调用**。
- 程序结束时会调用 `System.exit()`，确保 Vert.x 事件循环完全关闭。若 `run()` 过程中发生 `InterruptedException`，退出码为
  `1`，否则为 `0`。
- 默认会加载 `config.properties` 文件，若不存在将抛出 `IOException`。如果不需要配置文件，请重写
  `loadLocalConfiguration()` 方法。

[返回首页](.)
