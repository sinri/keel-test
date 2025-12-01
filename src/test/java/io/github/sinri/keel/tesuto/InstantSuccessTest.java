package io.github.sinri.keel.tesuto;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;


public class InstantSuccessTest extends KeelInstantRunner {
    @Override
    protected @NotNull Future<Void> run() {
        getLogger().info("GO");
        return asyncCallStepwise(3, i -> {
            getLogger().info("STEP " + i);
            return asyncSleep(1000L);
        })
                .compose(v -> {
                    getLogger().info("DONE");
                    return Future.succeededFuture();
                });
    }
}
