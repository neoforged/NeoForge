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
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.neoforged.neoforge.client.model.ComposedModelState;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/// A dynamic model that applies an armor trim texture on top of an existing model when the trim component exists.
///
/// NOTE: If multiple mods add a trim material with the same path, only the one that gets loaded last will have its proper colors.
///
/// Example: if mod A and mod B register a "tin" trim material, and mod B loads after A, mod B's material will be the one used.
///
/// **Mod A's ingot will still work just fine as a trim material, it will just look like whatever mod B defined for theirs!**
///
/// All this means is that if another mod is registering the same material(s) as you and uses this system, your trim colors *may* not look exactly how you want them to.
///
/// This issue can be avoided by making your material path more unique. We would encourage you to prefix your material with your mod id or something along those lines.
public class TrimmedArmorModel implements ItemModel {
    private static final Transformation TRIM_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1.002F, 1.002F, 1.002F), new Quaternionf());
    private static final ModelDebugName DEBUG_NAME = () -> "TrimmedArmorModel";
    private static final ModelState TRIM_STATE = new ComposedModelState(BlockModelRotation.IDENTITY, TRIM_TRANSFORM);

    private final Object2ObjectMap<String, ItemModel> itemsWithTrims = new Object2ObjectOpenHashMap<>();

    private final ItemModel baseModel;
    private final Identifier baseTrimTexture;
    private final BakingContext bakingContext;
    private final Matrix4fc transformation;
    private final ItemTransforms itemTransforms;

    private TrimmedArmorModel(ItemModel baseModel, Identifier baseTrimTexture, BakingContext bakingContext, Matrix4fc transformation) {
        this.baseModel = baseModel;
        this.baseTrimTexture = baseTrimTexture;
        this.bakingContext = bakingContext;
        this.transformation = transformation;
        var baseItemModel = bakingContext.blockModelBaker().getModel(Identifier.withDefaultNamespace("item/generated"));
        this.itemTransforms = baseItemModel.getTopTransforms();
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        this.baseModel.update(state, stack, resolver, context, level, owner, seed);

        if (stack.has(DataComponents.TRIM) && stack.has(DataComponents.EQUIPPABLE)) {
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);

            if (equippable.assetId().isPresent()) {
                Holder<TrimMaterial> material = Objects.requireNonNull(stack.get(DataComponents.TRIM)).material();
                String suffix = material.value().assets().assetId(equippable.assetId().get()).suffix();

                this.itemsWithTrims.computeIfAbsent(suffix, this::createTrimLayer).update(state, stack, resolver, context, level, owner, seed);
            }
        }
    }

    private ItemModel createTrimLayer(String suffix) {
        ModelBaker baker = this.bakingContext.blockModelBaker();
        MaterialBaker materials = baker.materials();

        Material.Baked overlayMat = materials.get(new Material(this.baseTrimTexture.withSuffix("_" + suffix)), DEBUG_NAME);
        ModelRenderProperties overlayRenderProps = new ModelRenderProperties(false, overlayMat, this.itemTransforms);
        QuadCollection overlayQuads = baker.compute(new ItemModelGenerator.ItemLayerKey(overlayMat, TRIM_STATE, 0));
        return new CuboidItemModelWrapper(List.of(), overlayQuads, overlayRenderProps, this.transformation);
    }

    public record Unbaked(ItemModel.Unbaked baseModel, Identifier baseTrimTexture) implements ItemModel.Unbaked {
        public static final MapCodec<TrimmedArmorModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemModels.CODEC.fieldOf("base_model").forGetter(Unbaked::baseModel),
                Identifier.CODEC.fieldOf("base_trim_texture").forGetter(Unbaked::baseTrimTexture))
                .apply(instance, Unbaked::new));

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            return new TrimmedArmorModel(this.baseModel().bake(context, transformation), this.baseTrimTexture(), context, transformation);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            this.baseModel.resolveDependencies(resolver);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
