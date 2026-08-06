/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftMossyCarpet extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.MossyCarpet {

    public CraftMossyCarpet() {
        super();
    }

    public CraftMossyCarpet(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftMossyCarpet

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty BOTTOM = getBoolean(net.minecraft.world.level.block.MossyCarpetBlock.class, "bottom");
    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Height>[] HEIGHTS = new org.bukkit.craftbukkit.block.data.CraftBlockStateEnum[]{
        getEnum(net.minecraft.world.level.block.MossyCarpetBlock.class, "north", Height.class),
        getEnum(net.minecraft.world.level.block.MossyCarpetBlock.class, "east", Height.class),
        getEnum(net.minecraft.world.level.block.MossyCarpetBlock.class, "south", Height.class),
        getEnum(net.minecraft.world.level.block.MossyCarpetBlock.class, "west", Height.class)
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
