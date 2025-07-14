package net.neoforged.neoforge.client.event;


import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.fluids.FluidRendererProvider;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

/**
 * Allows users to register custom {@link Fluid} renderers.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain  LogicalSide#CLIENT logical client}.
 */
public class RegisterFluidRenderersEvent extends Event implements IModBusEvent {
    private final Map<Fluid, FluidRendererProvider> providers;

    @ApiStatus.Internal
    public RegisterFluidRenderersEvent(Map<Fluid, FluidRendererProvider> providers) {
        this.providers = providers;
    }

    /**
     * Registers a renderer provider for the given {@link Fluid}.
     *
     * <p>
     * If the fluid is a flowing fluid like lava or water, entries for both
     * {@linkplain FlowingFluid#getSource() source} and
     * {@linkplain FlowingFluid#getFlowing() flowing} fluids will be added.
     *
     * @param fluid The fluid to register the provider for.
     * @param rendererProvider The provider that can be used to create renderers
     *                         for the fluid.
     */
    public void register(Fluid fluid, FluidRendererProvider rendererProvider) {
        if (fluid instanceof FlowingFluid flowing) {
            providers.put(flowing.getSource(), rendererProvider);
            providers.put(flowing.getFlowing(), rendererProvider);
        } else {
            providers.put(fluid, rendererProvider);
        }
    }
}
