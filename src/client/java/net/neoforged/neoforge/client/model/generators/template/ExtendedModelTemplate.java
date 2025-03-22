/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Quadrant;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

public final class ExtendedModelTemplate extends ModelTemplate {
    final Map<ItemDisplayContext, TransformVecBuilder> transforms;
    final List<ElementBuilder> elements;
    @Nullable
    final CustomLoaderBuilder customLoader;
    final RootTransformsBuilder rootTransforms;
    @Nullable
    final ResourceLocation renderType;
    @Nullable
    final Boolean ambientOcclusion;
    @Nullable
    final UnbakedModel.GuiLight guiLight;

    ExtendedModelTemplate(ExtendedModelTemplateBuilder builder) {
        super(builder.parent, builder.suffix, builder.requiredSlots.toArray(TextureSlot[]::new));
        this.transforms = Map.copyOf(builder.transforms);
        this.elements = List.copyOf(builder.elements);
        this.customLoader = builder.customLoader;
        this.rootTransforms = builder.rootTransforms;
        this.renderType = builder.renderType;
        this.ambientOcclusion = builder.ambientOcclusion;
        this.guiLight = builder.guiLight;
    }

    @Override
    public JsonObject createBaseTemplate(ResourceLocation modelPath, Map<TextureSlot, ResourceLocation> textureMap) {
        var root = super.createBaseTemplate(modelPath, textureMap);

        if (this.ambientOcclusion != null) {
            root.addProperty("ambientocclusion", this.ambientOcclusion);
        }

        if (this.guiLight != null) {
            root.addProperty("gui_light", this.guiLight.getSerializedName());
        }

        if (this.renderType != null) {
            root.addProperty("render_type", this.renderType.toString());
        }

        if (!this.transforms.isEmpty()) {
            JsonObject display = new JsonObject();
            for (Map.Entry<ItemDisplayContext, TransformVecBuilder> e : this.transforms.entrySet()) {
                JsonObject transform = new JsonObject();
                ItemTransform vec = e.getValue().build();
                if (vec.equals(ItemTransform.NO_TRANSFORM)) continue;
                var hasRightRotation = !vec.rightRotation().equals(ItemTransform.Deserializer.DEFAULT_ROTATION);
                if (!vec.translation().equals(ItemTransform.Deserializer.DEFAULT_TRANSLATION)) {
                    transform.add("translation", serializeVector3f(vec.translation()));
                }
                if (!vec.rotation().equals(ItemTransform.Deserializer.DEFAULT_ROTATION)) {
                    transform.add(hasRightRotation ? "left_rotation" : "rotation", serializeVector3f(vec.rotation()));
                }
                if (!vec.scale().equals(ItemTransform.Deserializer.DEFAULT_SCALE)) {
                    transform.add("scale", serializeVector3f(vec.scale()));
                }
                if (hasRightRotation) {
                    transform.add("right_rotation", serializeVector3f(vec.rightRotation()));
                }
                display.add(e.getKey().getSerializedName(), transform);
            }
            root.add("display", display);
        }

        if (!this.elements.isEmpty()) {
            JsonArray elements = new JsonArray();
            this.elements.stream().map(ElementBuilder::build).forEach(part -> {
                JsonObject partObj = new JsonObject();
                partObj.add("from", serializeVector3f(part.from()));
                partObj.add("to", serializeVector3f(part.to()));

                if (part.rotation() != null) {
                    JsonObject rotation = new JsonObject();
                    rotation.add("origin", serializeVector3f(part.rotation().origin()));
                    rotation.addProperty("axis", part.rotation().axis().getSerializedName());
                    rotation.addProperty("angle", part.rotation().angle());
                    if (part.rotation().rescale()) {
                        rotation.addProperty("rescale", true);
                    }
                    partObj.add("rotation", rotation);
                }

                if (!part.shade()) {
                    partObj.addProperty("shade", false);
                }

                if (part.lightEmission() != 0) {
                    partObj.addProperty("light_emission", part.lightEmission());
                }

                if (!part.faceData().equals(ExtraFaceData.DEFAULT)) {
                    partObj.add("neoforge_data", ExtraFaceData.CODEC.encodeStart(JsonOps.INSTANCE, part.faceData()).result().get());
                }

                JsonObject faces = new JsonObject();
                for (Direction dir : Direction.values()) {
                    BlockElementFace face = part.faces().get(dir);
                    if (face == null) continue;

                    JsonObject faceObj = new JsonObject();
                    faceObj.addProperty("texture", serializeLocOrKey(face.texture()));
                    BlockElementFace.UVs uvs = face.uvs();
                    if (uvs != null && !uvs.equals(FaceBakery.defaultFaceUV(part.from(), part.to(), dir))) {
                        JsonArray uvArr = new JsonArray();
                        uvArr.add(uvs.minU());
                        uvArr.add(uvs.minV());
                        uvArr.add(uvs.maxU());
                        uvArr.add(uvs.maxV());
                        faceObj.add("uv", uvArr);
                    }
                    if (face.cullForDirection() != null) {
                        faceObj.addProperty("cullface", face.cullForDirection().getSerializedName());
                    }
                    if (face.rotation() != Quadrant.R0) {
                        faceObj.addProperty("rotation", face.rotation().shift * 90);
                    }
                    if (face.tintIndex() != -1) {
                        faceObj.addProperty("tintindex", face.tintIndex());
                    }
                    if (!face.faceData().equals(ExtraFaceData.DEFAULT)) {
                        faceObj.add("neoforge_data", ExtraFaceData.CODEC.encodeStart(JsonOps.INSTANCE, face.faceData()).result().orElseThrow());
                    }
                    faces.add(dir.getSerializedName(), faceObj);
                }
                if (!part.faces().isEmpty()) {
                    partObj.add("faces", faces);
                }
                elements.add(partObj);
            });
            root.add("elements", elements);
        }

        // If there were any transform properties set, add them to the output.
        JsonObject transform = rootTransforms.toJson();
        if (!transform.isEmpty()) {
            root.add("transform", transform);
        }

        if (customLoader != null)
            return customLoader.toJson(root);

        return root;
    }

    private static String serializeLocOrKey(String tex) {
        if (tex.charAt(0) == '#') {
            return tex;
        }
        return ResourceLocation.parse(tex).toString();
    }

    private static JsonArray serializeVector3f(Vector3fc vec) {
        JsonArray ret = new JsonArray();
        ret.add(serializeFloat(vec.x()));
        ret.add(serializeFloat(vec.y()));
        ret.add(serializeFloat(vec.z()));
        return ret;
    }

    private static Number serializeFloat(float f) {
        if ((int) f == f) {
            return (int) f;
        }
        return f;
    }
}
