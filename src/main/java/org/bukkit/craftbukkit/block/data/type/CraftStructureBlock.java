package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.StructureBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftStructureBlock extends CraftBlockData implements StructureBlock {

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
