/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Per-listener negotiation handle. Reads the immutable {@link TooltipDocument.Snapshot} and submits declarative
/// intents. All listeners see the <em>same</em> snapshot; intents are collected and resolved by the
/// {@code TooltipArbitrator} only after every listener returns, so no listener can change what another sees.
///
/// Common calls are one-to-three lines:
/// ```java
/// NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, event -> {
///     var t = event.tooltip("mymod");
///     t.addAfter(TooltipTags.damage(), energy);
///     t.remove(TooltipTags.lore());
///     t.replace(TooltipTags.damage(), customDamage);
///     t.prefer(TooltipTags.MOD_NAME, "jade");
///     t.add(energy).after(TooltipTags.damage()).before(TooltipTags.attributes());
/// });
/// ```
public final class TooltipNegotiation {
    private final TooltipDocument.Snapshot snapshot;
    private final String providerId;
    private final List<TooltipIntent> intents = new ArrayList<>();
    private final List<Edit> pending = new ArrayList<>();
    @Nullable
    private final LongSupplier sharedOrdinals;
    private long counter;

    @ApiStatus.Internal
    public TooltipNegotiation(TooltipDocument.Snapshot snapshot, String providerId) {
        this(snapshot, providerId, null);
    }

    /// @param sharedOrdinals event-wide ordinal source, so intents from multiple listeners of the same provider
    ///                       never collide on {@code (priority, providerId, ordinal)}
    @ApiStatus.Internal
    public TooltipNegotiation(TooltipDocument.Snapshot snapshot, String providerId, @Nullable LongSupplier sharedOrdinals) {
        this.snapshot = snapshot;
        this.providerId = providerId;
        this.sharedOrdinals = sharedOrdinals;
    }

    public TooltipDocument.Snapshot snapshot() {
        return snapshot;
    }

    public String providerId() {
        return providerId;
    }

    /// Add a line; default placement is the document tail. Fluent for {@code .after/.before/.priority/.orElse}.
    public Edit add(Component content) {
        return edit(Edit.Kind.ADD).content(content);
    }

    public Edit addBefore(TooltipTag<?, ?> target, Component content) {
        return add(content).before(target);
    }

    public Edit addAfter(TooltipTag<?, ?> target, Component content) {
        return add(content).after(target);
    }

    /// Remove the single node matching {@code target} (exact-match).
    public Edit remove(TooltipTag<?, ?> target) {
        return edit(Edit.Kind.REMOVE).target(target);
    }

    /// Remove every node matching {@code target}.
    public Edit removeAll(TooltipTag<?, ?> target) {
        return remove(target).selector(TooltipIntent.Selector.ALL);
    }

    /// Replace the single node matching {@code target} with {@code replacement} (exact-match).
    public Edit replace(TooltipTag<?, ?> target, Component... replacement) {
        return edit(Edit.Kind.REPLACE).target(target).content(replacement);
    }

    /// Vote for {@code provider} in the negotiated channel {@code tag} (candidate-resolution input).
    ///
    /// @throws IllegalArgumentException if {@code tag} is not a negotiated channel (a programming error)
    public Edit prefer(TooltipTag<?, ?> tag, String provider) {
        if (!tag.isNegotiated()) {
            throw new IllegalArgumentException("prefer() requires a negotiated channel tag, got plain tag " + tag);
        }
        return edit(Edit.Kind.PREFER).prefer(tag, provider);
    }

    /// Deep-API selector over a tag's matches.
    public Match match(TooltipTag<?, ?> tag) {
        return new Match(this, tag);
    }

    void addIntent(TooltipIntent intent) {
        intents.add(intent);
    }

    long nextOrdinal() {
        return sharedOrdinals != null ? sharedOrdinals.getAsLong() : counter++;
    }

    private Edit edit(Edit.Kind kind) {
        Edit edit = new Edit(this, kind, nextOrdinal());
        pending.add(edit);
        return edit;
    }

    /// Commit any pending fluent edits and return the frozen intent list. Called by the event after the listener returns.
    @ApiStatus.Internal
    public List<TooltipIntent> collectIntents() {
        for (Edit edit : pending) {
            edit.commit();
        }
        pending.clear();
        return List.copyOf(intents);
    }

    /// Fluent handle for a single pending intent, returned by {@link TooltipNegotiation#add} and friends.
    /// Configure with {@code before/after/priority/orElse}; it auto-commits when the listener returns.
    ///
    /// <b>Intent-unit atomicity</b>: if any ordering constraint on an {@code add} fails to resolve (0 matches with
    /// no fallback, or &gt;1), the <em>entire</em> unit is dropped and a single diagnostic is emitted &mdash; no
    /// partial application.
    public static final class Edit {
        enum Kind {
            ADD,
            REMOVE,
            REPLACE,
            PREFER
        }

