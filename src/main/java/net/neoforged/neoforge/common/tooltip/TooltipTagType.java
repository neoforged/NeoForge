/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

/// Type-safe identity for a {@link TooltipTag}. Two flavours, both nested here:
/// {@link Plain} (locate only, all matches coexist) and {@link Negotiated} (a negotiation channel resolved by a
/// {@link TooltipResolver}).
///
/// Tag equality is based on {@code (id, nodeType, value)}, never object identity. Built-in tags register
/// themselves via {@link #register}; re-registering an identical type is idempotent, but the same id with a
/// differing kind/node-type/resolver is rejected.
public sealed interface TooltipTagType<N extends TooltipNode, V> {
    Identifier id();

    Class<N> nodeType();

    @ApiStatus.Internal
    ConcurrentHashMap<Identifier, TooltipTagType<?, ?>> REGISTRY = new ConcurrentHashMap<>();

    /// Register a tag type. Idempotent for identical types; rejects id collisions with a differing type.
    ///
    /// This doubles as the convention-channel mechanism, mirroring how `c:` tags work: any mod may create and
    /// register a channel under a shared id (e.g. `c:mod_name`). A convention only holds while every participant
    /// registers the <em>same</em> resolver instance &mdash; the built-in resolvers (e.g.
    /// {@link TooltipResolver#chooseOne()}) are singletons, so independent mods get the same instance for free;
    /// custom resolvers must come from a shared library singleton. Re-registering the same id with a different
    /// node type or a different resolver instance throws, locking the convention's semantics to the first
    /// declaration instead of silently letting two incompatible meanings share one id. Mods that never register
    /// (e.g. they only attach tags from a library) still participate, governed by whoever did register.
    static void register(TooltipTagType<?, ?> type) {
        var existing = REGISTRY.putIfAbsent(type.id(), type);
        if (existing != null && !existing.equals(type)) {
            throw new IllegalStateException("Tooltip tag id collision: '" + type.id() + "': existing " + existing + " vs " + type);
        }
    }

    /// A plain tag: locates nodes; all matches coexist.
    record Plain<N extends TooltipNode, V>(Identifier id, Class<N> nodeType) implements TooltipTagType<N, V> {
        public static <N extends TooltipNode, V> Plain<N, V> create(Identifier id, Class<N> nodeType) {
            return new Plain<>(id, nodeType);
        }

        public TooltipTag<N, V> tag(V value) {
            return new TooltipTag<>(this, value);
        }
    }

    /// A negotiated tag: the candidate set of all nodes carrying it is resolved by {@link #resolver()}; at most one per node.
    record Negotiated<N extends TooltipNode, V>(
            Identifier id,
            Class<N> nodeType,
            TooltipResolver resolver
    ) implements TooltipTagType<N, V> {
        public static <N extends TooltipNode, V> Negotiated<N, V> create(Identifier id, Class<N> nodeType, TooltipResolver resolver) {
            return new Negotiated<>(id, nodeType, resolver);
        }

        public TooltipTag<N, V> tag(V value) {
            return new TooltipTag<>(this, value);
        }

        /// Two negotiated types are identical only if they share the same resolver <em>instance</em> &mdash; same
        /// class is not enough, since two instances of one class can carry opposite semantics (e.g. "keep the
        /// first" vs "keep the last"). This is what makes a convention channel trustworthy: its meaning is
        /// defined by one shared singleton, never by whoever happens to come first.
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Negotiated<?, ?>(Identifier id1, Class<?> type, TooltipResolver resolver1))) return false;
            return id.equals(id1) && nodeType.equals(type) && resolver == resolver1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, nodeType, System.identityHashCode(resolver));
        }
    }
}
