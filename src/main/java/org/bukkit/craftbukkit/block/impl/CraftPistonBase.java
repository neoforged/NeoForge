/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftPistonBase extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Piston, org.bukkit.block.data.Directional {

    public CraftPistonBase() {
        super();
    }

    public CraftPistonBase(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftPiston

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty EXTENDED = getBoolean(net.minecraft.world.level.block.piston.PistonBaseBlock.class, "extended");

    @Override
    public boolean isExtended() {
        return get(EXTENDED);
    }

    @Override
    public void setExtended(boolean extended) {
        set(EXTENDED, extended);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.piston.PistonBaseBlock.class, "facing", org.bukkit.block.BlockFace.class);

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
