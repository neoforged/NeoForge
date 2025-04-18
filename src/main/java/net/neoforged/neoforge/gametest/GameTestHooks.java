/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.gametest;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import java.util.Objects;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public class GameTestHooks {
    private static boolean registeredGametests = false;

    public static boolean isGametestEnabled() {
        return !FMLLoader.isProduction() && (SharedConstants.IS_RUNNING_IN_IDE || isGametestServer() || Boolean.getBoolean("neoforge.enableGameTest"));
    }

    public static boolean isGametestServer() {
        return Objects.equals("neoforgegametestserverdev", Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.LAUNCHTARGET.get()).orElse(null));
    }

    public static void registerGametests(RegistryAccess registryAccess) {
        if (registeredGametests || !isGametestEnabled() || net.neoforged.fml.ModLoader.hasErrors()) {
            return;
        }
        Registry<TestEnvironmentDefinition> environments = registryAccess.lookupOrThrow(Registries.TEST_ENVIRONMENT);
        Registry<GameTestInstance> tests = registryAccess.lookupOrThrow(Registries.TEST_INSTANCE);
        RegisterGameTestsEvent event = new RegisterGameTestsEvent((WritableRegistry<TestEnvironmentDefinition>) environments, (WritableRegistry<GameTestInstance>) tests);

        ModLoader.postEvent(event);

        registeredGametests = true;
    }
}
