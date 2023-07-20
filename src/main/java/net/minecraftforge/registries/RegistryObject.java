/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @deprecated Use {@link DeferredHolder}
 */
@Deprecated(since = "1.20.1", forRemoval = true)
public final class RegistryObject<T> extends DeferredHolder<T>
{
    public static <T, U extends T> RegistryObject<U> create(final ResourceLocation name, IForgeRegistry<T> registry)
    {
        return create(name, registry.getRegistryKey());
    }

    public static <T, U extends T> RegistryObject<U> create(final ResourceLocation name, final ResourceKey<? extends Registry<T>> registryKey, String modid)
    {
        return create(name, registryKey);
    }

    public static <T, U extends T> RegistryObject<U> createOptional(final ResourceLocation name, final ResourceKey<? extends Registry<T>> registryKey,
            String modid)
    {
        return create(name, registryKey);
    }

    public static <T, U extends T> RegistryObject<U> create(final ResourceLocation name, final ResourceLocation registryName, String modid)
    {
        return create(name, ResourceKey.createRegistryKey(registryName));
    }

    public static <T, U extends T> RegistryObject<U> createOptional(final ResourceLocation name, final ResourceLocation registryName, String modid)
    {
        return create(name, ResourceKey.createRegistryKey(registryName));
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private static <T> RegistryObject<T> create(ResourceLocation name, ResourceKey registryKey)
    {
        return new RegistryObject<T>(ResourceKey.create(registryKey, name));
    }

    private static final RegistryObject<?> EMPTY = createOptional(new ResourceLocation("empty", "empty"), new ResourceLocation("empty", "empty"), "empty");

    private static <T> RegistryObject<T> empty() {
        @SuppressWarnings("unchecked")
        RegistryObject<T> t = (RegistryObject<T>) EMPTY;
        return t;
    }
    
    public Stream<T> stream()
    {
        return isPresent() ? Stream.of(get()) : Stream.of();
    }
    
    private RegistryObject(ResourceKey<T> key)
    {
        super(key);
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (isPresent())
            consumer.accept(get());
    }

    public RegistryObject<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent())
            return this;
        else
            return predicate.test(get()) ? this : empty();
    }

    public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return Optional.empty();
        else {
            return Optional.ofNullable(mapper.apply(get()));
        }
    }

    public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return Optional.empty();
        else {
            return Objects.requireNonNull(mapper.apply(get()));
        }
    }

    public<U> Supplier<U> lazyMap(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return () -> isPresent() ? mapper.apply(get()) : null;
    }

    public T orElse(T other) {
        return isPresent() ? get() : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return isPresent() ? get() : other.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return get();
        } else {
            throw exceptionSupplier.get();
        }
    }

    public Optional<Holder<T>> getHolder()
    {
        return Optional.of(this);
    }
}
