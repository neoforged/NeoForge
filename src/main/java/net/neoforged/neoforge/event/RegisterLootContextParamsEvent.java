/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/// Fired to register [ContextKey]s for use in loot context param set [ContextKeySet]s. Params registered can then
/// be used and queried within the loot context.
///
/// Fires on both [physical sides][Dist] on the mod-specific event bus.
public class RegisterLootContextParamsEvent extends Event implements IModBusEvent {
    private final Map<Identifier, ContextKey<?>> lootContextParams;

    @ApiStatus.Internal
    public RegisterLootContextParamsEvent(Map<Identifier, ContextKey<?>> lootContextParams) {
        this.lootContextParams = lootContextParams;
    }

    /// Registers a [ContextKey] for use as a loot context param.
    ///
    /// @param lootContextParam The [ContextKey] to register.
    /// @throws IllegalArgumentException If the param provided is in the vanilla namespace or has already been registered.
    public void register(ContextKey<?> lootContextParam) {
        if (lootContextParam.name().getNamespace().equals("minecraft")) {
            throw new IllegalArgumentException("Loot context param cannot be in the vanilla namespace.");
        } else if (this.lootContextParams.putIfAbsent(lootContextParam.name(), lootContextParam) != null) {
            throw new IllegalArgumentException("Loot context param already registered for " + lootContextParam.name());
        }
    }
}
