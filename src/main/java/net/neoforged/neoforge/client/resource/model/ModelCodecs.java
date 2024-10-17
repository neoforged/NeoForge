/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.resource.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Products.P6;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModel.GuiLight;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.registries.NeoForgeClientRegistries;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.apache.commons.lang3.mutable.MutableObject;

public class ModelCodecs {
    private ModelCodecs() {}

    private static final Codec<Material> MATERIAL = ResourceLocation.CODEC.xmap(rl -> new Material(TextureAtlas.LOCATION_BLOCKS, rl), Material::texture);
    // N.B. this parses as a ref first following the vanilla logic, hence why it's written backwards
    private static final Codec<Either<Material, String>> MATERIAL_OR_REFERENCE = Codec.xor(Codec.STRING.validate(str -> {
        if (str.length() <= 1) // vanilla calls charAt(0) and substring(1) for texture refs
            return DataResult.error(() -> "String not long enough");
        if (!BlockModel.isTextureReference(str))
            return DataResult.error(() -> "Not a texture reference");

        return DataResult.success(str.substring(1));
    }), MATERIAL).xmap(Either::swap, Either::swap);

    private static Codec<float[]> floatArray(int minSize, int maxSize) {
        return Codec.FLOAT.listOf(minSize, maxSize).xmap(lst -> {
            var res = new float[lst.size()];
            for (int i = 0; i < lst.size(); i++) {
                res[i] = lst.get(i);
            }
            return res;
        }, arr -> {
            var res = new ArrayList<Float>(arr.length);
            for (final float v : arr) {
                res.add(v);
            }
            return res;
        });
    }

    private static final MapCodec<BlockFaceUV> BLOCK_FACE_UV = RecordCodecBuilder.mapCodec(
            it -> it.group(
                    floatArray(4, 4).optionalFieldOf("uv").forGetter(uvs -> Optional.ofNullable(uvs.uvs)),
                    Codec.INT.optionalFieldOf("rotation", 0).forGetter(uvs -> uvs.rotation))
                    .apply(it, (uvs, rotation) -> new BlockFaceUV(uvs.orElse(null), rotation)));
    private static final Codec<BlockElementFace> BLOCK_ELEMENT_FACE = RecordCodecBuilder.create(
            it -> it.group(
                    Direction.CODEC.lenientOptionalFieldOf("cullface").forGetter(bef -> Optional.ofNullable(bef.cullForDirection())),
                    Codec.INT.optionalFieldOf("tintindex", BlockElementFace.NO_TINT).forGetter(BlockElementFace::tintIndex),
                    Codec.STRING.fieldOf("texture").forGetter(BlockElementFace::texture),
                    BLOCK_FACE_UV.forGetter(BlockElementFace::uv),
                    ExtraFaceData.CODEC.optionalFieldOf("neoforge_data").forGetter(bef -> Optional.ofNullable(bef.getFaceData())))
                    .apply(it, (cull, tint, texture, uv, extra) -> new BlockElementFace(cull.orElse(null), tint, texture, uv, extra.orElse(null), new MutableObject<>())));
    private static final Codec<BlockElementRotation> BLOCK_ELEMENT_ROTATION = RecordCodecBuilder.create(
            it -> it.group(
                    ExtraCodecs.VECTOR3F.fieldOf("origin").forGetter(BlockElementRotation::origin),
                    Direction.Axis.CODEC.fieldOf("axis").forGetter(BlockElementRotation::axis),
                    Codec.FLOAT.fieldOf("angle").validate(angle -> {
                        if (angle != 0.0f && Mth.abs(angle) != 22.5f && Mth.abs(angle) != 45.0f)
                            return DataResult.error(() -> "Invalid rotation " + angle + " found, only -45/-22.5/0/22.5/45 allowed");
                        return DataResult.success(angle);
                    }).forGetter(BlockElementRotation::angle),
                    Codec.BOOL.optionalFieldOf("rescale", false).forGetter(BlockElementRotation::rescale))
                    .apply(it, (origin, axis, angle, rescale) -> new BlockElementRotation(origin.mul(1 / 16f), axis, angle, rescale)));
    private static final Codec<BlockElement> BLOCK_ELEMENT = RecordCodecBuilder.create(
            it -> it.group(
                    ExtraCodecs.VECTOR3F.fieldOf("from").forGetter(elem -> elem.from),
                    ExtraCodecs.VECTOR3F.fieldOf("to").forGetter(elem -> elem.to),
                    Codec.simpleMap(Direction.CODEC, BLOCK_ELEMENT_FACE, StringRepresentable.keys(Direction.values())).fieldOf("faces").forGetter(elem -> elem.faces),
                    BLOCK_ELEMENT_ROTATION.optionalFieldOf("rotation").forGetter(elem -> Optional.ofNullable(elem.rotation)),
                    Codec.BOOL.optionalFieldOf("shade", true).forGetter(elem -> elem.shade),
                    ExtraFaceData.CODEC.optionalFieldOf("neoforge_data", ExtraFaceData.DEFAULT).forGetter(BlockElement::getFaceData))
                    .apply(it, (from, to, faces, rotation, shade, extra) -> new BlockElement(from, to, faces, rotation.orElse(null), shade, extra)));
    private static final Codec<BlockModel.GuiLight> GUI_LIGHT = Codec.STRING.xmap(BlockModel.GuiLight::getByName, BlockModel.GuiLight::name);
    private static final Codec<ItemOverride> ITEM_OVERRIDE = RecordCodecBuilder.create(
            it -> it.group(
                    ResourceLocation.CODEC.fieldOf("model").forGetter(ItemOverride::getModel),
                    Codec.compoundList(ResourceLocation.CODEC, Codec.FLOAT)
                            .xmap(pairList -> pairList.stream().map(pair -> new ItemOverride.Predicate(pair.getFirst(), pair.getSecond())),
                                    primitiveStream -> primitiveStream.map(predicate -> Pair.of(predicate.getProperty(), predicate.getValue())).toList())
                            .fieldOf("predicate")
                            .forGetter(ItemOverride::getPredicates))
                    .apply(it, (rl, prds) -> new ItemOverride(rl, prds.toList())));

