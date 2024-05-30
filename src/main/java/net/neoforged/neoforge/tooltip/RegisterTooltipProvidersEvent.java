/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.tooltip;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public abstract class RegisterTooltipProvidersEvent<T extends DataComponentHolder> extends Event implements IModBusEvent {
    private final List<TooltipManager.NamedTooltip<T>> registrations = Lists.newArrayList();

    protected RegisterTooltipProvidersEvent() {}

    public final void registerAboveAll(ResourceLocation identifier, TooltipManager.Provider<T> provider) {
        register(Ordering.ABOVE, null, identifier, provider);
    }

    public final void registerAbove(ResourceLocation other, ResourceLocation identifier, TooltipManager.Provider<T> provider) {
        register(Ordering.ABOVE, other, identifier, provider);
    }

    public final void registerBelow(ResourceLocation other, ResourceLocation identifier, TooltipManager.Provider<T> provider) {
        register(Ordering.BELOW, other, identifier, provider);
    }

    public final void registerBelowAll(ResourceLocation identifier, TooltipManager.Provider<T> provider) {
        register(Ordering.BELOW, null, identifier, provider);
    }

    private void register(Ordering ordering, @Nullable ResourceLocation other, ResourceLocation identifier, TooltipManager.Provider<T> provider) {
        Objects.requireNonNull(identifier);

        for (var registered : registrations) {
            Preconditions.checkArgument(!registered.identifier().equals(identifier), "Provider already registered: " + identifier);
        }

        int insertPosition;

        if (other == null)
            insertPosition = ordering == Ordering.ABOVE ? 0 : registrations.size();
        else {
            var otherIndex = IntStream.range(0, registrations.size())
                    .filter(i -> registrations.get(i).identifier().equals(other))
                    .findFirst();

            insertPosition = otherIndex.orElseThrow(() -> new IllegalArgumentException("Attempted to order against an unregistered provider: " + other)) + (ordering == Ordering.ABOVE ? 0 : 1);
        }

        registrations.add(insertPosition, new TooltipManager.NamedTooltip<>(identifier, provider));
    }

    public final List<TooltipManager.NamedTooltip<T>> getProviders() {
        return List.copyOf(registrations);
    }

    public enum Ordering {
        BELOW,
        ABOVE
    }

    public static final class Item extends RegisterTooltipProvidersEvent<ItemStack> {
        @ApiStatus.Internal
        public Item() {}
    }
}
