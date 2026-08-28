/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import net.neoforged.neoforge.common.util.ConcatenatedListView;
import org.jetbrains.annotations.Nullable;

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
public class TrimmedArmorModel implements IUnbakedGeometry<TrimmedArmorModel> {
    private static final IQuadTransformer TRIM_TRANSFORM = quad -> {
        int[] vertices = quad.getVertices();
        for (int quadIdx = 0; quadIdx < 4; quadIdx++) {
            int baseIdx = IQuadTransformer.POSITION + quadIdx * IQuadTransformer.STRIDE;
            for (int coordIdx = 0; coordIdx < 3; coordIdx++) {
                int index = baseIdx + coordIdx;
                float value = Float.intBitsToFloat(vertices[index]);
                value = (value - 0.5F) * 1.002F + 0.5F;
                vertices[index] = Float.floatToIntBits(value);
            }
        }
    };

    private final BlockModel baseModel;
    private final ResourceLocation baseTrimTexture;

    private TrimmedArmorModel(BlockModel baseModel, ResourceLocation baseTrimTexture) {
        this.baseModel = baseModel;
        this.baseTrimTexture = baseTrimTexture;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        BakedModel bakedModel = baseModel.bake(baker, baseModel, spriteGetter, modelState, context.useBlockLight());
        TrimmedArmorOverrideHandler trimOverrides = new TrimmedArmorOverrideHandler(bakedModel, baseTrimTexture, modelState);
        return new UntrimmedBaked(bakedModel, trimOverrides);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        baseModel.resolveParents(modelGetter);
    }

    private static List<BakedQuad> createTrimLayer(ResourceLocation trimTexture, ModelState modelState) {
        TextureAtlasSprite sprite = new Material(TextureAtlas.LOCATION_BLOCKS, trimTexture).sprite();
        List<BlockElement> elements = UnbakedGeometryHelper.createUnbakedItemElements(-1, sprite);
        List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(elements, mat -> sprite, modelState);
        TRIM_TRANSFORM.processInPlace(quads);
        return quads;
    }

    private static class UntrimmedBaked extends PropagatingBakedModelWrapper<BakedModel> {
        private final TrimmedArmorOverrideHandler overrides;

        public UntrimmedBaked(BakedModel originalModel, TrimmedArmorOverrideHandler overrides) {
            super(originalModel);
            this.overrides = overrides;
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }

        protected BakedModel rewrap(BakedModel model) {
            return model == originalModel ? this : new UntrimmedBaked(model, overrides.withModel(model));
        }
    }

    private static class TrimmedBaked extends PropagatingBakedModelWrapper<BakedModel> {
        private final List<BakedQuad> trimQuads;

        public TrimmedBaked(BakedModel originalModel, List<BakedQuad> trimQuads) {
            super(originalModel);
            this.trimQuads = trimQuads;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            return addTrimQuads(side, super.getQuads(state, side, rand));
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
            return addTrimQuads(side, super.getQuads(state, side, rand, extraData, renderType));
        }

        @Override
        protected BakedModel rewrap(BakedModel model) {
            return model == originalModel ? this : new TrimmedBaked(model, trimQuads);
        }

        private List<BakedQuad> addTrimQuads(@Nullable Direction side, List<BakedQuad> quads) {
            return side == null ? ConcatenatedListView.of(List.of(quads, trimQuads)) : quads;
        }
    }

    public static class Loader implements IGeometryLoader<TrimmedArmorModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public TrimmedArmorModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
            if (!jsonObject.has("base_model"))
                throw new JsonParseException("A trimmed armor model must have a \"base_model\" member.");

            BlockModel baseModel = ExtendedBlockModelDeserializer.INSTANCE.fromJson(jsonObject.getAsJsonObject("base_model"), BlockModel.class);

            if (!jsonObject.has("base_trim_texture"))
                throw new JsonParseException("A trimmed armor model must have a \"base_trim_texture\" member.");

            ResourceLocation baseTrimTexture = ResourceLocation.parse(jsonObject.get("base_trim_texture").getAsString());

            return new TrimmedArmorModel(baseModel, baseTrimTexture);
        }
    }

    private static final class TrimmedArmorOverrideHandler extends ItemOverrides {
        private final Object2ObjectMap<String, List<BakedQuad>> trimQuadLists = new Object2ObjectOpenHashMap<>();

        private final BakedModel baseModel;
        private final ResourceLocation baseTrimTexture;
        private final ModelState modelState;

        private TrimmedArmorOverrideHandler(BakedModel baseModel, ResourceLocation baseTrimTexture, ModelState modelState) {
            this.baseModel = baseModel;
            this.baseTrimTexture = baseTrimTexture;
            this.modelState = modelState;
        }

        @Override
        public @Nullable BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            BakedModel override = baseModel.getOverrides().resolve(baseModel, stack, level, entity, seed);
            ArmorTrim trim = stack.get(DataComponents.TRIM);
            if (override == null || trim == null || !(stack.getItem() instanceof ArmorItem armorItem)) {
                return override;
            }

            String suffix = ArmorTrim.getColorPaletteSuffix(trim.material(), armorItem.getMaterial());
            List<BakedQuad> trimQuads = trimQuadLists.computeIfAbsent(suffix, s -> createTrimLayer(baseTrimTexture.withSuffix("_" + suffix), modelState));
            return new TrimmedBaked(override, trimQuads);
        }

        private TrimmedArmorOverrideHandler withModel(BakedModel model) {
            return new TrimmedArmorOverrideHandler(model, baseTrimTexture, modelState);
        }
    }
}
