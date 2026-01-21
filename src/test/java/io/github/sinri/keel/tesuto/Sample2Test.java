package io.github.sinri.keel.tesuto;

import io.github.sinri.keel.base.logger.logger.StdoutLogger;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.*;

@NullMarked
public class Sample2Test extends KeelJUnit5Test {
    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public Sample2Test() {
        super();
        getUnitTestLogger().info("Sample2Test RTOC Vertx: " + rtoc.vertx());
    }

    @BeforeAll
    static void beforeAll(VertxTestContext testContext) {
        Vertx vertx = rtoc.vertx();
        StdoutLogger stdoutLogger = new StdoutLogger(Sample2Test.class.getName());
        stdoutLogger.info("beforeAll(" + vertx + "," + testContext + ")");
        vertx.setTimer(1000L, id -> {
            stdoutLogger.info("beforeAll Timer fired!");
            testContext.completeNow();
        });
    }

    @AfterAll
    static void afterAll(VertxTestContext testContext) {
        Vertx vertx = rtoc.vertx();
        StdoutLogger stdoutLogger = new StdoutLogger(Sample2Test.class.getName());
        stdoutLogger.info("afterAll(" + vertx + "," + testContext + ")");
        vertx.setTimer(1000L, id -> {
            stdoutLogger.info("afterAll Timer fired!");
            testContext.completeNow();
        });
    }

    @BeforeEach
    void beforeEach(
            // VertxTestContext testContext
    ) {
        getUnitTestLogger().info("beforeEach");
        //getUnitTestLogger().info("beforeEach("+testContext+") with field `vertx` "+getVertx());
        //        getVertx().setTimer(1000L, id -> {
        //            getUnitTestLogger().info("beforeEach Timer fired!");
        //            testContext.completeNow();
        //        });
    }

    @Test
    void test1(VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();

        getUnitTestLogger().info("Test1 started with testContext: " + testContext + " and Vertx: " + rtoc.vertx());

        getVertx().setTimer(2000L, id -> {
            getUnitTestLogger().info("Timer fired!");
            checkpoint.flag();
        });
    }

    @Test
    void test2(VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();

        getUnitTestLogger().info("Test2 started with testContext: " + testContext + " and Vertx: " + rtoc.vertx());

        getVertx().setTimer(2000L, id -> {
            getUnitTestLogger().info("Timer fired!");
            checkpoint.flag();
        });
    }

    @AfterEach
    void afterEach(
            //VertxTestContext testContext
    ) {
        getUnitTestLogger().info("afterEach");
        //        getUnitTestLogger().info("afterEach("+testContext+") with field `vertx` "+getVertx());
        //        getVertx().setTimer(1000L, id -> {
        //            getUnitTestLogger().info("afterEach Timer fired!");
        //            testContext.completeNow();
        //        });
    }

}
