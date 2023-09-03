package net.minecraftforge.event;

import java.util.Map;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is fired whenever the enchantment level of a particular item is requested for gameplay purposes.<br>
 * It is called from {@link IForgeItemStack#getEnchantmentLevel(Enchantment)} and {@link IForgeItemStack#getAllEnchantments()}.
 * <p>
 * It is not fired for interactions with NBT, which means these changes will not reflect in the item tooltip.
 * <p>
 * This event is fired on {@link MinecraftForge#EVENT_BUS}.<br>
 * This event is not cancellable.<br>
 * This event does not have a result.
 */
public class GetEnchantmentLevelEvent extends Event
{
    protected final ItemStack stack;
    protected final Map<Enchantment, Integer> enchantments;

    public GetEnchantmentLevelEvent(ItemStack stack, Map<Enchantment, Integer> enchantments)
    {
        this.stack = stack;
        this.enchantments = enchantments;
    }

    /**
     * Returns the item stack that is being queried.
     */
    public ItemStack getStack()
    {
        return this.stack;
    }

    /**
     * Returns the mutable enchantment->level map.
     */
    public Map<Enchantment, Integer> getEnchantments()
    {
        return this.enchantments;
    }
}
