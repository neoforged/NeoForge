/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util.dfu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link DelegatingOps} that can hold additional context, identified by {@linkplain Key}s.
 * <p>
 * Context can be injected with {@link #withContext(Map) withContext} and retrieved via {@link #getContext(Key) getContext}.
 * Utility methods like {@link #retrieveContext(Key)}, {@link #retrieveOptionalContext(Key)} or {@link #retrieveOptionalContext(Key, Object)}
 * can be used to retrieve the context through a {@linkplain MapCodec}.
 * <p>
 * The context map of {@linkplain ContextOps} is immutable, therefore any additional context can only be added by creating a new instance.
 *
 * @param <T> the type of the base element the delegate {@linkplain DynamicOps} serialises to and from
 * @apiNote This class should not be extended. Instead, use contexts via {@link #withContext}.
 * @see Key
 */
@ApiStatus.NonExtendable
public class ContextOps<T> extends DelegatingOps<T> {
    protected final Map<Key<?>, Object> contextObjects;

    /**
     * Create a new context that delegates to the given ops.
     *
     * @param delegate the ops to delegate to. If it is an instance of {@link ContextOps}, its context will be copied to this ops.
     */
    protected ContextOps(DynamicOps<T> delegate) {
        this(delegate, Map.of());
    }

    /**
     * Create a new context that delegates to the given ops, and has the given {@code additionalContext} as context.
     *
     * @param delegate          the ops to delegate to. If it is an instance of {@link ContextOps}, its context will be copied to this ops.
     * @param additionalContext additional context objects to add on top of the ones from the {@code delegate}
     */
    protected ContextOps(DynamicOps<T> delegate, Map<Key<?>, Object> additionalContext) {
        super(delegate);

        Map<Key<?>, Object> delegateContext = delegate instanceof ContextOps<?> contextAware ? contextAware.contextObjects : Map.of();
        if (additionalContext.isEmpty()) {
            this.contextObjects = delegateContext;
        } else {
            var newContext = new HashMap<>(delegateContext);
            newContext.putAll(additionalContext);
            this.contextObjects = Collections.unmodifiableMap(newContext);
        }
    }

    /**
     * {@return a context ops instance from the given {@code ops}}
     *
     * @param ops the ops to delegate to
     */
    public static <T> ContextOps<T> create(DynamicOps<T> ops) {
        return ops instanceof ContextOps<T> cops ? cops : new ContextOps<>(ops);
    }

    /**
     * {@return the context object with the given key, or {@code null} if context of that type is not present}
     *
     * @param key the key of the context object to retrieve
     * @param <Z> the type of the context object
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <Z> Z getContext(Key<Z> key) {
        return (Z) contextObjects.get(key);
    }

    /**
     * {@return a new context ops instance that has the given {@code value} as additional context}
     *
     * @param key   the key of the additional context object
     * @param value an additional context object to add to the new ops instance
     * @param <Z>   the type of the context object
     */
    public <Z> ContextOps<T> withContext(Key<Z> key, Z value) {
        return this.withContext(Map.of(key, value));
    }

    /**
     * {@return a new context ops instance that has the given {@code contextMap} as additional context}
     *
     * @param contextMap additional context to add to the new context ops
     */
    public ContextOps<T> withContext(Map<Key<?>, Object> contextMap) {
        return contextMap.isEmpty() ? this : new ContextOps<>(this.delegate, contextMap);
    }

    @Override
    public String toString() {
        return "ContextOps[delegate=" + delegate + ", context=" + contextObjects + "]";
    }

    /**
     * Creates a {@linkplain MapCodec} instance that retrieves the context of the given {@code type}
     * from the ops that it decodes with, {@linkplain DataResult#error(Supplier, Object) erroring} if
     * context of the given type is not provided.
     * <p>
     * Example method of retrieving context from a given {@code ops}:
     * {@snippet :
     * DynamicOps<?> ops;
     * retrieveContext(SOME_KEY).decode(ops, ops.emptyMap())
     * }
     * <p>
     * This codec can also be used as a field in a {@link RecordCodecBuilder}.
     *
     * @param type the key of the context object to retrieve
     * @param <T>  the type of the context object to retrieve
     */
    public static <T> MapCodec<T> retrieveContext(Key<T> type) {
        return ExtraCodecs.retrieveContext(ops -> {
            if (!(ops instanceof ContextOps<?> contextOps)) {
                return DataResult.error(() -> "Dynamic ops " + ops + " is not context-aware");
            }

            var value = contextOps.getContext(type);
            if (value == null) {
                return DataResult.error(() -> "Context ops " + ops + " does not have context with ID " + type.identifier() + ". Available context: " + contextOps.contextObjects.keySet());
            }

            return DataResult.success(value);
        });
    }

    /**
     * Creates a {@linkplain MapCodec} instance that retrieves the context of the given {@code type}
     * from the ops that it decodes with, or an {@linkplain Optional#empty() empty optional} if
     * context of the given type is not provided.
     * <p>
     * Example method of retrieving context from a given {@code ops}:
     * {@snippet :
     * DynamicOps<?> ops;
     * retrieveOptionalContext(SOME_KEY).decode(ops, ops.emptyMap())
     * }
     * <p>
     * This codec can also be used as a field in a {@link RecordCodecBuilder}.
     *
     * @param type the key of the context object to retrieve
     * @param <T>  the type of the context object to retrieve
     */
    public static <T> MapCodec<Optional<T>> retrieveOptionalContext(Key<T> type) {
        return ExtraCodecs.retrieveContext(ops -> {
            if (!(ops instanceof ContextOps<?> contextOps)) {
                return DataResult.success(Optional.empty());
            }

            var value = contextOps.getContext(type);
            if (value == null) {
                return DataResult.success(Optional.empty());
            }

            return DataResult.success(Optional.of(value));
        });
    }

    /**
     * Creates a {@linkplain MapCodec} instance that retrieves the context of the given {@code type}
     * from the ops that it decodes with, or the given {@code fallback} if
     * context of the given type is not provided.
     * <p>
     * Example method of retrieving context from a given {@code ops}:
     * {@snippet :
     * DynamicOps<?> ops;
     * retrieveOptionalContext(SOME_KEY, fallback).decode(ops, ops.emptyMap())
     * }
     * <p>
     * This codec can also be used as a field in a {@link RecordCodecBuilder}.
     *
     * @param type     the key of the context object to retrieve
     * @param fallback the object to return as context if the ops does not have context of the given {@code type} provided
     * @param <T>      the type of the context object to retrieve
     */
    public static <T> MapCodec<T> retrieveOptionalContext(Key<T> type, T fallback) {
        return ExtraCodecs.retrieveContext(ops -> {
            if (!(ops instanceof ContextOps<?> contextOps)) {
                return DataResult.success(fallback);
            }

            var value = contextOps.getContext(type);
            if (value == null) {
                return DataResult.success(fallback);
            }

            return DataResult.success(value);
        });
    }

    /**
     * A key for context objects stored within a {@link ContextOps}, that can be retrieved
     * from the ops to aid in building context-aware {@linkplain Codec}s.
     *
     * @param identifier the identifier of the context object this key is for
     * @param <T>        the generic type of the context object
     */
    public record Key<T>(ResourceLocation identifier) {
        @Override
        public String toString() {
            return identifier.toString();
        }
    }
}
