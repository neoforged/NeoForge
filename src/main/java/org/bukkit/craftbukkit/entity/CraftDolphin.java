package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Dolphin;

public class CraftDolphin extends CraftAgeable implements Dolphin {

    public CraftDolphin(CraftServer server, net.minecraft.world.entity.animal.dolphin.Dolphin entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.dolphin.Dolphin getHandle() {
        return (net.minecraft.world.entity.animal.dolphin.Dolphin) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftDolphin";
    }
}
