package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Crafter;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftCrafter extends CraftBlockData implements Crafter {

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty CRAFTING = getBoolean("crafting");
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty TRIGGERED = getBoolean("triggered");
    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Orientation> ORIENTATION = getEnum("orientation", Orientation.class);

    @Override
    public boolean isCrafting() {
        return get(CRAFTING);
    }

    @Override
    public void setCrafting(boolean crafting) {
        set(CRAFTING, crafting);
    }

    @Override
    public boolean isTriggered() {
        return get(TRIGGERED);
    }

    @Override
    public void setTriggered(boolean triggered) {
        set(TRIGGERED, triggered);
    }

    @Override
    public Orientation getOrientation() {
        return get(ORIENTATION);
    }

    @Override
    public void setOrientation(Orientation orientation) {
        set(ORIENTATION, orientation);
    }
}
