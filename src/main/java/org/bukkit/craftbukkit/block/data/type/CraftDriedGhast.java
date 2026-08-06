package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.DriedGhast;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftDriedGhast extends CraftBlockData implements DriedGhast {

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty HYDRATION = getInteger("hydration");

    @Override
    public int getHydration() {
        return get(HYDRATION);
    }

    @Override
    public void setHydration(int hydration) {
        set(HYDRATION, hydration);
    }

    @Override
    public int getMaximumHydration() {
        return getMax(HYDRATION);
    }
}
