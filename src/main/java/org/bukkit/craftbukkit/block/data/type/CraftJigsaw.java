package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftJigsaw extends CraftBlockData implements Jigsaw {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Orientation> ORIENTATION = getEnum("orientation", Orientation.class);

    @Override
    public Orientation getOrientation() {
        return get(ORIENTATION);
    }

    @Override
    public void setOrientation(Orientation orientation) {
        set(ORIENTATION, orientation);
    }
}
