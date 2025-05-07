/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executor;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Fired to allow mods to register their own default {@linkplain MetadataSectionType metadata section types} for use during sprite loading.
 * This event is fired once on startup, before the initial resource reload.
 *
 * <p>
 * It is important to note that this event only affects texture atlases using {@link SpriteLoader#DEFAULT_METADATA_SECTIONS},
 * which includes most of the vanilla atlases, and those using {@link SpriteLoader#loadAndStitch(ResourceManager, ResourceLocation, int, Executor)} directly.
 * Atlases using the {@link TextureAtlasHolder#TextureAtlasHolder(TextureManager, ResourceLocation, ResourceLocation, Set)} constructor, such as {@linkplain GuiSpriteManager},
 * or {@link SpriteLoader#loadAndStitch(ResourceManager, ResourceLocation, int, Executor, Collection)} instead override this list and should instead specify any additional
 * metadata section types in the last parameter, or, if they desire to use the default collection in addition to their own, lazily compute a merged collection.
 * </p>
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterSpriteDefaultMetadataSectionTypesEvent extends Event implements IModBusEvent {
    private final Set<MetadataSectionType<?>> defaultTypes;

    @Internal
    public RegisterSpriteDefaultMetadataSectionTypesEvent(Set<MetadataSectionType<?>> defaultTypes) {
        this.defaultTypes = defaultTypes;
    }

    public void register(MetadataSectionType<?> sectionType) {
        defaultTypes.add(sectionType);
    }
}
