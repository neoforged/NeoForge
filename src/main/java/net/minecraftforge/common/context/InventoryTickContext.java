package net.minecraftforge.common.context;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeItem;

public class InventoryTickContext
{

    public static final ContextKey<PlayerInvContext> KEY = ContextKey.getOrCreate(new ResourceLocation("forge", "player_inv"), PlayerInvContext.class);

    /**
     * Implementing this on a context object passed to {@link IForgeItem#inventoryTick} will cause the vanilla {@link Item#inventoryTick(ItemStack, Level, Entity, int, boolean)} to be called.
     */
    public static interface VanillaCompatible
    {
        /**
         * The entity that owns the inventory.
         */
        Entity getEntity();

        /**
         * The slot of the compartment being ticked (not the global slot index)
         */
        int getSlot();

        /**
         * If the current slot is the selected hotbar slot.
         */
        boolean isSelected();
    }

    /**
     * Additional Player Inventory Context for use in {@link IForgeItem#inventoryTick}.
     * 
     * @param player      The player whose inventory is being ticked.
     * @param compartment The inventory compartment being ticked.
     * @param invId       The name of the player inventory compartment being ticked. See the static fields in this class.
     * @param slot        The slot in the compartment where the current item is.
     * @param selected    If the compartment is {@link #PLAYER_ITEMS}, this is the index of the selected hotbar slot, otherwise -1.
     */
    public static record PlayerInvContext(Player player, NonNullList<ItemStack> compartment, ResourceLocation invId, int slot, int selected) implements VanillaCompatible
    {

        /**
         * Compartment ID for the player main inventory.
         * 
         * @see Inventory#items
         */
        public static final ResourceLocation PLAYER_ITEMS = new ResourceLocation("items");

        /**
         * Compartment ID for the player armor inventory.
         * 
         * @see Inventory#armor
         */
        public static final ResourceLocation PLAYER_ARMOR = new ResourceLocation("armor");

        /**
         * Compartment ID for the player offhand inventory.
         * 
         * @see Inventory#offhand
         */
        public static final ResourceLocation PLAYER_OFFHAND = new ResourceLocation("offhand");

        @Override
        public Entity getEntity()
        {
            return this.player;
        }

        @Override
        public int getSlot()
        {
            return this.slot;
        }

        @Override
        public boolean isSelected()
        {
            return this.slot == this.selected;
        }

        /**
         * This is in a separate method so it is only invoked once per compartment instead of once per slot.
         */
        @ApiStatus.Internal
        public static ResourceLocation getCompartmentId(Inventory inv, NonNullList<ItemStack> compartment)
        {
            return compartment == inv.items ? PLAYER_ITEMS : compartment == inv.armor ? PLAYER_ARMOR : PLAYER_OFFHAND;
        }

        @ApiStatus.Internal
        public static final ContextKey.Context<PlayerInvContext, PlayerInvContext> create(Inventory inv, NonNullList<ItemStack> compartment, ResourceLocation id, int slot, int selected)
        {
            return KEY.createCtx(new PlayerInvContext(inv.player, compartment, id, slot, compartment == inv.items ? selected : -1));
        }
    }

}
