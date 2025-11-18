package io.github.sinri.keel.tesuto;

import io.github.sinri.keel.base.KeelInstance;
import io.github.sinri.keel.base.json.JsonifiableSerializer;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * Vertx JUnit5 单元测试的基类。
 * <p>
 * 本类的构造函数将在注解为{@code @BeforeAll}的静态方法运行后运行，如果该方法存在。
 * 在构造方法中，将会以给定的Vertx实例初始化Keel。
 * <p>
 * 所有测试方法均需要加上{@code @Test}注解；
 * 如果测试方法内执行了异步逻辑，则需要加上方法参数{@code testContext}，类型为{@link VertxTestContext}。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
public abstract class KeelJUnit5Test {
    @NotNull
    private final Logger unitTestLogger;

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     */
    public KeelJUnit5Test(Vertx vertx) {
        JsonifiableSerializer.register();
        Keel.initializeVertx(vertx);
        try {
            this.loadLocalConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.unitTestLogger = buildUnitTestLogger();
    }

    /**
     * 加载执行测试必要的本地配置。
     * <p>
     * 默认加载 config.properties 文件内容到 Keel 的配置中，可用 {@link KeelInstance#getConfiguration()} 获取。
     *
     * @throws Exception 加载配置过程中出现的异常，如配置文件不存在等情况。
     */
    protected void loadLocalConfig() throws Exception {
        try {
            Keel.getConfiguration().loadPropertiesFile("config.properties");
        } catch (IOException ioException) {
            throw new Exception("Failed to load config.properties", ioException);
        }
    }

    @NotNull
    protected final Vertx getVertx() {
        return Keel.getVertx();
    }

    @NotNull
    protected Logger buildUnitTestLogger() {
        return StdoutLoggerFactory.getInstance().createLogger("KeelJUnit5Test");
    }

    @NotNull
    public final Logger getUnitTestLogger() {
        return unitTestLogger;
    }

    /**
     * 默认提供测试方法。在实现中，需要加上{@link Test}注解。
     * <p>
     * 提供了方法参数{@code testContext}，类型为{@link VertxTestContext}，用于异步测试的结束回调。
     *
     * @param testContext 测试上下文，用于等待异步操作的结果
     */
    @Test
    abstract protected void test(VertxTestContext testContext);
}
