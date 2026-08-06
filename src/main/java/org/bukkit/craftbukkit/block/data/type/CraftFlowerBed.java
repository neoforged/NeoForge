package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.FlowerBed;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftFlowerBed extends CraftBlockData implements FlowerBed {

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty FLOWER_AMOUNT = getInteger("flower_amount");

    @Override
    public int getFlowerAmount() {
        return get(FLOWER_AMOUNT);
    }

    @Override
    public void setFlowerAmount(int flower_amount) {
        set(FLOWER_AMOUNT, flower_amount);
    }

    @Override
    public int getMaximumFlowerAmount() {
        return getMax(FLOWER_AMOUNT);
    }
}
