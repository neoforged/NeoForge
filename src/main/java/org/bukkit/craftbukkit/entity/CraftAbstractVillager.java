package org.bukkit.craftbukkit.entity;

import net.minecraft.world.item.trading.Merchant;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftMerchant;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CraftAbstractVillager extends CraftAgeable implements CraftMerchant, AbstractVillager, InventoryHolder {

    public CraftAbstractVillager(CraftServer server, net.minecraft.world.entity.npc.villager.AbstractVillager entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.npc.villager.AbstractVillager getHandle() {
        return (net.minecraft.world.entity.npc.villager.AbstractVillager) entity;
    }

    @Override
    public Merchant getMerchant() {
        return getHandle();
    }

    @Override
    public String toString() {
        return "CraftAbstractVillager";
    }

    @Override
    public Inventory getInventory() {
        return new CraftInventory(getHandle().getInventory());
    }
}
