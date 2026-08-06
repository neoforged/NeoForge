/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftCopperGolemStatue extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.CopperGolemStatue, org.bukkit.block.data.Directional, org.bukkit.block.data.Waterlogged {

    public CraftCopperGolemStatue() {
        super();
    }

    public CraftCopperGolemStatue(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftCopperGolemStatue

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, CopperGolemPose> COPPER_GOLEM_POSE = getEnum(net.minecraft.world.level.block.CopperGolemStatueBlock.class, "copper_golem_pose", CopperGolemPose.class);

    @Override
    public CopperGolemPose getCopperGolemPose() {
        return get(COPPER_GOLEM_POSE);
    }

    @Override
    public void setCopperGolemPose(CopperGolemPose copperGolemPose) {
        set(COPPER_GOLEM_POSE, copperGolemPose);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.CopperGolemStatueBlock.class, "facing", org.bukkit.block.BlockFace.class);

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

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.CopperGolemStatueBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(WATERLOGGED, waterlogged);
    }
}
