package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Bamboo;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftBamboo extends CraftBlockData implements Bamboo {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Leaves> LEAVES = getEnum("leaves", Leaves.class);

    @Override
    public Leaves getLeaves() {
        return get(LEAVES);
    }

    @Override
    public void setLeaves(Leaves leaves) {
        set(LEAVES, leaves);
    }
}
