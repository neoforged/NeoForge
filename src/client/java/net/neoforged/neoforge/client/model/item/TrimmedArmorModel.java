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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.ComposedModelState;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record TrimmedArmorModel(ItemModel base, ResourceLocation trimTexture, ItemTransforms transforms, BakingContext context) implements ItemModel {

    private static final Transformation TRIM_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1.002F, 1.002F, 1.002F), new Quaternionf());
    private static final ModelState TRIM_STATE = new ComposedModelState(BlockModelRotation.X0_Y0, TRIM_TRANSFORM);
    private static final ModelDebugName DEBUG_NAME = () -> "TrimmedArmorModel";

    private static final Object2ObjectMap<TextureAtlasSprite, ItemModel> TRIM_LAYERS = new Object2ObjectOpenHashMap<>();

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        this.base().update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);

        if (stack.has(DataComponents.TRIM) && stack.has(DataComponents.EQUIPPABLE)) {
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);

            if (equippable.assetId().isPresent()) {
                Holder<TrimMaterial> material = Objects.requireNonNull(stack.get(DataComponents.TRIM)).material();
                String suffix = material.value().assets().assetId(equippable.assetId().get()).suffix();
                var sprite = this.context().blockModelBaker().sprites().get(ClientHooks.getBlockMaterial(this.trimTexture().withSuffix("_" + material.getKey().location().getPath() + suffix)), DEBUG_NAME);

                TRIM_LAYERS.computeIfAbsent(sprite, this::createTrimLayer).update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);
            }
        }
    }

    private ItemModel createTrimLayer(TextureAtlasSprite sprite) {
        var renderProperties = new ModelRenderProperties(false, sprite, this.transforms());

        var unbaked = UnbakedElementsHelper.createUnbakedItemElements(0, sprite);
        var quads = UnbakedElementsHelper.bakeElements(unbaked, $ -> sprite, TRIM_STATE);

        return new BlockModelWrapper(List.of(), quads, renderProperties, Sheets.translucentItemSheet());
    }
    public record Unbaked(BlockModelWrapper.Unbaked baseModel, ResourceLocation trimTexture) implements ItemModel.Unbaked {
        public static final MapCodec<TrimmedArmorModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockModelWrapper.Unbaked.MAP_CODEC.fieldOf("base_model").forGetter(TrimmedArmorModel.Unbaked::baseModel),
                ResourceLocation.CODEC.fieldOf("base_trim_texture").forGetter(Unbaked::trimTexture))
                .apply(instance, Unbaked::new));

        @Override
        public ItemModel bake(BakingContext context) {
            return new TrimmedArmorModel(this.baseModel().bake(context), this.trimTexture(), context.blockModelBaker().getModel(this.baseModel().model()).getTopTransforms(), context);
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
