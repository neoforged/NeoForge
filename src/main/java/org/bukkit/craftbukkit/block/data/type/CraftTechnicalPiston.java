package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.TechnicalPiston;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftTechnicalPiston extends CraftBlockData implements TechnicalPiston {

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
