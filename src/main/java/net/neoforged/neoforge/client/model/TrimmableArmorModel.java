/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

public class TrimmableArmorModel implements IUnbakedGeometry<TrimmableArmorModel> {
    private final BlockModel baseModel;
    private final BlockModel trimmedModel;
    private final String trimTextureTarget;
    private final ResourceLocation trimTextureId;
    private final ImmutableMap<String, BlockModel> materialOverrides;

    public TrimmableArmorModel(BlockModel baseModel, BlockModel trimmedModel, String trimTextureTarget, ResourceLocation trimTextureId, ImmutableMap<String, BlockModel> materialOverrides) {
        this.baseModel = baseModel;
        this.trimmedModel = trimmedModel;
        this.trimTextureTarget = trimTextureTarget;
        this.trimTextureId = trimTextureId;
        this.materialOverrides = materialOverrides;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        baseModel.resolveParents(modelGetter);
        trimmedModel.resolveParents(modelGetter);
        materialOverrides.values().forEach(blockModel -> blockModel.resolveParents(modelGetter));
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        Map<String, BakedModel> bakedOverrides = new LinkedHashMap<>();

        for (Map.Entry<String, BlockModel> entry : materialOverrides.entrySet()) {
            bakedOverrides.put(entry.getKey(), entry.getValue().bake(baker, trimmedModel, spriteGetter, modelState, new ResourceLocation("neoforge:trimmed_armor_override"), false));
        }

        return new Baked(
                baker, overrides, trimTextureTarget, trimTextureId,
                baseModel.bake(baker, baseModel, spriteGetter, modelState, modelLocation, context.useBlockLight()),
                trimmedModel, bakedOverrides);
    }

    public static class Baked extends BakedModelWrapper<BakedModel> {
        private final ItemOverrides overrides;

        public Baked(
                ModelBaker modelBaker, ItemOverrides overrides,
                String targetTexture, ResourceLocation baseLocation,
                BakedModel baseModel, BlockModel trimmedModel,
                Map<String, BakedModel> materialOverrides) {
            super(baseModel);
            this.overrides = new Overrides(overrides, modelBaker, trimmedModel, targetTexture, baseLocation, materialOverrides);
        }

        @Override
        public ItemOverrides getOverrides() {
            return this.overrides;
        }
    }

    public static class Overrides extends ItemOverrides {
        private final HashMap<String, BakedModel> models = new HashMap<>();
        private final BlockModel trimmedModel;
        private final ModelBaker baker;
        private final ItemOverrides nested;
        private final String targetTexture;
        private final ResourceLocation baseLocation;

        public Overrides(ItemOverrides nested, ModelBaker baker, BlockModel trimmedModel, String targetTexture, ResourceLocation baseLocation, Map<String, BakedModel> materialOverrides) {
            this.nested = nested;
            this.baker = baker;
            this.trimmedModel = trimmedModel;
            this.targetTexture = targetTexture;
            this.baseLocation = baseLocation;
            this.models.putAll(materialOverrides);
        }

        @Nullable
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            BakedModel overridden = nested.resolve(model, stack, level, entity, seed);
            if (overridden != model) return overridden;
            if (level != null && stack.is(ItemTags.TRIMMABLE_ARMOR)) {
                Optional<ArmorTrim> optional = ArmorTrim.getTrim(level.registryAccess(), stack, true);
                if (optional.isEmpty()) {
                    return model;
                }
                ArmorTrim armorTrim = optional.get();
                TrimMaterial trimMaterial = armorTrim.material().value();
                String assetName = trimMaterial.assetName();
                return models.computeIfAbsent(assetName, s -> {
                    String suffix = assetName;
                    if (stack.getItem() instanceof ArmorItem armorItem) {
                        ArmorMaterial armorMaterial = armorItem.getMaterial();
                        // TODO: allow armor trim materials to use modded armor materials
                        Map<ArmorMaterials, String> map = trimMaterial.overrideArmorMaterials();
                        suffix = armorMaterial instanceof ArmorMaterials && map.containsKey(armorMaterial) ? trimMaterial.overrideArmorMaterials().get(armorMaterial) : suffix;
                    }
                    HashMap<String, Either<Material, String>> textures = new HashMap<>();
                    ResourceLocation tex = new ResourceLocation(baseLocation.getNamespace(), baseLocation.getPath() + "_" + suffix);
                    textures.put(targetTexture, Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, tex)));
                    BlockModel blockModel = new BlockModel(null, Collections.emptyList(), textures, null, null, ItemTransforms.NO_TRANSFORMS, Collections.emptyList());
                    blockModel.parent = trimmedModel;
                    return blockModel.bake(baker, trimmedModel, Material::sprite, BlockModelRotation.X0_Y0, new ResourceLocation("neoforge:trimmed_armor_override"), false);
                });
            }
            return model;
        }
    }

    public static final class Loader implements IGeometryLoader<TrimmableArmorModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public TrimmableArmorModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
            BlockModel base = deserializationContext.deserialize(GsonHelper.getAsJsonObject(jsonObject, "base"), BlockModel.class);

            JsonObject trimmed = GsonHelper.getAsJsonObject(jsonObject, "trimmed");
            BlockModel trimmedModel = deserializationContext.deserialize(GsonHelper.getAsJsonObject(trimmed, "model"), BlockModel.class);
            String textureTarget = GsonHelper.getAsString(trimmed, "target_texture");
            ResourceLocation baseLocation = new ResourceLocation(GsonHelper.getAsString(trimmed, "base_asset"));

            ImmutableMap.Builder<String, BlockModel> overrideModels = ImmutableMap.builder();

            if (trimmed.has("overrides")) {
                for (Map.Entry<String, JsonElement> entry : trimmed.getAsJsonObject("overrides").entrySet()) {
                    JsonObject overrideModel = GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey());
                    overrideModels.put(entry.getKey(), deserializationContext.deserialize(overrideModel, BlockModel.class));
                }
            }

            return new TrimmableArmorModel(base, trimmedModel, textureTarget, baseLocation, overrideModels.build());
        }
    }
}
