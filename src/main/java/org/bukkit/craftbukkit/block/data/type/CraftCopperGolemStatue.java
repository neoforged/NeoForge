package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.CopperGolemStatue;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftCopperGolemStatue extends CraftBlockData implements CopperGolemStatue {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, CopperGolemPose> COPPER_GOLEM_POSE = getEnum("copper_golem_pose", CopperGolemPose.class);

    @Override
    public CopperGolemPose getCopperGolemPose() {
        return get(COPPER_GOLEM_POSE);
    }

    @Override
    public void setCopperGolemPose(CopperGolemPose copperGolemPose) {
        set(COPPER_GOLEM_POSE, copperGolemPose);
    }
}
