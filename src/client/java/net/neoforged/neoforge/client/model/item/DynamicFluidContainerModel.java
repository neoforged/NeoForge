/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.item;

import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.color.item.FluidContentsTint;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.ComposedModelState;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A dynamic fluid container model, capable of re-texturing itself at runtime to match the contained fluid.
 * <p>
 * Composed of a base layer, a fluid layer (applied with a mask) and a cover layer (optionally applied with a mask).
 * The entire model may optionally be flipped if the fluid is gaseous, and the fluid layer may glow if light-emitting.
 */
public class DynamicFluidContainerModel implements ItemModel {
    // Depth offsets to prevent Z-fighting
    private static final Transformation FLUID_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1.002f), new Quaternionf());
    private static final Transformation COVER_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1.004f), new Quaternionf());
    private static final ModelDebugName DEBUG_NAME = () -> "DynamicFluidContainerModel";

    private static RenderTypeGroup getLayerRenderTypes(boolean unlit) {
        return new RenderTypeGroup(ChunkSectionLayer.TRANSLUCENT, unlit ? NeoForgeRenderTypes.ITEM_UNSORTED_UNLIT_TRANSLUCENT.get() : NeoForgeRenderTypes.ITEM_UNSORTED_TRANSLUCENT.get());
    }

    private final Unbaked unbakedModel;
    private final BakingContext bakingContext;
    private final ItemTransforms itemTransforms;
    private final Map<Fluid, ItemModel> cache = new IdentityHashMap<>(); // contains all the baked models since they'll never change

    private DynamicFluidContainerModel(Unbaked unbakedModel, BakingContext bakingContext) {
        this.unbakedModel = unbakedModel;
        this.bakingContext = bakingContext;
        // Source ItemTransforms from the base item model
        var baseItemModel = bakingContext.blockModelBaker().getModel(ResourceLocation.withDefaultNamespace("item/generated"));
        if (baseItemModel == null) {
            throw new IllegalStateException("Failed to access item/generated model");
        }
        this.itemTransforms = baseItemModel.getTopTransforms();
    }

    private ItemModel bakeModelForFluid(Fluid fluid) {
        var sprites = bakingContext.blockModelBaker().sprites();

        Material particleLocation = unbakedModel.textures.particle.map(ClientHooks::getBlockMaterial).orElse(null);
        Material baseLocation = unbakedModel.textures.base.map(ClientHooks::getBlockMaterial).orElse(null);
        Material fluidMaskLocation = unbakedModel.textures.fluid.map(ClientHooks::getBlockMaterial).orElse(null);
        Material coverLocation = unbakedModel.textures.cover.map(ClientHooks::getBlockMaterial).orElse(null);

        TextureAtlasSprite baseSprite = baseLocation != null ? sprites.get(baseLocation, DEBUG_NAME) : null;
        TextureAtlasSprite fluidSprite = fluid != Fluids.EMPTY ? sprites.get(ClientHooks.getBlockMaterial(IClientFluidTypeExtensions.of(fluid).getStillTexture()), DEBUG_NAME) : null;
        TextureAtlasSprite coverSprite = (coverLocation != null && (!unbakedModel.coverIsMask || baseLocation != null)) ? sprites.get(coverLocation, DEBUG_NAME) : null;

        TextureAtlasSprite particleSprite = particleLocation != null ? sprites.get(particleLocation, DEBUG_NAME) : null;

        if (particleSprite == null) particleSprite = fluidSprite;
        if (particleSprite == null) particleSprite = baseSprite;
        if (particleSprite == null && !unbakedModel.coverIsMask) particleSprite = coverSprite;

        // If the fluid is lighter than air, rotate 180deg to turn it upside down
        ModelState state = BlockModelRotation.X0_Y0;
        if (unbakedModel.flipGas && fluid != Fluids.EMPTY && fluid.getFluidType().isLighterThanAir()) {
            state = new ComposedModelState(state, new Transformation(null, new Quaternionf(0, 0, 1, 0), null, null));
        }

        List<ItemModel> subModels = new ArrayList<>();
        ModelRenderProperties renderProperties = new ModelRenderProperties(false, particleSprite, itemTransforms);

        var normalRenderTypes = getLayerRenderTypes(false);

        if (baseLocation != null) {
            // Base texture
            var unbaked = UnbakedElementsHelper.createUnbakedItemElements(0, baseSprite);
            var quads = UnbakedElementsHelper.bakeElements(unbaked, $ -> baseSprite, state);
            subModels.add(new BlockModelWrapper(List.of(), quads, renderProperties, normalRenderTypes.entity()));
        }

        if (fluidMaskLocation != null && fluidSprite != null) {
            TextureAtlasSprite templateSprite = sprites.get(fluidMaskLocation, DEBUG_NAME);
            // Fluid layer
            var transformedState = new ComposedModelState(state, FLUID_TRANSFORM);
            var unbaked = UnbakedElementsHelper.createUnbakedItemMaskElements(0, templateSprite); // Use template as mask
            var quads = UnbakedElementsHelper.bakeElements(unbaked, $ -> fluidSprite, transformedState); // Bake with fluid texture

            var emissive = unbakedModel.applyFluidLuminosity && fluid.getFluidType().getLightLevel() > 0;
            var renderTypes = getLayerRenderTypes(emissive);
            if (emissive) QuadTransformers.settingMaxEmissivity().processInPlace(quads);

            subModels.add(new BlockModelWrapper(List.of(FluidContentsTint.INSTANCE), quads, renderProperties, renderTypes.entity()));
        }

        if (coverSprite != null) {
            var sprite = unbakedModel.coverIsMask ? baseSprite : coverSprite;
            // Cover/overlay
            var transformedState = new ComposedModelState(state, COVER_TRANSFORM);
            var unbaked = UnbakedElementsHelper.createUnbakedItemMaskElements(0, coverSprite); // Use cover as mask
            var quads = UnbakedElementsHelper.bakeElements(unbaked, $ -> sprite, transformedState); // Bake with selected texture
            subModels.add(new BlockModelWrapper(List.of(), quads, renderProperties, normalRenderTypes.entity()));
        }

        return new CompositeModel(subModels);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        var fluidStack = FluidUtil.getFirstStackContained(stack);
        var fluid = fluidStack.isEmpty() ? unbakedModel.fluid : fluidStack.getFluid();

        cache.computeIfAbsent(fluid, this::bakeModelForFluid)
                .update(renderState, stack, modelResolver, displayContext, level, owner, seed);
    }

    public record Textures(
            Optional<ResourceLocation> particle,
            Optional<ResourceLocation> base,
            Optional<ResourceLocation> fluid,
            Optional<ResourceLocation> cover) {
        public static final Codec<Textures> CODEC = RecordCodecBuilder.<Textures>create(
                instance -> instance
                        .group(
                                ResourceLocation.CODEC.optionalFieldOf("particle").forGetter(Textures::particle),
                                ResourceLocation.CODEC.optionalFieldOf("base").forGetter(Textures::base),
                                ResourceLocation.CODEC.optionalFieldOf("fluid").forGetter(Textures::fluid),
                                ResourceLocation.CODEC.optionalFieldOf("cover").forGetter(Textures::cover))
                        .apply(instance, Textures::new))
                .validate(textures -> {
                    if (textures.particle.isPresent() || textures.base.isPresent() || textures.fluid.isPresent() || textures.cover.isPresent()) {
                        return DataResult.success(textures);
                    }
                    return DataResult.error(() -> "Dynamic fluid container model requires at least one particle, base, fluid or cover texture.");
                });
    }

    public record Unbaked(Textures textures, Fluid fluid, boolean flipGas, boolean coverIsMask, boolean applyFluidLuminosity) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance
                        .group(
                                Textures.CODEC.fieldOf("textures").forGetter(Unbaked::textures),
                                BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(Unbaked::fluid),
                                Codec.BOOL.optionalFieldOf("flip_gas", false).forGetter(Unbaked::flipGas),
                                Codec.BOOL.optionalFieldOf("cover_is_mask", true).forGetter(Unbaked::coverIsMask),
                                Codec.BOOL.optionalFieldOf("apply_fluid_luminosity", true).forGetter(Unbaked::applyFluidLuminosity))
                        .apply(instance, Unbaked::new));

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(BakingContext bakingContext) {
            return new DynamicFluidContainerModel(this, bakingContext);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            //No dependencies
        }
    }
}
