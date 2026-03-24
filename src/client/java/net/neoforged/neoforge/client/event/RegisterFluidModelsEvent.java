/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/// Event fired when [FluidModel.Unbaked]s are created.
///
/// This event is fired on a worker thread during model loading. It is used to register custom fluid models.
///
/// This event is fired on the mod-specific event bus, only on the [logical client][LogicalSide#Client]
public final class RegisterFluidModelsEvent extends Event implements IModBusEvent {
    private final Map<Fluid, FluidModel> models;
    private final MaterialBaker materials;

    @ApiStatus.Internal
    public RegisterFluidModelsEvent(Map<Fluid, FluidModel> models, MaterialBaker materials) {
        this.models = models;
        this.materials = materials;
    }

    /// Register the given [FluidModel.Unbaked] for the given [Fluid]
    public void register(FluidModel.Unbaked model, Supplier<? extends Fluid> fluid) {
        this.register(model, fluid.get());
    }

    /// Register the given [FluidModel.Unbaked] for the given [Fluid]
    public void register(FluidModel.Unbaked model, Fluid fluid) {
        this.register(fluid, model.bake(materials, fluid::toString));
    }

    /// Register the given [FluidModel.Unbaked] for the two given [Fluid]s
    public void register(FluidModel.Unbaked model, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid) {
        this.register(model, stillFluid.get(), flowingFluid.get());
    }

    /// Register the given [FluidModel.Unbaked] for the two given [Fluid]s
    public void register(FluidModel.Unbaked model, Fluid stillFluid, Fluid flowingFluid) {
        FluidModel baked = model.bake(materials, stillFluid::toString);
        this.register(stillFluid, baked);
        this.register(flowingFluid, baked);
    }

    private void register(Fluid fluid, FluidModel model) {
        FluidModel prevModel = this.models.putIfAbsent(fluid, model);
        if (prevModel != null) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    "Duplicate FluidModel registration for Fluid %s (old: %s, new: %s)",
                    fluid,
                    prevModel,
                    model));
        }
    }
}
