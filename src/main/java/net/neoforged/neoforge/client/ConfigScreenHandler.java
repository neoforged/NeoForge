/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterConfigScreenEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class ConfigScreenHandler {
    private static final Map<String, IConfigScreenFactory> factories = new HashMap<>();

    /**
     * Retrieves the config screen factory for a mod, if present.
     */
    @Nullable
    public static IConfigScreenFactory getScreenFactory(String modid) {
        var ret = factories.get(modid);
        if (ret != null) {
            return ret;
        }

        // support legacy system
        return ModList.get().getModContainerById(modid)
                .flatMap(mc -> mc.getCustomExtension(ConfigScreenFactory.class))
                .map(f -> (IConfigScreenFactory) f.screenFunction()::apply)
                .orElse(null);
    }

    @ApiStatus.Internal
    public static void init() {
        ModLoader.get().postEvent(new RegisterConfigScreenEvent(factories));
    }

    @Deprecated(forRemoval = true, since = "1.20.4")
    public record ConfigScreenFactory(BiFunction<Minecraft, Screen, Screen> screenFunction) implements IExtensionPoint<ConfigScreenFactory> {}

    @Deprecated(forRemoval = true, since = "1.20.4")
    public static Optional<BiFunction<Minecraft, Screen, Screen>> getScreenFactoryFor(IModInfo selectedMod) {
        var factory = factories.get(selectedMod.getModId());
        if (factory != null) {
            return Optional.of(factory::createScreen);
        }

        return ModList.get().getModContainerById(selectedMod.getModId()).flatMap(mc -> mc.getCustomExtension(ConfigScreenFactory.class).map(ConfigScreenFactory::screenFunction));
    }
}
