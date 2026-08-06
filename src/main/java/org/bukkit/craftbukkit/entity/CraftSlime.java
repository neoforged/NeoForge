package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Slime;

public class CraftSlime extends CraftAbstractCubeMob implements Slime, CraftEnemy {

    public CraftSlime(CraftServer server, net.minecraft.world.entity.monster.cubemob.Slime entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.monster.cubemob.Slime getHandle() {
        return (net.minecraft.world.entity.monster.cubemob.Slime) entity;
    }

    @Override
    public String toString() {
        return "CraftSlime";
    }
}
