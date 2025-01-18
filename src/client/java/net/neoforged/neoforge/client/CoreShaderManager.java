/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.ShaderProgram;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CoreShaderManager {
    private static List<ShaderProgram> shaderPrograms = Collections.emptyList();

    private CoreShaderManager() {}

    public static void init() {
        List<ShaderProgram> programs = new ArrayList<>(CoreShaders.getProgramsToPreload());
        ModLoader.postEvent(new RegisterShadersEvent(programs));
        shaderPrograms = List.copyOf(programs);
    }

    public static List<ShaderProgram> getProgramsToPreload() {
        return shaderPrograms;
    }
}
