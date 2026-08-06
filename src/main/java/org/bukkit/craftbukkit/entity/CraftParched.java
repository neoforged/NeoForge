package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Parched;
import org.bukkit.entity.Skeleton;

public class CraftParched extends CraftAbstractSkeleton implements Parched {

    public CraftParched(CraftServer server, net.minecraft.world.entity.monster.skeleton.Parched entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.monster.skeleton.Parched getHandle() {
        return (net.minecraft.world.entity.monster.skeleton.Parched) entity;
    }

    @Override
    public String toString() {
        return "CraftParched";
    }

    @Override
    public Skeleton.SkeletonType getSkeletonType() {
        return Skeleton.SkeletonType.PARCHED;
    }
}
