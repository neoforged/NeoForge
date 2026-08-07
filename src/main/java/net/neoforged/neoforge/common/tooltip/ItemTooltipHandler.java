/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public final class ItemTooltipHandler {
    private static final List<DataComponentType<?>> VANILLA_APPENDER_ORDER = new ArrayList<>();
    private static final List<TooltipAppender> HEAD_APPENDERS = new ArrayList<>();
    private static final List<TooltipAppender> MIDDLE_APPENDERS = new ArrayList<>();
    private static final List<TooltipAppender> TAIL_APPENDERS = new ArrayList<>();
    private static final Map<TooltipAppender, DataComponentType<?>> COMPONENT_TYPES = new IdentityHashMap<>();

    public static List<Section> addDetailsToTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            @Nullable Player player,
            TooltipFlag tooltipFlag,
            Consumer<Component> builder) {
        return addDetailsToTooltip(stack, context, display, player, tooltipFlag, builder, 0);
    }

    public static List<Section> addDetailsToTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            @Nullable Player player,
            TooltipFlag tooltipFlag,
            Consumer<Component> builder,
            int initialLineCount) {
        List<Section> sections = new ArrayList<>();
        int[] count = { initialLineCount };
        Consumer<Component> sink = component -> {
            builder.accept(component);
            count[0]++;
        };
        for (TooltipAppender appender : HEAD_APPENDERS) {
            appendSection(appender, stack, context, display, player, tooltipFlag, sink, count, sections, null);
        }
        int hoverFrom = count[0];
        stack.getItem().appendHoverText(stack, context, display, sink, tooltipFlag);
        addSection(sections, Phase.HOVER_TEXT, hoverFrom, count[0]);
        for (TooltipAppender appender : MIDDLE_APPENDERS) {
            appendSection(appender, stack, context, display, player, tooltipFlag, sink, count, sections, COMPONENT_TYPES.get(appender));
        }
        int tailFrom = count[0];
        stack.addDetailsToTooltipTail(context, display, player, tooltipFlag, sink);
        addSection(sections, Phase.TAIL, tailFrom, count[0]);
        for (TooltipAppender appender : TAIL_APPENDERS) {
            appendSection(appender, stack, context, display, player, tooltipFlag, sink, count, sections, null);
        }
        return sections;
    }

    private static void appendSection(
            TooltipAppender appender,
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            @Nullable Player player,
            TooltipFlag tooltipFlag,
            Consumer<Component> sink,
            int[] count,
            List<Section> sections,
            @Nullable Object key) {
        int from = count[0];
        appender.append(stack, context, display, player, tooltipFlag, sink);
        addSection(sections, key, from, count[0]);
    }

    private static void addSection(List<Section> sections, @Nullable Object key, int from, int to) {
        if (to > from) {
            sections.add(new Section(key, from, to));
        }
    }

    public record Section(@Nullable Object key, int from, int to) {}

    public enum Phase {
        HOVER_TEXT,
        TAIL
    }

    public static void init() {
        Map<TooltipLocation, List<TooltipAppender>> appenders = new EnumMap<>(TooltipLocation.class);
        SequencedMap<DataComponentType<?>, TooltipAppender> componentAppenders = new LinkedHashMap<>();
        MutableGraph<DataComponentType<?>> componentGraph = GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
        SequencedMap<DataComponentType<?>, TooltipAppender> vanillaAppenders = VanillaDataComponentTooltips.collectVanillaAppenders();
        VANILLA_APPENDER_ORDER.addAll(vanillaAppenders.sequencedKeySet());
        buildInitialComponentGraph(componentAppenders, componentGraph, vanillaAppenders);
        ModLoader.postEvent(new RegisterTooltipAppendersEvent(
                appenders,
                componentAppenders,
                componentGraph,
                vanillaAppenders.firstEntry().getKey(),
                vanillaAppenders.lastEntry().getKey()));

        HEAD_APPENDERS.addAll(appenders.getOrDefault(TooltipLocation.HEAD, List.of()));

        MIDDLE_APPENDERS.addAll(appenders.getOrDefault(TooltipLocation.POST_CUSTOM, List.of()));
        addDataComponentAppenders(componentAppenders, componentGraph);
        MIDDLE_APPENDERS.addAll(appenders.getOrDefault(TooltipLocation.PRE_ITEM_INFO, List.of()));

        TAIL_APPENDERS.addAll(appenders.getOrDefault(TooltipLocation.TAIL, List.of()));
    }

    private static void buildInitialComponentGraph(
            SequencedMap<DataComponentType<?>, TooltipAppender> appenders,
            MutableGraph<DataComponentType<?>> graph,
            SequencedMap<DataComponentType<?>, TooltipAppender> vanillaAppenders) {
        vanillaAppenders.forEach((type, appender) -> {
            appenders.put(type, appender);
            graph.addNode(type);
        });
        List<DataComponentType<?>> types = List.copyOf(vanillaAppenders.keySet());
        for (int i = 1; i < types.size(); i++) {
            DataComponentType<?> prevType = types.get(i - 1);
            DataComponentType<?> type = types.get(i);
            graph.putEdge(prevType, type);
        }
    }

    private static void addDataComponentAppenders(SequencedMap<DataComponentType<?>, TooltipAppender> appenders, MutableGraph<DataComponentType<?>> graph) {
        List<DataComponentType<?>> sorted = CommonHooks.sortGraphChecked(graph, appenders.keySet(), "component tooltip appenders", Function.identity());
        for (DataComponentType<?> type : sorted) {
            TooltipAppender appender = appenders.get(type);
            COMPONENT_TYPES.put(appender, type);
            MIDDLE_APPENDERS.add(appender);
        }
    }

    @VisibleForTesting
    public static List<DataComponentType<?>> getVanillaAppenderOrder() {
        return VANILLA_APPENDER_ORDER;
    }

    private ItemTooltipHandler() {}
}
