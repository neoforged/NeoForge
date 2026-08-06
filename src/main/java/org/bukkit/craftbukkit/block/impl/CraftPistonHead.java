/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftPistonHead extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.PistonHead, org.bukkit.block.data.type.TechnicalPiston, org.bukkit.block.data.Directional {

    public CraftPistonHead() {
        super();
    }

    public CraftPistonHead(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftPistonHead

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty SHORT = getBoolean(net.minecraft.world.level.block.piston.PistonHeadBlock.class, "short");

    @Override
    public boolean isShort() {
        return get(SHORT);
    }

    @Override
    public void setShort(boolean _short) {
        set(SHORT, _short);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftTechnicalPiston

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Type> TYPE = getEnum(net.minecraft.world.level.block.piston.PistonHeadBlock.class, "type", Type.class);

    @Override
    public Type getType() {
        return get(TYPE);
    }

    @Override
    public void setType(Type type) {
        set(TYPE, type);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.piston.PistonHeadBlock.class, "facing", org.bukkit.block.BlockFace.class);

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
