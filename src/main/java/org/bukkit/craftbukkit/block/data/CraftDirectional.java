package org.bukkit.craftbukkit.block.data;

import org.bukkit.block.data.Directional;

public abstract class CraftDirectional extends CraftBlockData implements Directional {

    private static final CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum("facing", org.bukkit.block.BlockFace.class);

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(FACING);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(FACING);
    }
}
