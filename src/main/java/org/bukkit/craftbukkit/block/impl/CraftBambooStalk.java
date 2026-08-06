/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftBambooStalk extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Bamboo, org.bukkit.block.data.Ageable, org.bukkit.block.data.type.Sapling {

    public CraftBambooStalk() {
        super();
    }

    public CraftBambooStalk(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftBamboo

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Leaves> LEAVES = getEnum(net.minecraft.world.level.block.BambooStalkBlock.class, "leaves", Leaves.class);

    @Override
    public Leaves getLeaves() {
        return get(LEAVES);
    }

    @Override
    public void setLeaves(Leaves leaves) {
        set(LEAVES, leaves);
    }

    // org.bukkit.craftbukkit.block.data.CraftAgeable

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty AGE = getInteger(net.minecraft.world.level.block.BambooStalkBlock.class, "age");

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

    // org.bukkit.craftbukkit.block.data.type.CraftSapling

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty STAGE = getInteger(net.minecraft.world.level.block.BambooStalkBlock.class, "stage");

    @Override
    public int getStage() {
        return get(STAGE);
    }

    @Override
    public void setStage(int stage) {
        set(STAGE, stage);
    }

    @Override
    public int getMaximumStage() {
        return getMax(STAGE);
    }
}
