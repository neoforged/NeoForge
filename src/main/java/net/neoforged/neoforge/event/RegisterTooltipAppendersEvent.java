/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import com.google.common.graph.MutableGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.tooltip.TooltipAppender;
import net.neoforged.neoforge.common.tooltip.TooltipLocation;
import org.jetbrains.annotations.ApiStatus;

public final class RegisterTooltipAppendersEvent extends Event implements IModBusEvent {
    private final Map<TooltipLocation, List<TooltipAppender>> appenders;
    private final Map<DataComponentType<?>, TooltipAppender> componentAppenders;
    private final MutableGraph<DataComponentType<?>> componentGraph;
    private final DataComponentType<?> firstVanillaType;
    private final DataComponentType<?> lastVanillaType;

    @ApiStatus.Internal
    public RegisterTooltipAppendersEvent(
            Map<TooltipLocation, List<TooltipAppender>> appenders,
            Map<DataComponentType<?>, TooltipAppender> componentAppenders,
            MutableGraph<DataComponentType<?>> componentGraph,
            DataComponentType<?> firstVanillaType,
            DataComponentType<?> lastVanillaType) {
        this.appenders = appenders;
        this.componentAppenders = componentAppenders;
        this.componentGraph = componentGraph;
        this.firstVanillaType = firstVanillaType;
        this.lastVanillaType = lastVanillaType;
    }

    /// Register a [TooltipAppender] at the provided [TooltipLocation].
    ///
    /// @param location The location to apply the appender to
    /// @param appender The appender to register
    public void registerAppender(TooltipLocation location, TooltipAppender appender) {
        this.appenders.computeIfAbsent(location, _ -> new ArrayList<>()).add(appender);
    }

    /// Register a [TooltipAppender] for the provided [DataComponentType] before all vanilla data component tooltip appenders.
    ///
    /// @param type     The type to register the appender for
    /// @param appender The appender to register
    public void registerComponentAppenderBeforeAll(Supplier<? extends DataComponentType<?>> type, TooltipAppender appender) {
        this.registerComponentAppenderBeforeAll(type.get(), appender);
    }

    /// Register a [TooltipAppender] for the provided [DataComponentType] before all vanilla data component tooltip appenders.
    ///
    /// @param type     The type to register the appender for
    /// @param appender The appender to register
    public void registerComponentAppenderBeforeAll(DataComponentType<?> type, TooltipAppender appender) {
        this.registerComponentAppenderBefore(type, this.firstVanillaType, appender);
    }

    /// Register a [TooltipAppender] for the provided [DataComponentType] before the other provided data component type.
    ///
    /// @param type      The type to register the appender for
    /// @param otherType The type before which the provided appender should be applied
    /// @param appender  The appender to register
    public void registerComponentAppenderBefore(Supplier<? extends DataComponentType<?>> type, DataComponentType<?> otherType, TooltipAppender appender) {
        this.registerComponentAppenderBefore(type.get(), otherType, appender);
    }

    /// Register a [TooltipAppender] for the provided [DataComponentType] before the other provided data component type.
    ///
    /// @param type      The type to register the appender for
    /// @param otherType The type before which the provided appender should be applied
    /// @param appender  The appender to register
    public void registerComponentAppenderBefore(DataComponentType<?> type, DataComponentType<?> otherType, TooltipAppender appender) {
        this.registerComponentAppender(type, appender);
        this.componentGraph.putEdge(type, otherType);
    }

    /// Register a [TooltipAppender] for the provided [DataComponentType] after the other provided data component type.
    ///
    /// @param type      The type to register the appender for
    /// @param otherType The type after which the provided appender should be applied
    /// @param appender  The appender to register
    public void registerComponentAppenderAfter(Supplier<? extends DataComponentType<?>> type, DataComponentType<?> otherType, TooltipAppender appender) {
        this.registerComponentAppenderAfter(type.get(), otherType, appender);
    }

    /// Register a [TooltipAppender] for the provided [DataComponentType] after the other provided data component type.
    ///
    /// @param type      The type to register the appender for
    /// @param otherType The type after which the provided appender should be applied
    /// @param appender  The appender to register
    public void registerComponentAppenderAfter(DataComponentType<?> type, DataComponentType<?> otherType, TooltipAppender appender) {
        this.registerComponentAppender(type, appender);
        this.componentGraph.putEdge(otherType, type);
    }

    /// Register a [TooltipAppender] for the provided [DataComponentType] after all vanilla data component tooltip appenders.
    ///
    /// @param type     The type to register the appender for
    /// @param appender The appender to register
    public void registerComponentAppenderAfterAll(Supplier<? extends DataComponentType<?>> type, TooltipAppender appender) {
        this.registerComponentAppenderAfterAll(type.get(), appender);
    }

    /// Register a [TooltipAppender] for the provided [DataComponentType] after all vanilla data component tooltip appenders.
    ///
    /// @param type     The type to register the appender for
    /// @param appender The appender to register
    public void registerComponentAppenderAfterAll(DataComponentType<?> type, TooltipAppender appender) {
        this.registerComponentAppenderAfter(type, this.lastVanillaType, appender);
    }

    private void registerComponentAppender(DataComponentType<?> type, TooltipAppender appender) {
        if (this.componentAppenders.putIfAbsent(type, appender) != null) {
            throw new IllegalStateException("Duplicate appender registration for type: " + type);
        }
    }
}
