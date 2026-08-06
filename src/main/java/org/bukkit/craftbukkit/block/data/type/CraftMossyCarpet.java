package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.MossyCarpet;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftMossyCarpet extends CraftBlockData implements MossyCarpet {

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty BOTTOM = getBoolean("bottom");
    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Height>[] HEIGHTS = new org.bukkit.craftbukkit.block.data.CraftBlockStateEnum[]{
        getEnum("north", Height.class),
        getEnum("east", Height.class),
        getEnum("south", Height.class),
        getEnum("west", Height.class)
    };

    @Override
    public boolean isBottom() {
        return get(BOTTOM);
    }

    @Override
    public void setBottom(boolean up) {
        set(BOTTOM, up);
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
