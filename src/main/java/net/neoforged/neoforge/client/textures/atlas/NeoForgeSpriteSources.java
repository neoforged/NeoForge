/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.textures.atlas;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = "neoforge")
public class NeoForgeSpriteSources {
    @Nullable
    private static SpriteSourceType directoryPalettedPermutations;

    public static final Supplier<SpriteSourceType> DIRECTORY_PALETTED_PERMUTATIONS = () -> directoryPalettedPermutations;

    @SubscribeEvent
    public static void onRegister(RegisterSpriteSourceTypesEvent event) {
        directoryPalettedPermutations = event.register(new ResourceLocation("neoforge", "directory_paletted_permutations"), DirectoryPalettedPermutations.CODEC);
    }
}
