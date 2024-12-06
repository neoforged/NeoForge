/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.ConcatenatedListView;
import org.jetbrains.annotations.Nullable;

/**
 * A model composed of several named children.
 */
public class UnbakedCompositeModel implements UnbakedModel {
    private final ImmutableMap<String, ResourceLocation> children;
    private final ImmutableList<String> itemPasses;
    private final Transformation rootTransform;
    private final Map<String, Boolean> partVisibility;

    public UnbakedCompositeModel(ImmutableMap<String, ResourceLocation> children, ImmutableList<String> itemPasses, Transformation rootTransform, Map<String, Boolean> partVisibility) {
        this.children = children;
        this.itemPasses = itemPasses;
        this.rootTransform = rootTransform;
        this.partVisibility = partVisibility;
    }

    @Override
    public BakedModel bake(TextureSlots slots,
            ModelBaker baker,
            ModelState state,
            boolean useAmbientOcclusion,
            boolean usesBlockLight,
            ItemTransforms transforms) {
        TextureAtlasSprite particle = baker.findSprite(slots, TextureSlot.PARTICLE.getId());

        if (!rootTransform.isIdentity())
            state = UnbakedElementsHelper.composeRootTransformIntoModelState(state, rootTransform);

        var bakedPartsBuilder = ImmutableMap.<String, BakedModel>builder();
        for (var entry : children.entrySet()) {
            var name = entry.getKey();
            if (!partVisibility.getOrDefault(name, true))
                continue;
            var model = entry.getValue();
            bakedPartsBuilder.put(name, baker.bake(model, state));
        }
        var bakedParts = bakedPartsBuilder.build();

        var itemPassesBuilder = ImmutableList.<BakedModel>builder();
        for (String name : this.itemPasses) {
            var model = bakedParts.get(name);
            if (model == null)
                throw new IllegalStateException("Specified \"" + name + "\" in \"item_render_order\", but that is not a child of this model.");
            itemPassesBuilder.add(model);
        }

        return new Baked(usesBlockLight, useAmbientOcclusion, particle, transforms, bakedParts, itemPassesBuilder.build());
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        for (ResourceLocation path : children.values()) {
            resolver.resolve(path);
        }
    }

    public static class Baked implements IDynamicBakedModel {
        private final boolean isAmbientOcclusion;
        private final boolean isSideLit;
        private final TextureAtlasSprite particle;
        private final ItemTransforms transforms;
        private final ImmutableMap<String, BakedModel> children;
        private final ImmutableList<BakedModel> itemPasses;

        public Baked(boolean isSideLit, boolean isAmbientOcclusion, TextureAtlasSprite particle, ItemTransforms transforms, ImmutableMap<String, BakedModel> children, ImmutableList<BakedModel> itemPasses) {
            this.children = children;
            this.isAmbientOcclusion = isAmbientOcclusion;
            this.isSideLit = isSideLit;
            this.particle = particle;
            this.transforms = transforms;
            this.itemPasses = itemPasses;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
            List<List<BakedQuad>> quadLists = new ArrayList<>();
            for (Map.Entry<String, BakedModel> entry : children.entrySet()) {
                if (renderType == null || (state != null && entry.getValue().getRenderTypes(state, rand, data).contains(renderType))) {
                    quadLists.add(entry.getValue().getQuads(state, side, rand, Data.resolve(data, entry.getKey()), renderType));
                }
            }
            return ConcatenatedListView.of(quadLists);
        }

        @Override
        public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
            var builder = Data.builder();
            for (var entry : children.entrySet())
                builder.with(entry.getKey(), entry.getValue().getModelData(level, pos, state, Data.resolve(modelData, entry.getKey())));
            return modelData.derive().with(Data.PROPERTY, builder.build()).build();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return isAmbientOcclusion;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean usesBlockLight() {
            return isSideLit;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return particle;
        }

        @Override
        public ItemTransforms getTransforms() {
            return transforms;
        }

        @Override
        public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
            var sets = new ArrayList<ChunkRenderTypeSet>();
            for (Map.Entry<String, BakedModel> entry : children.entrySet())
                sets.add(entry.getValue().getRenderTypes(state, rand, Data.resolve(data, entry.getKey())));
            return ChunkRenderTypeSet.union(sets);
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack itemStack) {
            return itemPasses;
        }

        @Nullable
        public BakedModel getPart(String name) {
            return children.get(name);
        }

        public static Builder builder(boolean isAmbientOcclusion, boolean isGui3d, boolean isSideLit, TextureAtlasSprite particle, ItemTransforms cameraTransforms) {
            return new Builder(isAmbientOcclusion, isGui3d, isSideLit, particle, cameraTransforms);
        }

        public static class Builder {
            private final boolean isAmbientOcclusion;
            private final boolean isGui3d;
            private final boolean isSideLit;
            private final List<BakedModel> children = new ArrayList<>();
            private final List<BakedQuad> quads = new ArrayList<>();
            private final ItemTransforms transforms;
            private TextureAtlasSprite particle;
            private RenderTypeGroup lastRenderTypes = RenderTypeGroup.EMPTY;

            private Builder(boolean isAmbientOcclusion, boolean isGui3d, boolean isSideLit, TextureAtlasSprite particle, ItemTransforms transforms) {
                this.isAmbientOcclusion = isAmbientOcclusion;
                this.isGui3d = isGui3d;
                this.isSideLit = isSideLit;
                this.particle = particle;
                this.transforms = transforms;
            }

