/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.world;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.resources.InMemoryResourcePack;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.slf4j.Logger;

/**
 * A test mod used to test splitting the {@link net.minecraft.network.protocol.game.ClientboundLoginPacket}. <br>
 * In order to test this works, first {@link #ENABLED enable} the packet.
 * Start a local server and client. In the server console you should see how big the
 * registryaccess in the packet would be, and how much {@code %} of the packet limit is represents. <br>
 * Connect to the server from the client, and if you successfully connect and the {@code /big_data} command
 * reports 50000 entries then the packet has been successfully split. <br> <br>
 * To test if the packet is too large simply remove the login packet from the {@link net.neoforged.neoforge.network.filters.GenericPacketSplitter}
 * and try connecting again. You should see the connection fail.
 */

@Mod(LoginPacketSplitTest.MOD_ID)
public class LoginPacketSplitTest {
    public static final Logger LOG = LogUtils.getLogger();
    public static final String MOD_ID = "login_packet_split_test";
    public static final boolean ENABLED = true;
    private static final Gson GSON = new Gson();
    public static final ResourceKey<Registry<BigData>> BIG_DATA = ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "big_data"));

    public LoginPacketSplitTest(IEventBus bus) {
        bus.addListener((final DataPackRegistryEvent.NewRegistry event) -> event.dataPackRegistry(BIG_DATA, BigData.CODEC, BigData.CODEC));
        if (ENABLED) {
            bus.addListener((final AddPackFindersEvent event) -> {
                if (event.getPackType() == PackType.SERVER_DATA) {
                    final InMemoryResourcePack pack = new InMemoryResourcePack("virtual_bigdata");
                    generateEntries(pack);
                    event.addRepositorySource(packs -> packs.accept(Pack.readMetaAndCreate(
                            pack.packId(),
                            Component.literal("Pack containing big datapack registries"),
                            true,
                            BuiltInPackSource.fixedResources(pack),
                            PackType.SERVER_DATA,
                            Pack.Position.TOP,
                            PackSource.BUILT_IN)));
                }
            });

            if (FMLLoader.getDist().isClient()) {
                NeoForge.EVENT_BUS.addListener((final RegisterClientCommandsEvent event) -> event.getDispatcher().register(Commands.literal("big_data")
                        .executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("Registry has " + context.getSource().registryAccess().registryOrThrow(BIG_DATA).holders().count() + " entries."), true);
                            return Command.SINGLE_SUCCESS;
                        })));
            }
        }
    }

    private void generateEntries(InMemoryResourcePack pack) {
        final Stopwatch stopwatch = Stopwatch.createUnstarted();
        final Registry<BigData> dummyRegistry = new MappedRegistry<>(BIG_DATA, Lifecycle.stable(), false);
        final Random random = new Random();

        stopwatch.start();
        for (int i = 0; i < 50_000; i++) {
            final BigData bigData = new BigData(randomString(random, 30 + random.nextInt(10)).repeat(15), random.nextInt(Integer.MAX_VALUE));
            final JsonObject json = new JsonObject();
            json.addProperty("text", bigData.text);
            json.addProperty("number", bigData.number);
            pack.putData(PackType.SERVER_DATA, new ResourceLocation(MOD_ID, MOD_ID + "/big_data/entry_" + i + ".json"), json);
            Registry.register(dummyRegistry, new ResourceLocation(MOD_ID, MOD_ID + "/big_data/entry_" + i), bigData);
        }
        stopwatch.stop();
        LOG.warn("Setting up big data registry took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " miliseconds.");

        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        record RegistryData(Registry<BigData> registry) {}
        writeJsonWithCodec(buf, RecordCodecBuilder.create(in -> in.group(
                RegistryCodecs.networkCodec(BIG_DATA, Lifecycle.stable(), BigData.CODEC).fieldOf("registry").forGetter(RegistryData::registry)).apply(in, RegistryData::new)), new RegistryData(dummyRegistry)); // RegistryCodecs.networkCodec returns a list codec, and writeWithNbt doesn't like non-compounds

        final int size = buf.writerIndex();
        LOG.warn("Dummy big registry size: " + size + ", or " + ((double) size / CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH * 100) + "% of the maximum packet size.");
    }

    private String randomString(Random random, int length) {
        return random.ints(97, 122 + 1) // letter 'a' to letter 'z'
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public record BigData(String text, int number) {
        public static final Codec<BigData> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.STRING.fieldOf("text").forGetter(BigData::text),
                Codec.INT.fieldOf("number").forGetter(BigData::number)).apply(in, BigData::new));
    }

    public <T> void writeJsonWithCodec(FriendlyByteBuf buf, Codec<T> codec, T instance) {
        DataResult<JsonElement> dataresult = codec.encodeStart(JsonOps.INSTANCE, instance);
        final String s = GSON.toJson(Util.getOrThrow(dataresult, p_261421_ -> new EncoderException("Failed to encode: " + p_261421_ + " " + instance)));
        buf.writeVarInt(s.length());
        buf.writeUtf(s, s.length());
    }
}
