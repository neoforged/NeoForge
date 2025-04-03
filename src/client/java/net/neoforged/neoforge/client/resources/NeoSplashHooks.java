/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.resources;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class NeoSplashHooks {
    public static List<String> loadSplashes(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        List<String> list = Lists.newArrayList();
        for (String namespace : resourceManager.getNamespaces()) {
            try (net.minecraft.util.profiling.Zone zone = profilerFiller.zone(namespace)) {
                for (net.minecraft.server.packs.resources.Resource resource : resourceManager.getResourceStack(ResourceLocation.fromNamespaceAndPath(namespace, SplashManager.SPLASHES_LOCATION.getPath()))) {
                    profilerFiller.push(resource.sourcePackId());

                    try (BufferedReader reader = resource.openAsReader()) {
                        profilerFiller.push("parse");

                        list.addAll(reader.lines().map(String::trim).filter(p_118876_ -> p_118876_.hashCode() != 125780783).toList());

                        profilerFiller.pop();
                    } catch (RuntimeException runtimeexception) {
                        com.mojang.logging.LogUtils.getLogger().warn("Invalid {} in resourcepack: '{}'", SplashManager.SPLASHES_LOCATION.getPath(), resource.sourcePackId(), runtimeexception);
                    }

                    profilerFiller.pop();
                }
            } catch (IOException ignored) {}
        }
        return list;
    }
}
