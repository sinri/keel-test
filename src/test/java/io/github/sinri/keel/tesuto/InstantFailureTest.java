package io.github.sinri.keel.tesuto;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

public class InstantFailureTest extends KeelInstantRunner {
    @Override
    protected @NotNull Future<Void> run() throws Exception {
        return Future.failedFuture("I am failed!");
    }
}
