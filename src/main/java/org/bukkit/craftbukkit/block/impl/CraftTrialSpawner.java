/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftTrialSpawner extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.TrialSpawner {

    public CraftTrialSpawner() {
        super();
    }

    public CraftTrialSpawner(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftTrialSpawner

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, State> TRIAL_SPAWNER_STATE = getEnum(net.minecraft.world.level.block.TrialSpawnerBlock.class, "trial_spawner_state", State.class);
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty OMINOUS = getBoolean(net.minecraft.world.level.block.TrialSpawnerBlock.class, "ominous");

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
