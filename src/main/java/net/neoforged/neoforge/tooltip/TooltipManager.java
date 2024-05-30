/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.tooltip;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.Nullable;

public final class TooltipManager<T extends DataComponentHolder> {
    public static final TooltipManager<ItemStack> ITEM = new TooltipManager<>(RegisterTooltipProvidersEvent.Item::new);

    @Nullable
    private List<NamedTooltip<T>> tooltips;
    private final Supplier<RegisterTooltipProvidersEvent<T>> eventFactory;

    public TooltipManager(Supplier<RegisterTooltipProvidersEvent<T>> eventFactory) {
        this.eventFactory = eventFactory;
    }

    public void provide(T holder, @Nullable Player player, Item.TooltipContext context, Consumer<Component> adder, TooltipFlag flag) {
        if (tooltips == null || tooltips.isEmpty() || holder.has(DataComponents.HIDE_TOOLTIP))
            return;

        for (var tooltip : tooltips) {
            tooltip.provider().accept(holder, player, context, component -> {
                if (component != null)
                    adder.accept(component);
            }, flag);
        }
    }

    public void gather() {
        var event = eventFactory.get();

        if (event instanceof RegisterTooltipProvidersEvent.Item item)
            VanillaTooltipProviders.registerBuiltIn(item);

        ModLoader.postEventWrapContainerInModOrder(event);
        tooltips = event.getProviders();
    }

    public record NamedTooltip<T extends DataComponentHolder>(ResourceLocation identifier, Provider<T> provider) {}

    @FunctionalInterface
    public interface Provider<T> {
        void accept(T holder, @Nullable Player player, Item.TooltipContext context, Consumer<Component> adder, TooltipFlag flag);
    }
}
