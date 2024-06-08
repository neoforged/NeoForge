/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * An interface used to remove removals from registry data maps. This allows "decomposing" the data
 * and removing only a specific part of it (like a specific key in the case of {@linkplain Map map-based} data).
 *
 * @param <T> the data type
 * @param <R> the type of the registry this remover is for
 * @apiNote This is only useful for {@link AdvancedDataMapType}.
 */
@FunctionalInterface
public interface DataMapValueRemover<R, T> {
    /**
     * Remove the entry specified in this remover from the {@code value}.
     *
     * @param value    the data to remove. Do <b>NOT</b> mutate this object. You should return copies instead,
     *                 if you need to
     * @param registry the registry
     * @param source   the source of the data
     * @param object   the object to remove the data from, could be null when removing default value
     * @return the remainder. If an {@link Optional#empty() empty optional}, the value will be removed
     *         completely. Otherwise, this method returns the new value of the attached data.
     */
    Optional<T> remove(T value, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source, @Nullable R object);

    /**
     * A remover that completely removes the value.
     *
     * @param <T> the type of the data
     * @param <R> the registry type
     */
    class Default<T, R> implements DataMapValueRemover<R, T> {
        public static final Default<?, ?> INSTANCE = new Default<>();

        public static <T, R> Default<T, R> defaultRemover() {
            return (Default<T, R>) INSTANCE;
        }

        public static <T, R> Codec<Default<T, R>> codec() {
            return Codec.unit(defaultRemover());
        }

        private Default() {}

        @Override
        public Optional<T> remove(T value, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source, @Nullable R object) {
            return Optional.empty();
        }
    }

    /**
     * A remover for {@link Collection}s, remove strategy can be specified with {@link #remover}.
     * @param <C> the type of the {@link Collection}
     * @param <R> the registry type
     */
    class CollectionBacked<C extends Collection<?>, R> implements DataMapValueRemover<R, C> {
        private final C removals;
        private final BiFunction<C, C, C> remover;

        private CollectionBacked(C removals, BiFunction<C, C, C> remover) {
            this.removals = removals;
            this.remover = remover;
        }

        /**
         * Creates an instance for datagen, does not support {@link #remove(Collection, Registry, Either, Object)}.
         * @param removals the removals
         * @return an instance for datagen
         */
        public static <C extends Collection<?>, R> CollectionBacked<C, R> datagen(C removals) {
            return new CollectionBacked<>(removals, (c1, c2) -> {
                throw new IllegalStateException("DataMapValueRemover#remove should not be called for datagen instance!");
            });
        }

        /**
         * Creates a {@link Codec} for a specific type of {@link Collection} with custom remove strategy.
         * @param collectionCodec the {@link Codec} of the {@link Collection}
         * @param remover the remove strategy
         * @return a {@link Codec} for {@link CollectionBacked}
         */
        public static <C extends Collection<?>, R> Codec<CollectionBacked<C, R>> codec(Codec<C> collectionCodec, BiFunction<C, C, C> remover) {
            return collectionCodec.xmap(collection -> new CollectionBacked<>(collection, remover), CollectionBacked::removals);
        }

        /**
         * Creates a {@link Codec} for a specific type of {@link Collection} with default remove strategy removing specified elements.
         * @param collectionCodec the {@link Codec} of the {@link Collection}
         * @param collector the collector collecting elements to {@link Collection}
         * @return a {@link Codec} for {@link CollectionBacked}
         */
        public static <E, C extends Collection<E>, R> Codec<CollectionBacked<C, R>> codec(Codec<C> collectionCodec, Collector<E, ?, C> collector) {
            return collectionCodec.xmap(collection -> new CollectionBacked<>(collection, (values, removals) ->
                    values.stream().filter(Predicate.not(removals::contains)).collect(collector)
            ), CollectionBacked::removals);
        }

        /**
         * Creates a {@link Codec} for a {@link List} with default remove strategy removing specified elements.
         * @param listCodec the {@link Codec} of the {@link List}
         * @return a {@link Codec} for {@link CollectionBacked} supporting {@link List}
         */
        public static <E, R> Codec<CollectionBacked<List<E>, R>> listCodec(Codec<List<E>> listCodec) {
            return codec(listCodec, Collectors.toUnmodifiableList());
        }

        /**
         * Creates a {@link Codec} for a {@link Set} with default remove strategy removing specified elements.
         * @param setCodec the {@link Codec} of the {@link Set}
         * @return a {@link Codec} for {@link CollectionBacked}  supporting {@link Set}
         */
        public static <E, R> Codec<CollectionBacked<Set<E>, R>> setCodec(Codec<Set<E>> setCodec) {
            return codec(setCodec, Collectors.toUnmodifiableSet());
        }

        public C removals() {
            return removals;
        }

        @Override
        public Optional<C> remove(C values, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source, @Nullable R object) {
            C result = remover.apply(values, this.removals);
            return result.isEmpty() ? Optional.empty() : Optional.of(result);
        }
    }
}
