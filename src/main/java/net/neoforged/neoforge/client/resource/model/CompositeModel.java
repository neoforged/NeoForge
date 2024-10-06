/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.resource.model;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModel.GuiLight;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class CompositeModel implements TopLevelUnbakedModel {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Codec<Either<TopLevelUnbakedModel, ResourceLocation>> CHILD_OR_REFERENCE = Codec.either(ModelCodecs.UNBAKED_MODEL, ResourceLocation.CODEC);

    public static final MapCodec<CompositeModel> CODEC = RecordCodecBuilder.mapCodec(
            it -> it.group(
                    Codec.unboundedMap(Codec.STRING, CHILD_OR_REFERENCE)
                            .fieldOf("children")
                            .forGetter(o -> o._allChildren))
                    .and(ModelCodecs.commonCodec())
                    .apply(it, CompositeModel::new));

    private final Map<String, Either<TopLevelUnbakedModel, ResourceLocation>> _allChildren;
    private final Map<String, TopLevelUnbakedModel> _loadedChildren;
    private final Map<String, ResourceLocation> _unloadedChildren;

    private final Map<String, Either<Material, String>> _textureMap;
    @Nullable
    private final Boolean _hasAmbientOcclusion;
    @Nullable
    private final GuiLight _guiLight;
    private final ItemTransforms _transforms;
    private final List<ItemOverride> _overrides;

    @Nullable
    private String _name;

    @Nullable
    private ResourceLocation _parentLocation;
    @Nullable
    private TopLevelUnbakedModel _parent;

    private CompositeModel(
            Map<String, Either<TopLevelUnbakedModel, ResourceLocation>> children,
            Optional<ResourceLocation> parentLocation,
            Map<String, Either<Material, String>> textureMap,
            Optional<Boolean> hasAmbientOcclusion,
            Optional<GuiLight> guiLight,
            ItemTransforms transforms,
            List<ItemOverride> overrides) {
        _allChildren = children;
        _parentLocation = parentLocation.orElse(null);
        _textureMap = textureMap;
        _hasAmbientOcclusion = hasAmbientOcclusion.orElse(null);
        _guiLight = guiLight.orElse(null);
        _transforms = transforms;
        _overrides = overrides;

        _loadedChildren = new Reference2ObjectOpenHashMap<>();
        _unloadedChildren = new Reference2ObjectOpenHashMap<>();

        for (var pair : children.entrySet()) {
            pair.getValue()
                    .ifLeft(mdl -> _loadedChildren.put(pair.getKey(), mdl))
                    .ifRight(ref -> _unloadedChildren.put(pair.getKey(), ref));
        }
    }

    @Override
    public MapCodec<? extends TopLevelUnbakedModel> codec() {
        return CODEC;
    }

    @Override
    public void setName(String name) {
        _name = name;
    }

    @Nullable
    @Override
    public ResourceLocation getParentLocation() {
        return _parentLocation;
    }

    @Override
    public void setParentLocation(@Nullable ResourceLocation parentLocation) {
        _parentLocation = parentLocation;
    }

    @Nullable
    @Override
    public TopLevelUnbakedModel getParent() {
        return _parent;
    }

    @Override
    public void setParent(@Nullable TopLevelUnbakedModel parent) {
        _parent = parent;
    }

    @Override
    public Map<String, Either<Material, String>> getOwnTextureMap() {
        return _textureMap;
    }

    @Nullable
    @Override
    public Boolean getOwnAmbientOcclusion() {
        return _hasAmbientOcclusion;
    }

    @Nullable
    @Override
    public BlockModel.GuiLight getOwnGuiLight() {
        return _guiLight;
    }

    @Nullable
    @Override
    public ItemTransforms getOwnTransforms() {
        return _transforms;
    }

    @Nullable
    @Override
    public List<ItemOverride> getOwnOverrides() {
        return _overrides;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Stream.concat(
                Stream.of(_parentLocation),
                _unloadedChildren.values().stream())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> p_119538_) {
        Set<UnbakedModel> set = Sets.newLinkedHashSet();

        for (net.neoforged.neoforge.client.resource.model.TopLevelUnbakedModel blockmodel = this; blockmodel.getParentLocation() != null && blockmodel.getParent() == null; blockmodel = blockmodel.getParent()) {
            set.add(blockmodel);
            UnbakedModel unbakedmodel = p_119538_.apply(blockmodel.getParentLocation());
            if (unbakedmodel == null) {
                LOGGER.warn("No parent '{}' while loading model '{}'", _parentLocation, blockmodel);
            }

            if (set.contains(unbakedmodel)) {
                LOGGER.warn(
                        "Found 'parent' loop while loading model '{}' in chain: {} -> {}",
                        blockmodel,
                        set.stream().map(Object::toString).collect(Collectors.joining(" -> ")),
                        _parentLocation);
                unbakedmodel = null;
            }

            if (unbakedmodel == null) {
                blockmodel.setParentLocation(ModelBakery.MISSING_MODEL_LOCATION);
                unbakedmodel = p_119538_.apply(blockmodel.getParentLocation());
            }

            if (!(unbakedmodel instanceof net.neoforged.neoforge.client.resource.model.TopLevelUnbakedModel)) {
                throw new IllegalStateException("BlockModel parent has to be a block model.");
            }

            blockmodel.setParent((net.neoforged.neoforge.client.resource.model.TopLevelUnbakedModel) unbakedmodel);
        }

        for (var loadedChild : _loadedChildren.values()) {
            loadedChild.resolveParents(p_119538_);
        }
    }

    @Nullable
    @Override
    public BakedModel bake(
            final ModelBaker p_250133_,
            final Function<Material, TextureAtlasSprite> p_119535_,
            final ModelState p_119536_) {
        return new Baked(
                _loadedChildren.values().stream()
                        .map(child -> child.bake(p_250133_, p_119535_, p_119536_))
                        .filter(Objects::nonNull)
                        .toList(),
                _hasAmbientOcclusion != null ? _hasAmbientOcclusion : true,
                true,
                _guiLight != null ? _guiLight.lightLikeBlock() : true,
                p_119535_.apply(getMaterial("particle")),
                _overrides.isEmpty()
                        ? ItemOverrides.EMPTY
                        : new ItemOverrides(p_250133_, this, _overrides, p_119535_));
    }

    public Material getMaterial(String p_111481_) {
        if (BlockModel.isTextureReference(p_111481_)) {
            p_111481_ = p_111481_.substring(1);
        }

        List<String> list = Lists.newArrayList();

        while (true) {
            Either<Material, String> either = this.findTextureEntry(p_111481_);
            Optional<Material> optional = either.left();
            if (optional.isPresent()) {
                return optional.get();
            }

            p_111481_ = either.right().get();
            if (list.contains(p_111481_)) {
                LOGGER.warn("Unable to resolve texture due to reference chain {}->{} in {}", Joiner.on("->").join(list), p_111481_, this._name);
                return new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());
            }

            list.add(p_111481_);
        }
    }

    private Either<Material, String> findTextureEntry(String p_111486_) {
        for (TopLevelUnbakedModel blockmodel = this; blockmodel != null; blockmodel = blockmodel.getParent()) {
            Either<Material, String> either = blockmodel.getOwnTextureMap().get(p_111486_);
            if (either != null) {
                return either;
            }
        }

        return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));
    }

    public record Baked(
            List<BakedModel> children,
            boolean useAmbientOcclusion,
            boolean isGui3d,
            boolean usesBlockLight,
            TextureAtlasSprite particleIcon,
            ItemOverrides overrides) implements BakedModel {
        @Override
        public List<BakedQuad> getQuads(
                @Nullable final BlockState p_235039_, @Nullable final Direction p_235040_, final RandomSource p_235041_) {
            return children.stream()
                    .map(child -> child.getQuads(p_235039_, p_235040_, p_235041_))
                    .flatMap(List::stream)
                    .toList();
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
            return children.stream()
                    .map(child -> child.getQuads(state, side, rand, data, renderType))
                    .flatMap(List::stream)
                    .toList();
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return particleIcon;
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }
    }
}