    private static final Codec<ItemTransform> ITEM_TRANSFORM = RecordCodecBuilder.create(
            it -> it.group(
                    ExtraCodecs.VECTOR3F.optionalFieldOf("rotation", ItemTransform.Deserializer.DEFAULT_ROTATION).forGetter(t -> t.rotation),
                    ExtraCodecs.VECTOR3F.optionalFieldOf("translation", ItemTransform.Deserializer.DEFAULT_TRANSLATION)
                            .xmap(v -> v.div(16).set(Mth.clamp(v.x, -5f, 5f), Mth.clamp(v.y, -5f, 5f), Mth.clamp(v.z, -5f, 5f)),
                                    v -> v.mul(16F))
                            .forGetter(t -> t.translation),
                    ExtraCodecs.VECTOR3F.optionalFieldOf("scale", ItemTransform.Deserializer.DEFAULT_SCALE)
                            .xmap(v -> v.set(Mth.clamp(v.x, -4f, 4f), Mth.clamp(v.y, -4f, 4f), Mth.clamp(v.z, -4f, 4f)),
                                    Function.identity())
                            .forGetter(tf -> tf.scale),
                    ExtraCodecs.VECTOR3F.optionalFieldOf("right_rotation", ItemTransform.Deserializer.DEFAULT_ROTATION).forGetter(t -> t.rightRotation))
                    .apply(it, ItemTransform::new));

    private static final Codec<ItemTransforms> ITEM_TRANSFORMS = Codec.unboundedMap(ItemDisplayContext.CODEC, ITEM_TRANSFORM)
            .xmap(
                    map -> {
                        var tprh = map.getOrDefault(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, ItemTransform.NO_TRANSFORM);
                        var tplh = map.getOrDefault(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, tprh);
                        var fprh = map.getOrDefault(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemTransform.NO_TRANSFORM);
                        var fplh = map.getOrDefault(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, fprh);
                        var head = map.getOrDefault(ItemDisplayContext.HEAD, ItemTransform.NO_TRANSFORM);
                        var gui = map.getOrDefault(ItemDisplayContext.GUI, ItemTransform.NO_TRANSFORM);
                        var ground = map.getOrDefault(ItemDisplayContext.GROUND, ItemTransform.NO_TRANSFORM);
                        var fixed = map.getOrDefault(ItemDisplayContext.FIXED, ItemTransform.NO_TRANSFORM);
                        var modded = map.entrySet().stream()
                                .filter(p -> p.getKey().isModded())
                                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

                        return new ItemTransforms(tplh, tprh, fplh, fprh, head, gui, ground, fixed, modded);
                    },
                    tfs -> Arrays.stream(ItemDisplayContext.values())
                            .map(it -> Map.entry(it, tfs.getTransform(it)))
                            .filter(p -> p.getValue() != ItemTransform.NO_TRANSFORM)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    public static <O extends TopLevelUnbakedModel> P6<Mu<O>, Optional<ResourceLocation>, Map<String, Either<Material, String>>, Optional<Boolean>, Optional<GuiLight>, ItemTransforms, List<ItemOverride>> commonCodec() {
        return RecordCodecBuilder.<O>instance()
                .group(
                        ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(bm -> Optional.ofNullable(bm.getParentLocation())),
                        Codec.unboundedMap(Codec.STRING, MATERIAL_OR_REFERENCE).optionalFieldOf("textures", Map.of()).forGetter(
                                TopLevelUnbakedModel::getOwnTextureMap),
                        Codec.BOOL.optionalFieldOf("ambientocclusion").forGetter(bm -> Optional.ofNullable(bm.getOwnAmbientOcclusion())),
                        GUI_LIGHT.optionalFieldOf("gui_light").forGetter(bm -> Optional.ofNullable(bm.getOwnGuiLight())),
                        ITEM_TRANSFORMS.optionalFieldOf("display", ItemTransforms.NO_TRANSFORMS).forGetter(TopLevelUnbakedModel::getOwnTransforms),
                        ITEM_OVERRIDE.listOf().optionalFieldOf("overrides", List.of()).forGetter(TopLevelUnbakedModel::getOwnOverrides));
    }

    public static final MapCodec<BlockModel> BLOCK_MODEL = RecordCodecBuilder.mapCodec(
            it -> it.group(
                    BLOCK_ELEMENT.listOf().optionalFieldOf("elements", List.of()).forGetter(BlockModel::getElements))
                    .and(ModelCodecs.commonCodec())
                    .apply(it, (elements, parent, textures, ao, guiLight, display, overrides) -> new BlockModel(parent.orElse(null), elements, textures, ao.orElse(null), guiLight.orElse(null), display, overrides)));

    public static Codec<TopLevelUnbakedModel> UNBAKED_MODEL = NeoForgeExtraCodecs.dispatchMapOrElse(
            NeoForgeClientRegistries.UNBAKED_MODEL_SERIALIZERS.byNameCodec(),
            TopLevelUnbakedModel::codec, Function.identity(), BLOCK_MODEL)
            .xmap(Either::unwrap, Either::left)
            .codec();
    //public static Codec<Optional<WithConditions<UnbakedModel>>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(UNBAKED_MODEL);
}
