package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.LeafLitter;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftLeafLitter extends CraftBlockData implements LeafLitter {

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty SEGMENT_AMOUNT = getInteger("segment_amount");

    @Override
    public int getSegmentAmount() {
        return get(SEGMENT_AMOUNT);
    }

    @Override
    public void setSegmentAmount(int segment_amount) {
        set(SEGMENT_AMOUNT, segment_amount);
    }

    @Override
    public int getMaximumSegmentAmount() {
        return getMax(SEGMENT_AMOUNT);
    }
}
