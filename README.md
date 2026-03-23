# Keel-Test

Keel 框架的测试工具库，为基于 [Vert.x](https://vertx.io/) 和 [Keel](https://github.com/sinri/keel) 的项目提供测试基础设施。

## 概述

Keel-Test 提供两个核心抽象基类：

- **`KeelJUnit5Test`** — Vert.x JUnit 5 测试基类，集成 Keel 框架的初始化、配置加载和日志管理
- **`KeelInstantRunner`** — IDE 快速执行器，用于在 IDE 中直接运行 Vert.x 逻辑进行验证

## 环境要求

- Java 17+
- Vert.x 5.x
- Keel Base 5.x
- JUnit 5

## 引入依赖

> **注意**：本库应仅作为测试依赖引入（`testImplementation`），不应用于生产代码。

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

### 使用 KeelJUnit5Test 编写单元测试

继承 `KeelJUnit5Test`，即可获得完整的 Keel + Vert.x 测试环境：

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
            getUnitTestLogger().info("Async operation completed");
            checkpoint.flag();
        });
    }

    @Test
    void testSyncOperation() {
        // 无需 VertxTestContext 的同步测试
        getUnitTestLogger().info("Keel instance: " + getKeel());
    }
}
```

### 使用 KeelInstantRunner 快速验证逻辑

继承 `KeelInstantRunner`，在 IDE 中直接运行 `main` 方法：

```java
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;

public class MyQuickTest extends KeelInstantRunner {

    @Override
    protected Future<Void> run() throws Exception {
        getLogger().info("Running quick test...");
        // 你的验证逻辑
        return Future.succeededFuture();
    }
}
```

## 配置

两个基类均会在启动时加载 classpath 下的 `config.properties` 文件。将你的测试配置放在
`src/test/resources/config.properties` 中：

```properties
# 数据库连接等测试配置
db.host=localhost
db.port=3306
```

在测试中通过 `ConfigElement.root()` 访问配置项。

如果不需要配置文件，可以重写 `loadLocalConfig()`（JUnit 5）或 `loadLocalConfiguration()`（InstantRunner）方法。

## 详细文档

版本详细使用文档请参阅 [docs/5.0.2/](docs/5.0.2/) 目录。

## 项目关系

本库是 Keel 框架生态的一部分：

- [keel](https://github.com/sinri/keel) — Keel 主框架
- [keel-base](https://github.com/sinri/keel-base) — Keel 基础库（本库的核心依赖）

## 许可证

本项目基于 [GNU Lesser General Public License v3.0](LICENSE) 许可发布。
