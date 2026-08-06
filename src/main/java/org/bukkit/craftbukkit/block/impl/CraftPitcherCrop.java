/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftPitcherCrop extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.PitcherCrop, org.bukkit.block.data.Ageable, org.bukkit.block.data.Bisected {

    public CraftPitcherCrop() {
        super();
    }

    public CraftPitcherCrop(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.CraftAgeable

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty AGE = getInteger(net.minecraft.world.level.block.PitcherCropBlock.class, "age");

    @Override
    public int getAge() {
        return get(AGE);
    }

    @Override
    public void setAge(int age) {
        set(AGE, age);
    }

    @Override
    public int getMaximumAge() {
        return getMax(AGE);
    }

    // org.bukkit.craftbukkit.block.data.CraftBisected

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Half> HALF = getEnum(net.minecraft.world.level.block.PitcherCropBlock.class, "half", Half.class);

    @Override
    public Half getHalf() {
        return get(HALF);
    }

    @Override
    public void setHalf(Half half) {
        set(HALF, half);
    }
}
