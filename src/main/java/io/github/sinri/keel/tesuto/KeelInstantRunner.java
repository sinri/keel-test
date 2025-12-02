package io.github.sinri.keel.tesuto;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.configuration.ConfigTree;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.base.verticles.KeelVerticle;
import io.github.sinri.keel.logger.api.LogLevel;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 即时运行类，一个快速可执行程序基础实现类。
 * <p>
 * 自带 main 方法，用于在 IDEA 等 IDE 环境下直接运行其实现类以进行逻辑验证等作业。
 *
 * @since 5.0.0
 */
public abstract class KeelInstantRunner implements Keel {
    @NotNull
    private final ConfigTree configTree;
    @Nullable
    private Vertx vertx;
    @Nullable
    private LoggerFactory loggerFactory;
    @Nullable
    private Logger logger;

    protected KeelInstantRunner() {
        this.configTree = new ConfigTree();
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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
        testInstance.launch(args);
    }

    @Nullable
    private static String extractClassFromArgs(String[] full, String[] tail) {
        //  1. 统计数组 tail 的长度，记为 L；
        int L = tail == null ? 0 : tail.length;
        //  2. 从数组 full 中找出倒数第 L+1 个元素返回
        if (full == null || full.length < L + 1) return null;
        return full[full.length - L - 1];
    }

    @Override
    public @NotNull
    final Vertx getVertx() {
        return Objects.requireNonNull(vertx);
    }

    /**
     * 在此即时运行类的 launch 方法中新建一个{@link LoggerFactory}实例。
     * <p>
     * 重载此类以提供替代的日志记录方案。
     *
     * @return 本类实例中应用的{@link LoggerFactory}实例。
     */
    @NotNull
    protected LoggerFactory buildLoggerFactory() {
        return StdoutLoggerFactory.getInstance();
    }

    public final @NotNull LoggerFactory getLoggerFactory() {
        return Objects.requireNonNull(loggerFactory);
    }

    @NotNull
    public final Logger getLogger() {
        return Objects.requireNonNull(logger);
    }

    @NotNull
    public VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    protected void loadLocalConfiguration() throws IOException {
        configTree.loadPropertiesFile("config.properties");
    }

    @NotNull
    protected LogLevel buildVisibleLogLevel() {
        return LogLevel.DEBUG;
    }

    public final void launch(String[] args) {
        try {
            this.loadLocalConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        VertxOptions vertxOptions = this.buildVertxOptions();
        this.vertx = Vertx.builder().with(vertxOptions).build();
        this.loggerFactory = this.buildLoggerFactory();
        this.logger = this.loggerFactory.createLogger(getClass().getName());
        this.logger.visibleLevel(buildVisibleLogLevel());

        var countDownLatch = new CountDownLatch(1);

        Future.succeededFuture()
              .compose(v -> {
                  return this.beforeRun();
              })
              .compose(v -> {
                  KeelVerticle verticle = new AbstractKeelVerticle(this) {
                      @Override
                      protected @NotNull Future<Void> startVerticle() {
                          Future<Void> runFuture;
                          try {
                              runFuture = run();
                          } catch (Exception e) {
                              return Future.failedFuture(e);
                          }

                          runFuture.eventually(() -> {
                                       return afterRun();
                                   })
                                   .onComplete(ar -> {
                                       if (ar.failed()) {
                                           getLogger().fatal(log -> log.message("RUN FAILED").exception(ar.cause()));
                                       } else {
                                           getLogger().debug("RUN SUCCESSFULLY");
                                       }
                                       getVertx().undeploy(deploymentID())
                                                 .onComplete(undeployResult -> {
                                                     countDownLatch.countDown();
                                                 });
                                   });

                          return Future.succeededFuture();
                      }
                  };
                  return verticle.deployMe(buildDeploymentOptions());
              })
              .onSuccess(id -> {
                  getLogger().debug("Deployed verticle with id: " + id);
              })
              .onFailure(t -> {
                  getLogger().fatal(log -> log.message("Deploy failed").exception(t));
                  countDownLatch.countDown();
              });

        AtomicInteger returnCode = new AtomicInteger(0);
        try {
            getLogger().debug("Waiting for count down latch...");
            countDownLatch.await();
            getLogger().debug("Count down latch reached.");
        } catch (InterruptedException e) {
            getLogger().fatal(log -> log.message("CountDownLatch Interrupted!").exception(e));
            returnCode.set(1);
        } finally {
            close().onComplete(over -> {
                getLogger().debug("Closed Keel and vertx.");
                System.exit(returnCode.get());
            });
        }
    }

    /**
     * 运行正式逻辑之前，做一些准备工作。
     *
     * @return 准备完成
     */
    @NotNull
    protected Future<Void> beforeRun() {
        getLogger().debug("beforeRun...");
        return Future.succeededFuture();
    }

    /**
     * 正式逻辑会以 Verticle 形式运行，在此方法构建部署时所需的{@link DeploymentOptions}。
     */
    @NotNull
    protected DeploymentOptions buildDeploymentOptions() {
        return new DeploymentOptions();
    }

    /**
     * 正式逻辑
     *
     * @return 正式逻辑运行完成时的异步结果
     * @throws Exception 可能抛出的异常
     */
    @NotNull
    abstract protected Future<Void> run() throws Exception;

    /**
     * 运行正式逻辑之后，做一些清理工作。
     *
     * @return 清理完成
     */
    @NotNull
    protected Future<Void> afterRun() {
        getLogger().debug("afterRun...");
        return Future.succeededFuture();
    }

    @Override
    public final @NotNull ConfigTree getConfiguration() {
        return configTree;
    }
}
