package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.AbstractCubeMob;

public abstract class CraftAbstractCubeMob extends CraftAgeable implements AbstractCubeMob {

    public CraftAbstractCubeMob(CraftServer server, net.minecraft.world.entity.monster.cubemob.AbstractCubeMob entity) {
        super(server, entity);
    }

    @Override
    public int getSize() {
        return getHandle().getSize();
    }

    @Override
    public void setSize(int size) {
        getHandle().setSize(size, true);
    }

    @Override
    public net.minecraft.world.entity.monster.cubemob.AbstractCubeMob getHandle() {
        return (net.minecraft.world.entity.monster.cubemob.AbstractCubeMob) entity;
    }

    @Override
    public String toString() {
        return "CraftAbstractCubeMob";
    }
}
