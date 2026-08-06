/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftFarmland extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Farmland {

    public CraftFarmland() {
        super();
    }

    public CraftFarmland(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftFarmland

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty MOISTURE = getInteger(net.minecraft.world.level.block.FarmlandBlock.class, "moisture");

    @Override
    public int getMoisture() {
        return get(MOISTURE);
    }

    @Override
    public void setMoisture(int moisture) {
        set(MOISTURE, moisture);
    }

    @Override
    public int getMaximumMoisture() {
        return getMax(MOISTURE);
    }
}
