/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import org.lwjgl.BufferUtils;

@ForEachTest(side = Dist.CLIENT, groups = "client")
public class ClientTests {
    @TestHolder(description = { "Tests if custom audio streams work", "When the message \"sine wave\" is sent in chat, this should play a sine wave of 220Hz at the player's current position" })
    static void audioStreamTest(final ClientChatEvent event, final DynamicTest test) {
        if (event.getMessage().equalsIgnoreCase("sine wave")) {
            event.setCanceled(true);
            var mc = Minecraft.getInstance();
            mc.getSoundManager().play(new SineSound(mc.player.position()));

            test.requestConfirmation(mc.player, Component.literal("Did you hear the correct sound (sine wave of 220Hz) being played?"));
        }
    }

    @TestHolder(description = { "Tests if key mappings work", "Adds two keys both bound to backslash by default", "Will pass if the 'stick_key' key is pressed with a stick in the main hand, or if the 'rock_key' one is pressed with cobblestone in the main hand" })
    static void keyMappingTest(final DynamicTest test) {
        // these are two separate keys to stand in for keys added by different
        // mods that each do something similar with a held item from the
        // respective mod, so the user wants them on the same physical key.
        final KeyMapping stickKey = new KeyMapping("stick_key", InputConstants.KEY_BACKSLASH, KeyMapping.CATEGORY_MISC);
        final KeyMapping rockKey = new KeyMapping("rock_key", InputConstants.KEY_BACKSLASH, KeyMapping.CATEGORY_MISC);

        test.registrationHelper().provider(LanguageProvider.class, lang -> {
            lang.add(stickKey.getName(), "Stick key");
            lang.add(rockKey.getName(), "Rock key");
        });

        test.framework().modEventBus().addListener((final RegisterKeyMappingsEvent event) -> {
            event.register(rockKey);
            event.register(stickKey);
        });

        test.eventListeners().forge().addListener((ClientTickEvent.Pre event) -> {
            if (stickKey.consumeClick()) {
                Player player = Minecraft.getInstance().player;
                if (player != null && player.getMainHandItem().is(Items.STICK)) {
                    player.sendSystemMessage(Component.literal("stick found!"));
                    test.pass();
                }
            }
            if (rockKey.consumeClick()) {
                Player player = Minecraft.getInstance().player;
                if (player != null && player.getMainHandItem().is(Items.COBBLESTONE)) {
                    player.sendSystemMessage(Component.literal("rock found!"));
                    test.pass();
                }
            }
        });
    }

    private static final class SineSound extends AbstractSoundInstance {
        SineSound(Vec3 position) {
            super(ResourceLocation.fromNamespaceAndPath("neotests_audio_stream_test", "sine_wave"), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            x = position.x;
            y = position.y;
            z = position.z;
        }

        @Override
        public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
            return CompletableFuture.completedFuture(new SineStream());
        }
    }

    private static final class SineStream implements AudioStream {
        private static final AudioFormat FORMAT = new AudioFormat(44100, 8, 1, false, false);
        private static final double DT = 2 * Math.PI * 220 / 44100;

        private static double value = 0;

        @Override
        public AudioFormat getFormat() {
            return FORMAT;
        }

        @Override
        public ByteBuffer read(int capacity) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(capacity);
            for (int i = 0; i < capacity; i++) {
                buffer.put(i, (byte) (Math.sin(value) * 127));
                value = (value + DT) % Math.PI;
            }
            return buffer;
        }

        @Override
        public void close() {}
    }
}
