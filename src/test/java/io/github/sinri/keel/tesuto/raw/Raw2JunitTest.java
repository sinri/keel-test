package io.github.sinri.keel.tesuto.raw;

import io.github.sinri.keel.base.logger.logger.StdoutLogger;
import io.github.sinri.keel.tesuto.Sample2Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

@ExtendWith(VertxExtension.class)
public class Raw2JunitTest {
    @RegisterExtension
    static RunTestOnContext rtoc = new RunTestOnContext();

    public Raw2JunitTest() {
        super();
        System.out.println("Raw2JunitTest Constructor RTOC Vertx: " + rtoc.vertx());
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
            VertxTestContext testContext
    ) {
        System.out.println("beforeEach");
        //getUnitTestLogger().info("beforeEach("+testContext+") with field `vertx` "+getVertx());
        getVertx().setTimer(1000L, id -> {
            System.out.println("beforeEach Timer fired!");
            testContext.completeNow();
        });
    }

    Vertx getVertx() {
        return rtoc.vertx();
    }

    @Test
    void test1(VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();

        System.out.println("Test1 started with testContext: " + testContext);

        getVertx().setTimer(1000L, id -> {
            System.out.println("Test1 Timer fired!");
            checkpoint.flag();
        });
    }

    @Test
    void test2(VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();

        System.out.println("Test2 started with testContext: " + testContext);

        getVertx().setTimer(1000L, id -> {
            System.out.println("Test2 Timer fired!");
            checkpoint.flag();
        });
    }

    @AfterEach
    void afterEach(
            VertxTestContext testContext
    ) {
        System.out.println("afterEach");
        //        getUnitTestLogger().info("afterEach("+testContext+") with field `vertx` "+getVertx());
        getVertx().setTimer(1000L, id -> {
            System.out.println("afterEach Timer fired!");
            testContext.completeNow();
        });
    }
}
