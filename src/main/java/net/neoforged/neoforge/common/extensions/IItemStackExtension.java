/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jspecify.annotations.Nullable;

/*
 * Extension added to ItemStack that bounces to ItemSack sensitive Item methods. Typically this is just for convince.
 */
public interface IItemStackExtension extends ItemInstanceExtension {
    // Helpers for accessing Item data
    private ItemStack self() {
        return (ItemStack) this;
    }

    @Override
    default int getBurnTime(@Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
        // templates do not support empty types so we override here to return 0 when empty
        // and delegate back to super for non-empty
        return self().isEmpty() ? 0 : ItemInstanceExtension.super.getBurnTime(recipeType, fuelValues);
    }

    default InteractionResult onItemUseFirst(UseOnContext context) {
        Player entityplayer = context.getPlayer();
        BlockPos blockpos = context.getClickedPos();
        BlockInWorld blockworldstate = new BlockInWorld(context.getLevel(), blockpos, false);
        AdventureModePredicate adventureModePredicate = self().get(DataComponents.CAN_PLACE_ON);
        if (entityplayer != null && !entityplayer.getAbilities().mayBuild && (adventureModePredicate == null || !adventureModePredicate.test(blockworldstate))) {
            return InteractionResult.PASS;
        } else {
            Item item = self().getItem();
            InteractionResult enumactionresult = item.onItemUseFirst(self(), context);
            if (entityplayer != null && enumactionresult == InteractionResult.SUCCESS) {
                entityplayer.awardStat(Stats.ITEM_USED.get(item));
            }

            return enumactionresult;
        }
    }

    /**
     * Called when the player is mining a block and the item in his hand changes.
     * Allows to not reset blockbreaking if only NBT or similar changes.
     *
     * @param newStack The new stack
     * @return True to reset block break progress
     */
    default boolean shouldCauseBlockBreakReset(ItemStack newStack) {
        return self().getItem().shouldCauseBlockBreakReset(self(), newStack);
    }

    /**
     * Called when a entity tries to play the 'swing' animation.
     *
     * @param entity The entity swinging the item.
     * @param hand   The hand the item is held in.
     * @return True to cancel any further processing by {@link LivingEntity}
     */
    default boolean onEntitySwing(LivingEntity entity, InteractionHand hand) {
        return self().getItem().onEntitySwing(self(), entity, hand);
    }

    /**
     * Called when an entity stops using an item item for any reason.
     *
     * @param entity The entity using the item, typically a player
     * @param count  The amount of time in tick the item has been used for continuously
     */
    default void onStopUsing(LivingEntity entity, int count) {
        self().getItem().onStopUsing(self(), entity, count);
    }

    /**
     * Called by the default implemetation of EntityItem's onUpdate method, allowing
     * for cleaner control over the update of the item without having to write a
     * subclass.
     *
     * @param entity The entity Item
     * @return Return true to skip any further update code.
     */
    default boolean onEntityItemUpdate(ItemEntity entity) {
        return self().getItem().onEntityItemUpdate(self(), entity);
    }

    /**
     * Called every tick when this item is {@link DataComponents#EQUIPPABLE equipped} {@link EquipmentSlot#BODY as an armor item} by a {@link Mob} that can wear armor.
     *
     * @param level The level the horse is in
     * @param horse The horse wearing this item
     */
    default void onAnimalArmorTick(Level level, Mob horse) {
        self().getItem().onAnimalArmorTick(self(), level, horse);
    }

    /**
     * Called when a player drops the item into the world, returning false from this
     * will prevent the item from being removed from the players inventory and
     * spawning in the world
     *
     * @param player The player that dropped the item
     */
    default boolean onDroppedByPlayer(Player player) {
        return self().getItem().onDroppedByPlayer(self(), player);
    }

    /**
     * Allow the item one last chance to modify its name used for the tool highlight
     * useful for adding something extra that can't be removed by a user in the
     * displayed name, such as a mode of operation.
     *
     * @param displayName the name that will be displayed unless it is changed in
     *                    this method.
     */
    default Component getHighlightTip(Component displayName) {
        return self().getItem().getHighlightTip(self(), displayName);
    }

    @Override
    default boolean doesSneakBypassUse(LevelReader level, BlockPos pos, Player player) {
        return self().isEmpty() || ItemInstanceExtension.super.doesSneakBypassUse(level, pos, player);
    }

    /**
     * Called when an item entity for this stack is destroyed. Note: The {@link ItemStack} can be retrieved from the item entity.
     *
     * @param itemEntity   The item entity that was destroyed.
     * @param damageSource Damage source that caused the item entity to "die".
     */
    default void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        self().getItem().onDestroyed(itemEntity, damageSource);
    }

    @Nullable
    default <T, C extends @Nullable Object> T getCapability(ItemCapability<T, C> capability, C context) {
        return capability.getCapability(self(), context);
    }

    @Nullable
    default <T> T getCapability(ItemCapability<T, @Nullable Void> capability) {
        return capability.getCapability(self(), null);
    }
}
