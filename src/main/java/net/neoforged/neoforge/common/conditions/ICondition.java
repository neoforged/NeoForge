/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public interface ICondition {
    Codec<ICondition> CODEC = NeoForgeRegistries.CONDITION_SERIALIZERS.byNameCodec()
            .dispatch(ICondition::codec, Function.identity());
    Codec<List<ICondition>> LIST_CODEC = CODEC.listOf();

    static <V, T> Optional<T> getConditionally(Codec<T> codec, DynamicOps<V> ops, V element) {
        return getWithConditionalCodec(ConditionalOps.createConditionalCodec(codec), ops, element);
    }

    static <V, T> Optional<T> getWithConditionalCodec(Codec<Optional<T>> codec, DynamicOps<V> ops, V element) {
        return codec.parse(ops, element).getOrThrow(JsonParseException::new);
    }

    static <V, T> Optional<T> getWithWithConditionsCodec(Codec<Optional<WithConditions<T>>> codec, DynamicOps<V> ops, V elements) {
        return codec.parse(ops, elements).promotePartial((m) -> {}).getOrThrow(JsonParseException::new).map(WithConditions::carrier);
    }

    static <V> boolean conditionsMatched(DynamicOps<V> ops, V element) {
        final Codec<Unit> codec = Codec.unit(Unit.INSTANCE);
        return getConditionally(codec, ops, element).isPresent();
    }

    /**
     * Writes an array of conditions to a JSON object.
     */
    static void writeConditions(HolderLookup.Provider registries, JsonObject jsonObject, ICondition... conditions) {
        writeConditions(registries, jsonObject, List.of(conditions));
    }

    /**
     * Writes a list of conditions to a JSON object.
     */
    static void writeConditions(HolderLookup.Provider registries, JsonObject jsonObject, List<ICondition> conditions) {
        writeConditions(RegistryOps.create(JsonOps.INSTANCE, registries), jsonObject, conditions);
    }

    /**
     * Writes a list of conditions to a JSON object.
     */
    static void writeConditions(DynamicOps<JsonElement> jsonOps, JsonObject jsonObject, List<ICondition> conditions) {
        if (!conditions.isEmpty()) {
            var result = LIST_CODEC.encodeStart(jsonOps, conditions);
            JsonElement serializedConditions = result.result().orElseThrow(() -> new RuntimeException("Failed to serialize conditions"));
            jsonObject.add(ConditionalOps.DEFAULT_CONDITIONS_KEY, serializedConditions);
        }
    }

    boolean test(IContext context);

    MapCodec<? extends ICondition> codec();

    interface IContext { // TODO 1.21.2: potentially remove since all the info is available via RegistryOps
        IContext EMPTY = new IContext() {
            @Override
            public <T> boolean isTagLoaded(TagKey<T> key) {
                return false;
            }
        };

        IContext TAGS_INVALID = new IContext() {
            @Override
            public <T> boolean isTagLoaded(TagKey<T> key) {
                throw new UnsupportedOperationException("Usage of tag-based conditions is not permitted in this context!");
            }
        };

        /**
         * Returns {@code true} if the requested tag is available.
         */
        <T> boolean isTagLoaded(TagKey<T> key);
    }
}
