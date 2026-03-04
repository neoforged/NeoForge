package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.block.BuiltInBlockModels;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockModel;
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

    /// Returns the [BuiltInBlockModels.Builder] to register the [BlockModel.Unbaked]s to
    public BuiltInBlockModels.Builder getBuilder() {
        return builder;
    }
}
