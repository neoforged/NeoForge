/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.neoforged.neoforge.internal.NeoForgeProxy;
import org.lwjgl.glfw.GLFW;

/**
 * This class is in a client package and FML refers to its FQN directly for server launches,
 * hence it cannot be moved to the client source set.
 */
public final class NoVizFallback {
    private static long WINDOW;

    public static LongSupplier windowHandoff(IntSupplier width, IntSupplier height, Supplier<String> title, LongSupplier monitor) {
        return () -> WINDOW = GLFW.glfwCreateWindow(width.getAsInt(), height.getAsInt(), title.get(), monitor.getAsLong(), 0L);
    }

    public static Supplier<?> loadingOverlay(Supplier<?> mc, Supplier<ReloadInstance> ri, Consumer<Optional<Throwable>> ex, boolean fadein) {
        return NeoForgeProxy.INSTANCE.instantiateLoadingOverlay(mc, ri, ex, fadein);
    }

    public static Boolean windowPositioning(Optional<?> monitor, IntConsumer widthSetter, IntConsumer heightSetter, IntConsumer xSetter, IntConsumer ySetter) {
        return Boolean.FALSE;
    }

    public static String glVersion() {
        if (WINDOW != 0) {
            var maj = GLFW.glfwGetWindowAttrib(WINDOW, GLFW.GLFW_CONTEXT_VERSION_MAJOR);
            var min = GLFW.glfwGetWindowAttrib(WINDOW, GLFW.GLFW_CONTEXT_VERSION_MINOR);
            return maj + "." + min;
        } else {
            return "3.2";
        }
    }
}
