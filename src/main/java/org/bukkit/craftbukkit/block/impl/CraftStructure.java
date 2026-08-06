/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftStructure extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.StructureBlock {

    public CraftStructure() {
        super();
    }

    public CraftStructure(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftStructureBlock

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Mode> MODE = getEnum(net.minecraft.world.level.block.StructureBlock.class, "mode", Mode.class);

    @Override
    public Mode getMode() {
        return get(MODE);
    }

    @Override
    public void setMode(Mode mode) {
        set(MODE, mode);
    }
}
