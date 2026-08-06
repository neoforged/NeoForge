package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.SculkSensor;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftSculkSensor extends CraftBlockData implements SculkSensor {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Phase> PHASE = getEnum("sculk_sensor_phase", Phase.class);

    @Override
    public Phase getPhase() {
        return get(PHASE);
    }

    @Override
    public void setPhase(Phase phase) {
        set(PHASE, phase);
    }
}
