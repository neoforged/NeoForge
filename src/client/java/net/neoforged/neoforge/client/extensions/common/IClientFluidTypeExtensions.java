/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions.common;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

/**
 * {@linkplain LogicalSide#CLIENT Client-only} extensions to {@link FluidType}.
 *
 * @see net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent
 */
public interface IClientFluidTypeExtensions {
    IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions() {};

    static IClientFluidTypeExtensions of(FluidState state) {
        return of(state.getFluidType());
    }

    static IClientFluidTypeExtensions of(Fluid fluid) {
        return of(fluid.getFluidType());
    }

    static IClientFluidTypeExtensions of(FluidType type) {
        return ClientExtensionsManager.FLUID_TYPE_EXTENSIONS.getOrDefault(type, DEFAULT);
    }

    /**
     * Returns the location of the texture to apply to the camera when it is
     * within the fluid. If no location is specified, no overlay will be applied.
     *
     * <p>This should return a location to the texture and not a reference
     * (e.g. {@code minecraft:textures/misc/underwater.png} will use the texture
     * at {@code assets/minecraft/textures/misc/underwater.png}).
     *
     * @param mc the client instance
     * @return the location of the texture to apply to the camera when it is
     *         within the fluid
     */
    @Nullable
    default Identifier getRenderOverlayTexture(Minecraft mc) {
        return null;
    }

    /// Extracts an overlay renderer rendering the [#getRenderOverlayTexture(Minecraft)] onto the camera when within
    /// the fluid.
    ///
    /// @param minecraft         The client instance
    /// @param player            The player in the fluid
    /// @param playerRenderState The render state of the player in the fluid
    /// @param eyePos            The block position containing the player's eye position
    /// @param brightness        The brightness at the player's eye position
    default void extractOverlay(Minecraft minecraft, LocalPlayer player, PlayerRenderState playerRenderState, BlockPos eyePos, float brightness) {
        Identifier texture = this.getRenderOverlayTexture(minecraft);
        if (texture != null) {
            int color = ARGB.colorFromFloat(0.1F, brightness, brightness, brightness);
            PlayerRenderState.WaterOverlay overlay = new PlayerRenderState.WaterOverlay(color, -player.getYRot() / 64.0F, player.getXRot() / 64.0F);
            playerRenderState.customFluidOverlayRenderer = (_, _, submitNodeCollector, poseStack, _, _, _) -> {
                ScreenEffectRenderer.submitFluid(overlay, poseStack, submitNodeCollector, texture);
                return true;
            };
        }
    }

    /**
     * Modifies the color of the fog when the camera is within the fluid.
     *
     * <p>The result expects a four float vector representing the red, green,
     * blue and alpha channels respectively. Each channel should be between [0,1].
     *
     * @param camera            the camera instance
     * @param partialTick       the delta time of where the current frame is within a tick
     * @param level             the level the camera is located in
     * @param renderDistance    the render distance of the client
     * @param darkenWorldAmount the amount to darken the world by
     * @param fluidFogColor     the current RGBA color of the fog
     */
    default void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {}

    /**
     * Modifies how the fog is currently being rendered when the camera is
     * within a fluid.
     *
     * @param camera         the camera instance
     * @param environment    the type of fog being rendered
     * @param renderDistance the render distance of the client
     * @param partialTick    the delta time of where the current frame is within a tick
     * @param fogData        the parameters to use for rendering the fog, this object can be modified.
     */
    default void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {}
}
