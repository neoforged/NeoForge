/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftTnt extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.TNT {

    public CraftTnt() {
        super();
    }

    public CraftTnt(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftTNT

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty UNSTABLE = getBoolean(net.minecraft.world.level.block.TntBlock.class, "unstable");

    @Override
    public boolean isUnstable() {
        return get(UNSTABLE);
    }

    @Override
    public void setUnstable(boolean unstable) {
        set(UNSTABLE, unstable);
    }
}
