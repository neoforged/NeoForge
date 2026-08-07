/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.tooltip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.tooltip.TooltipArbitrator;
import net.neoforged.neoforge.common.tooltip.CommonTooltipTags;
import net.neoforged.neoforge.common.tooltip.TooltipIntent;
import net.neoforged.neoforge.common.tooltip.TooltipDocument;
import net.neoforged.neoforge.common.tooltip.TooltipEntry;
import net.neoforged.neoforge.common.tooltip.TooltipFallback;
import net.neoforged.neoforge.common.tooltip.TooltipGroup;
import net.neoforged.neoforge.common.tooltip.TooltipNegotiation;
import net.neoforged.neoforge.common.tooltip.TooltipNode;
import net.neoforged.neoforge.common.tooltip.TooltipResolver;
import net.neoforged.neoforge.common.tooltip.TooltipSnapshot;
import net.neoforged.neoforge.common.tooltip.TooltipTag;
import net.neoforged.neoforge.common.tooltip.TooltipTagType;
import net.neoforged.neoforge.common.tooltip.VanillaTooltipTags;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class TooltipNegotiationTest {
    private static final TooltipTagType.Plain<TooltipEntry, Void> DAMAGE = TooltipTagType.Plain.<TooltipEntry, Void>create(Identifier.fromNamespaceAndPath("test", "damage"), TooltipEntry.class);
    private static final TooltipTagType.Plain<TooltipEntry, Void> ARMOR = TooltipTagType.Plain.<TooltipEntry, Void>create(Identifier.fromNamespaceAndPath("test", "armor"), TooltipEntry.class);
    private static final TooltipTagType.Plain<TooltipEntry, Void> DUPE = TooltipTagType.Plain.<TooltipEntry, Void>create(Identifier.fromNamespaceAndPath("test", "dupe"), TooltipEntry.class);

    private static TooltipTag<TooltipEntry, Void> damage() {
        return DAMAGE.tag(null);
    }

    private static TooltipTag<TooltipEntry, Void> armor() {
        return ARMOR.tag(null);
    }

    /// Build a document from (provider, text, tags...) tuples, one auto source group each.
    private static TooltipSnapshot build(Row... rows) {
        TooltipDocument doc = new TooltipDocument();
        for (Row row : rows) {
            var out = doc.newOutput(row.provider);
            // Simulate the lifecycle bridge stamping the auto source-group identity tag.
            out.sourceTag(VanillaTooltipTags.appender(Identifier.fromNamespaceAndPath(row.provider, "a")));
            var taggable = out.add(row.text);
            for (TooltipTag<?, ?> tag : row.tags) {
                taggable.tag(tag);
            }
            doc.addSourceGroup(out);
        }
        return doc.freeze();
    }

    private record Row(String provider, Component text, List<TooltipTag<?, ?>> tags) {
    }

    private static Row row(String provider, String text, TooltipTag<?, ?>... tags) {
        return new Row(provider, Component.literal(text), List.of(tags));
    }

    private static List<String> texts(TooltipSnapshot snapshot) {
        List<String> out = new ArrayList<>();
        for (TooltipEntry entry : snapshot.flatten()) {
            out.add(entry.component().getString());
        }
        return out;
    }

    private static List<String> resolveTexts(TooltipSnapshot snapshot, Consumer<TooltipNegotiation> listener, String provider) {
        return resolveTexts(snapshot, listener, provider, message -> {});
    }

    private static List<String> resolveTexts(TooltipSnapshot snapshot, Consumer<TooltipNegotiation> listener, String provider, Consumer<String> diagnostics) {
        TooltipNegotiation negotiation = new TooltipNegotiation(snapshot, provider);
        listener.accept(negotiation);
        List<TooltipGroup> resolved = TooltipArbitrator.resolve(snapshot, negotiation.collectIntents(), diagnostics);
        return texts(TooltipSnapshot.of(resolved));
    }

    private static List<String> resolveTwo(TooltipSnapshot snapshot, Consumer<TooltipNegotiation> a, Consumer<TooltipNegotiation> b) {
        var negA = new TooltipNegotiation(snapshot, "modA");
        a.accept(negA);
        var negB = new TooltipNegotiation(snapshot, "modB");
        b.accept(negB);
        List<TooltipIntent> intents = new ArrayList<>();
        intents.addAll(negA.collectIntents());
        intents.addAll(negB.collectIntents());
        var resolved = TooltipArbitrator.resolve(snapshot, intents, message -> {});
        return texts(TooltipSnapshot.of(resolved));
    }

    @Test
    void testNoIntentsPreservesOrder() {
        var snapshot = build(
                row("minecraft", "Sword"),
                row("minecraft", "Damage: 5", damage()),
                row("minecraft", "Armor: 0", armor()));
        var result = resolveTexts(snapshot, n -> {}, "mymod");
        assertEquals(List.of("Sword", "Damage: 5", "Armor: 0"), result);
    }

    @Test
    void testRemoveSingle() {
        var snapshot = build(
                row("minecraft", "Sword"),
                row("minecraft", "Damage: 5", damage()),
                row("minecraft", "Armor: 0", armor()));
        var result = resolveTexts(snapshot, n -> n.remove(damage()), "mymod");
        assertEquals(List.of("Sword", "Armor: 0"), result);
    }

    @Test
    void testExactMatchAmbiguousDropsIntent() {
        var snapshot = build(
                row("minecraft", "a", DUPE.tag(null)),
                row("minecraft", "b", DUPE.tag(null)));
        List<String> diagnostics = new ArrayList<>();
        var result = resolveTexts(snapshot, n -> n.remove(DUPE.tag(null)), "mymod", diagnostics::add);
        // Ambiguous: nothing removed, diagnostic emitted.
        assertEquals(List.of("a", "b"), result);
        assertFalse(diagnostics.isEmpty(), "Expected an ambiguity diagnostic");
    }

    @Test
    void testRemoveAll() {
        var snapshot = build(
                row("minecraft", "a", DUPE.tag(null)),
                row("minecraft", "b", DUPE.tag(null)));
        var result = resolveTexts(snapshot, n -> n.removeAll(DUPE.tag(null)), "mymod");
        assertEquals(List.of(), result);
    }

    @Test
    void testReplaceSingle() {
        var snapshot = build(row("minecraft", "Damage: 5", damage()));
        var result = resolveTexts(snapshot, n -> n.replace(damage(), Component.literal("Durability: 99")), "mymod");
        assertEquals(List.of("Durability: 99"), result);
    }

    @Test
    void testRemoveVsReplacePriority() {
        var snapshot = build(row("minecraft", "Damage: 5", damage()));
        // replace wins on higher priority.
        var replaced = resolveTwo(snapshot,
                n -> n.remove(damage()).priority(0),
                n -> n.replace(damage(), Component.literal("Bar")).priority(1));
        assertEquals(List.of("Bar"), replaced);
        // remove wins when it has higher priority.
        var removed = resolveTwo(snapshot,
                n -> n.remove(damage()).priority(1),
                n -> n.replace(damage(), Component.literal("Bar")).priority(0));
        assertEquals(List.of(), removed);
    }

    @Test
    void testChooseOneModNameNoPreference() {
        var snapshot = build(
                row("jade", "Jade", CommonTooltipTags.modName("jade")),
                row("theoneprobe", "The One Probe", CommonTooltipTags.modName("theoneprobe")));
        // No preference -> first in document order survives.
        var result = resolveTexts(snapshot, n -> {}, "mymod");
        assertEquals(List.of("Jade"), result);
    }

    @Test
    void testChooseOneModNameWithPreference() {
        var snapshot = build(
                row("jade", "Jade", CommonTooltipTags.modName("jade")),
                row("theoneprobe", "The One Probe", CommonTooltipTags.modName("theoneprobe")));
        var result = resolveTexts(snapshot, n -> n.prefer(CommonTooltipTags.MOD_NAME, "theoneprobe"), "configmod");
        assertEquals(List.of("The One Probe"), result);
    }

    @Test
    void testListenerOrderIndependence() {
        var snapshot = build(
                row("jade", "Jade", CommonTooltipTags.modName("jade")),
                row("theoneprobe", "The One Probe", CommonTooltipTags.modName("theoneprobe")));
        // Two equal-priority preferences for different candidates; result must be identical regardless of order.
        var first = resolveTwoOrder(snapshot, "aaa", "zzz");
        var second = resolveTwoOrder(snapshot, "zzz", "aaa");
        assertEquals(first, second);
        assertEquals(List.of("Jade"), first); // vote from lexicographically smaller provider wins the tie
    }

    private static List<String> resolveTwoOrder(TooltipSnapshot snapshot, String voterA, String voterB) {
        // voterA prefers jade, voterB prefers theoneprobe, same priority -> tie broken by voter providerId.
        var snapA = new TooltipNegotiation(snapshot, voterA);
        snapA.prefer(CommonTooltipTags.MOD_NAME, "jade");
        var snapB = new TooltipNegotiation(snapshot, voterB);
        snapB.prefer(CommonTooltipTags.MOD_NAME, "theoneprobe");
        List<TooltipIntent> intents = new ArrayList<>();
        intents.addAll(snapA.collectIntents());
        intents.addAll(snapB.collectIntents());
        var resolved = TooltipArbitrator.resolve(snapshot, intents, message -> {});
        return texts(TooltipSnapshot.of(resolved));
    }

    @Test
    void testAddAfterAndBefore() {
        var snapshot = build(
                row("minecraft", "Damage: 5", damage()),
                row("minecraft", "Armor: 0", armor()));
        var result = resolveTexts(snapshot, n -> n.add(Component.literal("Energy: 100")).after(damage()).before(armor()), "energymod");
        assertEquals(List.of("Damage: 5", "Energy: 100", "Armor: 0"), result);
    }

    @Test
    void testAddDefaultGoesToTail() {
        var snapshot = build(
                row("minecraft", "Damage: 5", damage()),
                row("minecraft", "Armor: 0", armor()));
        var result = resolveTexts(snapshot, n -> n.add(Component.literal("Extra")), "mymod");
        assertEquals(List.of("Damage: 5", "Armor: 0", "Extra"), result);
    }

    @Test
    void testAddUnresolvedAnchorDropsIntent() {
        var snapshot = build(row("minecraft", "Damage: 5", damage()));
        List<String> diagnostics = new ArrayList<>();
        // The armor anchor does not exist and no fallback was given: the add must not silently append to the tail.
        var result = resolveTexts(snapshot, n -> n.add(Component.literal("Extra")).after(armor()), "mymod", diagnostics::add);
        assertEquals(List.of("Damage: 5"), result);
        assertFalse(diagnostics.isEmpty(), "Expected a dropped-intent diagnostic");
    }

    @Test
    void testAddUnresolvedAnchorWithTailFallback() {
        var snapshot = build(row("minecraft", "Damage: 5", damage()));
        var result = resolveTexts(snapshot,
                n -> n.add(Component.literal("Extra")).after(armor()).orElse(TooltipFallback.TAIL),
                "mymod");
        assertEquals(List.of("Damage: 5", "Extra"), result);
    }

    @Test
    void testOrderingIsDeterministicAndDoesNotCrash() {
        var snapshot = build(
                row("minecraft", "A", DAMAGE.tag(null)),
                row("minecraft", "B", ARMOR.tag(null)));
        // Two independent adds anchored on existing groups; result must be stable and contain all lines.
        var negA = new TooltipNegotiation(snapshot, "modA");
        negA.add(Component.literal("A'")).after(armor());
        var negB = new TooltipNegotiation(snapshot, "modB");
        negB.add(Component.literal("B'")).after(damage());
        List<TooltipIntent> intents = new ArrayList<>();
        intents.addAll(negA.collectIntents());
        intents.addAll(negB.collectIntents());
        var resolved = TooltipArbitrator.resolve(snapshot, intents, message -> {});
        var result = texts(TooltipSnapshot.of(resolved));
        assertEquals(4, result.size(), "All four lines present: " + result);
        assertTrue(result.containsAll(List.of("A", "B", "A'", "B'")));
        // Re-running with swapped submission order yields the same result (order-independent).
        var swapped = new ArrayList<TooltipIntent>();
        swapped.addAll(negB.collectIntents());
        swapped.addAll(negA.collectIntents());
        var resolved2 = TooltipArbitrator.resolve(snapshot, swapped, message -> {});
        assertEquals(result, texts(TooltipSnapshot.of(resolved2)));
    }

    @Test
    void testContradictoryAnchorsResolveWithoutCrash() {
        var snapshot = build(
                row("minecraft", "A", DAMAGE.tag(null)), // group 0
                row("minecraft", "B", ARMOR.tag(null))); // group 1
        // after(B-group) AND before(A-group): impossible placement (A precedes B). Must not crash; resolves deterministically.
        var result = resolveTexts(snapshot, n -> n.add(Component.literal("Z")).after(armor()).before(damage()), "mymod");
        assertEquals(3, result.size(), "Z inserted, no lines lost: " + result);
        assertTrue(result.contains("Z"));
    }

    @Test
    void testSnapshotImmutabilityAfterIntents() {
        var snapshot = build(
                row("minecraft", "Damage: 5", damage()),
                row("minecraft", "Armor: 0", armor()));
        var before = texts(snapshot);
        resolveTexts(snapshot, n -> {
            n.remove(damage());
            n.replace(armor(), Component.literal("X"));
            n.add(Component.literal("Y"));
        }, "mymod");
        assertEquals(before, texts(snapshot), "Frozen snapshot must not be mutated by intents");
    }

    @Test
    void testSecondNegotiatedTagRejected() {
        var doc = new TooltipDocument();
        var out = doc.newOutput("mymod");
        var taggable = out.add(Component.literal("Mod Name"));
        taggable.tag(CommonTooltipTags.modName("mymod"));
        var otherChannel = TooltipTagType.Negotiated.<TooltipEntry, String>create(
                Identifier.fromNamespaceAndPath("test", "other_channel"), TooltipEntry.class, TooltipResolver.keepAll());
        assertThrows(IllegalStateException.class, () -> taggable.tag(otherChannel.tag("mymod")));
    }

    @Test
    void testConventionReRegistrationWithSameResolverSingletonIsIdempotent() {
        // Two mods declaring the same convention with the shared built-in singleton: silent no-op.
        var id = Identifier.fromNamespaceAndPath("test", "convention_idempotent");
        var first = TooltipTagType.Negotiated.<TooltipEntry, String>create(id, TooltipEntry.class, TooltipResolver.chooseOne());
        TooltipTagType.register(first);
        var second = TooltipTagType.Negotiated.<TooltipEntry, String>create(id, TooltipEntry.class, TooltipResolver.chooseOne());
        TooltipTagType.register(second);
    }

    @Test
    void testConventionRejectsSameClassButDifferentResolverInstance() {
        // Same id, same node type, same resolver CLASS but a different instance: the two declarations could
        // carry opposite semantics, so the second must be rejected instead of silently sharing the id.
        var id = Identifier.fromNamespaceAndPath("test", "convention_instance");
        var first = TooltipTagType.Negotiated.<TooltipEntry, String>create(id, TooltipEntry.class, new KeepFirstResolver());
        TooltipTagType.register(first);
        var second = TooltipTagType.Negotiated.<TooltipEntry, String>create(id, TooltipEntry.class, new KeepFirstResolver());
        assertThrows(IllegalStateException.class, () -> TooltipTagType.register(second));
    }

    /** A configurable-style resolver implementation, instantiated independently to simulate per-mod declarations. */
    private static final class KeepFirstResolver implements TooltipResolver {
        @Override
        public List<TooltipNode> resolve(TooltipTagType.Negotiated<?, ?> tag, List<TooltipNode> candidates, TooltipResolver.Context context) {
            return List.of(candidates.get(0));
        }
    }

    @Test
    void testAutoSourceGroupAddressableWithoutAuthorTags() {
        // An entry with NO author tag still lives in an auto source group addressable by appender id.
        var snapshot = build(row("mymod", "Energy: 50"));
        // Whole-group remove via the appender source tag.
        var result = resolveTexts(snapshot,
                n -> n.remove(VanillaTooltipTags.appender(Identifier.fromNamespaceAndPath("mymod", "a"))),
                "other");
        assertEquals(List.of(), result);
    }
}
