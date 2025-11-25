package io.github.sinri.keel.tesuto;

import io.github.sinri.keel.base.KeelInstance;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.LogLevel;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * 即时运行类，一个快速可执行程序基础实现类。
 * <p>
 * 自带 main 方法，用于在 IDEA 等 IDE 环境下直接运行其实现类以进行逻辑验证等作业。
 *
 * @since 5.0.0
 */
public abstract class KeelInstantRunner {
    protected static final KeelInstance Keel = KeelInstance.Keel;

    private CountDownLatch countDownLatch;
    private LoggerFactory loggerFactory;
    private Logger logger;

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {
        // 获取调用此 main 方法的类名；采用新的 JDK 9+ 标准实现途径，弃用非标准的系统属性 sun.java.command 实现途径。
        ProcessHandle.Info info = ProcessHandle.current().info();
        Optional<String[]> arguments = info.arguments();
        if (arguments.isEmpty()) {
            throw new RuntimeException("No arguments of current process found!");
        }
        String calledClass = extractClassFromArgs(arguments.get(), args);

        // 通过反射加载调用类
        Class<?> aClass = Class.forName(calledClass);

        // 获取无参构造函数并创建实例
        Constructor<?> constructor = aClass.getConstructor();
        KeelInstantRunner testInstance = (KeelInstantRunner) constructor.newInstance();

        testInstance.countDownLatch = new CountDownLatch(1);

        testInstance.launch(args);

        testInstance.countDownLatch.await();
    }

    private static String extractClassFromArgs(String[] full, String[] tail) {
        //  1. 统计数组 tail 的长度，记为 L；
        int L = tail == null ? 0 : tail.length;
        //  2. 从数组 full 中找出倒数第 L+1 个元素返回
        if (full == null || full.length < L + 1) return null;
        return full[full.length - L - 1];
    }

    /**
     * 在此即时运行类的 launch 方法中新建一个{@link LoggerFactory}实例。
     * <p>
     * 重载此类以提供替代的日志记录方案。
     *
     * @return 本类实例中应用的{@link LoggerFactory}实例。
     */
    protected LoggerFactory buildLoggerFactory() {
        return StdoutLoggerFactory.getInstance();
    }

    public final LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public final Logger getLogger() {
        return logger;
    }

    @NotNull
    public VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    protected void loadLocalConfiguration() throws IOException {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    protected LogLevel buildVisibleLogLevel() {
        return LogLevel.DEBUG;
    }

    public void launch(String[] args) {
        try {
            this.loadLocalConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        VertxOptions vertxOptions = this.buildVertxOptions();
        Keel.initializeVertxStandalone(vertxOptions);

        this.loggerFactory = this.buildLoggerFactory();
        this.logger = this.loggerFactory.createLogger(getClass().getName());
        this.logger.visibleLevel(buildVisibleLogLevel());

        countDownLatch = new CountDownLatch(1);

        Future.succeededFuture()
              .compose(v -> {
                  return this.beforeRun();
              })
              .compose(v -> {
                  AbstractVerticle verticle = new AbstractVerticle() {
                      @Override
                      public void start() throws Exception {
                          run()
                                  .eventually(() -> {
                                      return afterRun();
                                  })
                                  .onComplete(ar -> {
                                      if (ar.failed()) {
                                          getLogger().fatal(log -> log.message("RUN FAILED").exception(ar.cause()));
                                      } else {
                                          getLogger().debug("RUN SUCCESSFULLY");
                                      }
                                      Keel.getVertx().undeploy(deploymentID())
                                          .onComplete(undeployResult -> {
                                              countDownLatch.countDown();
                                          });
                                  });
                      }
                  };
                  return Keel.getVertx().deployVerticle(verticle, buildDeploymentOptions());
              })
              .onSuccess(id -> {
                  getLogger().debug("Deployed verticle with id: " + id);
              })
              .onFailure(t -> {
                  getLogger().fatal(log -> log.message("Deploy failed").exception(t));
                  countDownLatch.countDown();
              });

        try {
            getLogger().debug("Waiting for count down latch...");
            countDownLatch.await();
            getLogger().debug("Count down latch reached.");
        } catch (InterruptedException e) {
            getLogger().fatal(log -> log.message("CountDownLatch Interrupted!").exception(e));
            System.exit(1);
        } finally {
            Keel.close()
                .onComplete(over -> {
                    getLogger().debug("All done.");
                    System.exit(0);
                });
        }
    }

    /**
     * 运行正式逻辑之前，做一些准备工作。
     *
     * @return 准备完成
     */
    protected Future<Void> beforeRun() {
        getLogger().debug("beforeRun...");
        return Future.succeededFuture();
    }

    /**
     * 正式逻辑会以 Verticle 形式运行，在此方法构建部署时所需的{@link DeploymentOptions}。
     */
    protected DeploymentOptions buildDeploymentOptions() {
        return new DeploymentOptions();
    }

    /**
     * 正式逻辑
     *
     * @return 正式逻辑运行完成时的异步结果
     * @throws Exception 可能抛出的异常
     */
    abstract protected Future<Void> run() throws Exception;

    /**
     * 运行正式逻辑之后，做一些清理工作。
     *
     * @return 清理完成
     */
    protected Future<Void> afterRun() {
        getLogger().debug("afterRun...");
        return Future.succeededFuture();
    }
}
