package io.github.sinri.keel.tesuto;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.json.JsonifiableSerializer;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;


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
@NullMarked
@ExtendWith(VertxExtension.class)
public abstract class KeelJUnit5Test implements Keel {

    private final Logger unitTestLogger;
    private final Vertx vertx;

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     *
     * @param vertx 由 VertxExtension 提供的 Vertx 实例。
     */
    public KeelJUnit5Test(Vertx vertx) {
        JsonifiableSerializer.register();
        this.vertx = vertx;
        try {
            this.loadLocalConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SHARED_LOGGER_FACTORY_REF.set(buildLoggerFactory());
        this.unitTestLogger = buildUnitTestLogger();
    }

    /**
     * 加载执行测试必要的本地配置。
     * <p>
     * 默认加载 config.properties 文件内容到 Keel 的配置中，可用 {@link Keel#getConfiguration()} 获取。
     * <p>
     * 如果你无需加载本地配置或需要特殊实现，可以重写此方法。
     *
     * @throws Exception 加载配置过程中出现的异常，如配置文件不存在等情况。
     */
    protected void loadLocalConfig() throws Exception {
        try {
            getConfiguration().loadPropertiesFile("config.properties");
        } catch (IOException ioException) {
            throw new Exception("Failed to load config.properties", ioException);
        }
    }

    public LoggerFactory buildLoggerFactory() {
        return StdoutLoggerFactory.getInstance();
    }

    /**
     * 获取本类运行时的 Vertx 实例；其通过构造函数注册在 Keel 中。
     *
     * @return 本类运行时的 Vertx 实例
     */
    public final Vertx getVertx() {
        return vertx;
    }

    /**
     * 为这个单元测试类构建 Logger 实例。
     * <p>
     * 仅设计在测试类的构造函数中调用。
     *
     * @return Logger 实例
     */
    protected Logger buildUnitTestLogger() {
        return getLoggerFactory().createLogger(getClass().getName());
    }

    /**
     * 获取构造函数中通过{@link KeelJUnit5Test#buildUnitTestLogger()}方法构建的 Logger 实例。
     *
     * @return 本类通用的 Logger 实例。
     */
    public final Logger getUnitTestLogger() {
        return unitTestLogger;
    }

    //    /**
    //     * 默认提供测试方法。在实现中，需要加上{@link Test}注解。
    //     * <p>
    //     * 提供了方法参数{@code testContext}，类型为{@link VertxTestContext}，用于异步测试的结束回调。
    //     *
    //     * @param testContext 测试上下文，用于等待异步操作的结果
    //     */
    //    @Test
    //    abstract protected void test(VertxTestContext testContext);
}
