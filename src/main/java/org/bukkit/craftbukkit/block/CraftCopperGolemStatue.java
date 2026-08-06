package org.bukkit.craftbukkit.block;

import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CopperGolemStatue;

public class CraftCopperGolemStatue extends CraftBlockEntityState<CopperGolemStatueBlockEntity> implements CopperGolemStatue {

    public CraftCopperGolemStatue(World world, CopperGolemStatueBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftCopperGolemStatue(CraftCopperGolemStatue state, Location location) {
        super(state, location);
    }

    @Override
    public CraftCopperGolemStatue copy() {
        return new CraftCopperGolemStatue(this, null);
    }

    @Override
    public CraftCopperGolemStatue copy(Location location) {
        return new CraftCopperGolemStatue(this, location);
    }
}
