package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.AbstractCow;

public abstract class CraftAbstractCow extends CraftAnimals implements AbstractCow {

    public CraftAbstractCow(CraftServer server, net.minecraft.world.entity.animal.cow.AbstractCow entity) {
        super(server, entity);
    }
}
