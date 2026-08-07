/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

/// Per-listener negotiation handle. Reads the immutable {@link TooltipSnapshot} and submits declarative intents.
/// All listeners see the <em>same</em> snapshot; intents are collected and resolved by the {@code TooltipArbitrator} only
/// after every listener returns, so no listener can change what another sees.
///
/// Common calls are one-to-three lines:
/// ```java
/// var t = event.tooltip();
/// t.addAfter(VanillaTooltipTags.damage(), energy);
/// t.remove(VanillaTooltipTags.lore());
/// t.replace(VanillaTooltipTags.damage(), customDamage);
/// t.prefer(CommonTooltipTags.MOD_NAME, "jade");
/// t.add(energy).after(VanillaTooltipTags.damage()).before(VanillaTooltipTags.attributes());
/// ```
public final class TooltipNegotiation {
    private final TooltipSnapshot snapshot;
    private final String providerId;
    private final List<TooltipIntent> intents = new ArrayList<>();
    private final List<TooltipEdit> pending = new ArrayList<>();
    private long counter;

    @ApiStatus.Internal
    public TooltipNegotiation(TooltipSnapshot snapshot, String providerId) {
        this.snapshot = snapshot;
        this.providerId = providerId;
    }

    public TooltipSnapshot snapshot() {
        return snapshot;
    }

    public String providerId() {
        return providerId;
    }

    /// Add a line; default placement is the document tail. Fluent for {@code .after/.before/.priority/.orElse}.
    public TooltipEdit add(Component content) {
        return edit(TooltipEdit.Kind.ADD).content(content);
    }

    public TooltipEdit addBefore(TooltipTag<?, ?> target, Component content) {
        return add(content).before(target);
    }

    public TooltipEdit addAfter(TooltipTag<?, ?> target, Component content) {
        return add(content).after(target);
    }

    /// Remove the single node matching {@code target} (exact-match).
    public TooltipEdit remove(TooltipTag<?, ?> target) {
        return edit(TooltipEdit.Kind.REMOVE).target(target);
    }

    /// Remove every node matching {@code target}.
    public TooltipEdit removeAll(TooltipTag<?, ?> target) {
        return remove(target).selector(TooltipIntent.Selector.ALL);
    }

    /// Replace the single node matching {@code target} with {@code replacement} (exact-match).
    public TooltipEdit replace(TooltipTag<?, ?> target, Component... replacement) {
        return edit(TooltipEdit.Kind.REPLACE).target(target).content(replacement);
    }

    /// Vote for {@code provider} in the negotiated channel {@code tag} (candidate-resolution input).
    public TooltipEdit prefer(TooltipTagType.Negotiated<?, ?> tag, String provider) {
        return edit(TooltipEdit.Kind.PREFER).prefer(tag, provider);
    }

    /// Deep-API selector over a tag's matches.
    public TooltipMatch match(TooltipTag<?, ?> tag) {
        return new TooltipMatch(this, tag);
    }

    // ---- internal: intent collection -------------------------------------------------

    void addIntent(TooltipIntent intent) {
        intents.add(intent);
    }

    long nextOrdinal() {
        return counter++;
    }

    private TooltipEdit edit(TooltipEdit.Kind kind) {
        TooltipEdit edit = new TooltipEdit(this, kind, nextOrdinal());
        pending.add(edit);
        return edit;
    }

    /// Commit any pending fluent edits and return the frozen intent list. Called by the event after the listener returns.
    @ApiStatus.Internal
    public List<TooltipIntent> collectIntents() {
        for (TooltipEdit edit : pending) {
            edit.commit();
        }
        pending.clear();
        return List.copyOf(intents);
    }
}
