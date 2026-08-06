package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftPointedDripstone extends CraftBlockData implements PointedDripstone {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> VERTICAL_DIRECTION = getEnum("vertical_direction", org.bukkit.block.BlockFace.class);
    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Thickness> THICKNESS = getEnum("thickness", Thickness.class);

    @Override
    public org.bukkit.block.BlockFace getVerticalDirection() {
        return get(VERTICAL_DIRECTION);
    }

    @Override
    public void setVerticalDirection(org.bukkit.block.BlockFace direction) {
        set(VERTICAL_DIRECTION, direction);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getVerticalDirections() {
        return getValues(VERTICAL_DIRECTION);
    }

    @Override
    public Thickness getThickness() {
        return get(THICKNESS);
    }

    @Override
    public void setThickness(Thickness thickness) {
        set(THICKNESS, thickness);
    }
}
