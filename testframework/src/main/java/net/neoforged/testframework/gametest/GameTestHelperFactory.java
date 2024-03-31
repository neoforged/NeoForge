package net.neoforged.testframework.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.neoforged.testframework.impl.ReflectionUtils;

import java.lang.invoke.MethodType;
import java.util.function.Function;

public interface GameTestHelperFactory<T extends GameTestHelper> extends Function<GameTestInfo, T> {

    static <T extends GameTestHelper> GameTestHelperFactory<T> forType(Class<?> helperClass) {
        var constructor = ReflectionUtils.constructor(helperClass, MethodType.methodType(void.class, GameTestInfo.class));

        return info -> {
            try {
                return (T) constructor.invokeWithArguments(info);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
