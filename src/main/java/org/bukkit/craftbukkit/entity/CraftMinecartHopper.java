package org.bukkit.craftbukkit.entity;

import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.inventory.Inventory;

public final class CraftMinecartHopper extends CraftMinecartContainer implements HopperMinecart {
    private final CraftInventory inventory;

    public CraftMinecartHopper(CraftServer server, MinecartHopper entity) {
        super(server, entity);
        inventory = new CraftInventory(entity);
    }

    @Override
    public String toString() {
        return "CraftMinecartHopper{" + "inventory=" + inventory + '}';
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean isEnabled() {
        return ((MinecartHopper) getHandle()).isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        ((MinecartHopper) getHandle()).setEnabled(enabled);
    }
}
