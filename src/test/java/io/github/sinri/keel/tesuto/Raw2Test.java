package io.github.sinri.keel.tesuto;

public class Raw2Test extends KeelRawInstantRunner {
    @Override
    protected void run() {
        getLogger().info("Raw2Test");
        throw new RuntimeException("Raw2Test");
    }
}
