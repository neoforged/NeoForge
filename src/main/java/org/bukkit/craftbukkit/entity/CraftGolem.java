package org.bukkit.craftbukkit.entity;

import net.minecraft.world.entity.animal.golem.AbstractGolem;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Golem;

public class CraftGolem extends CraftCreature implements Golem {
    public CraftGolem(CraftServer server, AbstractGolem entity) {
        super(server, entity);
    }

    @Override
    public AbstractGolem getHandle() {
        return (AbstractGolem) entity;
    }

    @Override
    public String toString() {
        return "CraftGolem";
    }
}
