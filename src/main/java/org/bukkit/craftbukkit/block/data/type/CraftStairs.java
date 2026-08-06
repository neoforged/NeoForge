package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Stairs;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftStairs extends CraftBlockData implements Stairs {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Shape> SHAPE = getEnum("shape", Shape.class);

    @Override
    public Shape getShape() {
        return get(SHAPE);
    }

    @Override
    public void setShape(Shape shape) {
        set(SHAPE, shape);
    }
}
