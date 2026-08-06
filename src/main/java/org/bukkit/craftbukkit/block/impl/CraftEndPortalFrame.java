/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftEndPortalFrame extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.EndPortalFrame, org.bukkit.block.data.Directional {

    public CraftEndPortalFrame() {
        super();
    }

    public CraftEndPortalFrame(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftEndPortalFrame

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty EYE = getBoolean(net.minecraft.world.level.block.EndPortalFrameBlock.class, "eye");

    @Override
    public boolean hasEye() {
        return get(EYE);
    }

    @Override
    public void setEye(boolean eye) {
        set(EYE, eye);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.EndPortalFrameBlock.class, "facing", org.bukkit.block.BlockFace.class);

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
