package net.neoforged.neoforge.client.fluids;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.world.FluidBehaviour;

/**
 * A custom renderer for a fluid when it is placed in world. Only fluids that
 * can be {@linkplain FluidBehaviour.Properties#canBePlacedInWorld placed in world}
 * need a renderer.
 *
 *
 * @see FluidRenderers the registry of fluid renderers.
 */
public interface FluidRenderer {
    void tesselate(BlockAndTintGetter level, BlockPos pos, VertexConsumer consumer, BlockState blockState, FluidState fluidState);
}
