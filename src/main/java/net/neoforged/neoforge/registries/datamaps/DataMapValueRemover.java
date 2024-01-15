/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/**
 * An interface used to remove values from registry data maps. This allows "decomposing" the data
 * and removing only a specific part of it (like a specific key in the case of {@linkplain java.util.Map map-based} data).
 *
 * @param <T> the data type
 * @param <R> the type of the registry this remover is for
 */
@FunctionalInterface
public interface DataMapValueRemover<T, R> {

    /**
     * Remove the entry specified in this remover from the {@code value}.
     *
     * @param value    the data to remove. Do <b>NOT</b> mutate this object. You should return copies instead,
     *                 if you need to
     * @param registry the registry
     * @param source   the source of the data
     * @param object   the object to remove the data from
     * @return the removed data map value. If an {@link Optional#empty() empty optional}, the value will be removed
     *         completely. Otherwise, this method returns the new value of the attached data.
     */
    Optional<T> remove(T value, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source, R object);

    /**
     * A remover that completely removes the value.
     *
     * @param <T> the type of the data
     * @param <R> the registry type
     */
    class Default<T, R> implements DataMapValueRemover<T, R> {
        public static final Default<?, ?> INSTANCE = new Default<>();

        public static <T, R> Default<T, R> defaultRemover() {
            return (Default<T, R>) INSTANCE;
        }

        public static <T, R> Codec<Default<T, R>> codec() {
            return Codec.unit(defaultRemover());
        }

        private Default() {}

        @Override
        public Optional<T> remove(T value, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source, R object) {
            return Optional.empty();
        }
    }
}
