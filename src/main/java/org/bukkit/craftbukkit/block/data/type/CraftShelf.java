package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Shelf;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftShelf extends CraftBlockData implements Shelf {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, SideChain> SIDE_CHAIN = getEnum("side_chain", SideChain.class);

    @Override
    public SideChain getSideChain() {
        return get(SIDE_CHAIN);
    }

    @Override
    public void setSideChain(SideChain sideChain) {
        set(SIDE_CHAIN, sideChain);
    }
}
