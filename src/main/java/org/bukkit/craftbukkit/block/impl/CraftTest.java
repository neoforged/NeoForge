/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftTest extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.TestBlock {

    public CraftTest() {
        super();
    }

    public CraftTest(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftTestBlock

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Mode> MODE = getEnum(net.minecraft.world.level.block.TestBlock.class, "mode", Mode.class);

    @Override
    public Mode getMode() {
        return get(MODE);
    }

    @Override
    public void setMode(Mode mode) {
        set(MODE, mode);
    }
}
