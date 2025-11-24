package io.github.sinri.keel.tesuto;

import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import static io.github.sinri.keel.base.KeelInstance.Keel;


public class SampleTest extends KeelJUnit5Test {
    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public SampleTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    protected void test(VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();

        getUnitTestLogger().info("Test started!");

        Keel.getVertx().setTimer(2000L, id -> {
            getUnitTestLogger().info("Timer fired!");
            checkpoint.flag();
        });
    }
}
