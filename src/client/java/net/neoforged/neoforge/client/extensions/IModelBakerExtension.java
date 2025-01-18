/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * An extension to {@link ModelBaker} that allows for custom model baking.
 */
public interface IModelBakerExtension {
    default ModelBaker self() {
        return (ModelBaker) this;
    }

    /**
     * Gets the unbaked model for the given location.
     *
     * @param location The location of the model
     * @return The unbaked model, or null if not found
     */
    @Nullable
    UnbakedModel getModel(ResourceLocation location);

    /**
     * Finds a sprite for the given slot name.
     *
     * @param slots    The texture slots
     * @param slotName The name of the slot
     * @return The sprite, or a missing reference sprite if not found
     */
    default TextureAtlasSprite findSprite(TextureSlots slots, String slotName) {
        Material material = slots.getMaterial(slotName);
        return material != null ? self().sprites().get(material) : self().sprites().reportMissingReference(slotName);
    }
}
