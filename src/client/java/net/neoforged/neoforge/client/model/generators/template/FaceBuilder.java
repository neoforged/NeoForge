/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

import com.google.common.base.Preconditions;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public final class FaceBuilder {
    @Nullable
    private Direction cullface;
    private int tintindex = -1;
    @Nullable
    private TextureSlot texture = null;
    @Nullable
    private BlockElementFace.UVs uvs;
    private Quadrant rotation = Quadrant.R0;
    private int color = 0xFFFFFFFF;
    private int blockLight = 0;
    private int skyLight = 0;
    private boolean hasAmbientOcclusion = true;

    /**
     * Sets which direction should cull this face when fully occluded, or null to never cull.
     */
    public FaceBuilder cullface(@Nullable Direction dir) {
        this.cullface = dir;
        return this;
    }

    /**
     * Sets the color index used when attempting to tint this face.
     */
    public FaceBuilder tintindex(int index) {
        this.tintindex = index;
        return this;
    }

    /**
     * Set the texture for the current face.
     *
     * @param texture the texture
     * @return this builder
     * @throws NullPointerException if {@code texture} is {@code null}
     */
    public FaceBuilder texture(TextureSlot texture) {
        Preconditions.checkNotNull(texture, "Texture must not be null");
        this.texture = texture;
        return this;
    }

    /**
     * Sets the texture uv mapping for this face.
     */
    public FaceBuilder uvs(float u1, float v1, float u2, float v2) {
        this.uvs = new BlockElementFace.UVs(u1, v1, u2, v2);
        return this;
    }

    /**
     * Set the texture rotation for the current face.
     *
     * @param rot the rotation
     * @return this builder
     * @throws NullPointerException if {@code rot} is {@code null}
     */
    public FaceBuilder rotation(Quadrant rot) {
        Preconditions.checkNotNull(rot, "Rotation must not be null");
        this.rotation = rot;
        return this;
    }

    /**
     * Set the block and sky light of the face (0-15).
     * Traditional "emissivity" values set both of these to the same value.
     *
     * @param blockLight the block light
     * @param skyLight   the sky light
     * @return this builder
     */
    public FaceBuilder emissivity(int blockLight, int skyLight) {
        this.blockLight = blockLight;
        this.skyLight = skyLight;
        return this;
    }

    /**
     * Sets the color of the face.
     *
     * @param color the color in ARGB format.
     * @return this builder
     */
    public FaceBuilder color(int color) {
        this.color = color;
        return this;
    }

    /**
     * Set the ambient occlusion of the face.
     *
     * @param ambientOcclusion the ambient occlusion
     * @return this builder
     */
    public FaceBuilder ambientOcclusion(boolean ambientOcclusion) {
        this.hasAmbientOcclusion = ambientOcclusion;
        return this;
    }

    BlockElementFace build() {
        if (this.texture == null) {
            throw new IllegalStateException("A model face must have a texture");
        }
        return new BlockElementFace(cullface, tintindex, texture.toString(), uvs, rotation, new ExtraFaceData(this.color, this.blockLight, this.skyLight, this.hasAmbientOcclusion), new MutableObject<>());
    }

    FaceBuilder copy() {
        FaceBuilder builder = new FaceBuilder();
        builder.texture = this.texture;
        builder.color = this.color;
        builder.cullface = this.cullface;
        builder.tintindex = this.tintindex;
        builder.uvs = this.uvs;
        builder.rotation = this.rotation;
        builder.blockLight = this.blockLight;
        builder.skyLight = this.skyLight;
        builder.hasAmbientOcclusion = this.hasAmbientOcclusion;
        return builder;
    }
}
