package io.github.sinri.keel.tesuto;

import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

@NullMarked
public class SampleTest extends KeelJUnit5Test {
    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public SampleTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    void test(VertxTestContext testContext) {
        Checkpoint checkpoint = testContext.checkpoint();

        getUnitTestLogger().info("Test started!");

        getVertx().setTimer(2000L, id -> {
            getUnitTestLogger().info("Timer fired!");
            checkpoint.flag();
        });
    }
}
