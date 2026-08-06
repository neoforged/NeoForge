/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftVault extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Vault, org.bukkit.block.data.Directional {

    public CraftVault() {
        super();
    }

    public CraftVault(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftVault

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, State> VAULT_STATE = getEnum(net.minecraft.world.level.block.VaultBlock.class, "vault_state", State.class);
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty OMINOUS = getBoolean(net.minecraft.world.level.block.VaultBlock.class, "ominous");

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

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.block.BlockFace> FACING = getEnum(net.minecraft.world.level.block.VaultBlock.class, "facing", org.bukkit.block.BlockFace.class);

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(FACING);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(FACING);
    }
}
