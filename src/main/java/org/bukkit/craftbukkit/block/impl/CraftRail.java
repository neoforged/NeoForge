/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftRail extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.Rail, org.bukkit.block.data.Waterlogged {

    public CraftRail() {
        super();
    }

    public CraftRail(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.CraftRail

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Shape> SHAPE = getEnum(net.minecraft.world.level.block.RailBlock.class, "shape", Shape.class);

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

    // org.bukkit.craftbukkit.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.RailBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(WATERLOGGED, waterlogged);
    }
}
