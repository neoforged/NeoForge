package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Wall;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftWall extends CraftBlockData implements Wall {

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty UP = getBoolean("up");
    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Height>[] HEIGHTS = new org.bukkit.craftbukkit.block.data.CraftBlockStateEnum[]{
        getEnum("north", Height.class),
        getEnum("east", Height.class),
        getEnum("south", Height.class),
        getEnum("west", Height.class)
    };

    @Override
    public boolean isUp() {
        return get(UP);
    }

    @Override
    public void setUp(boolean up) {
        set(UP, up);
    }

    @Override
    public Height getHeight(org.bukkit.block.BlockFace face) {
        return get(HEIGHTS[face.ordinal()]);
    }

    @Override
    public void setHeight(org.bukkit.block.BlockFace face, Height height) {
        set(HEIGHTS[face.ordinal()], height);
    }
}
