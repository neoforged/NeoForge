/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.RenderTypeGroup;

/**
 * Base interface for any object that collects culled and unculled faces and bakes them into a model.
 * <p>
 * Provides a generic base implementation via {@link #of(boolean, boolean, boolean, ItemTransforms, TextureAtlasSprite, RenderTypeGroup)}
 * and a quad-collecting alternative via {@link #collecting(List)}.
 *
 * @deprecated Use {@link SimpleBakedModel.Builder} instead.
 */
@Deprecated(forRemoval = true, since = "1.21.4")
public interface IModelBuilder<T extends IModelBuilder<T>> {
    /**
     * Creates a new model builder that uses the provided attributes in the final baked model.
     */
    static IModelBuilder<?> of(boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d,
            ItemTransforms transforms, TextureAtlasSprite particle,
            RenderTypeGroup renderTypes) {
        return new Simple(hasAmbientOcclusion, usesBlockLight, isGui3d, transforms, particle, renderTypes);
    }

    /**
     * Creates a new model builder that collects quads to the provided list, returning
     * {@linkplain EmptyModel#BAKED an empty model} if you call {@link #build()}.
     */
    static IModelBuilder<?> collecting(List<BakedQuad> quads) {
        return new Collecting(quads);
    }

    /**
     * Adds a face to the model that will be culled based on the provided facing.
     *
     * @param facing The facing
     * @param quad   The quad
     * @return This builder
     */
    T addCulledFace(Direction facing, BakedQuad quad);

    /**
     * Adds a face to the model that will not be culled.
     *
     * @param quad The quad
     * @return This builder
     */
    T addUnculledFace(BakedQuad quad);

    /**
     * Builds the model from the collected faces.
     *
     * @return The baked model
     */
    BakedModel build();

    class Simple implements IModelBuilder<Simple> {
        private final SimpleBakedModel.Builder builder;
        private final RenderTypeGroup renderTypes;

        private Simple(boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d,
                ItemTransforms transforms, TextureAtlasSprite particle,
                RenderTypeGroup renderTypes) {
            this.builder = new SimpleBakedModel.Builder(hasAmbientOcclusion, usesBlockLight, isGui3d, transforms).particle(particle);
            this.renderTypes = renderTypes;
        }

        @Override
        public Simple addCulledFace(Direction facing, BakedQuad quad) {
            builder.addCulledFace(facing, quad);
            return this;
        }

        @Override
        public Simple addUnculledFace(BakedQuad quad) {
            builder.addUnculledFace(quad);
            return this;
        }

        @Deprecated
        @Override
        public BakedModel build() {
            return builder.build(renderTypes);
        }
    }

    class Collecting implements IModelBuilder<Collecting> {
        private final List<BakedQuad> quads;

        private Collecting(List<BakedQuad> quads) {
            this.quads = quads;
        }

        @Override
        public Collecting addCulledFace(Direction facing, BakedQuad quad) {
            quads.add(quad);
            return this;
        }

        @Override
        public Collecting addUnculledFace(BakedQuad quad) {
            quads.add(quad);
            return this;
        }

        @Override
        public BakedModel build() {
            return EmptyModel.BAKED;
        }
    }
}