            public void addLayer(BakedModel model) {
                flushQuads(null);
                children.add(model);
            }

            private void addLayer(RenderTypeGroup renderTypes, List<BakedQuad> quads) {
                var modelBuilder = IModelBuilder.of(isAmbientOcclusion, isSideLit, isGui3d, transforms, particle, renderTypes);
                quads.forEach(modelBuilder::addUnculledFace);
                children.add(modelBuilder.build());
            }

            private void flushQuads(@Nullable RenderTypeGroup renderTypes) {
                if (!Objects.equals(renderTypes, lastRenderTypes)) {
                    if (!quads.isEmpty()) {
                        addLayer(lastRenderTypes, quads);
                        quads.clear();
                    }
                    lastRenderTypes = renderTypes;
                }
            }

            public Builder setParticle(TextureAtlasSprite particleSprite) {
                this.particle = particleSprite;
                return this;
            }

            public Builder addQuads(RenderTypeGroup renderTypes, BakedQuad... quadsToAdd) {
                flushQuads(renderTypes);
                Collections.addAll(quads, quadsToAdd);
                return this;
            }

            public Builder addQuads(RenderTypeGroup renderTypes, Collection<BakedQuad> quadsToAdd) {
                flushQuads(renderTypes);
                quads.addAll(quadsToAdd);
                return this;
            }

            public BakedModel build() {
                if (!quads.isEmpty()) {
                    addLayer(lastRenderTypes, quads);
                }
                var childrenBuilder = ImmutableMap.<String, BakedModel>builder();
                var itemPassesBuilder = ImmutableList.<BakedModel>builder();
                int i = 0;
                for (var model : this.children) {
                    childrenBuilder.put("model_" + (i++), model);
                    itemPassesBuilder.add(model);
                }
                return new Baked(isSideLit, isAmbientOcclusion, particle, transforms, childrenBuilder.build(), itemPassesBuilder.build());
            }
        }
    }

    /**
     * A model data container which stores data for child components.
     */
    public static class Data {
        public static final ModelProperty<Data> PROPERTY = new ModelProperty<>();

        private final Map<String, ModelData> partData;

        private Data(Map<String, ModelData> partData) {
            this.partData = partData;
        }

        @Nullable
        public ModelData get(String name) {
            return partData.get(name);
        }

        /**
         * Helper to get the data from a {@link ModelData} instance.
         *
         * @param modelData The object to get data from
         * @param name      The name of the part to get data for
         * @return The data for the part, or the one passed in if not found
         */
        public static ModelData resolve(ModelData modelData, String name) {
            var compositeData = modelData.get(PROPERTY);
            if (compositeData == null)
                return modelData;
            var partData = compositeData.get(name);
            return partData != null ? partData : modelData;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private final Map<String, ModelData> partData = new IdentityHashMap<>();

            public Builder with(String name, ModelData data) {
                partData.put(name, data);
                return this;
            }

            public Data build() {
                return new Data(partData);
            }
        }
    }

    public static final class Loader implements UnbakedModelLoader<UnbakedCompositeModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public UnbakedCompositeModel read(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            List<String> itemPasses = new ArrayList<>();
            ImmutableMap.Builder<String, ResourceLocation> childrenBuilder = ImmutableMap.builder();
            readChildren(jsonObject, "children", childrenBuilder, itemPasses);

            var children = childrenBuilder.build();
            if (children.isEmpty())
                throw new JsonParseException("Composite model requires a \"children\" element with at least one element.");

            if (jsonObject.has("item_render_order")) {
                itemPasses.clear();
                for (var element : jsonObject.getAsJsonArray("item_render_order")) {
                    var name = element.getAsString();
                    if (!children.containsKey(name))
                        throw new JsonParseException("Specified \"" + name + "\" in \"item_render_order\", but that is not a child of this model.");
                    itemPasses.add(name);
                }
            }

            final Map<String, Boolean> partVisibility;
            if (jsonObject.has("visibility")) {
                partVisibility = new HashMap<>();
                JsonObject visibility = jsonObject.getAsJsonObject("visibility");
                for (Map.Entry<String, JsonElement> part : visibility.entrySet()) {
                    partVisibility.put(part.getKey(), part.getValue().getAsBoolean());
                }
            } else {
                partVisibility = Collections.emptyMap();
            }

            final Transformation transformation;
            if (jsonObject.has("transform")) {
                transformation = BlockModel.GSON.fromJson(jsonObject.get("transform"), Transformation.class);
            } else {
                transformation = Transformation.identity();
            }

            return new UnbakedCompositeModel(children, ImmutableList.copyOf(itemPasses), transformation, partVisibility);
        }

        private void readChildren(JsonObject jsonObject, String name, ImmutableMap.Builder<String, ResourceLocation> children, List<String> itemPasses) {
            if (!jsonObject.has(name))
                return;
            var childrenJsonObject = jsonObject.getAsJsonObject(name);
            for (Map.Entry<String, JsonElement> entry : childrenJsonObject.entrySet()) {
                ResourceLocation location = ResourceLocation.parse(entry.getValue().getAsString());
                children.put(entry.getKey(), location);
                itemPasses.add(entry.getKey()); // We can do this because GSON preserves ordering during deserialization
            }
        }
    }
}
