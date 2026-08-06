/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftBell extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Bell, org.bukkit.block.data.Directional, org.bukkit.block.data.Powerable {

    public CraftBell() {
        super();
    }

    public CraftBell(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftBell

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Attachment> ATTACHMENT = getEnum(net.minecraft.world.level.block.BellBlock.class, "attachment", Attachment.class);

    @Override
    public Attachment getAttachment() {
        return get(ATTACHMENT);
    }

    @Override
    public void setAttachment(Attachment leaves) {
        set(ATTACHMENT, leaves);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.BellBlock.class, "facing", org.bukkit.block.BlockFace.class);

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

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty POWERED = getBoolean(net.minecraft.world.level.block.BellBlock.class, "powered");

    @Override
    public boolean isPowered() {
        return get(POWERED);
    }

    @Override
    public void setPowered(boolean powered) {
        set(POWERED, powered);
    }
}
