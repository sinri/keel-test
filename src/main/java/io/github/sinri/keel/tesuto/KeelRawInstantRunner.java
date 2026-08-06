package io.github.sinri.keel.tesuto;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.LogLevel;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

/**
 * A raw instant runner class for the tests do not need vert.x.
 *
 * @since 5.0.5
 */
public abstract class KeelRawInstantRunner {
    private final LateObject<Logger> lateLogger = new LateObject<>();
    private final LateObject<List<String>> lateArgs = new LateObject<>();

    protected KeelRawInstantRunner() {

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
        KeelRawInstantRunner testInstance = (KeelRawInstantRunner) constructor.newInstance();

        testInstance.launch(args);
    }

    private static @Nullable String extractClassFromArgs(String[] full, String[] tail) {
        //  1. 统计数组 tail 的长度，记为 L；
        int L = tail.length;
        //  2. 从数组 full 中找出倒数第 L+1 个元素返回
        if (full.length < L + 1) return null;
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

    protected void loadLocalConfiguration() throws IOException {
        ConfigElement.root().loadPropertiesFile("config.properties");
    }

    protected LogLevel buildVisibleLogLevel() {
        return LogLevel.DEBUG;
    }

    public final List<String> getArgs() {
        return lateArgs.get();
    }

    public final Logger getLogger() {
        return lateLogger.get();
    }

    public final void launch(String[] args) {
        lateArgs.set(List.of(args));

        try {
            this.loadLocalConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LoggerFactory.replaceShared(this.buildLoggerFactory());
        lateLogger.set(LoggerFactory.getShared().createLogger(getClass().getName()));
        this.getLogger().visibleLevel(buildVisibleLogLevel());

        run();
    }

    protected abstract void run();
}
