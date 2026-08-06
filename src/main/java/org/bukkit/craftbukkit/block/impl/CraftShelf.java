/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftShelf extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Shelf, org.bukkit.block.data.Directional, org.bukkit.block.data.Powerable, org.bukkit.block.data.Waterlogged {

    public CraftShelf() {
        super();
    }

    public CraftShelf(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftShelf

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, SideChain> SIDE_CHAIN = getEnum(net.minecraft.world.level.block.ShelfBlock.class, "side_chain", SideChain.class);

    @Override
    public SideChain getSideChain() {
        return get(SIDE_CHAIN);
    }

    @Override
    public void setSideChain(SideChain sideChain) {
        set(SIDE_CHAIN, sideChain);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.ShelfBlock.class, "facing", org.bukkit.block.BlockFace.class);

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

    // org.bukkit.craftbukkit.block.data.CraftPowerable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty POWERED = getBoolean(net.minecraft.world.level.block.ShelfBlock.class, "powered");

    @Override
    public boolean isPowered() {
        return get(POWERED);
    }

    @Override
    public void setPowered(boolean powered) {
        set(POWERED, powered);
    }

    // org.bukkit.craftbukkit.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.ShelfBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(WATERLOGGED, waterlogged);
    }
}
