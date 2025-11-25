package io.github.sinri.keel.tesuto;

import io.vertx.core.Future;


public class InstantSuccessTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() {
        getLogger().info("GO");
        return Keel.asyncCallStepwise(3, i -> {
                       getLogger().info("STEP " + i);
                       return Keel.asyncSleep(1000L);
                   })
                   .compose(v -> {
                       getLogger().info("DONE");
                       return Future.succeededFuture();
                   });
    }
}
