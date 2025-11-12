package io.github.sinri.keel.facade.tesuto;

import io.github.sinri.keel.core.json.JsonifiableSerializer;
import io.github.sinri.keel.logger.api.event.EventRecorder;
import io.github.sinri.keel.logger.base.factory.BaseRecorderFactory;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.Nonnull;
import java.io.IOException;

import static io.github.sinri.keel.facade.KeelInstance.Keel;


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
    @Nonnull
    private final EventRecorder unitTestLogger;

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
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    @Nonnull
    protected final Vertx getVertx() {
        return Keel.getVertx();
    }

    @Nonnull
    protected EventRecorder buildUnitTestLogger() {
        return BaseRecorderFactory.getInstance().createEventRecorder("KeelJUnit5Test");
    }

    @Nonnull
    public final EventRecorder getUnitTestLogger() {
        return unitTestLogger;
    }

    abstract protected void test(VertxTestContext testContext);
}
