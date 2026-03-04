/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.block.BuiltInBlockModels;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/// Event fired when [BlockModel.Unbaked]s are created
///
/// This event is fired on a worker thread during model loading. It is used to register custom special block models
/// which handle non-[BlockStateModel] geometry and dynamic rendering when the associated block is rendered in a
/// non-placed context such as in a minecart, a display entity or the Enderman's hands.
///
/// This event is fired on the mod-specific event bus, only on the [logical client][LogicalSide#Client]
public final class RegisterBlockModelsEvent extends Event implements IModBusEvent {
    private final BuiltInBlockModels.Builder builder;

    @ApiStatus.Internal
    public RegisterBlockModelsEvent(BuiltInBlockModels.Builder builder) {
        this.builder = builder;
    }

    /// Register the given [BlockModel.Unbaked] for the given [Block]
    public void register(BlockModel.Unbaked specialModel, Block block) {
        this.builder.put(specialModel, block);
    }

    /// Register [BlockModel.Unbaked]s created by the given [BuiltInBlockModels.ModelFactory] for the given [Block]
    public void register(BuiltInBlockModels.ModelFactory factory, Block block) {
        this.builder.put(factory, block);
    }

    /// Register [BlockModel.Unbaked]s created by the given [BuiltInBlockModels.ModelFactory] for the two given [Block]s
    public void register(BuiltInBlockModels.ModelFactory factory, Block blockOne, Block blockTwo) {
        this.builder.put(factory, blockOne, blockTwo);
    }

    /// Returns the [BuiltInBlockModels.Builder] to register the [BlockModel.Unbaked]s to
    public BuiltInBlockModels.Builder getBuilder() {
        return this.builder;
    }
}
