/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.*;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.registries.ForgeRegistries;

public interface ICondition {
    Codec<ICondition> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ForgeRegistries.CONDITION_SERIALIZERS.get().getCodec().dispatch(ICondition::codec, Function.identity()));
    Codec<List<ICondition>> LIST_CODEC = CODEC.listOf();

    static <V, T> Optional<T> getConditionally(Codec<T> codec, DynamicOps<V> ops, V element) {
        return getWithConditionalCodec(ConditionalOps.createConditionalCodec(codec).codec(), ops, element);
    }

    static <V, T> Optional<T> getWithConditionalCodec(Codec<Optional<T>> codec, DynamicOps<V> ops, V element) {
        return Util.getOrThrow(codec.parse(ops, element).promotePartial((m) -> {}), JsonParseException::new);
    }

    static <V> boolean conditionsMatched(DynamicOps<V> ops, V element) {
        final Codec<Unit> codec = Codec.unit(Unit.INSTANCE);
        return getConditionally(codec, ops, element).isPresent();
    }

    boolean test(IContext context);

    Codec<? extends ICondition> codec();

    interface IContext {
        IContext EMPTY = new IContext() {
            @Override
            public <T> Map<ResourceLocation, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry) {
                return Collections.emptyMap();
            }
        };

        IContext TAGS_INVALID = new IContext() {
            @Override
            public <T> Map<ResourceLocation, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry) {
                throw new UnsupportedOperationException("Usage of tag-based conditions is not permitted in this context!");
            }
        };

        /**
         * Return the requested tag if available, or an empty tag otherwise.
         */
        default <T> Collection<Holder<T>> getTag(TagKey<T> key) {
            return getAllTags(key.registry()).getOrDefault(key.location(), Set.of());
        }

        /**
         * Return all the loaded tags for the passed registry, or an empty map if none is available.
         * Note that the map and the tags are unmodifiable.
         */
        <T> Map<ResourceLocation, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry);
    }
}
