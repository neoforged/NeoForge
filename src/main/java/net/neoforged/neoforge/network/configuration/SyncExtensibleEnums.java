/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.payload.ExtensibleEnumDataPayload;
import org.jetbrains.annotations.ApiStatus;

/**
 * Syncs registries to the client
 */
@ApiStatus.Internal
public record SyncExtensibleEnums(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    private static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "sync_extensible_enums");
    public static final Type TYPE = new Type(ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        if (listener.hasChannel(ExtensibleEnumDataPayload.TYPE)) {
            sender.accept(ExtensibleEnumDataPayload.factory(List.of(Rarity.class, FireworkExplosion.Shape.class), List.of(MobCategory.class, BiomeSpecialEffects.GrassColorModifier.class)));
        }
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
