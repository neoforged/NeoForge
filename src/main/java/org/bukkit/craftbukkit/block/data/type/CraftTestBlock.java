package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.TestBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftTestBlock extends CraftBlockData implements TestBlock {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Mode> MODE = getEnum("mode", Mode.class);

    @Override
    public Mode getMode() {
        return get(MODE);
    }

    @Override
    public void setMode(Mode mode) {
        set(MODE, mode);
    }
}
