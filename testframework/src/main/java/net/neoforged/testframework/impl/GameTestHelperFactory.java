/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

public interface GameTestHelperFactory<T extends GameTestHelper> extends Function<GameTestInfo, T> {
    Map<Class<?>, GameTestHelperFactory<?>> CONSTRUCTORS = Util.make(new HashMap<>(), map -> {
        map.put(GameTestHelper.class, GameTestHelper::new);
        map.put(ExtendedGameTestHelper.class, ExtendedGameTestHelper::new);
    });

    static <T extends GameTestHelper> GameTestHelperFactory<T> forType(Class<T> helperClass) {
        return (GameTestHelperFactory<T>) CONSTRUCTORS.computeIfAbsent(helperClass, c -> {
            MethodHandle constructor;

            try {
                constructor = ReflectionUtils.constructor(c, MethodType.methodType(void.class, GameTestInfo.class));
            } catch (Exception e) {
                LogUtils.getLogger().warn("Failed to create constructor for GameTestHelper: " + helperClass, e);
                throw new RuntimeException(e);
            }

            return info -> {
                try {
                    return (T) constructor.invokeWithArguments(info);
                } catch (Throwable e) {
                    LogUtils.getLogger().warn("Failed to create GameTestHelper: " + helperClass, e);
                    throw new RuntimeException(e);
                }
            };
        });
    }
}
