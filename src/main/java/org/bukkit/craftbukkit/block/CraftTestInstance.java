package org.bukkit.craftbukkit.block;

import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.TestInstance;

public class CraftTestInstance extends CraftBlockEntityState<TestInstanceBlockEntity> implements TestInstance {

    public CraftTestInstance(World world, TestInstanceBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftTestInstance(CraftTestInstance state, Location location) {
        super(state, location);
    }

    @Override
    public CraftTestInstance copy() {
        return new CraftTestInstance(this, null);
    }

    @Override
    public CraftTestInstance copy(Location location) {
        return new CraftTestInstance(this, location);
    }
}
