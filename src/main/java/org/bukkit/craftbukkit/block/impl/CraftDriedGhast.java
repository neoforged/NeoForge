/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftDriedGhast extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.DriedGhast, org.bukkit.block.data.Directional, org.bukkit.block.data.Waterlogged {

    public CraftDriedGhast() {
        super();
    }

    public CraftDriedGhast(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftDriedGhast

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty HYDRATION = getInteger(net.minecraft.world.level.block.DriedGhastBlock.class, "hydration");

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

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.DriedGhastBlock.class, "facing", org.bukkit.block.BlockFace.class);

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

    // org.bukkit.craftbukkit.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.DriedGhastBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(WATERLOGGED, waterlogged);
    }
}
