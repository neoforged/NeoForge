package net.neoforged.neoforge.client.fluids;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A provider for instances of {@linkplain FluidRenderer}
 */
public interface FluidRendererProvider {
    /**
     * Creates a fluid renderer with the given context.
     *
     * @param context The context to create the fluid renderer.
     * @return A {@link FluidRenderer} which can be used to render fluids.
     */
    FluidRenderer create(Context context);

    /**
     * A record containing contextual information which may be used during the
     * creation of a fluid renderer.
     *
     * @param resourceManager The resource manager which may be used to access
     *                        resources.
     * @param blockAtlas The block atlas which may be used to look up sprites.
     * @param blockColors The map of block colors.
     */
    record Context(
        ResourceManager resourceManager,
        Function<ResourceLocation, TextureAtlasSprite> blockAtlas,
        BlockColors blockColors)
    { }

    /**
     * Creates a renderer which uses the same logic as vanilla for rendering fluids.
     * @param still The still texture for the fluid.
     * @param flowing The flowing texture for the fluid.
     * @param overlay The overlay texture for the fluid, or null if there is none.
     *
     * @return A fluid renderer which can render the given fluid.
     */
    static FluidRendererProvider vanilla(
        ResourceLocation still,
        ResourceLocation flowing,
        @Nullable
        ResourceLocation overlay) {

        return (context) ->
            new NeoLiquidBlockRenderer(
                context.blockAtlas().apply(still),
                context.blockAtlas().apply(flowing),
                overlay == null ? null : context.blockAtlas().apply(overlay)
            );
    }
}
