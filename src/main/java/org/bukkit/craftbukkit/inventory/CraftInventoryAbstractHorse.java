package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryAbstractHorse extends CraftInventory implements AbstractHorseInventory {

    protected final EntityEquipment equipment;

    public CraftInventoryAbstractHorse(Container inventory, EntityEquipment equipment) {
        super(inventory);
        this.equipment = equipment;
    }

    @Override
    public ItemStack getSaddle() {
        net.minecraft.world.item.ItemStack item = equipment.get(EquipmentSlot.SADDLE);
        return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
    }

    @Override
    public void setSaddle(ItemStack stack) {
        equipment.set(EquipmentSlot.SADDLE, CraftItemStack.asNMSCopy(stack));
    }
}
