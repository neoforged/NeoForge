/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.common.util.AttributeUtil;

final class VanillaDataComponentTooltips {
    private VanillaDataComponentTooltips() {}

    static SequencedMap<DataComponentType<?>, TooltipAppender> collectVanillaAppenders() {
        SequencedMap<DataComponentType<?>, TooltipAppender> appenders = new LinkedHashMap<>();
        putDefaultEntry(appenders, DataComponents.TROPICAL_FISH_PATTERN);
        putDefaultEntry(appenders, DataComponents.INSTRUMENT);
        putDefaultEntry(appenders, DataComponents.MAP_ID);
        putDefaultEntry(appenders, DataComponents.BEES);
        putDefaultEntry(appenders, DataComponents.CONTAINER_LOOT);
        putDefaultEntry(appenders, DataComponents.CONTAINER);
        putDefaultEntry(appenders, DataComponents.BANNER_PATTERNS);
        putDefaultEntry(appenders, DataComponents.POT_DECORATIONS);
        putDefaultEntry(appenders, DataComponents.WRITTEN_BOOK_CONTENT);
        putDefaultEntry(appenders, DataComponents.CHARGED_PROJECTILES);
        putDefaultEntry(appenders, DataComponents.FIREWORKS);
        putDefaultEntry(appenders, DataComponents.FIREWORK_EXPLOSION);
        putDefaultEntry(appenders, DataComponents.POTION_CONTENTS);
        putDefaultEntry(appenders, DataComponents.JUKEBOX_PLAYABLE);
        putDefaultEntry(appenders, DataComponents.TRIM);
        putDefaultEntry(appenders, DataComponents.STORED_ENCHANTMENTS);
        putDefaultEntry(appenders, DataComponents.ENCHANTMENTS);
        putDefaultEntry(appenders, DataComponents.DYED_COLOR);
        putDefaultEntry(appenders, DataComponents.PROFILE);
        putDefaultEntry(appenders, DataComponents.LORE);
        putDefaultEntry(appenders, DataComponents.SULFUR_CUBE_CONTENT);
        // Neo: Replace attribute tooltips with custom handling
        appenders.put(DataComponents.ATTRIBUTE_MODIFIERS, (stack, context, display, player, flag, builder) -> AttributeUtil.addAttributeTooltips(stack, builder, display, AttributeTooltipContext.of(player, context, display, flag)));
        putUnitEntry(appenders, DataComponents.INTANGIBLE_PROJECTILE, ItemStack.INTANGIBLE_TOOLTIP);
        putUnitEntry(appenders, DataComponents.UNBREAKABLE, ItemStack.UNBREAKABLE_TOOLTIP);
        putDefaultEntry(appenders, DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
        putDefaultEntry(appenders, DataComponents.SUSPICIOUS_STEW_EFFECTS);
        putDefaultEntry(appenders, DataComponents.BLOCK_STATE);
        putDefaultEntry(appenders, DataComponents.ENTITY_DATA);
        appenders.put(DataComponents.BLOCK_ENTITY_DATA, (stack, _, display, _, _, builder) -> {
            if ((stack.is(Items.SPAWNER) || stack.is(Items.TRIAL_SPAWNER)) && display.shows(DataComponents.BLOCK_ENTITY_DATA)) {
                TypedEntityData<BlockEntityType<?>> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                Spawner.appendHoverText(blockEntityData, builder, "SpawnData");
            }
        });
        appenders.put(DataComponents.CAN_BREAK, (stack, _, display, _, _, builder) -> {
            AdventureModePredicate predicate = stack.get(DataComponents.CAN_BREAK);
            if (predicate != null && display.shows(DataComponents.CAN_BREAK)) {
                builder.accept(CommonComponents.EMPTY);
                builder.accept(AdventureModePredicate.CAN_BREAK_HEADER);
                predicate.addToTooltip(builder);
            }
        });
        appenders.put(DataComponents.CAN_PLACE_ON, (stack, _, display, _, _, builder) -> {
            AdventureModePredicate predicate = stack.get(DataComponents.CAN_PLACE_ON);
            if (predicate != null && display.shows(DataComponents.CAN_PLACE_ON)) {
                builder.accept(CommonComponents.EMPTY);
                builder.accept(AdventureModePredicate.CAN_PLACE_HEADER);
                predicate.addToTooltip(builder);
            }
        });
        appenders.put(DataComponents.DAMAGE, (stack, _, display, _, flag, builder) -> {
            if (flag.isAdvanced()) {
                if (stack.isDamaged() && display.shows(DataComponents.DAMAGE)) {
                    builder.accept(Component.translatable("item.durability", stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()));
                }
            }
        });
        return appenders;
    }

    private static <T extends TooltipProvider> void putDefaultEntry(Map<DataComponentType<?>, TooltipAppender> appenders, DataComponentType<T> type) {
        appenders.put(type, TooltipAppender.createComponentAppender(type));
    }

    private static void putUnitEntry(Map<DataComponentType<?>, TooltipAppender> appenders, DataComponentType<Unit> type, Component component) {
        appenders.put(type, (stack, _, display, _, _, builder) -> {
            if (stack.has(type) && display.shows(type)) {
                builder.accept(component);
            }
        });
    }
}
