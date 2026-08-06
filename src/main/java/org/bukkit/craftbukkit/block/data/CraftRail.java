package org.bukkit.craftbukkit.block.data;

import org.bukkit.block.data.Rail;

public abstract class CraftRail extends CraftBlockData implements Rail {

    private static final CraftBlockStateEnum<?, Shape> SHAPE = getEnum("shape", Shape.class);

    @Override
    public Shape getShape() {
        return get(SHAPE);
    }

    @Override
    public void setShape(Shape shape) {
        set(SHAPE, shape);
    }

    @Override
    public java.util.Set<Shape> getShapes() {
        return getValues(SHAPE);
    }
}
