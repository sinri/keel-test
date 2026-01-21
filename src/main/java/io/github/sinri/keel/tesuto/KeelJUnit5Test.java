package io.github.sinri.keel.tesuto;

import io.github.sinri.keel.base.SharedVertxStorage;
import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.json.JsonifiableSerializer;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Vertx;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

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
public abstract class KeelJUnit5Test implements KeelAsyncMixin {
    /**
     * A static instance of {@link RunTestOnContext} registered as a JUnit 5 extension.
     * <p>
     * This field is utilized to run test cases within a dedicated Vert.x test context,
     * simplifying the management of an isolated and thread-safe execution environment
     * for each test method in subclasses of {@link KeelJUnit5Test}.
     * <p>
     * The {@code rtoc} instance provides access to a {@link Vertx} instance,
     * which is initialized and managed for testing purposes, ensuring that each
     * test operates with a fresh and consistent Vert.x context.
     */
    @RegisterExtension
    protected final static RunTestOnContext rtoc = new RunTestOnContext();

    private final Logger unitTestLogger;

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     */
    public KeelJUnit5Test() {
        JsonifiableSerializer.register();
        try {
            this.loadLocalConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LoggerFactory.replaceShared(buildLoggerFactory());
        this.unitTestLogger = buildUnitTestLogger();
    }

    @BeforeAll
    public static void beforeAll() throws Exception {
        // 需要在 BeforeAll 方法中让 io.vertx.junit5.RunTestOnContext.vertx 完成初始化，这样后续的构造方法 Test 方法
        // System.out.println("io.github.sinri.keel.tesuto.KeelJUnit5Test.beforeAll: io.github.sinri.keel.tesuto.KeelJUnit5Test.rtoc.vertx is " + rtoc.vertx());
        SharedVertxStorage.ensure(rtoc.vertx());
    }

    /**
     * 加载执行测试必要的本地配置。
     * <p>
     * 默认加载 config.properties 文件内容到 Keel 的配置中，可用 {@link ConfigElement#root()} 获取。
     * <p>
     * 如果你无需加载本地配置或需要特殊实现，可以重写此方法。
     *
     * @throws Exception 加载配置过程中出现的异常，如配置文件不存在等情况。
     */
    protected void loadLocalConfig() throws Exception {
        try {
            ConfigElement.root().loadPropertiesFile("config.properties");
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
        return rtoc.vertx();
    }

    /**
     * 为这个单元测试类构建 Logger 实例。
     * <p>
     * 仅设计在测试类的构造函数中调用。
     *
     * @return Logger 实例
     */
    protected Logger buildUnitTestLogger() {
        return LoggerFactory.getShared().createLogger(getClass().getName());
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
