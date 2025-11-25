package io.github.sinri.keel.tesuto;

import io.vertx.core.Future;

public class InstantFailureTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        return Future.failedFuture("I am failed!");
    }
}
