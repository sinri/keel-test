# Keel-Test 5.0.2 使用文档

Keel-Test 是 [Keel 框架](https://github.com/sinri/keel) 生态中的测试工具库，为基于 [Vert.x 5](https://vertx.io/) 和 Keel 的项目提供测试基础设施。

本库提供两个核心抽象基类，分别对应两种典型的测试场景：

| 基类                  | 适用场景                | 详细文档                                      |
|---------------------|---------------------|-------------------------------------------|
| `KeelJUnit5Test`    | JUnit 5 单元测试 / 集成测试 | [KeelJUnit5Test 文档](KeelJUnit5Test)       |
| `KeelInstantRunner` | IDE 中快速运行验证逻辑       | [KeelInstantRunner 文档](KeelInstantRunner) |

## 环境要求

- **Java** 17+
- **Vert.x** 5.0.8+
- **Keel Base** 5.0.2+
- **JUnit 5**（JUnit Platform）

## 引入依赖

> **注意**：Keel-Test 应仅作为测试依赖引入，不应用于生产代码。

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    testImplementation("io.github.sinri:keel-test:5.0.2")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    testImplementation 'io.github.sinri:keel-test:5.0.2'
}
```

### Maven

```xml

<dependency>
    <groupId>io.github.sinri</groupId>
    <artifactId>keel-test</artifactId>
    <version>5.0.2</version>
    <scope>test</scope>
</dependency>
```

## 快速开始

### 1. 编写 JUnit 5 单元测试

继承 `KeelJUnit5Test`，即可获得已初始化的 Keel + Vert.x 测试环境。

```java
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

public class MyServiceTest extends KeelJUnit5Test {

    @Test
    void testAsyncOperation(VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();
        getVertx().setTimer(1000L, id -> {
            getUnitTestLogger().info("异步操作完成");
            checkpoint.flag();
        });
    }

    @Test
    void testSyncOperation() {
        getUnitTestLogger().info("Keel 实例: " + getKeel());
    }
}
```

### 2. 使用 KeelInstantRunner 快速验证

继承 `KeelInstantRunner`，在 IDE 中直接运行 `main` 方法：

```java
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;

public class MyQuickTest extends KeelInstantRunner {

    @Override
    protected Future<Void> run() throws Exception {
        getLogger().info("执行验证逻辑...");
        return Future.succeededFuture();
    }
}
```

## 配置加载

两个基类在启动时均会自动加载 classpath 下的 `config.properties` 文件，并将内容注册到 `ConfigElement.root()` 中。

将测试配置文件放置在 `src/test/resources/config.properties`：

```properties
db.host=localhost
db.port=3306
```

在测试代码中访问配置：

```java
String dbHost = ConfigElement.root().getSubElement("db.host").getAsString();
```

如不需要加载配置文件，可重写对应方法：

- `KeelJUnit5Test` → 重写 `loadLocalConfig()`
- `KeelInstantRunner` → 重写 `loadLocalConfiguration()`

## 项目关系

本库是 Keel 框架生态的一部分：

- [keel](https://github.com/sinri/keel) — Keel 主框架
- [keel-base](https://github.com/sinri/keel-base) — Keel 基础库（本库的核心依赖）

## 许可证

本项目基于 [GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.txt) 许可发布。
