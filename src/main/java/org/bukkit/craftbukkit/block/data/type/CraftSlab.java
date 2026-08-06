package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Slab;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftSlab extends CraftBlockData implements Slab {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Type> TYPE = getEnum("type", Type.class);

    @Override
    public Type getType() {
        return get(TYPE);
    }

    @Override
    public void setType(Type type) {
        set(TYPE, type);
    }
}
