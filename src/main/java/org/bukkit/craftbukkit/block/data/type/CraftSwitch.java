package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Switch;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftSwitch extends CraftBlockData implements Switch {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Face> FACE = getEnum("face", Face.class);

    @Override
    public Face getFace() {
        return get(FACE);
    }

    @Override
    public void setFace(Face face) {
        set(FACE, face);
    }
}
