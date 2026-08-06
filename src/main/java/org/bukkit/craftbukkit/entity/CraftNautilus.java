package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Nautilus;

public class CraftNautilus extends CraftAbstractNautilus implements Nautilus {

    public CraftNautilus(CraftServer server, net.minecraft.world.entity.animal.nautilus.Nautilus entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.nautilus.Nautilus getHandle() {
        return (net.minecraft.world.entity.animal.nautilus.Nautilus) entity;
    }

    @Override
    public String toString() {
        return "CraftNautilus";
    }
}
