package net.neoforged.neoforge.client.fluids;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterFluidRenderersEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry of custom fluid renderers.
 */
public final class FluidRenderers {
    private static ImmutableMap<Fluid, FluidRendererProvider> PROVIDERS;

    private FluidRenderers() { }

    public static Map<Fluid, FluidRenderer> createFluidRenderers(FluidRendererProvider.Context context) {
        ImmutableMap.Builder<Fluid, FluidRenderer> builder = ImmutableMap.builder();

        PROVIDERS.forEach((fluid, provider) -> {
            try {
                builder.put(fluid, provider.create(context));
            } catch (Exception exception) {
                throw new IllegalArgumentException("Failed to create renderer for " + BuiltInRegistries.FLUID.getKey(fluid), exception);
            }
        });

        return builder.build();
    }

    @ApiStatus.Internal
    public static void init() {
        var renderers = new HashMap<Fluid, FluidRendererProvider>();
        var event = new RegisterFluidRenderersEvent(renderers);
        ModLoader.postEventWrapContainerInModOrder(event);
        PROVIDERS = ImmutableMap.copyOf(renderers);
    }
}
