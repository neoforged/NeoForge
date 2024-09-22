/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility code to support {@link IAttributeExtension}.
 */
public class AttributeUtil {
    /**
     * ID of the base modifier for Attack Damage
     */
    public static final ResourceLocation BASE_ATTACK_DAMAGE_ID = Item.BASE_ATTACK_DAMAGE_ID;

    /**
     * ID of the base modifier for Attack Speed
     */
    public static final ResourceLocation BASE_ATTACK_SPEED_ID = Item.BASE_ATTACK_SPEED_ID;

    /**
     * ID of the base modifier for Attack Range
     */
    public static final ResourceLocation BASE_ENTITY_REACH_ID = ResourceLocation.withDefaultNamespace("base_entity_reach");

    /**
     * ID used for attribute modifiers used to hold merged values when {@link NeoForgeMod#enableMergedAttributeTooltips()} is active.
     * <p>
     * Should not be used by any real attribute modifiers for gameplay purposes.
     */
    public static final ResourceLocation FAKE_MERGED_ID = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "fake_merged_modifier");

    /**
     * Comparator for {@link AttributeModifier}. First compares by operation, then amount, then the ID.
     */
    public static final Comparator<AttributeModifier> ATTRIBUTE_MODIFIER_COMPARATOR = Comparator.comparing(AttributeModifier::operation)
            .thenComparingDouble(a -> -Math.abs(a.amount())) // Sort most impactful modifiers first
            .thenComparing(AttributeModifier::id);

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Checks if attribute modifier tooltips should show, and if they should, adds tooltips for all attribute modifiers present on an item stack to the stack's tooltip lines.
     * <p>
     * After the tooltip lines have been added, fires the {@link AddAttributeTooltipsEvent} to allow mods to add additional attribute-related lines.
     * 
     * @param tooltip A consumer to add the tooltip lines to.
     * @param ctx     The tooltip context.
     */
    public static void addAttributeTooltips(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (modifiers.showInTooltip()) {
            AttributeUtil.applyModifierTooltips(stack, tooltip, ctx);
        }
        NeoForge.EVENT_BUS.post(new AddAttributeTooltipsEvent(stack, tooltip, ctx));
    }

    /**
     * Applies the attribute modifier tooltips for all attribute modifiers present on the item stack.
     * <p>
     * Before application, this method posts the {@link GatherSkippedAttributeTooltipsEvent} to determine which tooltips should be skipped.
     * <p>
     * This method is also responsible for adding the modifier group category labels.
     * 
     * @param tooltip A consumer to add the tooltip lines to.
     * @param ctx     The tooltip context.
     */
    public static void applyModifierTooltips(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx) {
        var event = NeoForge.EVENT_BUS.post(new GatherSkippedAttributeTooltipsEvent(stack, ctx));
        if (event.isSkippingAll()) {
            return;
        }

        for (EquipmentSlotGroup group : EquipmentSlotGroup.values()) {
            if (event.isSkipped(group)) {
                continue;
            }

            Multimap<Holder<Attribute>, AttributeModifier> modifiers = getSortedModifiers(stack, group);

            // Remove any skipped modifiers before doing any logic
            modifiers.values().removeIf(m -> event.isSkipped(m.id()));

            if (modifiers.isEmpty()) {
                continue;
            }

            // Add an empty line, then the name of the group, then the modifiers.
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("item.modifiers." + group.getSerializedName()).withStyle(ChatFormatting.GRAY));

            AttributeUtil.applyTextFor(stack, tooltip, modifiers, ctx);
        }
    }

    /**
     * Applies the text for the provided attribute modifiers to the tooltip for a given item stack.
     * <p>
     * This method will attempt to merge multiple modifiers for a single attribute into a single modifier if {@linkplain NeoForgeMod#enableMergedAttributeTooltips()} was called.
     * 
     * @param stack       The item stack that owns the modifiers.
     * @param tooltip     The consumer to append tooltip components to.
     * @param modifierMap A mutable map of modifiers to convert into tooltip lines.
     * @param ctx         The tooltip context.
     */
    public static void applyTextFor(ItemStack stack, Consumer<Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> modifierMap, AttributeTooltipContext ctx) {
        // Don't add anything if there is nothing in the group
        if (modifierMap.isEmpty()) {
            return;
        }

        // Collect all the base modifiers
        Map<Holder<Attribute>, BaseModifier> baseModifs = new IdentityHashMap<>();

        var it = modifierMap.entries().iterator();
        while (it.hasNext()) {
            Entry<Holder<Attribute>, AttributeModifier> entry = it.next();
            Holder<Attribute> attr = entry.getKey();
            AttributeModifier modif = entry.getValue();
            if (modif.id().equals(attr.value().getBaseId())) {
                baseModifs.put(attr, new BaseModifier(modif, new ArrayList<>()));
                // Remove base modifiers from the main map after collection so we don't need to check for them later.
                it.remove();
            }
        }

        // Collect children of all base modifiers for merging logic
        modifierMap.forEach((attr, modif) -> {
            BaseModifier base = baseModifs.get(attr);
            if (base != null) {
                base.children.add(modif);
            }
        });

        // Add tooltip lines for base modifiers
        for (Map.Entry<Holder<Attribute>, BaseModifier> entry : baseModifs.entrySet()) {
            Holder<Attribute> attr = entry.getKey();
            BaseModifier baseModif = entry.getValue();
            double entityBase = ctx.player() == null ? 0 : ctx.player().getAttributeBaseValue(attr);
            double base = baseModif.base.amount() + entityBase;
            final double rawBase = base;
            double amt = base;

            // Compute the base value including merged modifiers if merging is enabled
            if (NeoForgeMod.shouldMergeAttributeTooltips()) {
                for (AttributeModifier modif : baseModif.children) {
                    switch (modif.operation()) {
                        case ADD_VALUE:
                            base = amt = amt + modif.amount();
                            break;
                        case ADD_MULTIPLIED_BASE:
                            amt += modif.amount() * base;
                            break;
                        case ADD_MULTIPLIED_TOTAL:
                            amt *= 1 + modif.amount();
                            break;
                    }
                }
            }

            double baseBonus = attr.value().getBonusBaseValue(stack);
            amt += baseBonus;

            boolean isMerged = NeoForgeMod.shouldMergeAttributeTooltips() && (!baseModif.children.isEmpty() || baseBonus != 0);
            MutableComponent text = attr.value().toBaseComponent(amt, entityBase, isMerged, ctx.flag());
            tooltip.accept(Component.literal(" ").append(text).withStyle(isMerged ? ChatFormatting.GOLD : ChatFormatting.DARK_GREEN));
            if (ctx.flag().hasShiftDown() && isMerged) {
                // Display the raw base value, and then all children modifiers.
                text = attr.value().toBaseComponent(rawBase, entityBase, false, ctx.flag());
                tooltip.accept(AttributeUtil.list().append(text.withStyle(ChatFormatting.DARK_GREEN)));
                for (AttributeModifier modifier : baseModif.children) {
                    tooltip.accept(AttributeUtil.list().append(attr.value().toComponent(modifier, ctx.flag())));
                }
                if (baseBonus > 0) {
                    attr.value().addBonusTooltips(stack, tooltip, ctx.flag());
                }
            }
        }

        for (Holder<Attribute> attr : modifierMap.keySet()) {
            // Skip attributes who have already been processed during the base modifier stage
            if (NeoForgeMod.shouldMergeAttributeTooltips() && baseModifs.containsKey(attr)) {
                continue;
            }

            Collection<AttributeModifier> modifs = modifierMap.get(attr);
            // Initiate merged-tooltip logic if we have more than one modifier for a given attribute.
            if (NeoForgeMod.shouldMergeAttributeTooltips() && modifs.size() > 1) {
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

                    // If the merged value comes out to 0, just ignore the whole stack
                    if (sums[i] == 0) {
                        continue;
                    }

                    // Handle merged modifier stacks by creating a "fake" merged modifier with the underlying value.
                    if (merged[i]) {
                        TextColor color = attr.value().getMergedStyle(sums[i] > 0);
                        var fakeModif = new AttributeModifier(AttributeUtil.FAKE_MERGED_ID, sums[i], op);
                        MutableComponent comp = attr.value().toComponent(fakeModif, ctx.flag());
                        tooltip.accept(comp.withStyle(comp.getStyle().withColor(color)));
                        if (ctx.flag().hasShiftDown() && merged[i]) {
                            shiftExpands.get(Operation.BY_ID.apply(i)).forEach(modif -> tooltip.accept(AttributeUtil.list().append(attr.value().toComponent(modif, ctx.flag()))));
                        }
                    } else {
                        var fakeModif = new AttributeModifier(AttributeUtil.FAKE_MERGED_ID, sums[i], op);
                        tooltip.accept(attr.value().toComponent(fakeModif, ctx.flag()));
                    }
                }
            } else {
                for (AttributeModifier m : modifs) {
                    if (m.amount() != 0) {
                        tooltip.accept(attr.value().toComponent(m, ctx.flag()));
                    }
                }
            }
        }
    }

    /**
     * Adds tooltip lines for the attribute modifiers contained in a {@link PotionContents}.
     * 
     * @param list     The list of attribute modifiers generated by calling {@link MobEffect#createModifiers} for each mob effect instance on the potion.
     * @param tooltips The tooltip consumer to add lines to.
     */
    public static void addPotionTooltip(List<Pair<Holder<Attribute>, AttributeModifier>> list, Consumer<Component> tooltips) {
        if (!list.isEmpty()) {
            tooltips.accept(CommonComponents.EMPTY);
            tooltips.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Holder<Attribute>, AttributeModifier> pair : list) {
                tooltips.accept(pair.getFirst().value().toComponent(pair.getSecond(), AttributeUtil.getTooltipFlag()));
            }
        }
    }

    /**
     * Creates a sorted {@link TreeMultimap} used to ensure a stable iteration order of item attribute modifiers.
     */
    public static Multimap<Holder<Attribute>, AttributeModifier> sortedMap() {
        return TreeMultimap.create(Comparator.comparing(Holder::value, Comparator.comparing(BuiltInRegistries.ATTRIBUTE::getKey, ResourceLocation::compareTo)), ATTRIBUTE_MODIFIER_COMPARATOR);
    }

    /**
     * Returns a sorted, mutable {@link Multimap} containing all the attribute modifiers on an item stack for the given group.
     * <p>
     * This includes attribute modifiers from components (or default modifiers, if not present), enchantments, and the {@link ItemAttributeModifierEvent}.
     * 
     * @param stack The stack to query modifiers for.
     * @param slot  The slot group to query modifiers for.
     */
    public static Multimap<Holder<Attribute>, AttributeModifier> getSortedModifiers(ItemStack stack, EquipmentSlotGroup slot) {
        Multimap<Holder<Attribute>, AttributeModifier> map = AttributeUtil.sortedMap();
        stack.forEachModifier(slot, (attr, modif) -> {
            if (attr != null && modif != null) {
                map.put(attr, modif);
            } else {
                LOGGER.debug("Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}", stack, attr, modif);
            }
        });
        return map;
    }

    /**
     * Creates a mutable component starting with the char used to represent a drop-down list.
     */
    private static MutableComponent list() {
        return Component.literal(" \u2507 ").withStyle(ChatFormatting.GRAY);
    }

    /**
     * Gets the current global tooltip flag. Used by {@link AttributeUtil#addPotionTooltip} since one isn't available locally.
     * 
     * @return If called on the client, the current tooltip flag, otherwise {@link TooltipFlag#NORMAL}
     */
    private static TooltipFlag getTooltipFlag() {
        if (FMLEnvironment.dist.isClient()) {
            return ClientAccess.getTooltipFlag();
        }
        return TooltipFlag.NORMAL;
    }

    /**
     * Stores a single base modifier (determined by {@link IAttributeExtension#getBaseId()}) and any other children non-base modifiers for the same attribute.
     * <p>
     * Used during attribute merging logic within {@link AttributeUtil#applyTextFor}.
     */
    private static record BaseModifier(AttributeModifier base, List<AttributeModifier> children) {}

    /**
     * Client bouncer class to avoid class loading issues. Access to this class still needs a dist check.
     */
    private static class ClientAccess {
        @SuppressWarnings("resource")
        static TooltipFlag getTooltipFlag() {
            return Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
        }
    }
}
