package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Door;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftDoor extends CraftBlockData implements Door {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Hinge> HINGE = getEnum("hinge", Hinge.class);

    @Override
    public Hinge getHinge() {
        return get(HINGE);
    }

    @Override
    public void setHinge(Hinge hinge) {
        set(HINGE, hinge);
    }
}
