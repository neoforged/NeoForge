/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.tooltip;

import com.google.common.collect.Lists;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface VanillaTooltipProviders {
    ResourceLocation NAME = key("name");
    ResourceLocation ADDITIONAL_TOOLTIPS = key("additional_tooltip");
    ResourceLocation DATA_COMPONENTS = key("data_components");
    ResourceLocation DATA_COMPONENT_COUNT = key("data_component_count");
    ResourceLocation DISABLED = key("disabled");

    ResourceLocation ITEM_MAP_ID = key("item/map_id");
    ResourceLocation ITEM_ATTRIBUTES = key("item/data_components");
    ResourceLocation ITEM_ADVENTURE_MODE_PREDICATES = key("item/adventure_mode_predicates");
    ResourceLocation ITEM_DURABILITY = key("item/durability");
    ResourceLocation ITEM_ID = key("item/id");

    @ApiStatus.Internal
    static void registerBuiltIn(RegisterTooltipProvidersEvent.Item event) {
        event.registerAboveAll(NAME, (holder, player, context, adder, flag) -> {
            var name = Component.empty().append(holder.getHoverName()).withStyle(holder.getRarity().getStyleModifier());

            if (holder.has(DataComponents.CUSTOM_NAME))
                name.withStyle(ChatFormatting.ITALIC);

            adder.accept(name);
        });

        event.registerBelow(NAME, ITEM_MAP_ID, (holder, player, context, adder, flag) -> {
            if (flag.isAdvanced() || holder.has(DataComponents.CUSTOM_NAME) || !holder.is(Items.FILLED_MAP))
                return;

            var mapId = holder.get(DataComponents.MAP_ID);

            if (mapId != null)
                adder.accept(MapItem.getTooltipForId(mapId));
        });

        event.registerBelow(ITEM_MAP_ID, ADDITIONAL_TOOLTIPS, (holder, player, context, adder, flag) -> {
            if (!holder.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
                // TODO: find better way than creating new list every invocation
                var additionalTooltips = Lists.<Component>newArrayList();
                holder.getItem().appendHoverText(holder, context, additionalTooltips, flag);
                additionalTooltips.forEach(adder);
            }
        });

        event.registerBelow(ADDITIONAL_TOOLTIPS, DATA_COMPONENTS, (holder, player, context, adder, flag) -> {
            holder.addToTooltip(DataComponents.TRIM, context, adder, flag);
            holder.addToTooltip(DataComponents.STORED_ENCHANTMENTS, context, adder, flag);
            holder.addToTooltip(DataComponents.ENCHANTMENTS, context, adder, flag);
            holder.addToTooltip(DataComponents.DYED_COLOR, context, adder, flag);
            holder.addToTooltip(DataComponents.LORE, context, adder, flag);
            // below attributes in vanilla code
            // with the tooltip manager this is not possible without another
            // tooltip layer for components
            holder.addToTooltip(DataComponents.UNBREAKABLE, context, adder, flag);
        });

        event.registerBelow(DATA_COMPONENTS, ITEM_ATTRIBUTES, (holder, player, context, adder, flag) -> {
            var attributeModifiers = holder.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

            if (attributeModifiers.showInTooltip()) {
                for (var equipmentSlot : EquipmentSlot.values()) {
                    var flag1 = new MutableBoolean(true);

                    holder.forEachModifier(equipmentSlot, (attributeHolder, attributeModifier) -> {
                        if (flag1.isTrue()) {
                            adder.accept(CommonComponents.EMPTY);
                            adder.accept(Component.translatable("item.modifiers." + equipmentSlot.getName()).withStyle(ChatFormatting.GRAY));
                            flag1.setFalse();
                        }

                        addModifierTooltip(holder, adder, player, attributeHolder, attributeModifier);
                    });
                }
            }
        });

        event.registerBelow(ITEM_ATTRIBUTES, ITEM_ADVENTURE_MODE_PREDICATES, (holder, player, context, adder, flag) -> {
            var canBreak = holder.get(DataComponents.CAN_BREAK);

            if (canBreak != null && canBreak.showInTooltip()) {
                adder.accept(CommonComponents.EMPTY);
                adder.accept(AdventureModePredicate.CAN_BREAK_HEADER);
                canBreak.addToTooltip(adder);
            }

            var canPlaceOn = holder.get(DataComponents.CAN_PLACE_ON);

            if (canPlaceOn != null && canPlaceOn.showInTooltip()) {
                adder.accept(CommonComponents.EMPTY);
                adder.accept(AdventureModePredicate.CAN_PLACE_HEADER);
                canPlaceOn.addToTooltip(adder);
            }
        });

        event.registerBelowAll(DISABLED, (holder, player, context, adder, flag) -> {
            if (player != null && !holder.isItemEnabled(player.level().enabledFeatures()))
                adder.accept(Component.translatable("item.disabled").withStyle(ChatFormatting.RED));
        });

        event.registerAbove(DISABLED, DATA_COMPONENT_COUNT, (holder, player, context, adder, flag) -> {
            if (flag.isAdvanced()) {
                var componentCount = holder.getComponents().size();

                if (componentCount > 0)
                    adder.accept(Component.translatable("item.components", componentCount).withStyle(ChatFormatting.DARK_GRAY));
            }
        });

        event.registerAbove(DATA_COMPONENT_COUNT, ITEM_ID, (holder, player, context, adder, flag) -> {
            if (flag.isAdvanced())
                adder.accept(Component.literal(Util.getRegisteredName(BuiltInRegistries.ITEM, holder.getItem())).withStyle(ChatFormatting.DARK_GRAY));
        });

        event.registerAbove(ITEM_ID, ITEM_DURABILITY, (holder, player, context, adder, flag) -> {
            if (flag.isAdvanced() && holder.isDamaged()) {
                var maxDamage = holder.getMaxDamage();
                adder.accept(Component.translatable("item.durability", maxDamage - holder.getDamageValue(), maxDamage));
            }
        });
    }

    private static ResourceLocation key(String identifier) {
        return new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, identifier);
    }

    private static void addModifierTooltip(ItemStack stack, Consumer<Component> adder, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier attributeModifier) {
        // Copied from ItemStack | private in vanilla
        var d0 = attributeModifier.amount();
        var flag = false;

        if (player != null) {
            if (attributeModifier.id() == Item.BASE_ATTACK_DAMAGE_UUID) {
                d0 += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                d0 += EnchantmentHelper.getDamageBonus(stack, null);
                flag = true;
            } else if (attributeModifier.id() == Item.BASE_ATTACK_SPEED_UUID) {
                d0 += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                flag = true;
            }
        }

        double d1;
        var operation = attributeModifier.operation();

        if (operation == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || operation == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
            d1 = d0 * 100D;
        else if (attribute.is(Attributes.KNOCKBACK_RESISTANCE))
            d1 = d0 * 10D;
        else
            d1 = d0;

        Component component = null;

        if (flag)
            component = CommonComponents.space().append(
                    Component.translatable(
                            "attribute.modifier.equals." + operation.id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                            Component.translatable(attribute.value().getDescriptionId())).withStyle(ChatFormatting.DARK_GREEN));
        else if (d0 > 0D)
            component = Component.translatable(
                    "attribute.modifier.plus." + operation.id(),
                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                    Component.translatable(attribute.value().getDescriptionId())).withStyle(ChatFormatting.BLUE);
        else if (d0 < 0D)
            component = Component.translatable(
                    "attribute.modifier.take." + operation.id(),
                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                    Component.translatable(attribute.value().getDescriptionId())).withStyle(ChatFormatting.RED);

        adder.accept(component);
    }
}
