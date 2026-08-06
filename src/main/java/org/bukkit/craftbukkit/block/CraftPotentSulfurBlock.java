package org.bukkit.craftbukkit.block;

import net.minecraft.world.level.block.entity.PotentSulfurBlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.PotentSulfur;

public class CraftPotentSulfurBlock extends CraftBlockEntityState<PotentSulfurBlockEntity> implements PotentSulfur {

    public CraftPotentSulfurBlock(World world, PotentSulfurBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftPotentSulfurBlock(CraftPotentSulfurBlock state, Location location) {
        super(state, location);
    }

    @Override
    public CraftPotentSulfurBlock copy() {
        return new CraftPotentSulfurBlock(this, null);
    }

    @Override
    public CraftPotentSulfurBlock copy(Location location) {
        return new CraftPotentSulfurBlock(this, location);
    }
}
