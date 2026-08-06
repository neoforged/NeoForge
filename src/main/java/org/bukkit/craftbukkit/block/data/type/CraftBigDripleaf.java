package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.BigDripleaf;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftBigDripleaf extends CraftBlockData implements BigDripleaf {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Tilt> TILT = getEnum("tilt", Tilt.class);

    @Override
    public Tilt getTilt() {
        return get(TILT);
    }

    @Override
    public void setTilt(Tilt tilt) {
        set(TILT, tilt);
    }
}
