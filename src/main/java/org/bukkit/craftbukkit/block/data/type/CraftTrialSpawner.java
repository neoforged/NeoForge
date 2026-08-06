package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftTrialSpawner extends CraftBlockData implements TrialSpawner {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, State> TRIAL_SPAWNER_STATE = getEnum("trial_spawner_state", State.class);
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty OMINOUS = getBoolean("ominous");

    @Override
    public State getTrialSpawnerState() {
        return get(TRIAL_SPAWNER_STATE);
    }

    @Override
    public void setTrialSpawnerState(State state) {
        set(TRIAL_SPAWNER_STATE, state);
    }

    @Override
    public boolean isOminous() {
        return get(OMINOUS);
    }

    @Override
    public void setOminous(boolean ominous) {
        set(OMINOUS, ominous);
    }
}
