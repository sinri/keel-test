# KeelJUnit5Test

`io.github.sinri.keel.tesuto.KeelJUnit5Test` 是 Vert.x JUnit 5 单元测试的抽象基类，集成了 Keel 框架的初始化、配置加载和日志管理。

继承此类后，每个测试方法都运行在独立的 Vert.x 测试上下文中，无需手动管理 Vertx 实例的创建与销毁。

## 类定义

```java

@ExtendWith(VertxExtension.class)
public abstract class KeelJUnit5Test { ...
}
```

该类已使用 `@ExtendWith(VertxExtension.class)` 注解，子类无需重复声明。

## 生命周期

```
@BeforeAll (静态方法)
  └─ 初始化 RunTestOnContext，创建 Vertx 实例
  └─ 调用 Keel.share(vertx)，注册全局 Keel 实例
      │
      ▼
构造方法
  └─ 注册 JsonifiableSerializer
  └─ 调用 loadLocalConfig() 加载配置
  └─ 调用 buildLoggerFactory() 替换全局 LoggerFactory
  └─ 调用 buildUnitTestLogger() 构建测试 Logger
      │
      ▼
@BeforeEach（如有）
      │
      ▼
@Test 方法执行
      │
      ▼
@AfterEach（如有）
      │
      ▼
@AfterAll（如有）
```

> **注意**：构造方法在 `@BeforeAll` 静态方法之后运行。每个 `@Test` 方法执行前都会重新调用构造方法创建新的测试类实例（JUnit 5 默认行为）。

## API 参考

### 受保护的字段

| 字段     | 类型                 | 说明                                  |
|--------|--------------------|-------------------------------------|
| `rtoc` | `RunTestOnContext` | JUnit 5 扩展，管理测试用 Vertx 实例（`static`） |

### 构造方法

```java
public KeelJUnit5Test()
```

构造方法执行以下操作：

1. 注册 `JsonifiableSerializer`，提供 JSON 序列化能力
2. 调用 `loadLocalConfig()` 加载本地配置
3. 替换全局 `LoggerFactory` 为 `buildLoggerFactory()` 的返回值
4. 通过 `buildUnitTestLogger()` 构建本测试类专用的 Logger

### 实例方法

#### getVertx()

```java
private final Vertx getV
```

返回当前测试运行时的 `Vertx` 实例。该实例由 `RunTestOnContext` 管理，每个测试方法共享同一实例。

#### getKeel()

```java
public Keel getKeel()
```

返回全局的 `Keel` 实例。如果当前 `Vertx` 实例本身就是 `Keel` 类型，则直接注册并返回；否则基于当前 `Vertx` 创建新的
`Keel` 实例。

#### getUnitTestLogger()

```java
public Logger getUnitTestLogger()
```

返回构造函数中构建的 `Logger` 实例，日志主题默认为当前测试类的全限定类名。

### 可重写方法

#### loadLocalConfig()

```java
private void loadLocalConfig() throws Exception
```

加载测试所需的本地配置。默认实现从 classpath 加载 `config.properties` 到 `ConfigElement.root()`。

如不需要配置文件或需要自定义配置加载逻辑，可重写此方法：

```java

@Override
private void loadLocalConfig() throws Exception {
    // 不加载任何配置
}
```

或加载自定义配置：

```java

@Override
private void loadLocalConfig() throws Exception {
    ConfigElement.root().loadPropertiesFile("my-test-config.properties");
}
```

#### buildLoggerFactory()

```java
public LoggerFactory buildLoggerFactory()
```

构建本测试类使用的 `LoggerFactory`。默认返回 `StdoutLoggerFactory.getInstance()`（输出到标准输出）。

重写此方法以使用自定义的日志工厂：

```java

@Override
public LoggerFactory buildLoggerFactory() {
    return MyCustomLoggerFactory.getInstance();
}
```

#### buildUnitTestLogger()

```java
private Logger buildUnitTestLogger()
```

构建本测试类专用的 `Logger` 实例。默认以当前类的全限定名为日志主题。

## 使用模式

### 基础异步测试

使用 `VertxTestContext` 和 `Checkpoint` 控制异步测试的完成：

```java
public class BasicAsyncTest extends KeelJUnit5Test {

    @Test
    void testTimer(VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();
        getVertx().setTimer(1000L, id -> {
            getUnitTestLogger().info("定时器触发");
            checkpoint.flag();
        });
    }
}
```

### 多检查点测试

一个测试方法中可设置多个检查点，所有检查点都被标记后测试才视为通过：

```java

@Test
void testMultipleCheckpoints(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);

    for (int i = 0; i < 3; i++) {
        int step = i;
        getVertx().setTimer(500L * (i + 1), id -> {
            getUnitTestLogger().info("步骤 " + step + " 完成");
            checkpoint.flag();
        });
    }
}
```

### 同步测试

不涉及异步逻辑的测试方法无需声明 `VertxTestContext` 参数：

```java

@Test
void testSync() {
    getUnitTestLogger().info("同步测试 - Keel: " + getKeel());
}
```

### 使用 @BeforeAll / @AfterAll

子类可定义自己的 `@BeforeAll` 和 `@AfterAll` 方法，但需声明为 `static`。可通过 `rtoc` 字段获取 Vertx 实例：

```java
public class LifecycleTest extends KeelJUnit5Test {

    @BeforeAll
    static void setup(VertxTestContext testContext) {
        Vertx vertx = rtoc.vertx();
        vertx.setTimer(1000L, id -> {
            // 异步初始化完成
            testContext.completeNow();
        });
    }

    @AfterAll
    static void teardown(VertxTestContext testContext) {
        Vertx vertx = rtoc.vertx();
        vertx.setTimer(500L, id -> {
            // 异步清理完成
            testContext.completeNow();
        });
    }

    @Test
    void test(VertxTestContext testContext) {
        testContext.completeNow();
    }
}
```

### 使用 @BeforeEach / @AfterEach

每个测试方法前后执行的逻辑可通过实例方法定义：

```java
public class EachLifecycleTest extends KeelJUnit5Test {

    @BeforeEach
    void beforeEach() {
        getUnitTestLogger().info("测试方法即将开始");
    }

    @AfterEach
    void afterEach() {
        getUnitTestLogger().info("测试方法已结束");
    }

    @Test
    void testA(VertxTestContext testContext) {
        testContext.completeNow();
    }
}
```

> **提示**：`@BeforeEach` / `@AfterEach` 方法同样可以接收 `VertxTestContext` 参数以支持异步操作。

### 继承基类扩展

可以创建自己的中间基类，在其中封装通用的测试初始化逻辑，然后让具体测试类继承：

```java
public class MyBaseTest extends KeelJUnit5Test {

    @BeforeAll
    static void commonSetup(VertxTestContext testContext) {
        // 通用初始化
        testContext.completeNow();
    }
}

public class MyFeatureTest extends MyBaseTest {

    @Test
    void testFeature(VertxTestContext testContext) {
        // 具体测试逻辑
        testContext.completeNow();
    }
}
```

## 注意事项

- 构造方法中若 `config.properties` 不存在会抛出异常。如果测试不需要配置文件，请重写 `loadLocalConfig()` 方法。
- `rtoc` 字段是 `static` 的，在 `@BeforeAll` 等静态方法中可通过 `rtoc.vertx()` 访问 Vertx 实例。
- 每个 `@Test` 方法默认超时时间由 Vert.x JUnit 5 扩展控制（默认 30 秒）。

[返回首页](.)
