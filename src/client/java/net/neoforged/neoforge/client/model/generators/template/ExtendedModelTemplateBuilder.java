/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent;
import org.jetbrains.annotations.Nullable;

public class ExtendedModelTemplateBuilder {
    Optional<ResourceLocation> parent = Optional.empty();
    Optional<String> suffix = Optional.empty();
    final Set<TextureSlot> requiredSlots = new HashSet<>();
    final Map<ItemDisplayContext, TransformVecBuilder> transforms = new LinkedHashMap<>();
    final List<ElementBuilder> elements = new ArrayList<>();
    @Nullable
    CustomLoaderBuilder customLoader = null;
    final RootTransformsBuilder rootTransforms = new RootTransformsBuilder();
    @Nullable
    ResourceLocation renderType = null;
    @Nullable
    Boolean ambientOcclusion = null; // UnbakedModel.DEFAULT_AMBIENT_OCCLUSION
    @Nullable
    UnbakedModel.GuiLight guiLight = null;

    public static ExtendedModelTemplateBuilder of(ModelTemplate template) {
        ExtendedModelTemplateBuilder builder = new ExtendedModelTemplateBuilder();
        builder.parent = template.model;
        builder.suffix = template.suffix;
        builder.requiredSlots.addAll(template.requiredSlots);
        if (template instanceof ExtendedModelTemplate ext) {
            ext.transforms.forEach((ctx, vecBuilder) -> builder.transforms.put(ctx, vecBuilder.copy()));
            ext.elements.forEach(elem -> builder.elements.add(elem.copy()));
            builder.customLoader = ext.customLoader != null ? ext.customLoader.copy() : null;
            builder.rootTransforms.copyFrom(ext.rootTransforms);
            builder.renderType = ext.renderType;
            builder.ambientOcclusion = ext.ambientOcclusion;
            builder.guiLight = ext.guiLight;
        }
        return builder;
    }

    public static ExtendedModelTemplateBuilder builder() {
        return new ExtendedModelTemplateBuilder();
    }

    /**
     * Parent model which this template will inherit its properties from.
     */
    public ExtendedModelTemplateBuilder parent(ResourceLocation parent) {
        this.parent = Optional.of(parent);
        return this;
    }

    /**
     * Suffix appended onto the models file path.
     */
    public ExtendedModelTemplateBuilder suffix(String suffix) {
        this.suffix = Optional.of(suffix);
        return this;
    }

    /**
     * Marks the given {@link TextureSlot slot} as required, meaning that it must be specified in the given {@link TextureMapping texture mappings}.
     */
    public ExtendedModelTemplateBuilder requiredTextureSlot(TextureSlot slot) {
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
    public ExtendedModelTemplateBuilder renderType(String renderType) {
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
    public ExtendedModelTemplateBuilder renderType(ResourceLocation renderType) {
        Preconditions.checkNotNull(renderType, "Render type must not be null");
        this.renderType = renderType;
        return this;
    }

    /**
     * Begin building a new transform for the given perspective.
     *
     * @param type the perspective to create or return the builder for
     * @return the builder for the given perspective
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public ExtendedModelTemplateBuilder transform(ItemDisplayContext type, Consumer<TransformVecBuilder> action) {
        Preconditions.checkNotNull(type, "Perspective cannot be null");
        var builder = transforms.computeIfAbsent(type, TransformVecBuilder::new);
        action.accept(builder);
        return this;
    }

    /**
     * Sets whether or not this model should apply ambient occlusion.
     */
    public ExtendedModelTemplateBuilder ambientOcclusion(boolean ambientOcclusion) {
        this.ambientOcclusion = ambientOcclusion;
        return this;
    }

    /**
     * Sets the gui light style for this model.
     *
     * <ul>
     * <li>{@link UnbakedModel.GuiLight#FRONT} for head on light, commonly used for items.</li>
     * <li>{@link UnbakedModel.GuiLight#SIDE} for the model to be side lit, commonly used for blocks.</li>
     * </ul>
     */
    public ExtendedModelTemplateBuilder guiLight(UnbakedModel.GuiLight light) {
        this.guiLight = light;
        return this;
    }

    /**
     * Creates a new element for this model while also allowing mutation.
     *
     * @throws IllegalStateException if {@code customLoader} does not allow inline elements.
     */
    public ExtendedModelTemplateBuilder element(Consumer<ElementBuilder> action) {
        Preconditions.checkState(
                customLoader == null || customLoader.allowInlineElements,
                "Custom model loader %s does not support inline elements",
                customLoader != null ? customLoader.loaderId : null);
        ElementBuilder ret = new ElementBuilder();
        action.accept(ret);
        elements.add(ret);
        return this;
    }

    /**
     * Get an existing element builder
     *
     * @param index the index of the existing element builder
     * @return the element builder
     * @throws IndexOutOfBoundsException if {@code} index is out of bounds
     */
    public ExtendedModelTemplateBuilder element(int index, Consumer<ElementBuilder> action) {
        Preconditions.checkState(
                customLoader == null || customLoader.allowInlineElements,
                "Custom model loader %s does not support inline elements",
                customLoader != null ? customLoader.loaderId : null);
        Preconditions.checkElementIndex(index, elements.size(), "Element index");
        action.accept(elements.get(index));
        return this;
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
    public <L extends CustomLoaderBuilder> ExtendedModelTemplateBuilder customLoader(Supplier<L> customLoaderFactory, Consumer<L> action) {
        Preconditions.checkNotNull(customLoaderFactory, "customLoaderFactory must not be null");
        L customLoader = customLoaderFactory.get();
        Preconditions.checkState(
                customLoader.allowInlineElements || elements.isEmpty(),
                "Custom model loader %s does not support inline elements",
                customLoader.loaderId);
        this.customLoader = customLoader;
        action.accept(customLoader);
        return this;
    }

    /**
     * Modifies the transformation applied right before item display transformations and rotations specified in block states.
     */
    public ExtendedModelTemplateBuilder rootTransforms(Consumer<RootTransformsBuilder> action) {
        action.accept(rootTransforms);
        return this;
    }

    /**
     * Finalizes this builder into a compiled {@link ModelTemplate}.
     */
    public ExtendedModelTemplate build() {
        return new ExtendedModelTemplate(this);
    }
}
