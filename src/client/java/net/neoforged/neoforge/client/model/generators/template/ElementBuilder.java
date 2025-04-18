/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

import com.google.common.base.Preconditions;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class ElementBuilder {
    private Vector3f from = new Vector3f();
    private Vector3f to = new Vector3f(16, 16, 16);
    private final Map<Direction, FaceBuilder> faces = new LinkedHashMap<>();
    @Nullable
    private RotationBuilder rotation;
    private boolean shade = true;
    private int lightEmission = 0;
    private int color = 0xFFFFFFFF;
    private int blockLight = 0;
    private int skyLight = 0;
    private boolean hasAmbientOcclusion = true;

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
    public ElementBuilder face(Direction dir, Consumer<FaceBuilder> action) {
        Preconditions.checkNotNull(dir, "Direction must not be null");
        var builder = faces.computeIfAbsent(dir, $ -> new FaceBuilder());
        action.accept(builder);
        return this;
    }

    /**
     * Allows modifying the rotation for this element.
     */
    public ElementBuilder rotation(Consumer<RotationBuilder> action) {
        if (this.rotation == null) {
            this.rotation = new RotationBuilder();
        }
        action.accept(rotation);
        return this;
    }

    /**
     * Sets whether or not this element should be shaded.
     */
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
    public ElementBuilder allFaces(BiConsumer<Direction, FaceBuilder> action) {
        Stream.of(Direction.values()).forEach(d -> face(d, b -> action.accept(d, b)));
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
    public ElementBuilder allFacesExcept(BiConsumer<Direction, FaceBuilder> action, Set<Direction> exc) {
        Stream.of(Direction.values()).filter(d -> !exc.contains(d)).forEach(d -> face(d, b -> action.accept(d, b)));
        return this;
    }

    /**
     * Modify all <em>existing</em> faces dynamically using a function.
     *
     * @param action the function to apply to each direction
     * @return this builder
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public ElementBuilder faces(BiConsumer<Direction, FaceBuilder> action) {
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
        Preconditions.checkArgument(lightEmission < 16 && lightEmission >= 0, "lightEmission %s not in valid range - [0,15)", lightEmission);
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
     * @param ambientOcclusion the ambient occlusion
     * @return this builder
     */
    public ElementBuilder ambientOcclusion(boolean ambientOcclusion) {
        this.hasAmbientOcclusion = ambientOcclusion;
        return this;
    }

    private static BiConsumer<Direction, FaceBuilder> addTexture(TextureSlot texture) {
        return ($, f) -> f.texture(texture);
    }

    private static void validateCoordinate(float coord, char name) {
        Preconditions.checkArgument(!(coord < -16.0F) && !(coord > 32.0F), "Position " + name + " out of range, must be within [-16, 32]. Found: %d", coord);
    }

    private static void validatePosition(Vector3f pos) {
        validateCoordinate(pos.x(), 'x');
        validateCoordinate(pos.y(), 'y');
        validateCoordinate(pos.z(), 'z');
    }

    BlockElement build() {
        Map<Direction, BlockElementFace> faces = this.faces.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().build(), (k1, k2) -> {
            throw new IllegalArgumentException();
        }, LinkedHashMap::new));
        return new BlockElement(from, to, faces, rotation == null ? null : rotation.build(), shade, lightEmission, new ExtraFaceData(this.color, this.blockLight, this.skyLight, this.hasAmbientOcclusion));
    }

    ElementBuilder copy() {
        ElementBuilder builder = new ElementBuilder();
        builder.from.set(this.from);
        builder.to.set(this.to);
        this.faces.forEach((side, faceBuilder) -> builder.faces.put(side, faceBuilder.copy()));
        builder.rotation = this.rotation != null ? this.rotation.copy() : null;
        builder.shade = this.shade;
        builder.lightEmission = this.lightEmission;
        builder.color = this.color;
        builder.blockLight = this.blockLight;
        builder.skyLight = this.skyLight;
        builder.hasAmbientOcclusion = this.hasAmbientOcclusion;
        return builder;
    }
}
