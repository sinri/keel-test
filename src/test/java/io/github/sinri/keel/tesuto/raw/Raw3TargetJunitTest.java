package io.github.sinri.keel.tesuto.raw;

import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

public class Raw3TargetJunitTest extends Raw3BaseJunitTest {
    @Test
    void testA(VertxTestContext context) throws Exception {
        getVertx().setTimer(1000L, id -> context.completeNow());
    }

    @Test
    void testB(VertxTestContext context) throws Exception {
        getVertx().setTimer(1000L, id -> context.completeNow());
    }
}
