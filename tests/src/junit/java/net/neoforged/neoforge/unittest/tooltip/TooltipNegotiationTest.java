/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.tooltip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.tooltip.ItemTooltipHandler;
import net.neoforged.neoforge.common.tooltip.TooltipArbitrator;
import net.neoforged.neoforge.common.tooltip.TooltipDocument;
import net.neoforged.neoforge.common.tooltip.TooltipIntent;
import net.neoforged.neoforge.common.tooltip.TooltipNegotiation;
import net.neoforged.neoforge.common.tooltip.TooltipNode;
import net.neoforged.neoforge.common.tooltip.TooltipPipeline;
import net.neoforged.neoforge.common.tooltip.TooltipResolver;
import net.neoforged.neoforge.common.tooltip.TooltipTag;
import net.neoforged.neoforge.common.tooltip.TooltipTags;
import net.neoforged.neoforge.event.TooltipNegotiationEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class TooltipNegotiationTest {
    private static final TooltipTag<TooltipNode.Entry, Void> DAMAGE = TooltipTag.plain(Identifier.fromNamespaceAndPath("test", "damage"), TooltipNode.Entry.class);
    private static final TooltipTag<TooltipNode.Entry, Void> ARMOR = TooltipTag.plain(Identifier.fromNamespaceAndPath("test", "armor"), TooltipNode.Entry.class);
    private static final TooltipTag<TooltipNode.Entry, Void> DUPE = TooltipTag.plain(Identifier.fromNamespaceAndPath("test", "dupe"), TooltipNode.Entry.class);

    private static TooltipTag<TooltipNode.Entry, Void> damage() {
        return DAMAGE;
    }

    private static TooltipTag<TooltipNode.Entry, Void> armor() {
        return ARMOR;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    /// Build a document from (provider, text, tags...) tuples, one auto source group each.
    private static TooltipDocument.Snapshot build(Row... rows) {
        TooltipDocument doc = new TooltipDocument();
        for (Row row : rows) {
            var out = doc.newOutput(row.provider);
            // Simulate the lifecycle bridge stamping the auto source-group identity tag.
            out.sourceTag(TooltipTags.appender(Identifier.fromNamespaceAndPath(row.provider, "a")));
            var taggable = out.add(row.text);
            for (TooltipTag<?, ?> tag : row.tags) {
                taggable.tag(tag);
            }
            doc.addSourceGroup(out);
        }
        return doc.freeze();
    }

    /// Build a document with a single source group containing one nested group holding all rows.
    private static TooltipDocument.Snapshot buildGrouped(Row... rows) {
        TooltipDocument doc = new TooltipDocument();
        var out = doc.newOutput("minecraft");
        out.group(group -> {
            for (Row row : rows) {
                var taggable = group.add(row.text);
                for (TooltipTag<?, ?> tag : row.tags) {
                    taggable.tag(tag);
                }
            }
        });
        doc.addSourceGroup(out);
        return doc.freeze();
    }

    private record Row(String provider, Component text, List<TooltipTag<?, ?>> tags) {}

    private static Row row(String provider, String text, TooltipTag<?, ?>... tags) {
        return new Row(provider, Component.literal(text), List.of(tags));
    }

    private static List<String> texts(TooltipDocument.Snapshot snapshot) {
        List<String> out = new ArrayList<>();
        for (TooltipNode.Entry entry : snapshot.flatten()) {
            out.add(entry.component().getString());
        }
        return out;
    }

    private static List<TooltipNode.Group> resolveGroups(TooltipDocument.Snapshot snapshot, Consumer<TooltipNegotiation> listener, String provider, Consumer<String> diagnostics) {
        TooltipNegotiation negotiation = new TooltipNegotiation(snapshot, provider);
        listener.accept(negotiation);
        return TooltipArbitrator.resolve(snapshot, negotiation.collectIntents(), diagnostics);
    }

    private static List<String> resolveTexts(TooltipDocument.Snapshot snapshot, Consumer<TooltipNegotiation> listener, String provider) {
        return resolveTexts(snapshot, listener, provider, message -> {});
    }

    private static List<String> resolveTexts(TooltipDocument.Snapshot snapshot, Consumer<TooltipNegotiation> listener, String provider, Consumer<String> diagnostics) {
        return texts(TooltipDocument.Snapshot.of(resolveGroups(snapshot, listener, provider, diagnostics)));
    }

    private static List<String> resolveTwo(TooltipDocument.Snapshot snapshot, Consumer<TooltipNegotiation> a, Consumer<TooltipNegotiation> b) {
        var negA = new TooltipNegotiation(snapshot, "modA");
        a.accept(negA);
        var negB = new TooltipNegotiation(snapshot, "modB");
        b.accept(negB);
        List<TooltipIntent> intents = new ArrayList<>();
        intents.addAll(negA.collectIntents());
        intents.addAll(negB.collectIntents());
        var resolved = TooltipArbitrator.resolve(snapshot, intents, message -> {});
        return texts(TooltipDocument.Snapshot.of(resolved));
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
                row("jade", "Jade", TooltipTags.modName("jade")),
                row("theoneprobe", "The One Probe", TooltipTags.modName("theoneprobe")));
        // No preference -> first in document order survives.
        var result = resolveTexts(snapshot, n -> {}, "mymod");
        assertEquals(List.of("Jade"), result);
    }

    @Test
    void testChooseOneModNameWithPreference() {
        var snapshot = build(
                row("jade", "Jade", TooltipTags.modName("jade")),
                row("theoneprobe", "The One Probe", TooltipTags.modName("theoneprobe")));
        var result = resolveTexts(snapshot, n -> n.prefer(TooltipTags.MOD_NAME, "theoneprobe"), "configmod");
        assertEquals(List.of("The One Probe"), result);
    }

    @Test
    void testListenerOrderIndependence() {
        var snapshot = build(
                row("jade", "Jade", TooltipTags.modName("jade")),
                row("theoneprobe", "The One Probe", TooltipTags.modName("theoneprobe")));
        // Two equal-priority preferences for different candidates; result must be identical regardless of order.
        var first = resolveTwoOrder(snapshot, "aaa", "zzz");
        var second = resolveTwoOrder(snapshot, "zzz", "aaa");
        assertEquals(first, second);
        assertEquals(List.of("Jade"), first); // vote from lexicographically smaller provider wins the tie
    }

    private static List<String> resolveTwoOrder(TooltipDocument.Snapshot snapshot, String voterA, String voterB) {
        // voterA prefers jade, voterB prefers theoneprobe, same priority -> tie broken by voter providerId.
        var snapA = new TooltipNegotiation(snapshot, voterA);
        snapA.prefer(TooltipTags.MOD_NAME, "jade");
        var snapB = new TooltipNegotiation(snapshot, voterB);
        snapB.prefer(TooltipTags.MOD_NAME, "theoneprobe");
        List<TooltipIntent> intents = new ArrayList<>();
        intents.addAll(snapA.collectIntents());
        intents.addAll(snapB.collectIntents());
        var resolved = TooltipArbitrator.resolve(snapshot, intents, message -> {});
        return texts(TooltipDocument.Snapshot.of(resolved));
    }

    @Test
    void testAddAfterAndBefore() {
        var snapshot = buildGrouped(
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
                n -> n.add(Component.literal("Extra")).after(armor()).orElse(TooltipNegotiation.Fallback.TAIL),
                "mymod");
        assertEquals(List.of("Damage: 5", "Extra"), result);
    }

    @Test
    void testOrderingIsDeterministicAndDoesNotCrash() {
        var snapshot = build(
                row("minecraft", "A", DAMAGE.tag(null)),
                row("minecraft", "B", ARMOR.tag(null)));
        // Two independent adds anchored on existing entries; result must be stable and contain all lines.
        var negA = new TooltipNegotiation(snapshot, "modA");
        negA.add(Component.literal("A'")).after(armor());
        var negB = new TooltipNegotiation(snapshot, "modB");
        negB.add(Component.literal("B'")).after(damage());
        List<TooltipIntent> intents = new ArrayList<>();
        intents.addAll(negA.collectIntents());
        intents.addAll(negB.collectIntents());
        var resolved = TooltipArbitrator.resolve(snapshot, intents, message -> {});
        var result = texts(TooltipDocument.Snapshot.of(resolved));
        assertEquals(4, result.size(), "All four lines present: " + result);
        assertTrue(result.containsAll(List.of("A", "B", "A'", "B'")));
        // Re-running with swapped submission order yields the same result (order-independent).
        var swapped = new ArrayList<TooltipIntent>();
        swapped.addAll(negB.collectIntents());
        swapped.addAll(negA.collectIntents());
        var resolved2 = TooltipArbitrator.resolve(snapshot, swapped, message -> {});
        assertEquals(result, texts(TooltipDocument.Snapshot.of(resolved2)));
    }

    @Test
    void testContradictoryAnchorsResolveWithoutCrash() {
        var snapshot = buildGrouped(
                row("minecraft", "A", DAMAGE.tag(null)),
                row("minecraft", "B", ARMOR.tag(null)));
        // after(B) AND before(A): impossible placement (A precedes B). Must not crash; resolves deterministically.
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
        taggable.tag(TooltipTags.modName("mymod"));
        var otherChannel = TooltipTag.<TooltipNode.Entry, String>negotiated(
                Identifier.fromNamespaceAndPath("test", "other_channel"), TooltipNode.Entry.class, TooltipResolver.keepAll());
        assertThrows(IllegalStateException.class, () -> taggable.tag(otherChannel.tag("mymod")));
    }

    @Test
    void testAutoSourceGroupAddressableWithoutAuthorTags() {
        // An entry with NO author tag still lives in an auto source group addressable by appender id.
        var snapshot = build(row("mymod", "Energy: 50"));
        // Whole-group remove via the appender source tag.
        var result = resolveTexts(snapshot,
                n -> n.remove(TooltipTags.appender(Identifier.fromNamespaceAndPath("mymod", "a"))),
                "other");
        assertEquals(List.of(), result);
    }

    // ---- Channel adjudication (conflicting declarations never throw; the winner is deterministic) -----------

    @Test
    void testSameResolverIdDifferentInstancesMergesIdempotently() {
        // Two mods declaring the same channel with distinct resolver instances that share a resolver id:
        // declarations merge (idempotent) and the resolver keeps its semantics. No exception.
        var channel = id("adj_same_id");
        var resolverId = id("keep_first_resolver");
        var declA = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepFirstResolver(resolverId));
        var declB = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepFirstResolver(resolverId), 3);
        var snapshot = build(
                row("jade", "Jade", declA.tag("jade")),
                row("top", "TOP", declB.tag("top")));
        var result = resolveTexts(snapshot, n -> {}, "mymod");
        assertEquals(List.of("Jade"), result);
    }

    @Test
    void testDifferentResolverIdsDefaultLexicographicallySmallestWins() {
        // All-default conflicting declarations: the lexicographically smallest resolver id wins, no exception.
        var channel = id("adj_default");
        var declFirst = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepFirstResolver(id("aaa_first")));
        var declLast = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepLastResolver(id("zzz_last")));
        var snapshot = build(
                row("jade", "Jade", declFirst.tag("jade")),
                row("top", "TOP", declLast.tag("top")));
        var result = resolveTexts(snapshot, n -> {}, "mymod");
        assertEquals(List.of("Jade"), result);
    }

    @Test
    void testHigherPriorityWins() {
        var channel = id("adj_priority");
        var declFirst = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepFirstResolver(id("aaa_first")), 0);
        var declLast = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepLastResolver(id("zzz_last")), 1);
        var snapshot = build(
                row("jade", "Jade", declFirst.tag("jade")),
                row("top", "TOP", declLast.tag("top")));
        var result = resolveTexts(snapshot, n -> {}, "mymod");
        assertEquals(List.of("TOP"), result);
    }

    @Test
    void testPreemptsBeatsPriority() {
        // zzz_last preempts aaa_first: explicit constraint beats aaa_first's higher priority.
        var channel = id("adj_preempts");
        var declFirst = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepFirstResolver(id("aaa_first")), 10);
        var declLast = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepLastResolver(id("zzz_last")), 0,
                List.of(id("aaa_first")), List.of());
        var snapshot = build(
                row("jade", "Jade", declFirst.tag("jade")),
                row("top", "TOP", declLast.tag("top")));
        var result = resolveTexts(snapshot, n -> {}, "mymod");
        assertEquals(List.of("TOP"), result);
    }

    @Test
    void testDefersToYields() {
        // aaa_first defers to zzz_last: zzz_last wins despite aaa_first's higher priority.
        var channel = id("adj_defers");
        var declFirst = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepFirstResolver(id("aaa_first")), 10,
                List.of(), List.of(id("zzz_last")));
        var declLast = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepLastResolver(id("zzz_last")), 0);
        var snapshot = build(
                row("jade", "Jade", declFirst.tag("jade")),
                row("top", "TOP", declLast.tag("top")));
        var result = resolveTexts(snapshot, n -> {}, "mymod");
        assertEquals(List.of("TOP"), result);
    }

    @Test
    void testContradictoryConstraintsBreakCycleDeterministically() {
        // Mutual preempts form a cycle; the edge from the least-preferred declaration (larger resolver id on a
        // priority tie) is dropped, so aaa_first wins. Declaration order must not matter.
        var cycleA = adjudicateCycle(id("adj_cycle_a"), true);
        var cycleB = adjudicateCycle(id("adj_cycle_b"), false);
        assertEquals(List.of("Jade"), cycleA);
        assertEquals(cycleA, cycleB, "Declaration order shuffle must not change the winner");
    }

    private static List<String> adjudicateCycle(Identifier channel, boolean firstDeclaresFirst) {
        var resolverFirst = id("aaa_first");
        var resolverLast = id("zzz_last");
        var declFirst = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepFirstResolver(resolverFirst), 0,
                List.of(resolverLast), List.of());
        var declLast = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepLastResolver(resolverLast), 0,
                List.of(resolverFirst), List.of());
        var snapshot = firstDeclaresFirst
                ? build(row("jade", "Jade", declFirst.tag("jade")), row("top", "TOP", declLast.tag("top")))
                : build(row("jade", "Jade", declLast.tag("jade")), row("top", "TOP", declFirst.tag("top")));
        // The channel winner is aaa_first (KeepFirstResolver): the first candidate in document order survives.
        return resolveTexts(snapshot, n -> {}, "mymod");
    }

    @Test
    void testConstraintsPointingAtAbsentResolverIdsAreIgnored() {
        // preempts(nonexistent) contributes no edge; falls back to priority/id ordering.
        var channel = id("adj_absent");
        var declFirst = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepFirstResolver(id("aaa_first")), 0,
                List.of(id("nonexistent_resolver")), List.of());
        var declLast = TooltipTag.<TooltipNode.Entry, String>negotiated(channel, TooltipNode.Entry.class, new KeepLastResolver(id("zzz_last")), 0);
        var snapshot = build(
                row("jade", "Jade", declFirst.tag("jade")),
                row("top", "TOP", declLast.tag("top")));
        var result = resolveTexts(snapshot, n -> {}, "mymod");
        assertEquals(List.of("Jade"), result);
    }

    @Test
    void testDeclarationOrderShuffleKeepsWinner() {
        // Same two resolver ids, same priorities, declared in opposite orders on two channels: same winner.
        var channelA = id("adj_shuffle_a");
        TooltipTag.<TooltipNode.Entry, String>negotiated(channelA, TooltipNode.Entry.class, new KeepFirstResolver(id("aaa_first")), 1);
        TooltipTag.<TooltipNode.Entry, String>negotiated(channelA, TooltipNode.Entry.class, new KeepLastResolver(id("zzz_last")), 0);
        var channelB = id("adj_shuffle_b");
        TooltipTag.<TooltipNode.Entry, String>negotiated(channelB, TooltipNode.Entry.class, new KeepLastResolver(id("zzz_last")), 0);
        TooltipTag.<TooltipNode.Entry, String>negotiated(channelB, TooltipNode.Entry.class, new KeepFirstResolver(id("aaa_first")), 1);

        var declA = TooltipTag.<TooltipNode.Entry, String>negotiated(channelA, TooltipNode.Entry.class, new KeepFirstResolver(id("aaa_first")));
        var declB = TooltipTag.<TooltipNode.Entry, String>negotiated(channelB, TooltipNode.Entry.class, new KeepFirstResolver(id("aaa_first")));
        var resultA = resolveTexts(build(row("jade", "Jade", declA.tag("jade")), row("top", "TOP", declA.tag("top"))), n -> {}, "mymod");
        var resultB = resolveTexts(build(row("jade", "Jade", declB.tag("jade")), row("top", "TOP", declB.tag("top"))), n -> {}, "mymod");
        assertEquals(List.of("Jade"), resultA);
        assertEquals(resultA, resultB);
    }

    // ---- Intra-group ordering (Phase 3) ---------------------------------------------------------------------

    @Test
    void testAddAnchoredInsideGroupInsertsAsGroupChild() {
        // Anchoring at an entry inside a group inserts the add as a child of that group at the anchor position,
        // wrapped in its own source group — no new top-level group is created.
        var snapshot = buildGrouped(
                row("minecraft", "a", damage()),
                row("minecraft", "b"),
                row("minecraft", "c", armor()));
        var resolved = resolveGroups(snapshot, n -> n.add(Component.literal("NEW")).after(damage()), "mymod", message -> {});
        assertEquals(1, resolved.size(), "No new top-level group: " + resolved);
        assertEquals(List.of("a", "NEW", "b", "c"), texts(TooltipDocument.Snapshot.of(resolved)));
    }

    @Test
    void testTwoModsAddAfterSameInternalAnchorOrderStably() {
        // Two mods addAfter the same internal anchor: ordered by (priority, providerId, ordinal), independent of
        // listener submission order. modB has the higher priority, so B lands directly after the anchor.
        var snapshot = buildGrouped(
                row("minecraft", "a", damage()),
                row("minecraft", "b"));
        var first = resolveTwo(snapshot,
                n -> n.add(Component.literal("A")).after(damage()).priority(0),
                n -> n.add(Component.literal("B")).after(damage()).priority(1));
        assertEquals(List.of("a", "B", "A", "b"), first);
        // Swapped listener submission order -> identical result.
        var negA = new TooltipNegotiation(snapshot, "modA");
        negA.add(Component.literal("A")).after(damage()).priority(0);
        var negB = new TooltipNegotiation(snapshot, "modB");
        negB.add(Component.literal("B")).after(damage()).priority(1);
        var swapped = new ArrayList<TooltipIntent>();
        swapped.addAll(negB.collectIntents());
        swapped.addAll(negA.collectIntents());
        var second = texts(TooltipDocument.Snapshot.of(TooltipArbitrator.resolve(snapshot, swapped, message -> {})));
        assertEquals(first, second);
    }

    @Test
    void testIntraGroupContradictoryConstraintsBreakWithoutCrash() {
        // modA: X after(damage) before(armor); modB: Y after(armor) before(damage). The constraints form a cycle
        // inside the group; it must break deterministically without losing any line.
        var snapshot = buildGrouped(
                row("minecraft", "a", damage()),
                row("minecraft", "b", armor()));
        var first = resolveTwo(snapshot,
                n -> n.add(Component.literal("X")).after(damage()).before(armor()),
                n -> n.add(Component.literal("Y")).after(armor()).before(damage()));
        assertEquals(4, first.size(), "No lines lost: " + first);
        assertTrue(first.containsAll(List.of("a", "b", "X", "Y")));
        // Swapped listener submission order -> identical result.
        var negA = new TooltipNegotiation(snapshot, "modA");
        negA.add(Component.literal("X")).after(damage()).before(armor());
        var negB = new TooltipNegotiation(snapshot, "modB");
        negB.add(Component.literal("Y")).after(armor()).before(damage());
        var swapped = new ArrayList<TooltipIntent>();
        swapped.addAll(negB.collectIntents());
        swapped.addAll(negA.collectIntents());
        var second = texts(TooltipDocument.Snapshot.of(TooltipArbitrator.resolve(snapshot, swapped, message -> {})));
        assertEquals(first, second);
    }

    @Test
    void testAnchorsInDifferentGroupsDropWithDiagnostic() {
        // after/before anchors living in different parent groups are irreconcilable: diagnostic + drop (no fallback).
        var snapshot = build(
                row("minecraft", "Damage: 5", damage()),
                row("minecraft", "Armor: 0", armor()));
        List<String> diagnostics = new ArrayList<>();
        var result = resolveTexts(snapshot,
                n -> n.add(Component.literal("Z")).after(damage()).before(armor()),
                "mymod", diagnostics::add);
        assertEquals(List.of("Damage: 5", "Armor: 0"), result);
        assertFalse(diagnostics.isEmpty(), "Expected an irreconcilable/dropped diagnostic");
    }

    @Test
    void testAnchorsInDifferentGroupsWithTailFallback() {
        var snapshot = build(
                row("minecraft", "Damage: 5", damage()),
                row("minecraft", "Armor: 0", armor()));
        List<String> diagnostics = new ArrayList<>();
        var result = resolveTexts(snapshot,
                n -> n.add(Component.literal("Z")).after(damage()).before(armor()).orElse(TooltipNegotiation.Fallback.TAIL),
                "mymod", diagnostics::add);
        assertEquals(List.of("Damage: 5", "Armor: 0", "Z"), result);
        assertFalse(diagnostics.isEmpty(), "Expected an irreconcilable diagnostic");
    }

    @Test
    void testUntouchedChildrenKeepRelativeOrder() {
        var snapshot = buildGrouped(
                row("minecraft", "a"),
                row("minecraft", "b", damage()),
                row("minecraft", "c"),
                row("minecraft", "d"));
        var result = resolveTexts(snapshot, n -> n.add(Component.literal("NEW")).after(damage()), "mymod");
        assertEquals(List.of("a", "b", "NEW", "c", "d"), result);
    }

    @Test
    void testPipelineListenerRemoveItemName() {
        List<Component> lines = new ArrayList<>(List.of(Component.literal("Stone"), Component.literal("Lore line")));
        Consumer<TooltipNegotiationEvent> listener = event -> event.tooltip("testmod").remove(TooltipTags.itemName());
        NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, listener);
        try {
            TooltipPipeline.negotiateItemTooltip(ItemStack.EMPTY, null, lines, TooltipFlag.NORMAL, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, List.of());
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        assertEquals(List.of("Lore line"), lines.stream().map(Component::getString).toList());
    }

    @Test
    void testPipelineListenerStampsProvider() {
        List<Component> lines = new ArrayList<>(List.of(Component.literal("Stone"), Component.literal("Lore line")));
        List<String> providers = new ArrayList<>();
        Consumer<TooltipNegotiationEvent> listener = event -> providers.add(event.tooltip("testmod").providerId());
        NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, listener);
        try {
            TooltipPipeline.negotiateItemTooltip(ItemStack.EMPTY, null, lines, TooltipFlag.NORMAL, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, List.of());
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        assertEquals(List.of("testmod"), providers);
        // No intents submitted: the lines pass through untouched.
        assertEquals(List.of("Stone", "Lore line"), lines.stream().map(Component::getString).toList());
    }

    @Test
    void testPipelineAddsModNameCandidate(MinecraftServer server) {
        List<Component> lines = new ArrayList<>(List.of(Component.literal("Stone")));
        TooltipPipeline.negotiateItemTooltip(new ItemStack(Items.STONE), null, lines, TooltipFlag.NORMAL, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, List.of());
        assertEquals(
                List.of("Stone", expectedModDisplayName("minecraft")),
                lines.stream().map(Component::getString).toList());
    }

    @Test
    void testPipelineModNameCandidateRemovableViaChannel(MinecraftServer server) {
        List<Component> lines = new ArrayList<>(List.of(Component.literal("Stone")));
        Consumer<TooltipNegotiationEvent> listener = event -> event.tooltip("testmod").remove(TooltipTags.modName("minecraft"));
        NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, listener);
        try {
            TooltipPipeline.negotiateItemTooltip(new ItemStack(Items.STONE), null, lines, TooltipFlag.NORMAL, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, List.of());
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        assertEquals(List.of("Stone"), lines.stream().map(Component::getString).toList());
    }

    @Test
    void testPipelineDefersToExistingModNameLine(MinecraftServer server) {
        // Another renderer (e.g. JEI) already emitted the mod name as plain text: no duplicate is added.
        String displayName = expectedModDisplayName("minecraft");
        List<Component> lines = new ArrayList<>(List.of(Component.literal("Stone"), Component.literal(displayName)));
        TooltipPipeline.negotiateItemTooltip(new ItemStack(Items.STONE), null, lines, TooltipFlag.NORMAL, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, List.of());
        assertEquals(List.of("Stone", displayName), lines.stream().map(Component::getString).toList());
    }

    // ---- Vanilla section tagging (emission sections -> pipeline tags) ------------------------------------

    /// Capture every snapshot seen by a negotiation listener while {@code action} runs.
    private static List<TooltipDocument.Snapshot> captureSnapshots(Runnable action) {
        List<TooltipDocument.Snapshot> seen = new ArrayList<>();
        Consumer<TooltipNegotiationEvent> listener = event -> seen.add(event.snapshot());
        NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, listener);
        try {
            action.run();
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        return seen;
    }

    private static String entryText(TooltipDocument.Snapshot snapshot, TooltipTag<?, ?> tag) {
        var node = snapshot.findFirst(tag);
        assertNotNull(node, "Expected a node tagged " + tag);
        return ((TooltipNode.Entry) node).component().getString();
    }

    @Test
    void testAddDetailsToTooltipReturnsCoveringSections(MinecraftServer server) {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.setDamageValue(10);
        List<Component> lines = new ArrayList<>();
        // Seeded at 1 as if the styled hover name were already present.
        var sections = ItemTooltipHandler.addDetailsToTooltip(stack, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, null, TooltipFlag.ADVANCED, lines::add, 1);
        var keys = sections.stream().map(ItemTooltipHandler.Section::key).toList();
        assertTrue(keys.contains(DataComponents.DAMAGE), "durability appender section keyed by component type");
        assertTrue(keys.contains(ItemTooltipHandler.Phase.TAIL), "advanced tail section recorded");
        int cursor = 1;
        for (var section : sections) {
            assertEquals(cursor, section.from(), "sections are contiguous");
            cursor = section.to();
        }
        assertEquals(lines.size() + 1, cursor, "sections cover every emitted line");
    }

    @Test
    void testHoverTextSectionTaggedAsLore() {
        List<Component> lines = new ArrayList<>(List.of(Component.literal("Stone"), Component.literal("L1"), Component.literal("L2")));
        var sections = List.of(new ItemTooltipHandler.Section(ItemTooltipHandler.Phase.HOVER_TEXT, 1, 3));
        var snapshots = captureSnapshots(() -> TooltipPipeline.negotiateItemTooltip(ItemStack.EMPTY, null, lines, TooltipFlag.NORMAL, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, sections));
        var snapshot = snapshots.get(0);
        assertEquals(1, snapshot.count(TooltipTags.lore()), "one lore group");
        assertEquals("L1", entryText(snapshot, TooltipTags.loreLine(0)));
        assertEquals("L2", entryText(snapshot, TooltipTags.loreLine(1)));
        assertEquals("Stone", entryText(snapshot, TooltipTags.itemName()));
        assertEquals(List.of("Stone", "L1", "L2"), lines.stream().map(Component::getString).toList());
    }

    @Test
    void testComponentAndTailSectionsTagged(MinecraftServer server) {
        List<Component> lines = new ArrayList<>(List.of(
                Component.literal("Sword"),
                Component.literal("Sharpness V"),
                Component.literal("Durability: 5 / 10"),
                Component.literal("minecraft:stone"),
                Component.literal("+2 components")));
        var sections = List.of(
                new ItemTooltipHandler.Section(DataComponents.ENCHANTMENTS, 1, 2),
                new ItemTooltipHandler.Section(DataComponents.DAMAGE, 2, 3),
                new ItemTooltipHandler.Section(ItemTooltipHandler.Phase.TAIL, 3, 5));
        var snapshots = captureSnapshots(() -> TooltipPipeline.negotiateItemTooltip(ItemStack.EMPTY, null, lines, TooltipFlag.ADVANCED, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, sections));
        var snapshot = snapshots.get(0);
        assertEquals(1, snapshot.count(TooltipTags.enchantments()), "one enchantments group");
        assertEquals(1, snapshot.count(TooltipTags.component(DataComponents.ENCHANTMENTS)));
        assertEquals(1, snapshot.count(TooltipTags.component(DataComponents.DAMAGE)));
        assertEquals("Durability: 5 / 10", entryText(snapshot, TooltipTags.damage()));
        // Advanced mode: the first tail line is the registry id.
        assertEquals("minecraft:stone", entryText(snapshot, TooltipTags.itemId()));
    }

    @Test
    void testTailItemIdNotTaggedWithoutAdvanced(MinecraftServer server) {
        List<Component> lines = new ArrayList<>(List.of(Component.literal("Sword"), Component.literal("Item is disabled")));
        var sections = List.of(new ItemTooltipHandler.Section(ItemTooltipHandler.Phase.TAIL, 1, 2));
        var snapshots = captureSnapshots(() -> TooltipPipeline.negotiateItemTooltip(ItemStack.EMPTY, null, lines, TooltipFlag.NORMAL, Item.TooltipContext.EMPTY, TooltipDisplay.DEFAULT, sections));
        assertEquals(0, snapshots.get(0).count(TooltipTags.itemId()), "no item id line in non-advanced mode");
    }

    // ---- End-to-end through ItemStack#getTooltipLines / FluidStack#getTooltipLines -------------------------

    @Test
    void testRealItemTooltipHasVanillaSectionTags(MinecraftServer server) {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.setDamageValue(10);
        List<Component> lines = new ArrayList<>();
        var snapshots = captureSnapshots(() -> lines.addAll(stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.ADVANCED)));
        assertEquals(1, snapshots.size(), "one negotiation for the tooltip");
        var snapshot = snapshots.get(0);
        assertNotNull(snapshot.findFirst(TooltipTags.itemName()));
        assertNotNull(snapshot.findFirst(TooltipTags.damage()), "damaged item shows a tagged durability line");
        assertNotNull(snapshot.findFirst(TooltipTags.itemId()), "advanced tooltip shows a tagged registry id line");
        assertNotNull(snapshot.findFirst(TooltipTags.modName("minecraft")));
        var texts = lines.stream().map(Component::getString).toList();
        assertTrue(texts.contains("minecraft:diamond_sword"), texts::toString);
        assertEquals(expectedModDisplayName("minecraft"), texts.get(texts.size() - 1), texts::toString);
    }

    @Test
    void testRealLoreComponentSectionTagged(MinecraftServer server) {
        ItemStack stack = new ItemStack(Items.PAPER);
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("custom lore"))));
        List<Component> lines = new ArrayList<>();
        var snapshots = captureSnapshots(() -> lines.addAll(stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)));
        var snapshot = snapshots.get(0);
        assertEquals(1, snapshot.count(TooltipTags.component(DataComponents.LORE)), "lore component section tagged");
        assertTrue(lines.stream().map(Component::getString).toList().contains("custom lore"));
    }

    @Test
    void testFluidTooltipNegotiation(MinecraftServer server) {
        FluidStack fluid = new FluidStack(Fluids.WATER, 1000);
        List<TooltipNegotiationEvent> events = new ArrayList<>();
        List<Component> lines = new ArrayList<>();
        Consumer<TooltipNegotiationEvent> listener = events::add;
        NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, listener);
        try {
            lines.addAll(fluid.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.ADVANCED));
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        assertEquals(1, events.size(), "one negotiation for the fluid tooltip");
        var event = events.get(0);
        assertTrue(event.isFluid());
        assertEquals(fluid, event.getFluidStack());
        assertEquals(ItemStack.EMPTY, event.getItemStack());
        var snapshot = event.snapshot();
        assertNotNull(snapshot.findFirst(TooltipTags.itemName()));
        assertNotNull(snapshot.findFirst(TooltipTags.itemId()), "advanced fluid tooltip shows a tagged registry id line");
        assertNotNull(snapshot.findFirst(TooltipTags.modName("minecraft")));
        var texts = lines.stream().map(Component::getString).toList();
        assertTrue(texts.contains("minecraft:water"), texts::toString);
        assertEquals(expectedModDisplayName("minecraft"), texts.get(texts.size() - 1), texts::toString);
    }

    @Test
    void testFluidModNameRemovableViaChannel(MinecraftServer server) {
        FluidStack fluid = new FluidStack(Fluids.WATER, 1000);
        Consumer<TooltipNegotiationEvent> listener = event -> event.tooltip("testmod").remove(TooltipTags.modName("minecraft"));
        NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, listener);
        List<Component> lines;
        try {
            lines = fluid.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL);
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        var texts = lines.stream().map(Component::getString).toList();
        assertFalse(texts.contains(expectedModDisplayName("minecraft")), "mod name removed: " + texts);
    }

    private static String expectedModDisplayName(String modId) {
        ModList modList = ModList.get();
        if (modList == null) {
            return modId;
        }
        return modList.getModContainerById(modId).map(container -> container.getModInfo().getDisplayName()).orElse(modId);
    }

    /** A resolver that keeps the first candidate, with a configurable resolver id to simulate per-mod declarations. */
    private static final class KeepFirstResolver implements TooltipResolver {
        private final Identifier id;

        KeepFirstResolver(Identifier id) {
            this.id = id;
        }

        @Override
        public Identifier id() {
            return id;
        }

        @Override
        public List<TooltipNode> resolve(Identifier channel, List<TooltipNode> candidates, TooltipResolver.Context context) {
            return List.of(candidates.get(0));
        }
    }

    /** A resolver that keeps the last candidate, with a configurable resolver id to simulate per-mod declarations. */
    private static final class KeepLastResolver implements TooltipResolver {
        private final Identifier id;

        KeepLastResolver(Identifier id) {
            this.id = id;
        }

        @Override
        public Identifier id() {
            return id;
        }

        @Override
        public List<TooltipNode> resolve(Identifier channel, List<TooltipNode> candidates, TooltipResolver.Context context) {
            return List.of(candidates.get(candidates.size() - 1));
        }
    }
}
