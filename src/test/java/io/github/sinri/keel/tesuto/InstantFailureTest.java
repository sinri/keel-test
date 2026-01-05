package io.github.sinri.keel.tesuto;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class InstantFailureTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        return Future.failedFuture("I am failed!");
    }
}
