/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftPotentSulfur extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.PotentSulfur {

    public CraftPotentSulfur() {
        super();
    }

    public CraftPotentSulfur(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftPotentSulfur

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, State> POTENT_SULFUR_STATE = getEnum(net.minecraft.world.level.block.PotentSulfurBlock.class, "potent_sulfur_state", State.class);

    @Override
    public State getPotentSulfurState() {
        return get(POTENT_SULFUR_STATE);
    }

    @Override
    public void setPotentSulfurState(State state) {
        set(POTENT_SULFUR_STATE, state);
    }
}
