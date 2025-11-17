package io.github.sinri.keel.tesuto;

import io.github.sinri.keel.base.json.JsonifiableSerializer;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * The base class for Vertx-JUnit5 unit test classes.
 * <p>
 * Any implementation of this class should be annotated with {@code @ExtendWith(VertxExtension.class)}.
 * <p>
 * The constructor would run after the {@code @BeforeAll} annotated method (if defined)
 * to initialize Keel with the Vertx instance provided by the Vertx-JUnit5 framework.
 * <p>
 * For those methods annotated with {@code @Test},
 * if it runs in async mode with vertx event loop, parameter {@code VertxTestContext testContext} should be provided to
 * await the async test logic ends.
 *
 * @since 4.1.1
 */
@ExtendWith(VertxExtension.class)
public abstract class KeelJUnit5Test {
    @NotNull
    private final Logger unitTestLogger;

    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public KeelJUnit5Test(Vertx vertx) {
        JsonifiableSerializer.register();
        Keel.initializeVertx(vertx);
        try {
            this.loadLocalConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.unitTestLogger = buildUnitTestLogger();
    }

    protected void loadLocalConfig() throws IOException {
        try {
            Keel.getConfiguration().loadPropertiesFile("config.properties");
        } catch (IOException ignored) {
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

    abstract protected void test(VertxTestContext testContext);
}