        private final TooltipNegotiation negotiation;
        private final Kind kind;
        private final long ordinal;

        private List<Component> content = List.of();
        @Nullable
        private TooltipTag<?, ?> target;
        @Nullable
        private TooltipTag<?, ?> after;
        @Nullable
        private TooltipTag<?, ?> before;
        private Fallback fallback = Fallback.NONE;
        @Nullable
        private TooltipTag<?, ?> fallbackTag;
        private boolean fallbackAfter;
        private int priority;
        private TooltipIntent.Selector selector = TooltipIntent.Selector.EXACT;
        @Nullable
        private TooltipTag<?, ?> preferTag;
        @Nullable
        private String preferProvider;

        Edit(TooltipNegotiation negotiation, Kind kind, long ordinal) {
            this.negotiation = negotiation;
            this.kind = kind;
            this.ordinal = ordinal;
        }

        public Edit before(TooltipTag<?, ?> tag) {
            this.before = tag;
            return this;
        }

        public Edit after(TooltipTag<?, ?> tag) {
            this.after = tag;
            return this;
        }

        public Edit priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Edit orElse(Fallback fallback) {
            this.fallback = fallback;
            return this;
        }

        public Edit orElseAfter(TooltipTag<?, ?> tag) {
            this.fallbackTag = tag;
            this.fallbackAfter = true;
            return this;
        }

        public Edit orElseBefore(TooltipTag<?, ?> tag) {
            this.fallbackTag = tag;
            this.fallbackAfter = false;
            return this;
        }

        Edit selector(TooltipIntent.Selector selector) {
            this.selector = selector;
            return this;
        }

        Edit content(Component... components) {
            this.content = List.of(components);
            return this;
        }

        Edit target(TooltipTag<?, ?> target) {
            this.target = target;
            return this;
        }

        Edit prefer(TooltipTag<?, ?> tag, String provider) {
            this.preferTag = tag;
            this.preferProvider = provider;
            return this;
        }

        void commit() {
            if (target == null && (kind == Kind.REMOVE || kind == Kind.REPLACE)) {
                throw new IllegalStateException(kind + " edit without a target");
            }
            TooltipIntent.Key key = new TooltipIntent.Key(priority, negotiation.providerId(), ordinal);
            switch (kind) {
                case ADD -> negotiation.addIntent(new TooltipIntent.Add(key, content, after, before, fallback, fallbackTag, fallbackAfter));
                case REMOVE -> negotiation.addIntent(new TooltipIntent.Remove(key, target, selector));
                case REPLACE -> negotiation.addIntent(new TooltipIntent.Replace(key, target, selector, content));
                case PREFER -> {
                    if (preferTag != null && preferProvider != null) {
                        negotiation.addIntent(new TooltipIntent.Prefer(key, preferTag.id(), preferProvider));
                    }
                }
            }
        }
    }

    /// Deep-API selector over a tag's matches, returned by {@link TooltipNegotiation#match}. Deliberately a
    /// separate type so {@code first/last/all} cannot leak into the terse one-liner surface. Select a subset, then
    /// call {@link #remove()} or {@link #replace(Component...)}. Default selection is exact (one match).
    public static final class Match {
        private final TooltipNegotiation negotiation;
        private final TooltipTag<?, ?> tag;
        private TooltipIntent.Selector selector = TooltipIntent.Selector.EXACT;

        Match(TooltipNegotiation negotiation, TooltipTag<?, ?> tag) {
            this.negotiation = negotiation;
            this.tag = tag;
        }

        public int count() {
            return negotiation.snapshot().count(tag);
        }

        public Match first() {
            this.selector = TooltipIntent.Selector.FIRST;
            return this;
        }

        public Match last() {
            this.selector = TooltipIntent.Selector.LAST;
            return this;
        }

        public Match all() {
            this.selector = TooltipIntent.Selector.ALL;
            return this;
        }

        public void remove() {
            negotiation.remove(tag).selector(selector);
        }

        public void replace(Component... replacement) {
            negotiation.replace(tag, replacement).selector(selector);
        }
    }

    /// Fallback placement for an {@code add} whose anchor was not found. {@link #NONE} (default) means the intent
    /// is a no-op when the target is missing &mdash; there is <strong>never</strong> an implicit tail-append. Use
    /// {@link Edit#orElseAfter} for a value-carrying fallback relative to another tag. {@link #TAIL} places at the
    /// tail of the tooltip, {@link #HEAD} at the head.
    public enum Fallback {
        NONE,
        TAIL,
        HEAD
    }
}
