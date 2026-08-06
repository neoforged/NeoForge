package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Vault;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftVault extends CraftBlockData implements Vault {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, State> VAULT_STATE = getEnum("vault_state", State.class);
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty OMINOUS = getBoolean("ominous");

    @Override
    public State getVaultState() {
        return get(VAULT_STATE);
    }

    @Override
    public State getTrialSpawnerState() {
        return getVaultState();
    }

    @Override
    public void setVaultState(State state) {
        set(VAULT_STATE, state);
    }

    @Override
    public void setTrialSpawnerState(State state) {
        setVaultState(state);
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
