/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftWall extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Wall, org.bukkit.block.data.Waterlogged {

    public CraftWall() {
        super();
    }

    public CraftWall(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftWall

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty UP = getBoolean(net.minecraft.world.level.block.WallBlock.class, "up");
    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Height>[] HEIGHTS = new org.bukkit.craftbukkit.block.data.CraftBlockStateEnum[]{
        getEnum(net.minecraft.world.level.block.WallBlock.class, "north", Height.class),
        getEnum(net.minecraft.world.level.block.WallBlock.class, "east", Height.class),
        getEnum(net.minecraft.world.level.block.WallBlock.class, "south", Height.class),
        getEnum(net.minecraft.world.level.block.WallBlock.class, "west", Height.class)
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

    // org.bukkit.craftbukkit.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.WallBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(WATERLOGGED, waterlogged);
    }
}
