package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.AbstractNautilus;
import org.bukkit.inventory.Inventory;

public abstract class CraftAbstractNautilus extends CraftTameableAnimal implements AbstractNautilus {

    public CraftAbstractNautilus(CraftServer server, net.minecraft.world.entity.animal.nautilus.AbstractNautilus entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.nautilus.AbstractNautilus getHandle() {
        return (net.minecraft.world.entity.animal.nautilus.AbstractNautilus) super.getHandle();
    }

    @Override
    public Inventory getInventory() {
        return new CraftInventory(getHandle().inventory);
    }
}
