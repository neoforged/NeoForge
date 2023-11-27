package net.neoforged.neoforge.debug.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@ForEachTest(side = Dist.CLIENT, groups = "client")
public class ClientTests {
    @TestHolder(description = {"Tests if custom audio streams work", "When the message \"sine wave\" is sent in chat, this should play a sine wave of 220Hz at the player's current position"})
    static void audioStreamTest(final ClientChatEvent event, final DynamicTest test) {
        if (event.getMessage().equalsIgnoreCase("sine wave")) {
            event.setCanceled(true);
            var mc = Minecraft.getInstance();
            mc.getSoundManager().play(new SineSound(mc.player.position()));

            test.requestConfirmation(mc.player, Component.literal("Did you hear the correct sound (sine wave of 220Hz) being played?"));
        }
    }

    private static final class SineSound extends AbstractSoundInstance {
        SineSound(Vec3 position) {
            super(new ResourceLocation("neotests_audio_stream_test", "sine_wave"), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            x = position.x;
            y = position.y;
            z = position.z;
        }

        @NotNull
        @Override
        public CompletableFuture<AudioStream> getStream(@NotNull SoundBufferLibrary soundBuffers, @NotNull Sound sound, boolean looping) {
            return CompletableFuture.completedFuture(new SineStream());
        }
    }

    private static final class SineStream implements AudioStream {
        private static final AudioFormat FORMAT = new AudioFormat(44100, 8, 1, false, false);
        private static final double DT = 2 * Math.PI * 220 / 44100;

        private static double value = 0;

        @NotNull
        @Override
        public AudioFormat getFormat() {
            return FORMAT;
        }

        @NotNull
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
