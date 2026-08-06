package org.bukkit.craftbukkit.block.data;

import org.bukkit.block.data.Orientable;

public class CraftOrientable extends CraftBlockData implements Orientable {

    private static final CraftBlockStateEnum<?, org.bukkit.Axis> AXIS = getEnum("axis", org.bukkit.Axis.class);

    @Override
    public org.bukkit.Axis getAxis() {
        return get(AXIS);
    }

    @Override
    public void setAxis(org.bukkit.Axis axis) {
        set(AXIS, axis);
    }

    @Override
    public java.util.Set<org.bukkit.Axis> getAxes() {
        return getValues(AXIS);
    }
}
