/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

/*
 * Extension added to ItemStack that bounces to ItemSack sensitive Item methods. Typically this is just for convince.
 */
public interface IItemStackExtension {
    // Helpers for accessing Item data
    private ItemStack self() {
        return (ItemStack) this;
    }

    /**
     * ItemStack sensitive version of {@link Item#getCraftingRemainingItem()}.
     * Returns a full ItemStack instance of the result.
     *
     * @return The resulting ItemStack
     */
    default ItemStack getCraftingRemainingItem() {
        return self().getItem().getCraftingRemainingItem(self());
    }

    /**
     * ItemStack sensitive version of {@link Item#hasCraftingRemainingItem()}.
     *
     * @return True if this item has a crafting remaining item
     */
    default boolean hasCraftingRemainingItem() {
        return self().getItem().hasCraftingRemainingItem(self());
    }

    /**
     * Returns the fuel burn time for this item stack. If it is zero, this item is not a fuel.
     * <p>
     * Will never return a negative value.
     * 
     * @return the fuel burn time for this item stack in a furnace.
     * @apiNote This method by default returns the {@code burn_time} specified in
     *          the {@code furnace_fuels.json} file.
     */
    default int getBurnTime(@Nullable RecipeType<?> recipeType) {
        if (self().isEmpty()) {
            return 0;
        }
        int burnTime = self().getItem().getBurnTime(self(), recipeType);
        if (burnTime < 0) {
            throw new IllegalStateException("Stack of item " + BuiltInRegistries.ITEM.getKey(self().getItem()) + " has a negative burn time");
        }
        return EventHooks.getItemBurnTime(self(), burnTime, recipeType);
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
     * Queries if an item can perform the given action.
     * See {@link ToolActions} for a description of each stock action
     * 
     * @param toolAction The action being queried
     * @return True if the stack can perform the action
     */
    default boolean canPerformAction(ToolAction toolAction) {
        return self().getItem().canPerformAction(self(), toolAction);
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
     * @see {@link IItemExtension#isPrimaryItemFor(ItemStack, Holder)}
     */
    default boolean isPrimaryItemFor(Holder<Enchantment> enchantment) {
        return self().getItem().isPrimaryItemFor(self(), enchantment);
    }

    /**
     * Gets the gameplay level of the target enchantment on this stack.
     * <p>
     * Use in place of {@link EnchantmentHelper#getTagEnchantmentLevel} for gameplay logic.
     * <p>
     * Use {@link EnchantmentHelper#getEnchantmentsForCrafting} and {@link EnchantmentHelper#setEnchantments} when modifying the item's enchantments.
     *
     * @param enchantment The enchantment being checked for.
     * @return The level of the enchantment, or 0 if not present.
     * @see {@link #getAllEnchantments} to get all gameplay enchantments
     */
    default int getEnchantmentLevel(Holder<Enchantment> enchantment) {
        int level = self().getItem().getEnchantmentLevel(self(), enchantment);
        return EventHooks.getEnchantmentLevelSpecific(level, self(), enchantment);
    }

    /**
     * Gets the gameplay level of all enchantments on this stack.
     * <p>
     * Use in place of {@link ItemStack#getTagEnchantments()} for gameplay logic.
     * <p>
     * Use {@link EnchantmentHelper#getEnchantmentsForCrafting} and {@link EnchantmentHelper#setEnchantments} when modifying the item's enchantments.
     *
     * @return Map of all enchantments on the stack, or an empty map if no enchantments are present.
     * @see {@link #getEnchantmentLevel} to get the level of a single enchantment for gameplay purposes
     */
    default ItemEnchantments getAllEnchantments(RegistryLookup<Enchantment> lookup) {
        var enchantments = self().getItem().getAllEnchantments(self(), lookup);
        return EventHooks.getAllEnchantmentLevels(enchantments, self(), lookup);
    }

    /**
     * ItemStack sensitive version of {@link Item#getEnchantmentValue()}.
     *
     * @return the enchantment value of this ItemStack
     */
    default int getEnchantmentValue() {
        return self().getItem().getEnchantmentValue(self());
    }

    /**
     * Override this to set a non-default armor slot for an ItemStack, but <em>do
     * not use this to get the armor slot of said stack; for that, use
     * {@link LivingEntity#getEquipmentSlotForItem(ItemStack)}.</em>
     *
     * @return the armor slot of the ItemStack, or {@code null} to let the default
     *         vanilla logic as per {@code LivingEntity.getSlotForItemStack(stack)}
     *         decide
     */
    @Nullable
    default EquipmentSlot getEquipmentSlot() {
        return self().getItem().getEquipmentSlot(self());
    }

    /**
     * Can this Item disable a shield
     *
     * @param shield   The shield in question
     * @param entity   The LivingEntity holding the shield
     * @param attacker The LivingEntity holding the ItemStack
     * @return True if this ItemStack can disable the shield in question.
     */
    default boolean canDisableShield(ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return self().getItem().canDisableShield(self(), shield, entity, attacker);
    }

    /**
     * Called when a entity tries to play the 'swing' animation.
     *
     * @param entity The entity swinging the item.
     * @return True to cancel any further processing by EntityLiving
     */
    default boolean onEntitySwing(LivingEntity entity) {
        return self().getItem().onEntitySwing(self(), entity);
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
     * Retrieves the normal 'lifespan' of this item when it is dropped on the ground
     * as a EntityItem. This is in ticks, standard result is 6000, or 5 mins.
     *
     * @param level The level the entity is in
     * @return The normal lifespan in ticks.
     */
    default int getEntityLifespan(Level level) {
        return self().getItem().getEntityLifespan(self(), level);
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
     * Determines the amount of durability the mending enchantment
     * will repair, on average, per point of experience.
     */
    default float getXpRepairRatio() {
        return self().getItem().getXpRepairRatio(self());
    }

    /**
     * Called every tick when this item is equipped {@linkplain Mob#isBodyArmorItem(ItemStack) as an armor item} by a horse {@linkplain Mob#canWearBodyArmor()} that can wear armor}.
     * <p>
     * In vanilla, only {@linkplain Horse horses} and {@linkplain Wolf wolves} can wear armor, and they can only equip items that extend {@link AnimalArmorItem}.
     *
     * @param level The level the horse is in
     * @param horse The horse wearing this item
     */
    default void onAnimalArmorTick(Level level, Mob horse) {
        self().getItem().onAnimalArmorTick(self(), level, horse);
    }

    /**
     * Determines if the specific ItemStack can be placed in the specified armor
     * slot, for the entity.
     *
     * @param armorType Armor slot to be verified.
     * @param entity    The entity trying to equip the armor
     * @return True if the given ItemStack can be inserted in the slot
     */
    default boolean canEquip(EquipmentSlot armorType, LivingEntity entity) {
        return self().getItem().canEquip(self(), armorType, entity);
    }

    /**
     * Allow or forbid the specific book/item combination as an anvil enchant
     *
     * @param book The book
     * @return if the enchantment is allowed
     */
    default boolean isBookEnchantable(ItemStack book) {
        return self().getItem().isBookEnchantable(self(), book);
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

    /**
     *
     * Should this item, when held, allow sneak-clicks to pass through to the underlying block?
     *
     * @param level  The level
     * @param pos    Block position in level
     * @param player The Player that is wielding the item
     */
    default boolean doesSneakBypassUse(net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) {
        return self().isEmpty() || self().getItem().doesSneakBypassUse(self(), level, pos, player);
    }

    /**
     * Determines if a item is reparable, used by Repair recipes and Grindstone.
     *
     * @return True if reparable
     */
    default boolean isRepairable() {
        return self().getItem().isRepairable(self());
    }

    /**
     * Called by Piglins when checking to see if they will give an item or something in exchange for this item.
     *
     * @return True if this item can be used as "currency" by piglins
     */
    default boolean isPiglinCurrency() {
        return self().getItem().isPiglinCurrency(self());
    }

    /**
     * Called by Piglins to check if a given item prevents hostility on sight. If this is true the Piglins will be neutral to the entity wearing this item, and will not
     * attack on sight. Note: This does not prevent Piglins from becoming hostile due to other actions, nor does it make Piglins that are already hostile stop being so.
     *
     * @param wearer The entity wearing this ItemStack
     *
     * @return True if piglins are neutral to players wearing this item in an armor slot
     */
    default boolean makesPiglinsNeutral(LivingEntity wearer) {
        return self().getItem().makesPiglinsNeutral(self(), wearer);
    }

    /**
     * Whether this Item can be used to hide player head for enderman.
     *
     * @param player         The player watching the enderman
     * @param endermanEntity The enderman that the player look
     * @return true if this Item can be used.
     */
    default boolean isEnderMask(Player player, EnderMan endermanEntity) {
        return self().getItem().isEnderMask(self(), player, endermanEntity);
    }

    /**
     * Used to determine if the player can use Elytra flight.
     * This is called Client and Server side.
     *
     * @param entity The entity trying to fly.
     * @return True if the entity can use Elytra flight.
     */
    default boolean canElytraFly(LivingEntity entity) {
        return self().getItem().canElytraFly(self(), entity);
    }

    /**
     * Used to determine if the player can continue Elytra flight,
     * this is called each tick, and can be used to apply ItemStack damage,
     * consume Energy, or what have you.
     * For example the Vanilla implementation of this, applies damage to the
     * ItemStack every 20 ticks.
     *
     * @param entity      The entity currently in Elytra flight.
     * @param flightTicks The number of ticks the entity has been Elytra flying for.
     * @return True if the entity should continue Elytra flight or False to stop.
     */
    default boolean elytraFlightTick(LivingEntity entity, int flightTicks) {
        return self().getItem().elytraFlightTick(self(), entity, flightTicks);
    }

    /**
     * Called by the powdered snow block to check if a living entity wearing this can walk on the snow, granting the same behavior as leather boots.
     * Only affects items worn in the boots slot.
     *
     * @param wearer The entity wearing this ItemStack
     *
     * @return True if the entity can walk on powdered snow
     */
    default boolean canWalkOnPowderedSnow(LivingEntity wearer) {
        return self().getItem().canWalkOnPowderedSnow(self(), wearer);
    }

    /**
     * Get a bounding box ({@link AABB}) of a sweep attack.
     *
     * @param player the performing the attack the attack.
     * @param target the entity targeted by the attack.
     * @return the bounding box.
     */

    default AABB getSweepHitBox(Player player, Entity target) {
        return self().getItem().getSweepHitBox(self(), player, target);
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

    /**
     * Get the food properties for this item.
     * This is a bouncer for easier use of {@link IItemExtension#getFoodProperties(ItemStack, LivingEntity)}
     *
     * The @Nullable annotation was only added, due to the default method, also being @Nullable.
     * Use this with a grain of salt, as if you return null here and true at {@link Item#isEdible()}, NPEs will occur!
     *
     * @param entity The entity which wants to eat the food. Be aware that this can be null!
     * @return The current FoodProperties for the item.
     */
    @Nullable // read javadoc to find a potential problem
    default FoodProperties getFoodProperties(@Nullable LivingEntity entity) {
        return self().getItem().getFoodProperties(self(), entity);
    }

    /**
     * Whether this stack should be excluded (if possible) when selecting the target hotbar slot of a "pick" action.
     * By default, this returns true for enchanted stacks.
     *
     * @see Inventory#getSuitableHotbarSlot()
     * @param player        the player performing the picking
     * @param inventorySlot the inventory slot of the item being up for replacement
     * @return true to leave this stack in the hotbar if possible
     */
    default boolean isNotReplaceableByPickAction(Player player, int inventorySlot) {
        return self().getItem().isNotReplaceableByPickAction(self(), player, inventorySlot);
    }

    /**
     * {@return true if the given ItemStack can be put into a grindstone to be repaired and/or stripped of its enchantments}
     */
    default boolean canGrindstoneRepair() {
        return self().getItem().canGrindstoneRepair(self());
    }

    @Nullable
    default <T, C> T getCapability(ItemCapability<T, C> capability, C context) {
        return capability.getCapability(self(), context);
    }

    @Nullable
    default <T> T getCapability(ItemCapability<T, Void> capability) {
        return capability.getCapability(self(), null);
    }

    /**
     * Computes the gameplay attribute modifiers for this item stack. Used in place of direct access to {@link DataComponents.ATTRIBUTE_MODIFIERS}
     * or {@link Item#getDefaultAttributeModifiers(ItemStack)} when querying attributes for gameplay purposes.
     * <p>
     * This method first computes the default modifiers, using {@link DataComponents.ATTRIBUTE_MODIFIERS} if present, otherwise
     * falling back to {@link Item#getDefaultAttributeModifiers(ItemStack)}.
     * <p>
     * The {@link ItemAttributeModifiersEvent} is then fired to allow external adjustments.
     */
    default ItemAttributeModifiers getAttributeModifiers() {
        ItemAttributeModifiers defaultModifiers = self().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        if (defaultModifiers.modifiers().isEmpty()) {
            defaultModifiers = self().getItem().getDefaultAttributeModifiers(self());
        }

        return CommonHooks.computeModifiedAttributes(self(), defaultModifiers);
    }
}
