/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.common.util.TransformationHelper;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ExtendedModelTemplate extends ModelTemplate {
    private final TransformsBuilder transforms;
    private final List<ElementBuilder> elements;
    @Nullable
    private final CustomLoaderBuilder customLoader;
    private final RootTransformsBuilder rootTransforms;
    private final List<OverrideBuilder> overrides;
    @Nullable
    private final ResourceLocation renderType;
    @Nullable
    private final Boolean ambientOcclusion;
    @Nullable
    private final BlockModel.GuiLight guiLight;

    private ExtendedModelTemplate(Builder builder) {
        super(builder.parent, builder.suffix, builder.requiredSlots.toArray(TextureSlot[]::new));
        this.transforms = builder.transforms;
        this.elements = List.copyOf(builder.elements);
        this.customLoader = builder.customLoader;
        this.rootTransforms = builder.rootTransforms;
        this.overrides = List.copyOf(builder.overrides);
        this.renderType = builder.renderType;
        this.ambientOcclusion = builder.ambientOcclusion;
        this.guiLight = builder.guiLight;
    }

    public static ExtendedModelTemplate.Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static final class Builder {
        private Optional<ResourceLocation> parent = Optional.empty();
        private Optional<String> suffix = Optional.empty();
        private final Set<TextureSlot> requiredSlots = new HashSet<>();
        private final TransformsBuilder transforms = new TransformsBuilder(this);
        private final List<ElementBuilder> elements = new ArrayList<>();
        @Nullable
        private CustomLoaderBuilder customLoader = null;
        private final RootTransformsBuilder rootTransforms = new RootTransformsBuilder(this);
        private final List<OverrideBuilder> overrides = new ArrayList<>();
        @Nullable
        private ResourceLocation renderType = null;
        @Nullable
        private Boolean ambientOcclusion = null; // BlockModel.DEFAULT_AMBIENT_OCCLUSION
        @Nullable
        private BlockModel.GuiLight guiLight = null;

        public static Builder of(ModelTemplate template) {
            Builder builder = new Builder();
            builder.parent = template.model;
            builder.suffix = template.suffix;
            builder.requiredSlots.addAll(template.requiredSlots);
            if (template instanceof ExtendedModelTemplate ext) {
                builder.transforms.copyFrom(ext.transforms);
                ext.elements.forEach(elem -> builder.elements.add(elem.copy(builder)));
                builder.customLoader = ext.customLoader != null ? ext.customLoader.copy(builder) : null;
                builder.rootTransforms.copyFrom(ext.rootTransforms);
                ext.overrides.forEach(override -> builder.overrides.add(override.copy(builder)));
                builder.renderType = ext.renderType;
                builder.ambientOcclusion = ext.ambientOcclusion;
                builder.guiLight = ext.guiLight;
            }
            return builder;
        }

        public Builder parent(ResourceLocation parent) {
            this.parent = Optional.of(parent);
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = Optional.of(suffix);
            return this;
        }

        public Builder requiredTextureSlot(TextureSlot slot) {
            this.requiredSlots.add(slot);
            return this;
        }

        /**
         * Set the render type for this model.
         *
         * @param renderType the render type. Must be registered via
         *                   {@link RegisterNamedRenderTypesEvent}
         * @return this builder
         * @throws NullPointerException if {@code renderType} is {@code null}
         */
        public Builder renderType(String renderType) {
            Preconditions.checkNotNull(renderType, "Render type must not be null");
            return renderType(ResourceLocation.parse(renderType));
        }

        /**
         * Set the render type for this model.
         *
         * @param renderType the render type. Must be registered via
         *                   {@link RegisterNamedRenderTypesEvent}
         * @return this builder
         * @throws NullPointerException if {@code renderType} is {@code null}
         */
        public Builder renderType(ResourceLocation renderType) {
            Preconditions.checkNotNull(renderType, "Render type must not be null");
            this.renderType = renderType;
            return this;
        }

        public TransformsBuilder transforms() {
            return transforms;
        }

        public Builder ao(boolean ao) {
            this.ambientOcclusion = ao;
            return this;
        }

        public Builder guiLight(BlockModel.GuiLight light) {
            this.guiLight = light;
            return this;
        }

        public ElementBuilder element() {
            Preconditions.checkState(
                    customLoader == null || customLoader.allowInlineElements,
                    "Custom model loader %s does not support inline elements",
                    customLoader != null ? customLoader.loaderId : null);
            ElementBuilder ret = new ElementBuilder(this);
            elements.add(ret);
            return ret;
        }

        /**
         * Get an existing element builder
         *
         * @param index the index of the existing element builder
         * @return the element builder
         * @throws IndexOutOfBoundsException if {@code} index is out of bounds
         */
        public ElementBuilder element(int index) {
            Preconditions.checkState(
                    customLoader == null || customLoader.allowInlineElements,
                    "Custom model loader %s does not support inline elements",
                    customLoader != null ? customLoader.loaderId : null);
            Preconditions.checkElementIndex(index, elements.size(), "Element index");
            return elements.get(index);
        }

        /**
         * {@return the number of elements in this model builder}
         */
        public int getElementCount() {
            return elements.size();
        }

        /**
         * Use a custom loader instead of the vanilla elements.
         *
         * @param customLoaderFactory function that returns the custom loader to set, given this
         * @return the custom loader builder
         */
        public <L extends CustomLoaderBuilder> L customLoader(Function<Builder, L> customLoaderFactory) {
            Preconditions.checkNotNull(customLoaderFactory, "customLoaderFactory must not be null");
            L customLoader = customLoaderFactory.apply(this);
            Preconditions.checkState(
                    customLoader.allowInlineElements || elements.isEmpty(),
                    "Custom model loader %s does not support inline elements",
                    customLoader.loaderId);
            this.customLoader = customLoader;
            return customLoader;
        }

        public RootTransformsBuilder rootTransforms() {
            return rootTransforms;
        }

        public OverrideBuilder override() {
            OverrideBuilder ret = new OverrideBuilder(this);
            overrides.add(ret);
            return ret;
        }

        /**
         * Get an existing override builder
         *
         * @param index the index of the existing override builder
         * @return the override builder
         * @throws IndexOutOfBoundsException if {@code} index is out of bounds
         */
        public OverrideBuilder override(int index) {
            Preconditions.checkElementIndex(index, overrides.size(), "override");
            return overrides.get(index);
        }

        public ExtendedModelTemplate build() {
            return new ExtendedModelTemplate(this);
        }
    }

    public static final class ElementBuilder {
        private final Builder owner;
        private Vector3f from = new Vector3f();
        private Vector3f to = new Vector3f(16, 16, 16);
        private final Map<Direction, ElementBuilder.FaceBuilder> faces = new LinkedHashMap<>();
        @Nullable
        private ElementBuilder.RotationBuilder rotation;
        private boolean shade = true;
        private int lightEmission = 0;
        private int color = 0xFFFFFFFF;
        private int blockLight = 0;
        private int skyLight = 0;
        private boolean hasAmbientOcclusion = true;

        private ElementBuilder(Builder owner) {
            this.owner = owner;
        }

        private static void validateCoordinate(float coord, char name) {
            Preconditions.checkArgument(!(coord < -16.0F) && !(coord > 32.0F), "Position " + name + " out of range, must be within [-16, 32]. Found: %d", coord);
        }

        private static void validatePosition(Vector3f pos) {
            validateCoordinate(pos.x(), 'x');
            validateCoordinate(pos.y(), 'y');
            validateCoordinate(pos.z(), 'z');
        }

        /**
         * Set the "from" position for this element.
         *
         * @param x x-position for this vector
         * @param y y-position for this vector
         * @param z z-position for this vector
         * @return this builder
         * @throws IllegalArgumentException if the vector is out of bounds (any
         *                                  coordinate not between -16 and 32,
         *                                  inclusive)
         */
        public ElementBuilder from(float x, float y, float z) {
            this.from = new Vector3f(x, y, z);
            validatePosition(this.from);
            return this;
        }

        /**
         * Set the "to" position for this element.
         *
         * @param x x-position for this vector
         * @param y y-position for this vector
         * @param z z-position for this vector
         * @return this builder
         * @throws IllegalArgumentException if the vector is out of bounds (any
         *                                  coordinate not between -16 and 32,
         *                                  inclusive)
         */
        public ElementBuilder to(float x, float y, float z) {
            this.to = new Vector3f(x, y, z);
            validatePosition(this.to);
            return this;
        }

        /**
         * Return or create the face builder for the given direction.
         *
         * @param dir the direction
         * @return the face builder for the given direction
         * @throws NullPointerException if {@code dir} is {@code null}
         */
        public ElementBuilder.FaceBuilder face(Direction dir) {
            Preconditions.checkNotNull(dir, "Direction must not be null");
            return faces.computeIfAbsent(dir, $ -> new FaceBuilder(this));
        }

        public ElementBuilder.RotationBuilder rotation() {
            if (this.rotation == null) {
                this.rotation = new ElementBuilder.RotationBuilder(this);
            }
            return this.rotation;
        }

        public ElementBuilder shade(boolean shade) {
            this.shade = shade;
            return this;
        }

        /**
         * Modify all <em>possible</em> faces dynamically using a function, creating new
         * faces as necessary.
         *
         * @param action the function to apply to each direction
         * @return this builder
         * @throws NullPointerException if {@code action} is {@code null}
         */
        public ElementBuilder allFaces(BiConsumer<Direction, ElementBuilder.FaceBuilder> action) {
            Arrays.stream(Direction.values())
                    .forEach(d -> action.accept(d, face(d)));
            return this;
        }

        /**
         * Creates <em>possible</em> faces for the model as needed, excluding those
         * specified in the second argument, and then applies a function to modify added faces.
         *
         * @param action the function to apply to each direction
         * @param exc    directions which will be excluded from adding to model file
         * @return this builder
         * @throws NullPointerException if {@code action} is {@code null}
         */
        public ElementBuilder allFacesExcept(BiConsumer<Direction, ElementBuilder.FaceBuilder> action, Set<Direction> exc) {
            Arrays.stream(Direction.values()).filter(d -> !exc.contains(d)).forEach(d -> action.accept(d, face(d)));
            return this;
        }

        /**
         * Modify all <em>existing</em> faces dynamically using a function.
         *
         * @param action the function to apply to each direction
         * @return this builder
         * @throws NullPointerException if {@code action} is {@code null}
         */
        public ElementBuilder faces(BiConsumer<Direction, ElementBuilder.FaceBuilder> action) {
            faces.forEach(action);
            return this;
        }

        /**
         * Texture all <em>possible</em> faces in the current element with the given
         * texture, creating new faces where necessary.
         *
         * @param texture the texture
         * @return this builder
         * @throws NullPointerException if {@code texture} is {@code null}
         */
        public ElementBuilder textureAll(TextureSlot texture) {
            return allFaces(addTexture(texture));
        }

        /**
         * Texture all <em>existing</em> faces in the current element with the given
         * texture.
         *
         * @param texture the texture
         * @return this builder
         * @throws NullPointerException if {@code texture} is {@code null}
         */
        public ElementBuilder texture(TextureSlot texture) {
            return faces(addTexture(texture));
        }

        /**
         * Create a typical cube element, creating new faces as needed, applying the
         * given texture, and setting the cullface.
         *
         * @param texture the texture
         * @return this builder
         * @throws NullPointerException if {@code texture} is {@code null}
         */
        public ElementBuilder cube(TextureSlot texture) {
            return allFaces(addTexture(texture).andThen((dir, f) -> f.cullface(dir)));
        }

        /**
         * Set the block and sky light of the element (0-15).
         * Traditional "emissivity" values were set both of these to the same value.
         *
         * @param blockLight the block light
         * @param skyLight   the sky light
         * @return this builder
         */
        public ElementBuilder emissivity(int blockLight, int skyLight) {
            this.blockLight = blockLight;
            this.skyLight = skyLight;
            return this;
        }

        /**
         * Set the light emission of the element (0-15)
         * <p>
         * If block and sky light values should be different, use {@link #emissivity(int, int)} instead
         *
         * @param lightEmission the light value
         * @return this builder
         */
        public ElementBuilder lightEmission(int lightEmission) {
            this.lightEmission = lightEmission;
            return this;
        }

        /**
         * Sets the color of the element.
         *
         * @param color the color in ARGB format.
         * @return this builder
         */
        public ElementBuilder color(int color) {
            this.color = color;
            return this;
        }

        /**
         * Set the ambient occlusion of the element.
         *
         * @param ao the ambient occlusion
         * @return this builder
         */
        public ElementBuilder ao(boolean ao) {
            this.hasAmbientOcclusion = ao;
            return this;
        }

        private static BiConsumer<Direction, ElementBuilder.FaceBuilder> addTexture(TextureSlot texture) {
            return ($, f) -> f.texture(texture);
        }

        BlockElement build() {
            Map<Direction, BlockElementFace> faces = this.faces.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().build(), (k1, k2) -> {
                        throw new IllegalArgumentException();
                    }, LinkedHashMap::new));
            return new BlockElement(from, to, faces, rotation == null ? null : rotation.build(), shade, lightEmission, new ExtraFaceData(this.color, this.blockLight, this.skyLight, this.hasAmbientOcclusion));
        }

        public Builder end() {
            return owner;
        }

        ElementBuilder copy(Builder owner) {
            ElementBuilder builder = new ElementBuilder(owner);
            builder.from.set(this.from);
            builder.to.set(this.to);
            this.faces.forEach((side, faceBuilder) -> builder.faces.put(side, faceBuilder.copy(builder)));
            builder.rotation = this.rotation != null ? this.rotation.copy(builder) : null;
            builder.shade = this.shade;
            builder.lightEmission = this.lightEmission;
            builder.color = this.color;
            builder.blockLight = this.blockLight;
            builder.skyLight = this.skyLight;
            builder.hasAmbientOcclusion = this.hasAmbientOcclusion;
            return builder;
        }

        public static final class FaceBuilder {
            private final ElementBuilder owner;
            @Nullable
            private Direction cullface;
            private int tintindex = -1;
            @Nullable
            private TextureSlot texture = null;
            private float @Nullable [] uvs;
            private FaceRotation rotation = FaceRotation.ZERO;
            private int color = 0xFFFFFFFF;
            private int blockLight = 0;
            private int skyLight = 0;
            private boolean hasAmbientOcclusion = true;

            FaceBuilder(ElementBuilder owner) {
                this.owner = owner;
            }

            public ElementBuilder.FaceBuilder cullface(@Nullable Direction dir) {
                this.cullface = dir;
                return this;
            }

            public ElementBuilder.FaceBuilder tintindex(int index) {
                this.tintindex = index;
                return this;
            }

            /**
             * Set the texture for the current face.
             *
             * @param texture the texture
             * @return this builder
             * @throws NullPointerException if {@code texture} is {@code null}
             */
            public ElementBuilder.FaceBuilder texture(TextureSlot texture) {
                Preconditions.checkNotNull(texture, "Texture must not be null");
                this.texture = texture;
                return this;
            }

            public ElementBuilder.FaceBuilder uvs(float u1, float v1, float u2, float v2) {
                this.uvs = new float[] { u1, v1, u2, v2 };
                return this;
            }

            /**
             * Set the texture rotation for the current face.
             *
             * @param rot the rotation
             * @return this builder
             * @throws NullPointerException if {@code rot} is {@code null}
             */
            public ElementBuilder.FaceBuilder rotation(FaceRotation rot) {
                Preconditions.checkNotNull(rot, "Rotation must not be null");
                this.rotation = rot;
                return this;
            }

            /**
             * Set the block and sky light of the face (0-15).
             * Traditional "emissivity" values set both of these to the same value.
             *
             * @param blockLight the block light
             * @param skyLight   the sky light
             * @return this builder
             */
            public ElementBuilder.FaceBuilder emissivity(int blockLight, int skyLight) {
                this.blockLight = blockLight;
                this.skyLight = skyLight;
                return this;
            }

            /**
             * Sets the color of the face.
             *
             * @param color the color in ARGB format.
             * @return this builder
             */
            public ElementBuilder.FaceBuilder color(int color) {
                this.color = color;
                return this;
            }

            /**
             * Set the ambient occlusion of the face.
             *
             * @param ao the ambient occlusion
             * @return this builder
             */
            public ElementBuilder.FaceBuilder ao(boolean ao) {
                this.hasAmbientOcclusion = ao;
                return this;
            }

            BlockElementFace build() {
                if (this.texture == null) {
                    throw new IllegalStateException("A model face must have a texture");
                }
                return new BlockElementFace(cullface, tintindex, texture.toString(), new BlockFaceUV(uvs, rotation.rotation), new ExtraFaceData(this.color, this.blockLight, this.skyLight, this.hasAmbientOcclusion), new MutableObject<>());
            }

            public ElementBuilder end() {
                return owner;
            }

            FaceBuilder copy(ElementBuilder owner) {
                FaceBuilder builder = new FaceBuilder(owner);
                builder.cullface = this.cullface;
                builder.tintindex = this.tintindex;
                builder.uvs = this.uvs != null ? Arrays.copyOf(this.uvs, this.uvs.length) : null;
                builder.rotation = this.rotation;
                builder.blockLight = this.blockLight;
                builder.skyLight = this.skyLight;
                builder.hasAmbientOcclusion = this.hasAmbientOcclusion;
                return builder;
            }
        }

        public static final class RotationBuilder {
            private final ElementBuilder owner;
            @Nullable
            private Vector3f origin;
            @Nullable
            private Direction.Axis axis;
            private float angle;
            private boolean rescale;

            RotationBuilder(ElementBuilder owner) {
                this.owner = owner;
            }

            public ElementBuilder.RotationBuilder origin(float x, float y, float z) {
                this.origin = new Vector3f(x, y, z);
                return this;
            }

            /**
             * @param axis the axis of rotation
             * @return this builder
             * @throws NullPointerException if {@code axis} is {@code null}
             */
            public ElementBuilder.RotationBuilder axis(Direction.Axis axis) {
                Preconditions.checkNotNull(axis, "Axis must not be null");
                this.axis = axis;
                return this;
            }

            /**
             * @param angle the rotation angle
             * @return this builder
             * @throws IllegalArgumentException if {@code angle} is invalid (not one of 0, +/-22.5, +/-45)
             */
            public ElementBuilder.RotationBuilder angle(float angle) {
                // Same logic from BlockPart.Deserializer#parseAngle
                Preconditions.checkArgument(angle == 0.0F || Mth.abs(angle) == 22.5F || Mth.abs(angle) == 45.0F, "Invalid rotation %f found, only -45/-22.5/0/22.5/45 allowed", angle);
                this.angle = angle;
                return this;
            }

            public ElementBuilder.RotationBuilder rescale(boolean rescale) {
                this.rescale = rescale;
                return this;
            }

            BlockElementRotation build() {
                Preconditions.checkNotNull(origin, "No origin specified");
                Preconditions.checkNotNull(axis, "No axis specified");
                return new BlockElementRotation(origin, axis, angle, rescale);
            }

            public ElementBuilder end() {
                return this.owner;
            }

            public RotationBuilder copy(ElementBuilder owner) {
                RotationBuilder builder = new RotationBuilder(owner);
                builder.origin = this.origin != null ? new Vector3f(this.origin) : null;
                builder.axis = this.axis;
                builder.angle = this.angle;
                builder.rescale = this.rescale;
                return builder;
            }
        }
    }

    public enum FaceRotation {
        ZERO(0),
        CLOCKWISE_90(90),
        UPSIDE_DOWN(180),
        COUNTERCLOCKWISE_90(270),
        ;

        final int rotation;

        private FaceRotation(int rotation) {
            this.rotation = rotation;
        }
    }

    public static class TransformsBuilder {
        private final Map<ItemDisplayContext, TransformsBuilder.TransformVecBuilder> transforms = new LinkedHashMap<>();
        private final Builder owner;

        private TransformsBuilder(Builder owner) {
            this.owner = owner;
        }

        /**
         * Begin building a new transform for the given perspective.
         *
         * @param type the perspective to create or return the builder for
         * @return the builder for the given perspective
         * @throws NullPointerException if {@code type} is {@code null}
         */
        public TransformsBuilder.TransformVecBuilder transform(ItemDisplayContext type) {
            Preconditions.checkNotNull(type, "Perspective cannot be null");
            return transforms.computeIfAbsent(type, TransformsBuilder.TransformVecBuilder::new);
        }

        Map<ItemDisplayContext, ItemTransform> build() {
            return this.transforms.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().build(), (k1, k2) -> {
                        throw new IllegalArgumentException();
                    }, LinkedHashMap::new));
        }

        public Builder end() {
            return this.owner;
        }

        private void copyFrom(TransformsBuilder builder) {
            builder.transforms.forEach((ctx, vecBuilder) -> this.transforms.put(ctx, vecBuilder.copy()));
        }

        public class TransformVecBuilder {
            private Vector3f rotation = new Vector3f(ItemTransform.Deserializer.DEFAULT_ROTATION);
            private Vector3f translation = new Vector3f(ItemTransform.Deserializer.DEFAULT_TRANSLATION);
            private Vector3f scale = new Vector3f(ItemTransform.Deserializer.DEFAULT_SCALE);
            private Vector3f rightRotation = new Vector3f(ItemTransform.Deserializer.DEFAULT_ROTATION);

            TransformVecBuilder(ItemDisplayContext type) {
                // param unused for functional match
            }

            public TransformsBuilder.TransformVecBuilder rotation(float x, float y, float z) {
                this.rotation = new Vector3f(x, y, z);
                return this;
            }

            public TransformsBuilder.TransformVecBuilder leftRotation(float x, float y, float z) {
                return rotation(x, y, z);
            }

            public TransformsBuilder.TransformVecBuilder translation(float x, float y, float z) {
                this.translation = new Vector3f(x, y, z);
                return this;
            }

            public TransformsBuilder.TransformVecBuilder scale(float sc) {
                return scale(sc, sc, sc);
            }

            public TransformsBuilder.TransformVecBuilder scale(float x, float y, float z) {
                this.scale = new Vector3f(x, y, z);
                return this;
            }

            public TransformsBuilder.TransformVecBuilder rightRotation(float x, float y, float z) {
                this.rightRotation = new Vector3f(x, y, z);
                return this;
            }

            ItemTransform build() {
                return new ItemTransform(rotation, translation, scale, rightRotation);
            }

            public TransformsBuilder end() {
                return TransformsBuilder.this;
            }

            TransformVecBuilder copy() {
                TransformVecBuilder builder = new TransformVecBuilder(ItemDisplayContext.NONE);
                builder.rotation.set(this.rotation);
                builder.translation.set(this.translation);
                builder.scale.set(this.scale);
                builder.rightRotation.set(this.rightRotation);
                return builder;
            }
        }
    }

    public static class RootTransformsBuilder {
        private static final Vector3f ONE = new Vector3f(1, 1, 1);

        private final Builder owner;
        private Vector3f translation = new Vector3f();
        private Quaternionf leftRotation = new Quaternionf();
        private Quaternionf rightRotation = new Quaternionf();
        private Vector3f scale = ONE;
        @Nullable
        private TransformationHelper.TransformOrigin origin;
        @Nullable
        private Vector3f originVec;

        private RootTransformsBuilder(Builder owner) {
            this.owner = owner;
        }

        /**
         * Sets the translation of the root transform.
         *
         * @param translation the translation
         * @return this builder
         * @throws NullPointerException if {@code translation} is {@code null}
         */
        public RootTransformsBuilder translation(Vector3f translation) {
            this.translation = Preconditions.checkNotNull(translation, "Translation must not be null");
            return this;
        }

        /**
         * Sets the translation of the root transform.
         *
         * @param x x translation
         * @param y y translation
         * @param z z translation
         * @return this builder
         */
        public RootTransformsBuilder translation(float x, float y, float z) {
            return translation(new Vector3f(x, y, z));
        }

        /**
         * Sets the left rotation of the root transform.
         *
         * @param rotation the left rotation
         * @return this builder
         * @throws NullPointerException if {@code rotation} is {@code null}
         */
        public RootTransformsBuilder rotation(Quaternionf rotation) {
            this.leftRotation = Preconditions.checkNotNull(rotation, "Rotation must not be null");
            return this;
        }

        /**
         * Sets the left rotation of the root transform.
         *
         * @param x         x rotation
         * @param y         y rotation
         * @param z         z rotation
         * @param isDegrees whether the rotation is in degrees or radians
         * @return this builder
         */
        public RootTransformsBuilder rotation(float x, float y, float z, boolean isDegrees) {
            return rotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        /**
         * Sets the left rotation of the root transform.
         *
         * @param leftRotation the left rotation
         * @return this builder
         * @throws NullPointerException if {@code leftRotation} is {@code null}
         */
        public RootTransformsBuilder leftRotation(Quaternionf leftRotation) {
            return rotation(leftRotation);
        }

        /**
         * Sets the left rotation of the root transform.
         *
         * @param x         x rotation
         * @param y         y rotation
         * @param z         z rotation
         * @param isDegrees whether the rotation is in degrees or radians
         * @return this builder
         */
        public RootTransformsBuilder leftRotation(float x, float y, float z, boolean isDegrees) {
            return leftRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        /**
         * Sets the right rotation of the root transform.
         *
         * @param rightRotation the right rotation
         * @return this builder
         * @throws NullPointerException if {@code rightRotation} is {@code null}
         */
        public RootTransformsBuilder rightRotation(Quaternionf rightRotation) {
            this.rightRotation = Preconditions.checkNotNull(rightRotation, "Rotation must not be null");
            return this;
        }

        /**
         * Sets the right rotation of the root transform.
         *
         * @param x         x rotation
         * @param y         y rotation
         * @param z         z rotation
         * @param isDegrees whether the rotation is in degrees or radians
         * @return this builder
         */
        public RootTransformsBuilder rightRotation(float x, float y, float z, boolean isDegrees) {
            return rightRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        /**
         * Sets the right rotation of the root transform.
         *
         * @param postRotation the right rotation
         * @return this builder
         * @throws NullPointerException if {@code rightRotation} is {@code null}
         */
        public RootTransformsBuilder postRotation(Quaternionf postRotation) {
            return rightRotation(postRotation);
        }

        /**
         * Sets the right rotation of the root transform.
         *
         * @param x         x rotation
         * @param y         y rotation
         * @param z         z rotation
         * @param isDegrees whether the rotation is in degrees or radians
         * @return this builder
         */
        public RootTransformsBuilder postRotation(float x, float y, float z, boolean isDegrees) {
            return postRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        /**
         * Sets the scale of the root transform.
         *
         * @param scale the scale
         * @return this builder
         */
        public RootTransformsBuilder scale(float scale) {
            return scale(new Vector3f(scale, scale, scale));
        }

        /**
         * Sets the scale of the root transform.
         *
         * @param xScale x scale
         * @param yScale y scale
         * @param zScale z scale
         * @return this builder
         */
        public RootTransformsBuilder scale(float xScale, float yScale, float zScale) {
            return scale(new Vector3f(xScale, yScale, zScale));
        }

        /**
         * Sets the scale of the root transform.
         *
         * @param scale the scale vector
         * @return this builder
         * @throws NullPointerException if {@code scale} is {@code null}
         */
        public RootTransformsBuilder scale(Vector3f scale) {
            this.scale = Preconditions.checkNotNull(scale, "Scale must not be null");
            return this;
        }

        /**
         * Sets the root transform.
         *
         * @param transformation the transformation to use
         * @return this builder
         * @throws NullPointerException if {@code transformation} is {@code null}
         */
        public RootTransformsBuilder transform(Transformation transformation) {
            Preconditions.checkNotNull(transformation, "Transformation must not be null");
            this.translation = transformation.getTranslation();
            this.leftRotation = transformation.getLeftRotation();
            this.rightRotation = transformation.getRightRotation();
            this.scale = transformation.getScale();
            return this;
        }

        /**
         * Sets the origin of the root transform.
         *
         * @param origin the origin vector
         * @return this builder
         * @throws NullPointerException if {@code origin} is {@code null}
         */
        public RootTransformsBuilder origin(Vector3f origin) {
            this.originVec = Preconditions.checkNotNull(origin, "Origin must not be null");
            this.origin = null;
            return this;
        }

        /**
         * Sets the origin of the root transform.
         *
         * @param origin the origin name
         * @return this builder
         * @throws NullPointerException     if {@code origin} is {@code null}
         * @throws IllegalArgumentException if {@code origin} is not {@code center}, {@code corner} or {@code opposing-corner}
         */
        public RootTransformsBuilder origin(TransformationHelper.TransformOrigin origin) {
            this.origin = Preconditions.checkNotNull(origin, "Origin must not be null");
            this.originVec = null;
            return this;
        }

        /**
         * Finish configuring the parent builder
         *
         * @return the parent block model builder
         */
        public Builder end() {
            return this.owner;
        }

        public JsonObject toJson() {
            // Write the transform to an object
            JsonObject transform = new JsonObject();

            if (!translation.equals(0, 0, 0)) {
                transform.add("translation", writeVec3(translation));
            }

            if (!scale.equals(ONE)) {
                transform.add("scale", writeVec3(scale));
            }

            if (!leftRotation.equals(0, 0, 0, 1)) {
                transform.add("rotation", writeQuaternion(leftRotation));
            }

            if (!rightRotation.equals(0, 0, 0, 1)) {
                transform.add("post_rotation", writeQuaternion(rightRotation));
            }

            if (origin != null) {
                transform.addProperty("origin", origin.getSerializedName());
            } else if (originVec != null && !originVec.equals(0, 0, 0)) {
                transform.add("origin", writeVec3(originVec));
            }

            return transform;
        }

        private static JsonArray writeVec3(Vector3f vector) {
            JsonArray array = new JsonArray();
            array.add(vector.x());
            array.add(vector.y());
            array.add(vector.z());
            return array;
        }

        private static JsonArray writeQuaternion(Quaternionf quaternion) {
            JsonArray array = new JsonArray();
            array.add(quaternion.x());
            array.add(quaternion.y());
            array.add(quaternion.z());
            array.add(quaternion.w());
            return array;
        }

        public void copyFrom(RootTransformsBuilder other) {
            this.translation.set(other.translation);
            this.leftRotation.set(other.leftRotation);
            this.rightRotation.set(other.rightRotation);
            this.scale.set(other.scale);
            this.origin = other.origin;
            this.originVec = other.originVec != null ? new Vector3f(other.originVec) : null;
        }
    }

    public static final class OverrideBuilder {
        private final Builder owner;
        @Nullable
        private ResourceLocation model;
        private final Map<ResourceLocation, Float> predicates = new LinkedHashMap<>();

        OverrideBuilder(ExtendedModelTemplate.Builder owner) {
            this.owner = owner;
        }

        public OverrideBuilder model(ResourceLocation model) {
            this.model = model;
            return this;
        }

        public OverrideBuilder predicate(ResourceLocation key, float value) {
            this.predicates.put(key, value);
            return this;
        }

        public ExtendedModelTemplate.Builder end() {
            return this.owner;
        }

        OverrideBuilder copy(Builder owner) {
            OverrideBuilder builder = new OverrideBuilder(owner);
            builder.model = model;
            builder.predicates.putAll(this.predicates);
            return builder;
        }

        JsonObject toJson() {
            Preconditions.checkNotNull(model, "No model specified");

            JsonObject ret = new JsonObject();
            JsonObject predicatesJson = new JsonObject();
            predicates.forEach((key, val) -> predicatesJson.addProperty(key.toString(), val));
            ret.add("predicate", predicatesJson);
            ret.addProperty("model", model.toString());
            return ret;
        }
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

        Map<ItemDisplayContext, ItemTransform> transforms = this.transforms.build();
        if (!transforms.isEmpty()) {
            JsonObject display = new JsonObject();
            for (Map.Entry<ItemDisplayContext, ItemTransform> e : transforms.entrySet()) {
                JsonObject transform = new JsonObject();
                ItemTransform vec = e.getValue();
                if (vec.equals(ItemTransform.NO_TRANSFORM)) continue;
                var hasRightRotation = !vec.rightRotation.equals(ItemTransform.Deserializer.DEFAULT_ROTATION);
                if (!vec.translation.equals(ItemTransform.Deserializer.DEFAULT_TRANSLATION)) {
                    transform.add("translation", serializeVector3f(e.getValue().translation));
                }
                if (!vec.rotation.equals(ItemTransform.Deserializer.DEFAULT_ROTATION)) {
                    transform.add(hasRightRotation ? "left_rotation" : "rotation", serializeVector3f(vec.rotation));
                }
                if (!vec.scale.equals(ItemTransform.Deserializer.DEFAULT_SCALE)) {
                    transform.add("scale", serializeVector3f(e.getValue().scale));
                }
                if (hasRightRotation) {
                    transform.add("right_rotation", serializeVector3f(vec.rightRotation));
                }
                display.add(e.getKey().getSerializedName(), transform);
            }
            root.add("display", display);
        }

        if (!this.elements.isEmpty()) {
            JsonArray elements = new JsonArray();
            this.elements.stream().map(ElementBuilder::build).forEach(part -> {
                JsonObject partObj = new JsonObject();
                partObj.add("from", serializeVector3f(part.from));
                partObj.add("to", serializeVector3f(part.to));

                if (part.rotation != null) {
                    JsonObject rotation = new JsonObject();
                    rotation.add("origin", serializeVector3f(part.rotation.origin()));
                    rotation.addProperty("axis", part.rotation.axis().getSerializedName());
                    rotation.addProperty("angle", part.rotation.angle());
                    if (part.rotation.rescale()) {
                        rotation.addProperty("rescale", true);
                    }
                    partObj.add("rotation", rotation);
                }

                if (!part.shade) {
                    partObj.addProperty("shade", false);
                }

                if (!part.getFaceData().equals(ExtraFaceData.DEFAULT)) {
                    partObj.add("neoforge_data", ExtraFaceData.CODEC.encodeStart(JsonOps.INSTANCE, part.getFaceData()).result().get());
                }

                JsonObject faces = new JsonObject();
                for (Direction dir : Direction.values()) {
                    BlockElementFace face = part.faces.get(dir);
                    if (face == null) continue;

                    JsonObject faceObj = new JsonObject();
                    faceObj.addProperty("texture", serializeLocOrKey(face.texture()));
                    if (!Arrays.equals(face.uv().uvs, part.uvsByFace(dir))) {
                        faceObj.add("uv", new Gson().toJsonTree(face.uv().uvs));
                    }
                    if (face.cullForDirection() != null) {
                        faceObj.addProperty("cullface", face.cullForDirection().getSerializedName());
                    }
                    if (face.uv().rotation != 0) {
                        faceObj.addProperty("rotation", face.uv().rotation);
                    }
                    if (face.tintIndex() != -1) {
                        faceObj.addProperty("tintindex", face.tintIndex());
                    }
                    if (!face.faceData().equals(ExtraFaceData.DEFAULT)) {
                        faceObj.add("neoforge_data", ExtraFaceData.CODEC.encodeStart(JsonOps.INSTANCE, face.faceData()).result().orElseThrow());
                    }
                    faces.add(dir.getSerializedName(), faceObj);
                }
                if (!part.faces.isEmpty()) {
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

        if (!overrides.isEmpty()) {
            JsonArray overridesJson = new JsonArray();
            overrides.stream().map(OverrideBuilder::toJson).forEach(overridesJson::add);
            root.add("overrides", overridesJson);
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

    private static JsonArray serializeVector3f(Vector3f vec) {
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
