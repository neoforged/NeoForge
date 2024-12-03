/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ExtendedModelTemplate;
import org.jetbrains.annotations.Nullable;

public class DynamicFluidContainerModelBuilder extends CustomLoaderBuilder {
    public static DynamicFluidContainerModelBuilder begin(ExtendedModelTemplate.Builder parent) {
        return new DynamicFluidContainerModelBuilder(parent);
    }

    @Nullable
    private ResourceLocation fluid;
    @Nullable
    private Boolean flipGas;
    @Nullable
    private Boolean applyTint;
    @Nullable
    private Boolean coverIsMask;
    @Nullable
    private Boolean applyFluidLuminosity;

    protected DynamicFluidContainerModelBuilder(ExtendedModelTemplate.Builder parent) {
        super(ResourceLocation.fromNamespaceAndPath("neoforge", "fluid_container"), parent, false);
    }

    public DynamicFluidContainerModelBuilder fluid(Fluid fluid) {
        Preconditions.checkNotNull(fluid, "fluid must not be null");
        this.fluid = BuiltInRegistries.FLUID.getKey(fluid);
        return this;
    }

    public DynamicFluidContainerModelBuilder flipGas(boolean flip) {
        this.flipGas = flip;
        return this;
    }

    public DynamicFluidContainerModelBuilder applyTint(boolean tint) {
        this.applyTint = tint;
        return this;
    }

    public DynamicFluidContainerModelBuilder coverIsMask(boolean coverIsMask) {
        this.coverIsMask = coverIsMask;
        return this;
    }

    public DynamicFluidContainerModelBuilder applyFluidLuminosity(boolean applyFluidLuminosity) {
        this.applyFluidLuminosity = applyFluidLuminosity;
        return this;
    }

    @Override
    protected CustomLoaderBuilder copyInternal(ExtendedModelTemplate.Builder owner) {
        DynamicFluidContainerModelBuilder builder = new DynamicFluidContainerModelBuilder(owner);
        builder.fluid = this.fluid;
        builder.flipGas = this.flipGas;
        builder.applyTint = this.applyTint;
        builder.coverIsMask = this.coverIsMask;
        builder.applyFluidLuminosity = this.applyFluidLuminosity;
        return builder;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        Preconditions.checkNotNull(fluid, "fluid must not be null");

        json.addProperty("fluid", fluid.toString());

        if (flipGas != null)
            json.addProperty("flip_gas", flipGas);

        if (applyTint != null)
            json.addProperty("apply_tint", applyTint);

        if (coverIsMask != null)
            json.addProperty("cover_is_mask", coverIsMask);

        if (applyFluidLuminosity != null)
            json.addProperty("apply_fluid_luminosity", applyFluidLuminosity);

        return json;
    }
}
