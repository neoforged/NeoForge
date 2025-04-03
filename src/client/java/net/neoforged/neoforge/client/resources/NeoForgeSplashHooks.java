/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.resources;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class NeoForgeSplashHooks {
    public static List<String> loadSplashes(ResourceManager resourceManager) {
        List<String> list = Lists.newArrayList();
        for (Resource resource : resourceManager.listResources("texts", location -> location.getPath().equals(SplashManager.SPLASHES_LOCATION.getPath())).values()) {
            try (BufferedReader reader = resource.openAsReader()) {
                list.addAll(reader.lines().map(String::trim).filter(p_118876_ -> p_118876_.hashCode() != 125780783).toList());
            } catch (IOException e) {
                LogUtils.getLogger().warn("Invalid {} in resourcepack: '{}'", SplashManager.SPLASHES_LOCATION.getPath(), resource.sourcePackId(), e);
            }
        }
        return list;
    }
}
