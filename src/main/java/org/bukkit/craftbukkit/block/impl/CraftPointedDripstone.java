/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftPointedDripstone extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.PointedDripstone, org.bukkit.block.data.Waterlogged {

    public CraftPointedDripstone() {
        super();
    }

    public CraftPointedDripstone(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftPointedDripstone

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> VERTICAL_DIRECTION = getEnum(net.minecraft.world.level.block.PointedDripstoneBlock.class, "vertical_direction", org.bukkit.block.BlockFace.class);
    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Thickness> THICKNESS = getEnum(net.minecraft.world.level.block.PointedDripstoneBlock.class, "thickness", Thickness.class);

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

    // org.bukkit.craftbukkit.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.PointedDripstoneBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(WATERLOGGED, waterlogged);
    }
}
