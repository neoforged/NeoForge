/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.item;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.model.ComposedModelState;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record TrimmedArmorModel(ItemModel base, ResourceLocation trimTexture, Optional<ResourceLocation> darkerTrim, ItemTransforms transforms, BakingContext context) implements ItemModel {

    private static final Transformation TRIM_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1.002F, 1.002F, 1.002F), new Quaternionf());
    private static final ModelState TRIM_STATE = new ComposedModelState(BlockModelRotation.X0_Y0, TRIM_TRANSFORM);
    private static final ModelDebugName DEBUG_NAME = () -> "TrimmedArmorModel";

    private static final Object2ObjectMap<ResourceKey<TrimMaterial>, Function<ResourceLocation, ItemModel>> TRIM_LAYERS = new Object2ObjectOpenHashMap<>();

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        this.base().update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);

        if (stack.get(DataComponents.TRIM) != null) {
            ResourceKey<TrimMaterial> key = Objects.requireNonNull(stack.get(DataComponents.TRIM)).material().getKey();
            TRIM_LAYERS.computeIfAbsent(key, this::createTrimLayer).apply(this.trimTexture()).update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);
        }
    }

    private Function<ResourceLocation, ItemModel> createTrimLayer(ResourceKey<TrimMaterial> material) {
        return trimTex -> {
            boolean darker = this.darkerTrim().isPresent() && material.location().equals(this.darkerTrim().get());
            var sprite = this.context().blockModelBaker().sprites().get(ClientHooks.getBlockMaterial(trimTex.withSuffix("_" + material.location().getPath() + (darker ? "_darker" : ""))), DEBUG_NAME);
            var renderProperties = new ModelRenderProperties(false, sprite, this.transforms());

            var unbaked = UnbakedElementsHelper.createUnbakedItemElements(0, sprite);
            var quads = UnbakedElementsHelper.bakeElements(unbaked, $ -> sprite, TRIM_STATE);

            return new BlockModelWrapper(List.of(), quads, renderProperties, NeoForgeRenderTypes.ITEM_UNSORTED_UNLIT_TRANSLUCENT.get());
        };
    }
    public record Unbaked(BlockModelWrapper.Unbaked baseModel, ResourceLocation trimTexture, Optional<ResourceLocation> darkerTrim) implements ItemModel.Unbaked {

        public static final MapCodec<TrimmedArmorModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockModelWrapper.Unbaked.MAP_CODEC.fieldOf("base_model").forGetter(TrimmedArmorModel.Unbaked::baseModel),
                ResourceLocation.CODEC.fieldOf("base_trim_texture").forGetter(Unbaked::trimTexture),
                ResourceLocation.CODEC.optionalFieldOf("darker_trim_override").forGetter(Unbaked::darkerTrim))
                .apply(instance, Unbaked::new));
        @Override
        public ItemModel bake(BakingContext context) {
            return new TrimmedArmorModel(this.baseModel().bake(context), this.trimTexture(), this.darkerTrim(), context.blockModelBaker().getModel(this.baseModel().model()).getTopTransforms(), context);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            this.baseModel().resolveDependencies(resolver);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
