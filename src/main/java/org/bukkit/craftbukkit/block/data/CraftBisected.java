package org.bukkit.craftbukkit.block.data;

import org.bukkit.block.data.Bisected;

public class CraftBisected extends CraftBlockData implements Bisected {

    private static final CraftBlockStateEnum<?, Half> HALF = getEnum("half", Half.class);

    @Override
    public Half getHalf() {
        return get(HALF);
    }

    @Override
    public void setHalf(Half half) {
        set(HALF, half);
    }
}
