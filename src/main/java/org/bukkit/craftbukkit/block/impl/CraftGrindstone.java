/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftGrindstone extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Grindstone, org.bukkit.block.data.Directional, org.bukkit.block.data.FaceAttachable {

    public CraftGrindstone() {
        super();
    }

    public CraftGrindstone(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.GrindstoneBlock.class, "facing", org.bukkit.block.BlockFace.class);

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

    // org.bukkit.craftbukkit.block.data.CraftFaceAttachable

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, AttachedFace> ATTACH_FACE = getEnum(net.minecraft.world.level.block.GrindstoneBlock.class, "face", AttachedFace.class);

    @Override
    public AttachedFace getAttachedFace() {
        return get(ATTACH_FACE);
    }

    @Override
    public void setAttachedFace(AttachedFace face) {
        set(ATTACH_FACE, face);
    }
}
