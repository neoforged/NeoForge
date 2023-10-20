/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.Optional;
import java.util.function.BiFunction;

public class ConfigScreenHandler
{
    public record ConfigScreenFactory(BiFunction<Minecraft, Screen, Screen> screenFunction) implements IExtensionPoint<ConfigScreenFactory> {}
    public static Optional<BiFunction<Minecraft, Screen, Screen>> getScreenFactoryFor(IModInfo selectedMod)
    {
        return ModList.get().getModContainerById(selectedMod.getModId()).
                flatMap(mc -> mc.getCustomExtension(ConfigScreenFactory.class).map(ConfigScreenFactory::screenFunction));
    }
}
