package io.github.sinri.keel.tesuto;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class InstantSuccessTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() {
        getLogger().info("GO");
        return getKeel().asyncCallStepwise(3, i -> {
            getLogger().info("STEP " + i);
            return getKeel().asyncSleep(1000L);
        })
                .compose(v -> {
                    getLogger().info("DONE");
                    return Future.succeededFuture();
                });
    }
}
