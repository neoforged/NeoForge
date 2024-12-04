/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(CustomParticleTypeTest.MOD_ID)
public class CustomParticleTypeTest {
    public static final String MOD_ID = "custom_particle_type_test";
    private static final boolean ENABLED = true;

    public CustomParticleTypeTest() {}

    @EventBusSubscriber(modid = CustomParticleTypeTest.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientEvents {
        private static final ParticleRenderType CUSTOM_TYPE = new ParticleRenderType("CUSTOM_TYPE", RenderType.translucentParticle(TextureAtlas.LOCATION_BLOCKS));
        private static final ParticleRenderType CUSTOM_TYPE_TWO = new ParticleRenderType("CUSTOM_TYPE_TWO", RenderType.translucentParticle(TextureAtlas.LOCATION_BLOCKS));

        private static class CustomParticle extends TerrainParticle {
            public CustomParticle(ClientLevel level, double x, double y, double z) {
                super(level, x, y, z, 0, .25, 0, Blocks.OBSIDIAN.defaultBlockState());
            }

            @Override
            public ParticleRenderType getRenderType() {
                return CUSTOM_TYPE;
            }
        }

        private static class AnotherCustomParticle extends TerrainParticle {
            public AnotherCustomParticle(ClientLevel level, double x, double y, double z) {
                super(level, x, y, z, 0, .25, 0, Blocks.SAND.defaultBlockState());
            }

            @Override
            public ParticleRenderType getRenderType() {
                return CUSTOM_TYPE_TWO;
            }
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            if (!ENABLED) {
                return;
            }

            ClientLevel level = Minecraft.getInstance().level;
            Player player = Minecraft.getInstance().player;
            if (player == null || level == null || !player.isShiftKeyDown()) {
                return;
            }

            Minecraft.getInstance().particleEngine.add(new CustomParticle(level, player.getX(), player.getY(), player.getZ()));
            Minecraft.getInstance().particleEngine.add(new AnotherCustomParticle(level, player.getX(), player.getY(), player.getZ()));
        }
    }
}
