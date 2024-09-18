/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.util;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.AttributeUtil;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.Nullable;

public class TooltipUtil {
    public static final ResourceLocation FAKE_MERGED_ID = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "fake_merged_modifier");

    public static void addAttributeTooltips(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip, TooltipFlag flag) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (modifiers.showInTooltip()) {
            applyModifierTooltips(player, stack, tooltip, flag);
        }
        NeoForge.EVENT_BUS.post(new AddAttributeTooltipsEvent(stack, player, tooltip, flag));
    }

    public static void applyModifierTooltips(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip, TooltipFlag flag) {
        Set<ResourceLocation> skips = new HashSet<>();
        NeoForge.EVENT_BUS.post(new GatherSkippedAttributeTooltipsEvent(stack, player, skips, flag));

        for (EquipmentSlotGroup group : EquipmentSlotGroup.values()) {
            Multimap<Holder<Attribute>, AttributeModifier> modifiers = AttributeUtil.getSortedModifiers(stack, group);
            applyTextFor(player, stack, tooltip, modifiers, group, skips, flag);
        }
    }

    public static void applyTextFor(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> modifierMap,
            EquipmentSlotGroup group, Set<ResourceLocation> skips, TooltipFlag flag) {
        if (!modifierMap.isEmpty()) {
            modifierMap.values().removeIf(m -> skips.contains(m.id()));

            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("item.modifiers." + group.getSerializedName()).withStyle(ChatFormatting.GRAY));

            if (modifierMap.isEmpty()) return;

            Map<Holder<Attribute>, BaseModifier> baseModifs = new IdentityHashMap<>();

            modifierMap.forEach((attr, modif) -> {
                if (modif.id().equals(attr.value().getBaseID())) {
                    baseModifs.put(attr, new BaseModifier(modif, new ArrayList<>()));
                }
            });

            modifierMap.forEach((attr, modif) -> {
                BaseModifier base = baseModifs.get(attr);
                if (base != null && base.base != modif) {
                    base.children.add(modif);
                }
            });

            for (Map.Entry<Holder<Attribute>, BaseModifier> entry : baseModifs.entrySet()) {
                Holder<Attribute> attr = entry.getKey();
                BaseModifier baseModif = entry.getValue();
                double entityBase = player == null ? 0 : player.getAttributeBaseValue(attr);
                double base = baseModif.base.amount() + entityBase;
                final double rawBase = base;
                double amt = base;
                double baseBonus = attr.value().getBonusBaseValue(stack);
                for (AttributeModifier modif : baseModif.children) {
                    if (modif.operation() == Operation.ADD_VALUE) base = amt = amt + modif.amount();
                    else if (modif.operation() == Operation.ADD_MULTIPLIED_BASE) amt += modif.amount() * base;
                    else amt *= 1 + modif.amount();
                }
                amt += baseBonus;
                boolean isMerged = !baseModif.children.isEmpty() || baseBonus != 0;
                MutableComponent text = attr.value().toBaseComponent(amt, entityBase, isMerged, flag);
                tooltip.accept(padded(" ", text).withStyle(isMerged ? ChatFormatting.GOLD : ChatFormatting.DARK_GREEN));
                if (Screen.hasShiftDown() && isMerged) {
                    // Display the raw base value, and then all children modifiers.
                    text = attr.value().toBaseComponent(rawBase, entityBase, false, flag);
                    tooltip.accept(list().append(text.withStyle(ChatFormatting.DARK_GREEN)));
                    for (AttributeModifier modifier : baseModif.children) {
                        tooltip.accept(list().append(attr.value().toComponent(modifier, flag)));
                    }
                    if (baseBonus > 0) {
                        attr.value().addBonusTooltips(stack, tooltip, flag);
                    }
                }
            }

            for (Holder<Attribute> attr : modifierMap.keySet()) {
                if (baseModifs.containsKey(attr)) continue;
                Collection<AttributeModifier> modifs = modifierMap.get(attr);
                // Initiate merged-tooltip logic if we have more than one modifier for a given attribute.
                if (modifs.size() > 1) {
                    double[] sums = new double[3];
                    boolean[] merged = new boolean[3];
                    Map<Operation, List<AttributeModifier>> shiftExpands = new HashMap<>();
                    for (AttributeModifier modifier : modifs) {
                        if (modifier.amount() == 0) continue;
                        if (sums[modifier.operation().ordinal()] != 0) merged[modifier.operation().ordinal()] = true;
                        sums[modifier.operation().ordinal()] += modifier.amount();
                        shiftExpands.computeIfAbsent(modifier.operation(), k -> new LinkedList<>()).add(modifier);
                    }
                    for (Operation op : Operation.values()) {
                        int i = op.ordinal();
                        if (sums[i] == 0) continue;
                        if (merged[i]) {
                            TextColor color = sums[i] < 0 ? TextColor.fromRgb(0xF93131) : TextColor.fromRgb(0x7A7AF9);
                            if (sums[i] < 0) sums[i] *= -1;
                            var fakeModif = new AttributeModifier(FAKE_MERGED_ID, sums[i], op);
                            MutableComponent comp = attr.value().toComponent(fakeModif, flag);
                            tooltip.accept(comp.withStyle(comp.getStyle().withColor(color)));
                            if (merged[i] && Screen.hasShiftDown()) {
                                shiftExpands.get(Operation.BY_ID.apply(i)).forEach(modif -> tooltip.accept(list().append(attr.value().toComponent(modif, flag))));
                            }
                        } else {
                            var fakeModif = new AttributeModifier(FAKE_MERGED_ID, sums[i], op);
                            tooltip.accept(attr.value().toComponent(fakeModif, flag));
                        }
                    }
                } else modifs.forEach(m -> {
                    if (m.amount() != 0) tooltip.accept(attr.value().toComponent(m, flag));
                });
            }
        }
    }

    /**
     * Creates a mutable component starting with the char used to represent a drop-down list.
     */
    public static MutableComponent list() {
        return Component.literal(" \u2507 ").withStyle(ChatFormatting.GRAY);
    }

    /**
     * Creates a literal component holding {@code padding} and appends {@code comp} to it, returning the result.
     */
    private static MutableComponent padded(String padding, Component comp) {
        return Component.literal(padding).append(comp);
    }

    public static record BaseModifier(AttributeModifier base, List<AttributeModifier> children) {}
}
