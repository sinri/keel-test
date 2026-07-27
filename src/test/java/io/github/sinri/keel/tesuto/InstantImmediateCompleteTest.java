package io.github.sinri.keel.tesuto;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

/**
 * 回归：{@code run()} 返回已完成 Future 时，undeploy 不得早于部署登记
 * （VIRTUAL_THREAD + await 也会落到同一路径：start 返回前 run 已结束）。
 */
@NullMarked
public class InstantImmediateCompleteTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() {
        getLogger().info("immediate complete");
        return Future.succeededFuture();
    }
}
