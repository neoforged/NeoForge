/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;

/**
 * Exposes the armor or hands inventory of an {@link LivingEntity} as a {@code ResourceHandler<ItemResource>}
 * using {@link LivingEntity#getItemBySlot(EquipmentSlot)} and {@link LivingEntity#setItemSlot(EquipmentSlot, ItemStack)}.
 *
 * @see PlayerInventoryWrapper
 */
public class LivingEntityEquipmentWrapper {
    /**
     * See {@link VanillaContainerWrapper#wrappers} which is similar.
     *
     * <p>We use weak keys and values to avoid keeping a strong reference to the {@link LivingEntity} until the next time the map is cleaned.
     * As long as a slot wrapper is used, there is a strong reference to the outer {@link LivingEntityEquipmentWrapper} class,
     * which also references the living entity. This ensures that the entries remain in the map at least as long as the wrappers are in use.
     */
    private static final Map<LivingEntity, LivingEntityEquipmentWrapper> wrappers = new MapMaker().weakKeys().weakValues().makeMap();

    /**
     * Gets a wrapper for all equipment slots of a {@linkplain EquipmentSlot.Type given type}.
     *
     * @param entity        the entity whose equipment slots should be wrapped
     * @param equipmentType the type of equipment slots to wrap
     * @throws IllegalArgumentException if the entity is a player and the equipment type is neither
     *                                  {@link EquipmentSlot.Type#HAND} nor {@link EquipmentSlot.Type#HUMANOID_ARMOR}
     */
    public static ResourceHandler<ItemResource> of(LivingEntity entity, EquipmentSlot.Type equipmentType) {
        if (entity instanceof Player player) {
            return switch (equipmentType) {
                case HAND -> PlayerInventoryWrapper.of(player).getHandSlots();
                case HUMANOID_ARMOR -> PlayerInventoryWrapper.of(player).getArmorSlots();
                default -> throw new IllegalArgumentException("Wrapping the equipment type " + equipmentType + " of a player is not supported.");
            };
        }
        // Only expose a ResourceHandler in this method.
        return internalOf(entity, equipmentType);
    }

    /**
     * Gets a wrapper for a single {@linkplain EquipmentSlot equipment slot}.
     *
     * @param entity        the entity whose equipment slots should be wrapped
     * @param equipmentSlot the equipment slot to wrap
     * @throws IllegalArgumentException if the entity is a player and the equipment slot's type is neither
     *                                  {@link EquipmentSlot.Type#HAND} nor {@link EquipmentSlot.Type#HUMANOID_ARMOR}
     */
    public static ResourceHandler<ItemResource> of(LivingEntity entity, EquipmentSlot equipmentSlot) {
        if (entity instanceof Player player) {
            if (equipmentSlot == EquipmentSlot.MAINHAND) {
                return PlayerInventoryWrapper.of(player).getMainHandSlot();
            } else if (equipmentSlot == EquipmentSlot.OFFHAND) {
                return PlayerInventoryWrapper.of(player).getHandSlot(InteractionHand.OFF_HAND);
            } else if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                return PlayerInventoryWrapper.of(player).getArmorSlot(equipmentSlot);
            }
            throw new IllegalArgumentException("Wrapping the equipment slot " + equipmentSlot + " of a player is not supported.");
        }
        return internalOf(entity, equipmentSlot.getType()).getSlotWrapper(equipmentSlot.getIndex());
    }

    private static EquipmentTypeWrapper internalOf(LivingEntity entity, EquipmentSlot.Type equipmentType) {
        var wrapper = wrappers.computeIfAbsent(entity, LivingEntityEquipmentWrapper::new);
        return wrapper.byType.get(equipmentType);
    }

    private final LivingEntity entity;
    private final Map<EquipmentSlot.Type, EquipmentTypeWrapper> byType;

    private LivingEntityEquipmentWrapper(LivingEntity entity) {
        this.entity = entity;
        this.byType = new EnumMap<>(EquipmentSlot.Type.class);
        for (var equipmentType : EquipmentSlot.Type.values()) {
            var slotWrappers = new ArrayList<SlotWrapper>();
            for (var equipmentSlot : EquipmentSlot.VALUES) {
                if (equipmentSlot.getType() == equipmentType) {
                    slotWrappers.add(new SlotWrapper(equipmentSlot));
                }
            }
            this.byType.put(equipmentType, new EquipmentTypeWrapper(slotWrappers.toArray(SlotWrapper[]::new)));
        }
    }

    private class EquipmentTypeWrapper extends CombinedResourceHandler<ItemResource> {
        EquipmentTypeWrapper(SlotWrapper... handlers) {
            super(handlers);
        }

        SlotWrapper getSlotWrapper(int index) {
            return (SlotWrapper) getHandlerFromIndex(index);
        }
    }

    /**
     * The wrapper for a single {@link EquipmentSlot}, used as a building block.
     */
    private class SlotWrapper extends ItemStackResourceHandler {
        private final EquipmentSlot slot;

        private SlotWrapper(EquipmentSlot slot) {
            this.slot = slot;
        }

        @Override
        protected ItemStack getStack() {
            return entity.getItemBySlot(slot);
        }

        @Override
        protected void setStack(ItemStack stack) {
            // We pass insideTransaction = true to disable all non-transactional actions.
            entity.setItemSlot(slot, stack, true);
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return resource.toStack().canEquip(slot, entity);
        }

        @Override
        protected int getCapacity(ItemResource resource) {
            int slotLimit = slot.countLimit == 0 ? Item.ABSOLUTE_MAX_STACK_SIZE : slot.countLimit;
            return resource.isEmpty() ? slotLimit : Math.min(slotLimit, resource.getMaxStackSize());
        }

        @Override
        protected void onRootCommit(ItemStack originalState) {
            // Perform the delayed non-transactional actions
            // Note that this will not capture the details of all intermediate item changes that happened inside the transaction.
            entity.onEquipItem(slot, originalState, getStack());
        }

        @Override
        public String toString() {
            return "entity equipment wrapper[entity=" + entity + ",slot=" + slot + "]";
        }
    }
}
