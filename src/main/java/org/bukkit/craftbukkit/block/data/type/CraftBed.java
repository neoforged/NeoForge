package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Bed;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftBed extends CraftBlockData implements Bed {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Part> PART = getEnum("part", Part.class);
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty OCCUPIED = getBoolean("occupied");

    @Override
    public Part getPart() {
        return get(PART);
    }

    @Override
    public void setPart(Part part) {
        set(PART, part);
    }

    @Override
    public boolean isOccupied() {
        return get(OCCUPIED);
    }
}
