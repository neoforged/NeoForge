package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryHorse extends CraftInventoryAbstractHorse implements HorseInventory {

    public CraftInventoryHorse(Container inventory, EntityEquipment equipment) {
        super(inventory, equipment);
    }

    @Override
    public ItemStack getArmor() {
        net.minecraft.world.item.ItemStack item = equipment.get(EquipmentSlot.BODY);
        return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
    }

    @Override
    public void setArmor(ItemStack stack) {
        equipment.set(EquipmentSlot.BODY, CraftItemStack.asNMSCopy(stack));
    }
}
