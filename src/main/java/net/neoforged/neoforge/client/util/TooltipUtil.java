/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.util;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import net.neoforged.neoforge.common.util.AttributeUtil;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.Nullable;

/**
 * Tooltip generating utility code to support {@link IAttributeExtension}.
 * 
 * @see {@link AttributeUtil} for additional non-tooltip code
 */
public class TooltipUtil {
    public static final ResourceLocation FAKE_MERGED_ID = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "fake_merged_modifier");

    /**
     * Checks if attribute modifer tooltips should show, and if they should, adds tooltips for all attribute modifiers present on an item stack to the stack's tooltip lines.
     * <p>
     * After the tooltip lines have been added, fires the {@link AddAttributeTooltipsEvent} to allow mods to add additional attribute-related lines.
     * 
     * @param tooltip A consumer to add the tooltip lines to.
     * @param ctx     The tooltip context
     */
    public static void addAttributeTooltips(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (modifiers.showInTooltip()) {
            applyModifierTooltips(stack, tooltip, ctx);
        }
        NeoForge.EVENT_BUS.post(new AddAttributeTooltipsEvent(stack, tooltip, ctx));
    }

    /**
     * Unconditionally applies the attribute modifier tooltips for all attribute modifiers present on the item stack.
     * <p>
     * Before application, this method posts the {@link GatherSkippedAttributeTooltipsEvent} to determine which tooltips should be skipped.
     * 
     * @param stack
     * @param tooltip
     * @param ctx
     */
    public static void applyModifierTooltips(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx) {
        Set<ResourceLocation> skips = new HashSet<>();
        var event = NeoForge.EVENT_BUS.post(new GatherSkippedAttributeTooltipsEvent(stack, skips, ctx));
        if (event.isSkippingAll()) {
            return;
        }

        for (EquipmentSlotGroup group : EquipmentSlotGroup.values()) {
            Multimap<Holder<Attribute>, AttributeModifier> modifiers = AttributeUtil.getSortedModifiers(stack, group);
            Component groupName = Component.translatable("item.modifiers." + group.getSerializedName()).withStyle(ChatFormatting.GRAY);
            applyTextFor(stack, tooltip, modifiers, groupName, skips, ctx);
        }
    }

    /**
     * Applies the text for a single group of modifiers to the tooltip for a given item stack.
     * <p>
     * This method will attempt to merge multiple modifiers for a single attribute into a single modifier if {@linkplain NeoForgeMod#enableMergedAttributeTooltips()} was called.
     * 
     * @param stack       The item stack that owns the modifiers
     * @param tooltip     The consumer to append tooltip components to
     * @param modifierMap A mutable map of modifiers for the given group
     * @param groupName   The name of the current modifier group
     * @param skips       A set of modifier IDs to not apply to the tooltip
     * @param ctx         The tooltip context
     */
    public static void applyTextFor(ItemStack stack, Consumer<Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> modifierMap, Component groupName, Set<ResourceLocation> skips, AttributeTooltipContext ctx) {
        // Remove any skipped modifiers before doing any logic
        modifierMap.values().removeIf(m -> skips.contains(m.id()));

        // Don't add anything if there is nothing in the group
        if (modifierMap.isEmpty()) {
            return;
        }

        // Add an empty line, then the name of the group
        tooltip.accept(Component.empty());
        tooltip.accept(groupName);

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
            tooltip.accept(padded(" ", text).withStyle(isMerged ? ChatFormatting.GOLD : ChatFormatting.DARK_GREEN));
            if (ctx.flag().hasShiftDown() && isMerged) {
                // Display the raw base value, and then all children modifiers.
                text = attr.value().toBaseComponent(rawBase, entityBase, false, ctx.flag());
                tooltip.accept(list().append(text.withStyle(ChatFormatting.DARK_GREEN)));
                for (AttributeModifier modifier : baseModif.children) {
                    tooltip.accept(list().append(attr.value().toComponent(modifier, ctx.flag())));
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
                        var fakeModif = new AttributeModifier(FAKE_MERGED_ID, sums[i], op);
                        MutableComponent comp = attr.value().toComponent(fakeModif, ctx.flag());
                        tooltip.accept(comp.withStyle(comp.getStyle().withColor(color)));
                        if (ctx.flag().hasShiftDown() && merged[i]) {
                            shiftExpands.get(Operation.BY_ID.apply(i)).forEach(modif -> tooltip.accept(list().append(attr.value().toComponent(modif, ctx.flag()))));
                        }
                    } else {
                        var fakeModif = new AttributeModifier(FAKE_MERGED_ID, sums[i], op);
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
     * @param tooltips The tooltip consumer to add lines to
     */
    public static void addPotionTooltip(List<Pair<Holder<Attribute>, AttributeModifier>> list, Consumer<Component> tooltips) {
        if (!list.isEmpty()) {
            tooltips.accept(CommonComponents.EMPTY);
            tooltips.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Holder<Attribute>, AttributeModifier> pair : list) {
                tooltips.accept(pair.getFirst().value().toComponent(pair.getSecond(), getTooltipFlag()));
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
     * Gets the current tooltip flag.
     * 
     * @return If called on the client, the current tooltip flag, otherwise {@link TooltipFlag#NORMAL}
     */
    public static TooltipFlag getTooltipFlag() {
        if (FMLEnvironment.dist.isClient()) {
            return ClientAccess.getTooltipFlag();
        }
        return TooltipFlag.NORMAL;
    }

    /**
     * Creates a literal component holding {@code padding} and appends {@code comp} to it, returning the result.
     */
    private static MutableComponent padded(String padding, Component comp) {
        return Component.literal(padding).append(comp);
    }

    public static record BaseModifier(AttributeModifier base, List<AttributeModifier> children) {}

    /**
     * Extended {@link TooltipContext} used when generating attribute tooltips.
     */
    public static interface AttributeTooltipContext extends Item.TooltipContext {
        /**
         * {@return the player for whom tooltips are being generated for, if known}
         */
        @Nullable
        Player player();

        /**
         * {@return the current tooltip flag}
         */
        TooltipFlag flag();

        public static AttributeTooltipContext of(@Nullable Player player, Item.TooltipContext itemCtx, TooltipFlag flag) {
            return new AttributeTooltipContext() {
                @Override
                public Provider registries() {
                    return itemCtx.registries();
                }

                @Override
                public float tickRate() {
                    return itemCtx.tickRate();
                }

                @Override
                public MapItemSavedData mapData(MapId id) {
                    return itemCtx.mapData(id);
                }

                @Override
                public Level level() {
                    return itemCtx.level();
                }

                @Nullable
                @Override
                public Player player() {
                    return player;
                }

                @Override
                public TooltipFlag flag() {
                    return flag;
                }
            };
        }
    }

    private static class ClientAccess {
        static TooltipFlag getTooltipFlag() {
            return Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
        }
    }
}
