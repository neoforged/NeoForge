/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftDoublePlant extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.Bisected {

    public CraftDoublePlant() {
        super();
    }

    public CraftDoublePlant(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.CraftBisected

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Half> HALF = getEnum(net.minecraft.world.level.block.DoublePlantBlock.class, "half", Half.class);

    @Override
    public Half getHalf() {
        return get(HALF);
    }

    @Override
    public void setHalf(Half half) {
        set(HALF, half);
    }
}
