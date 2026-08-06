/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftJigsaw extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Jigsaw {

    public CraftJigsaw() {
        super();
    }

    public CraftJigsaw(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftJigsaw

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Orientation> ORIENTATION = getEnum(net.minecraft.world.level.block.JigsawBlock.class, "orientation", Orientation.class);

    @Override
    public Orientation getOrientation() {
        return get(ORIENTATION);
    }

    @Override
    public void setOrientation(Orientation orientation) {
        set(ORIENTATION, orientation);
    }
}
