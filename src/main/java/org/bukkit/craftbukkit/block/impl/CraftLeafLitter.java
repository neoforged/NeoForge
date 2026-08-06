/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftLeafLitter extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.LeafLitter, org.bukkit.block.data.Directional {

    public CraftLeafLitter() {
        super();
    }

    public CraftLeafLitter(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftLeafLitter

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty SEGMENT_AMOUNT = getInteger(net.minecraft.world.level.block.LeafLitterBlock.class, "segment_amount");

    @Override
    public int getSegmentAmount() {
        return get(SEGMENT_AMOUNT);
    }

    @Override
    public void setSegmentAmount(int segment_amount) {
        set(SEGMENT_AMOUNT, segment_amount);
    }

    @Override
    public int getMaximumSegmentAmount() {
        return getMax(SEGMENT_AMOUNT);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.LeafLitterBlock.class, "facing", org.bukkit.block.BlockFace.class);

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
