/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.loading.toposort.CyclePresentException;
import net.neoforged.fml.loading.toposort.TopologicalSort;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public final class ItemTooltipHandler {
    private static final List<TooltipAppender> HEAD_APPENDERS = new ArrayList<>();
    private static final List<TooltipAppender> MIDDLE_APPENDERS = new ArrayList<>();
    private static final List<TooltipAppender> TAIL_APPENDERS = new ArrayList<>();

    public static void addDetailsToTooltipHead(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            @Nullable Player player,
            TooltipFlag tooltipFlag,
            Consumer<Component> builder) {
        for (TooltipAppender appender : HEAD_APPENDERS) {
            appender.append(stack, context, display, player, tooltipFlag, builder);
        }
    }

    public static void addDetailsToTooltipMiddle(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            @Nullable Player player,
            TooltipFlag tooltipFlag,
            Consumer<Component> builder) {
        for (TooltipAppender appender : MIDDLE_APPENDERS) {
            appender.append(stack, context, display, player, tooltipFlag, builder);
        }
    }

    public static void addDetailsToTooltipTail(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            @Nullable Player player,
            TooltipFlag tooltipFlag,
            Consumer<Component> builder) {
        for (TooltipAppender appender : TAIL_APPENDERS) {
            appender.append(stack, context, display, player, tooltipFlag, builder);
        }
    }

    public static void init() {
        Map<TooltipLocation, List<TooltipAppender>> appenders = new EnumMap<>(TooltipLocation.class);
        SequencedMap<DataComponentType<?>, TooltipAppender> componentAppenders = new LinkedHashMap<>();
        MutableGraph<DataComponentType<?>> componentGraph = GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
        SequencedMap<DataComponentType<?>, TooltipAppender> vanillaAppenders = VanillaDataComponentTooltips.collectVanillaAppenders();
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
        // Build the index mapping in a way that can be used as a comparator to preserve insertion order.
        Object2IntMap<DataComponentType<?>> insertionOrder = new Object2IntOpenHashMap<>();
        int idx = 0;
        for (DataComponentType<?> listener : appenders.keySet()) {
            insertionOrder.put(listener, idx++);
        }

        // Do the sort.
        List<DataComponentType<?>> sorted;
        try {
            sorted = TopologicalSort.topologicalSort(graph, Comparator.comparingInt(insertionOrder::getInt));
        } catch (CyclePresentException ex) {
            // Build a real error message and re-throw.
            StringBuilder sb = new StringBuilder();
            sb.append("Cycles were detected during component tooltip appender sorting:\n");

            Set<Set<DataComponentType<?>>> cycles = ex.getCycles();
            idx = 0;
            for (Set<DataComponentType<?>> cycle : cycles) {
                sb.append(idx++).append(": ");
                for (DataComponentType<?> key : cycle) {
                    sb.append(key).append("->");
                }
                sb.append(cycle.iterator().next()).append('\n');
            }

            throw new IllegalArgumentException(sb.toString());
        }

        for (DataComponentType<?> type : sorted) {
            MIDDLE_APPENDERS.add(appenders.get(type));
        }
    }

    private ItemTooltipHandler() {}
}
