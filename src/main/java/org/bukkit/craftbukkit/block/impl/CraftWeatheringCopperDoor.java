/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftWeatheringCopperDoor extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Door, org.bukkit.block.data.Bisected, org.bukkit.block.data.Directional, org.bukkit.block.data.Openable, org.bukkit.block.data.Powerable {

    public CraftWeatheringCopperDoor() {
        super();
    }

    public CraftWeatheringCopperDoor(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftDoor

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Hinge> HINGE = getEnum(net.minecraft.world.level.block.WeatheringCopperDoorBlock.class, "hinge", Hinge.class);

    @Override
    public Hinge getHinge() {
        return get(HINGE);
    }

    @Override
    public void setHinge(Hinge hinge) {
        set(HINGE, hinge);
    }

    // org.bukkit.craftbukkit.block.data.CraftBisected

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Half> HALF = getEnum(net.minecraft.world.level.block.WeatheringCopperDoorBlock.class, "half", Half.class);

    @Override
    public Half getHalf() {
        return get(HALF);
    }

    @Override
    public void setHalf(Half half) {
        set(HALF, half);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.WeatheringCopperDoorBlock.class, "facing", org.bukkit.block.BlockFace.class);

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

    // org.bukkit.craftbukkit.block.data.CraftOpenable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty OPEN = getBoolean(net.minecraft.world.level.block.WeatheringCopperDoorBlock.class, "open");

    @Override
    public boolean isOpen() {
        return get(OPEN);
    }

    @Override
    public void setOpen(boolean open) {
        set(OPEN, open);
    }

    // org.bukkit.craftbukkit.block.data.CraftPowerable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty POWERED = getBoolean(net.minecraft.world.level.block.WeatheringCopperDoorBlock.class, "powered");

    @Override
    public boolean isPowered() {
        return get(POWERED);
    }

    @Override
    public void setPowered(boolean powered) {
        set(POWERED, powered);
    }
}
