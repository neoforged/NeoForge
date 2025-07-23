/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.util.dfu.ContextOps;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

public class ContextOpsTests {
    private static final Gson GSON = new Gson();

    @Test
    void testRCBContextRetrieval() {
        class SomeContext {}

        var key = new ContextOps.Key<SomeContext>(ResourceLocation.parse("a:b"));

        var context = new SomeContext();

        var ops = ContextOps.create(JsonOps.INSTANCE)
                .withContext(key, context);

        record Obj(SomeContext context, int value) {}
        var codec = RecordCodecBuilder.<Obj>create(in -> in.group(
                ContextOps.retrieveContext(key).forGetter(Obj::context),
                Codec.INT.fieldOf("val").forGetter(Obj::value)).apply(in, Obj::new));

        var result = parseJson(ops, codec, "{\"val\": 1}");

        assertThat(result.result())
                .isPresent()
                .hasValue(new Obj(context, 1));
    }

    @Test
    void testUnavailableContext() {
        var codec = ContextOps.retrieveContext(new ContextOps.Key<>(ResourceLocation.parse("does_not_exist")));

        var result = parseJson(ContextOps.create(JsonOps.INSTANCE)
                .withContext(new ContextOps.Key<>(ResourceLocation.parse("abc:d")), "some value"),
                codec.codec(), "{}");

        assertThat(result.error())
                .isPresent()
                .map(DataResult.Error::message)
                .get(as(STRING))
                .contains("does not have context with ID minecraft:does_not_exist. Available context: [abc:d]");
    }

    @Test
    void testNotContextOps() {
        var codec = ContextOps.retrieveContext(new ContextOps.Key<>(ResourceLocation.parse("does_not_exist")));

        var result = parseJson(JsonOps.INSTANCE, codec.codec(), "{}");

        assertThat(result.error())
                .isPresent()
                .map(DataResult.Error::message)
                .hasValue("Dynamic ops JSON is not context-aware");
    }

    @Test
    @ExtendWith(EphemeralTestServerProvider.class)
    void testRegistryOps(MinecraftServer server) {
        var key = new ContextOps.Key<MinecraftServer>(ResourceLocation.parse("server_context"));

        record Obj(MinecraftServer server, Holder<Biome> biome) {}
        var codec = RecordCodecBuilder.<Obj>create(in -> in.group(
                ContextOps.retrieveContext(key).forGetter(Obj::server),
                Biome.CODEC.fieldOf("biome").forGetter(Obj::biome)).apply(in, Obj::new));

        var result = parseJson(ContextOps.create(server.registryAccess().createSerializationContext(JsonOps.INSTANCE))
                .withContext(key, server), codec, "{\"biome\": \"minecraft:plains\"}");

        assertThat(result.result())
                .isPresent()
                .hasValueSatisfying(obj -> {
                    assertThat(obj.server()).isEqualTo(server);
                    assertThat(obj.biome().getKey()).isEqualTo(Biomes.PLAINS);
                });
    }

    private static <T> DataResult<T> parseJson(DynamicOps<JsonElement> ops, Codec<T> codec, @Language("json") String json) {
        return codec.decode(ops, GSON.fromJson(json, JsonElement.class)).map(Pair::getFirst);
    }
}
