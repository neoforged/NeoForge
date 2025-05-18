/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.pipeline;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class PipelineModifiers {
    static final Map<ResourceKey<PipelineModifier>, PipelineModifier> MODIFIERS = new Reference2ReferenceOpenHashMap<>();

    private PipelineModifiers() {}

    public static void init() {
        ModLoader.postEvent(new RegisterPipelineModifiersEvent());
    }
}
