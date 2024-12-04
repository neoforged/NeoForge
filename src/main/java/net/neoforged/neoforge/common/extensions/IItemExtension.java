/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.EnchantmentDefinition;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

// TODO systemic review of all extension functions. lots of unused -C
public interface IItemExtension {
    private Item self() {
        return (Item) this;
    }

    /**
     * ItemStack sensitive version of getDefaultAttributeModifiers. Used when a stack has no {@link DataComponents#ATTRIBUTE_MODIFIERS} component.
     * 
     * @see {@link IItemStackExtension#getAttributeModifiers()} for querying effective attribute modifiers.
     */
    @SuppressWarnings("deprecation")
    default ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return ItemAttributeModifiers.EMPTY;
    }

    /**
     * Called when a player drops the item into the world, returning false from this
     * will prevent the item from being removed from the players inventory and
     * spawning in the world
     *
     * @param player The player that dropped the item
     * @param item   The item stack, before the item is removed.
     */
    default boolean onDroppedByPlayer(ItemStack item, Player player) {
        return true;
    }

    /**
     * Allow the item one last chance to modify its name used for the tool highlight
     * useful for adding something extra that can't be removed by a user in the
     * displayed name, such as a mode of operation.
     *
     * @param item        the ItemStack for the item.
     * @param displayName the name that will be displayed unless it is changed in
     *                    this method.
     */
    default Component getHighlightTip(ItemStack item, Component displayName) {
        return displayName;
    }

    /**
     * This is called when the item is used, before the block is activated.
     *
     * @return Return PASS to allow vanilla handling, any other to skip normal code.
     */
    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    /**
     * Called by Piglins when checking to see if they will give an item or something in exchange for this item.
     *
     * @return True if this item can be used as "currency" by piglins
     */
    default boolean isPiglinCurrency(ItemStack stack) {
        return stack.getItem() == PiglinAi.BARTERING_ITEM;
    }

    /**
     * Called by Piglins to check if a given item prevents hostility on sight. If this is true the Piglins will be neutral to the entity wearing this item, and will not
     * attack on sight. Note: This does not prevent Piglins from becoming hostile due to other actions, nor does it make Piglins that are already hostile stop being so.
     *
     * @param wearer The entity wearing this ItemStack
     *
     * @return True if piglins are neutral to players wearing this item in an armor slot
     */
    default boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return stack.is(ItemTags.PIGLIN_SAFE_ARMOR);
    }

    /**
     * Called by CraftingManager to determine if an item is reparable.
     *
     * @return True if reparable
     */
    boolean isRepairable(ItemStack stack);

    /**
     * Determines the amount of durability the mending enchantment
     * will repair, on average, per 0.5 points of experience.
     */
    default float getXpRepairRatio(ItemStack stack) {
        return 1f;
    }

    /**
     * Called when an entity stops using an item for any reason, notably when selecting another item without releasing or finishing.
     * This method is called in addition to any other hooks called when an item is finished using; when another hook is also called it will be called before this method.
     *
     * Note that if you break an item while using it (that is, it becomes empty without swapping the stack instance), this hook may not be called on the serverside as you are
     * technically still using the empty item (thus this hook is called on air instead). It is necessary to call {@link LivingEntity#stopUsingItem()} as part of your
     * {@link ItemStack#hurtAndBreak(int, ServerLevel, LivingEntity, Consumer)} callback to prevent this issue.
     *
     * For most uses, you likely want one of the following:
     * <ul>
     * <li>{@link Item#finishUsingItem(ItemStack, Level, LivingEntity)} for when the player releases and enough ticks have passed
     * <li>{@link Item#releaseUsing(ItemStack, Level, LivingEntity, int)} (ItemStack, Level, LivingEntity)} for when the player releases but the full timer has not passed
     * </ul>
     *
     * @param stack  The Item being used
     * @param entity The entity using the item, typically a player
     * @param count  The amount of time in tick the item has been used for continuously
     */
    default void onStopUsing(ItemStack stack, LivingEntity entity, int count) {}

    /**
     * Called when the player Left Clicks (attacks) an entity. Processed before
     * damage is done, if return value is true further processing is canceled and
     * the entity is not attacked.
     *
     * @param stack  The Item being used
     * @param player The player that is attacking
     * @param entity The entity being attacked
     * @return True to cancel the rest of the interaction.
     */
    default boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return false;
    }

    /**
     * ItemStack sensitive version of {@link Item#getCraftingRemainder()} ()}.
     * Returns a full ItemStack instance of the result.
     *
     * @param itemStack The current ItemStack
     * @return The resulting ItemStack
     */
    @SuppressWarnings("deprecation")
    default ItemStack getCraftingRemainder(ItemStack itemStack) {
        return self().getCraftingRemainder();
    }

    /**
     * Retrieves the normal 'lifespan' of this item when it is dropped on the ground
     * as a EntityItem. This is in ticks, standard result is 6000, or 5 mins.
     *
     * @param itemStack The current ItemStack
     * @param level     The level the entity is in
     * @return The normal lifespan in ticks.
     */
    default int getEntityLifespan(ItemStack itemStack, Level level) {
        return 6000;
    }

    /**
     * Determines if this Item has a special entity for when they are in the world.
     * Is called when a EntityItem is spawned in the world, if true and
     * Item#createCustomEntity returns non null, the EntityItem will be destroyed
     * and the new Entity will be added to the world.
     *
     * @param stack The current item stack
     * @return True of the item has a custom entity, If true,
     *         Item#createCustomEntity will be called
     */
    default boolean hasCustomEntity(ItemStack stack) {
        return false;
    }

    /**
     * This function should return a new entity to replace the dropped item.
     * Returning null here will not kill the EntityItem and will leave it to
     * function normally. Called when the item it placed in a level.
     *
     * @param level    The level object
     * @param location The EntityItem object, useful for getting the position of
     *                 the entity
     * @param stack    The current item stack
     * @return A new Entity object to spawn or null
     */
    @Nullable
    default Entity createEntity(Level level, Entity location, ItemStack stack) {
        return null;
    }

    /**
     * Called by the default implemetation of EntityItem's onUpdate method, allowing
     * for cleaner control over the update of the item without having to write a
     * subclass.
     *
     * @param entity The entity Item
     * @return Return true to skip any further update code.
     */
    default boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        return false;
    }

    /**
     *
     * Should this item, when held, allow sneak-clicks to pass through to the
     * underlying block?
     *
     * @param level  The level
     * @param pos    Block position in level
     * @param player The Player that is wielding the item
     */
    default boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) {
        return false;
    }

    /**
     * Determines if the specific ItemStack can be placed in the specified armor
     * slot, for the entity.
     *
     * @param stack     The ItemStack
     * @param armorType Armor slot to be verified.
     * @param entity    The entity trying to equip the armor
     * @return True if the given ItemStack can be inserted in the slot
     */
    default boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(stack) == armorType;
    }

    /**
     * Override this to set a non-default armor slot for an ItemStack, but <em>do
     * not use this to get the armor slot of said stack; for that, use
     * {@link LivingEntity#getEquipmentSlotForItem(ItemStack)}..</em>
     *
     * @param stack the ItemStack
     * @return the armor slot of the ItemStack, or {@code null} to let the default
     *         vanilla logic as per {@code LivingEntity.getSlotForItemStack(stack)}
     *         decide
     */
    @Nullable
    default EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return null;
    }

    /**
     * Allow or forbid the specific book/item combination as an anvil enchant
     *
     * @param stack The item
     * @param book  The book
     * @return if the enchantment is allowed
     */
    default boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    /**
     * Called when a entity tries to play the 'swing' animation.
     *
     * @param entity The entity swinging the item.
     * @return True to cancel any further processing by {@link LivingEntity}
     */
    default boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        return false;
    }

    /**
     * Return the itemDamage represented by this ItemStack. Defaults to the Damage
     * entry in the stack NBT, but can be overridden here for other sources.
     *
     * @param stack The itemstack that is damaged
     * @return the damage value
     */
    default int getDamage(ItemStack stack) {
        return Mth.clamp(stack.getOrDefault(DataComponents.DAMAGE, 0), 0, stack.getMaxDamage());
    }

    /**
     * Return the maxDamage for this ItemStack. Defaults to the maxDamage field in
     * this item, but can be overridden here for other sources such as NBT.
     *
     * @param stack The itemstack that is damaged
     * @return the damage value
     */
    default int getMaxDamage(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAX_DAMAGE, 0);
    }

    /**
     * Return if this itemstack is damaged. Note only called if
     * {@link ItemStack#isDamageableItem()} is true.
     *
     * @param stack the stack
     * @return if the stack is damaged
     */
    default boolean isDamaged(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }

    /**
     * Set the damage for this itemstack. Note, this method is responsible for zero
     * checking.
     *
     * @param stack  the stack
     * @param damage the new damage value
     */
    default void setDamage(ItemStack stack, int damage) {
        stack.set(DataComponents.DAMAGE, Mth.clamp(damage, 0, stack.getMaxDamage()));
    }

    /**
     * Queries if an item can perform the given action.
     * See {@link ItemAbilities} for a description of each stock action
     * 
     * @param stack       The stack being used
     * @param itemAbility The action being queried
     * @return True if the stack can perform the action
     */
    default boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return false;
    }

    /**
     * Gets the maximum number of items that this stack should be able to hold.
     *
     * @param stack The ItemStack
     * @return The maximum size this item can be stacked to
     */
    default int getMaxStackSize(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    /**
     * Checks if an item should be treated as a primary item for a given enchantment.
     * <p>
     * Primary items are those that are able to receive the enchantment during enchanting,
     * either from the enchantment table or other random enchantment mechanisms.
     * As a special case, books are primary items for every enchantment.
     * <p>
     * Other application mechanisms, such as the anvil, check {@link #supportsEnchantment(ItemStack, Holder)} instead.
     * If you want those mechanisms to be able to apply an enchantment, you will need to add your item to the relevant tag or override that method.
     *
     * @param stack       the item stack to be enchanted
     * @param enchantment the enchantment to be applied
     * @return true if this item should be treated as a primary item for the enchantment
     * @apiNote Call via {@link IItemStackExtension#isPrimaryItemFor(Holder)}
     * 
     * @see #supportsEnchantment(ItemStack, Holder)
     */
    @ApiStatus.OverrideOnly
    default boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        if (stack.getItem() == Items.BOOK) {
            return true;
        }
        Optional<HolderSet<Item>> primaryItems = enchantment.value().definition().primaryItems();
        return this.supportsEnchantment(stack, enchantment) && (primaryItems.isEmpty() || stack.is(primaryItems.get()));
    }

    /**
     * Checks if the provided enchantment is applicable to the passed item stack.
     * <p>
     * By default, this checks if the {@link EnchantmentDefinition#supportedItems()} contains this item,
     * special casing enchanted books as they may receive any enchantment.
     * <p>
     * Overriding this method allows for dynamic logic that would not be possible using the tag system.
     *
     * @param stack       the item stack to be enchanted
     * @param enchantment the enchantment to be applied
     * @return true if this item can accept the enchantment
     * @apiNote Call via {@link IItemStackExtension#supportsEnchantment(Holder)}
     * 
     * @see #isPrimaryItemFor(ItemStack, Holder)
     */
    @ApiStatus.OverrideOnly
    default boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.is(Items.ENCHANTED_BOOK) || enchantment.value().isSupportedItem(stack);
    }

    /**
     * Gets the level of the enchantment currently present on the stack. By default, returns the enchantment level present in NBT.
     * Most enchantment implementations rely upon this method.
     * The returned value must be the same as getting the enchantment from {@link #getAllEnchantments}
     *
     * @param stack       The item stack being checked
     * @param enchantment The enchantment being checked for
     * @return Level of the enchantment, or 0 if not present
     * @see #getAllEnchantments
     * @apiNote Call via {@link IItemStackExtension#getEnchantmentLevel}.
     */
    @ApiStatus.OverrideOnly
    default int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        ItemEnchantments itemenchantments = stack.getTagEnchantments();
        return itemenchantments.getLevel(enchantment);
    }

    /**
     * Gets a map of all enchantments present on the stack. By default, returns the enchantments present in NBT.
     * Used in several places in code including armor enchantment hooks.
     * The returned value(s) must have the same level as {@link #getEnchantmentLevel}.
     *
     * @param stack  The item stack being checked
     * @param lookup A registry lookup, used to resolve enchantment {@link Holder}s.
     * @return Map of all enchantments on the stack, empty if no enchantments are present
     * @see #getEnchantmentLevel
     * @apiNote Call via {@link IItemStackExtension#getAllEnchantments}.
     */
    @ApiStatus.OverrideOnly
    default ItemEnchantments getAllEnchantments(ItemStack stack, RegistryLookup<Enchantment> lookup) {
        return stack.getTagEnchantments();
    }

    /**
     * Determine if the player switching between these two item stacks
     *
     * @param oldStack    The old stack that was equipped
     * @param newStack    The new stack
     * @param slotChanged If the current equipped slot was changed, Vanilla does not
     *                    play the animation if you switch between two slots that
     *                    hold the exact same item.
     * @return True to play the item change animation
     */
    default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.equals(newStack); // !ItemStack.areItemStacksEqual(oldStack, newStack);
    }

    /**
     * Called when the player is mining a block and the item in his hand changes.
     * Allows to not reset blockbreaking if only NBT or similar changes.
     *
     * @param oldStack The old stack that was used for mining. Item in players main
     *                 hand
     * @param newStack The new stack
     * @return True to reset block break progress
     */
    default boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        // Fix MC-176559 mending resets mining progress / breaking animation
        if (!newStack.is(oldStack.getItem()))
            return true;

        if (!newStack.isDamageableItem() || !oldStack.isDamageableItem())
            return !ItemStack.isSameItemSameComponents(newStack, oldStack);

        DataComponentMap newComponents = newStack.getComponents();
        DataComponentMap oldComponents = oldStack.getComponents();

        if (newComponents.isEmpty() || oldComponents.isEmpty())
            return !(newComponents.isEmpty() && oldComponents.isEmpty());

        Set<DataComponentType<?>> newKeys = new HashSet<>(newComponents.keySet());
        Set<DataComponentType<?>> oldKeys = new HashSet<>(oldComponents.keySet());

        newKeys.remove(DataComponents.DAMAGE);
        oldKeys.remove(DataComponents.DAMAGE);

        if (!newKeys.equals(oldKeys))
            return true;

        return !newKeys.stream().allMatch(key -> Objects.equals(newComponents.get(key), oldComponents.get(key)));
    }

    /**
     * Called while an item is in 'active' use to determine if usage should
     * continue. Allows items to continue being used while sustaining damage, for
     * example.
     *
     * @param oldStack the previous 'active' stack
     * @param newStack the stack currently in the active hand
     * @return true to set the new stack to active and continue using it
     */
    default boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        if (oldStack == newStack) {
            return true;
        } else {
            return !oldStack.isEmpty() && !newStack.isEmpty() && ItemStack.isSameItem(newStack, oldStack);
        }
    }

    /**
     * Called to get the Mod ID of the mod that *created* the ItemStack, instead of
     * the real Mod ID that *registered* it.
     *
     * For example the Forge Universal Bucket creates a subitem for each modded
     * fluid, and it returns the modded fluid's Mod ID here.
     *
     * Mods that register subitems for other mods can override this. Informational
     * mods can call it to show the mod that created the item.
     *
     * @param itemStack the ItemStack to check
     * @return the Mod ID for the ItemStack, or null when there is no specially
     *         associated mod and {@link net.minecraft.core.Registry#getKey(Object)} would return null.
     */
    @Nullable
    default String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {
        return CommonHooks.getDefaultCreatorModId(registries, itemStack);
    }

    /**
     * Can this Item disable a shield
     *
     * @param stack    The ItemStack
     * @param shield   The shield in question
     * @param entity   The LivingEntity holding the shield
     * @param attacker The LivingEntity holding the ItemStack
     * @return True if this ItemStack can disable the shield in question.
     */
    default boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return this instanceof AxeItem;
    }

    /**
     * @return the fuel burn time for this item stack in a furnace. Return 0 to make it not act as a fuel.
     * @apiNote This method takes precedence over the {@link net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps#FURNACE_FUELS data map}.
     *          However, you should use the data map unless necessary (i.e. NBT-based burn times) so that users can configure burn times.
     */
    @ApiStatus.OverrideOnly
    default int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
        return fuelValues.burnDuration(itemStack);
    }

    /**
     * Called every tick when this item is equipped {@linkplain DataComponents#EQUIPPABLE as an armor item} by an animal.
     * <p>
     * In vanilla, only {@linkplain Horse horses} and {@linkplain Wolf wolves} can wear armor, and they can only equip items that extend {@link AnimalArmorItem}.
     *
     * @param stack The armor stack
     * @param level The level the horse is in
     * @param horse The horse wearing this item
     * @apiNote Call from {@link IItemStackExtension#onAnimalArmorTick(Level, Mob)}.
     */
    @ApiStatus.OverrideOnly
    default void onAnimalArmorTick(ItemStack stack, Level level, Mob horse) {}

    /**
     * Reduce the durability of this item by the amount given.
     * This can be used to e.g. consume power from NBT before durability.
     *
     * @param stack    The itemstack to damage
     * @param amount   The amount to damage
     * @param entity   The entity damaging the item
     * @param onBroken The on-broken callback from vanilla
     * @return The amount of damage to pass to the vanilla logic
     */
    default <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return amount;
    }

    /**
     * Called when an item entity for this stack is destroyed. Note: The {@link ItemStack} can be retrieved from the item entity.
     *
     * @param itemEntity   The item entity that was destroyed.
     * @param damageSource Damage source that caused the item entity to "die".
     */
    default void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        self().onDestroyed(itemEntity);
    }

    /**
     * Whether this {@link Item} can be used to hide player's gaze from Endermen and Creakings.
     *
     * @param stack  the ItemStack
     * @param player The player watching the entity
     * @param entity The entity the player is looking at, may be null
     * @return true if this {@link Item} hides the player's gaze from the given entity
     */
    default boolean isGazeDisguise(ItemStack stack, Player player, @Nullable LivingEntity entity) {
        return stack.is(ItemTags.GAZE_DISGUISE_EQUIPMENT);
    }

    /**
     * Called by the powdered snow block to check if a living entity wearing this can walk on the snow, granting the same behavior as leather boots.
     * Only affects items worn in the boots slot.
     *
     * @param stack  Stack instance
     * @param wearer The entity wearing this ItemStack
     *
     * @return True if the entity can walk on powdered snow
     */
    default boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return stack.is(Items.LEATHER_BOOTS);
    }

    /**
     * Used to test if this item can be damaged, but with the ItemStack in question.
     * Please note that in some cases no ItemStack is available, so the stack-less method will be used.
     *
     * @param stack ItemStack in the Chest slot of the entity.
     */
    default boolean isDamageable(ItemStack stack) {
        return stack.has(DataComponents.MAX_DAMAGE);
    }

    /**
     * Get a bounding box ({@link AABB}) of a sweep attack.
     *
     * @param stack  the stack held by the player.
     * @param player the performing the attack the attack.
     * @param target the entity targeted by the attack.
     * @return the bounding box.
     */

    default AABB getSweepHitBox(ItemStack stack, Player player, Entity target) {
        return target.getBoundingBox().inflate(1.0D, 0.25D, 1.0D);
    }

    /**
     * Whether the given ItemStack should be excluded (if possible) when selecting the target hotbar slot of a "pick" action.
     * By default, this returns true for enchanted stacks.
     *
     * @see Inventory#getSuitableHotbarSlot()
     * @param player        the player performing the picking
     * @param inventorySlot the inventory slot of the item being up for replacement
     * @return true to leave this stack in the hotbar if possible
     */
    default boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        return stack.isEnchanted();
    }

    /**
     * {@return true if the given ItemStack can be put into a grindstone to be repaired and/or stripped of its enchantments}
     */
    default boolean canGrindstoneRepair(ItemStack stack) {
        return false;
    }

    /**
     * {@return false to make item entity immune to the damage.}
     */
    default boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return true;
    }

    /**
     * Handles enchanting an item (i.e. in the enchanting table), potentially transforming it to a new item in the process.
     * <p>
     * {@linkplain Items#BOOK Books} use this functionality to transform themselves into enchanted books.
     *
     * @param stack        The stack being enchanted.
     * @param enchantments The enchantments being applied.
     * @return The newly-enchanted stack.
     */
    default ItemStack applyEnchantments(ItemStack stack, List<EnchantmentInstance> enchantments) {
        if (stack.is(Items.BOOK)) {
            stack = stack.transmuteCopy(Items.ENCHANTED_BOOK);
        }

        for (EnchantmentInstance inst : enchantments) {
            stack.enchant(inst.enchantment, inst.level);
        }

        return stack;
    }
}
