package io.github.sinri.keel.facade.tesuto.unit;

import io.github.sinri.keel.core.json.JsonifiableSerializer;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Vertx;

import javax.annotation.Nonnull;

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
public abstract class KeelJUnit5Test implements KeelJUnit5TestCore {
    private final KeelIssueRecorder<KeelEventLog> unitTestLogger;

    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public KeelJUnit5Test(Vertx vertx) {
        // System.out.println("KeelJUnit5Test constructor with vertx " + vertx);

        JsonifiableSerializer.register();
        Keel.initializeVertx(vertx);
        Keel.getConfiguration().loadPropertiesFile("config.properties");

        this.unitTestLogger = buildUnitTestLogger();
    }

    @Nonnull
    protected KeelIssueRecorder<KeelEventLog> buildUnitTestLogger() {
        return KeelIssueRecordCenter.outputCenter().generateIssueRecorder("KeelJUnit5Test", KeelEventLog::new);
    }

    @Override
    public final KeelIssueRecorder<KeelEventLog> getUnitTestLogger() {
        return unitTestLogger;
    }
}
