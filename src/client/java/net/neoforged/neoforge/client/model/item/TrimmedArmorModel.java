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
import java.util.function.Function;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.ComposedModelState;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * A dynamic model that applies an armor trim texture on top of an existing model when the trim component exists.
 * <p>
 * NOTE: If multiple mods add a trim material with the same path, only the one that gets loaded last will have its proper colors. <br>
 * Example: if mod A and mod B register a "tin" trim material, and mod B loads after A, mod B's material will be the one used. <br>
 * <b>Mod A's ingot will still work just fine as a trim material, it will just look like whatever mod B defined for theirs!</b> <br>
 * All this means is that if another mod is registering the same material(s) as you and uses this system, your trim colors <i>may</i> not look exactly how you want them to. <br>
 * This issue can be avoided by making your material path more unique. We would encourage you to prefix your material with your mod id or something along those lines.
 */
public class TrimmedArmorModel implements ItemModel {
    private static final Transformation TRIM_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1.002F, 1.002F, 1.002F), new Quaternionf());
    private static final ModelState TRIM_STATE = new ComposedModelState(BlockModelRotation.IDENTITY, TRIM_TRANSFORM);
    private static final ModelDebugName DEBUG_NAME = () -> "TrimmedArmorModel";

    private final Object2ObjectMap<Identifier, ItemModel> trimLayers = new Object2ObjectOpenHashMap<>();

    private final ItemModel base;
    private final Identifier trimTexture;
    private final ItemTransforms transforms;
    private final BakingContext context;

    public TrimmedArmorModel(ItemModel base, Identifier trimTexture, ItemTransforms transforms, BakingContext context) {
        this.base = base;
        this.trimTexture = trimTexture;
        this.transforms = transforms;
        this.context = context;
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        this.base.update(renderState, stack, itemModelResolver, displayContext, level, owner, seed);

        if (stack.has(DataComponents.TRIM) && stack.has(DataComponents.EQUIPPABLE)) {
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);

            if (equippable.assetId().isPresent()) {
                Holder<TrimMaterial> material = Objects.requireNonNull(stack.get(DataComponents.TRIM)).material();
                String suffix = material.value().assets().assetId(equippable.assetId().get()).suffix();

                this.trimLayers.computeIfAbsent(this.trimTexture.withSuffix("_" + suffix), this::createTrimLayer).update(renderState, stack, itemModelResolver, displayContext, level, owner, seed);
            }
        }
    }

    private ItemModel createTrimLayer(Identifier suffixedTrimTexture) {
        TextureAtlasSprite sprite = this.context.blockModelBaker().sprites().get(ClientHooks.getBlockMaterial(suffixedTrimTexture), DEBUG_NAME);
        ModelRenderProperties renderProperties = new ModelRenderProperties(false, sprite, this.transforms);

        List<BlockElement> unbaked = UnbakedElementsHelper.createUnbakedItemElements(0, sprite);
        List<BakedQuad> quads = UnbakedElementsHelper.bakeElements(unbaked, $ -> sprite, TRIM_STATE);
        Function<ItemStack, RenderType> renderType = RenderTypeHelper.detectItemModelRenderType(quads, new RenderTypeGroup(ChunkSectionLayer.TRANSLUCENT, NeoForgeRenderTypes::getItemLayeredTranslucent));

        return new BlockModelWrapper(List.of(), quads, renderProperties, renderType);
    }

    public record Unbaked(BlockModelWrapper.Unbaked baseModel, Identifier trimTexture) implements ItemModel.Unbaked {
        public static final MapCodec<TrimmedArmorModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockModelWrapper.Unbaked.MAP_CODEC.fieldOf("base_model").forGetter(TrimmedArmorModel.Unbaked::baseModel),
                Identifier.CODEC.fieldOf("base_trim_texture").forGetter(Unbaked::trimTexture))
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
