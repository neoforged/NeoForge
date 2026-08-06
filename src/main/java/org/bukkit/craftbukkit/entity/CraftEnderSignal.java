package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Items;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.EnderSignal;
import org.bukkit.inventory.ItemStack;

public class CraftEnderSignal extends CraftEntity implements EnderSignal {
    public CraftEnderSignal(CraftServer server, EyeOfEnder entity) {
        super(server, entity);
    }

    @Override
    public EyeOfEnder getHandle() {
        return (EyeOfEnder) entity;
    }

    @Override
    public String toString() {
        return "CraftEnderSignal";
    }

    @Override
    public Location getTargetLocation() {
        return CraftLocation.toBukkit(getHandle().target, getWorld(), getHandle().getYRot(), getHandle().getXRot());
    }

    @Override
    public void setTargetLocation(Location location) {
        Preconditions.checkArgument(getWorld().equals(location.getWorld()), "Cannot target EnderSignal across worlds");
        getHandle().signalTo(CraftLocation.toVec3D(location));
    }

    @Override
    public boolean getDropItem() {
        return getHandle().surviveAfterDeath;
    }

    @Override
    public void setDropItem(boolean shouldDropItem) {
        getHandle().surviveAfterDeath = shouldDropItem;
    }

    @Override
    public ItemStack getItem() {
        return CraftItemStack.asBukkitCopy(getHandle().getItem());
    }

    @Override
    public void setItem(ItemStack item) {
        getHandle().setItem(item != null ? CraftItemStack.asNMSCopy(item) : Items.ENDER_EYE.getDefaultInstance());
    }

    @Override
    public int getDespawnTimer() {
        return getHandle().life;
    }

    @Override
    public void setDespawnTimer(int time) {
        getHandle().life = time;
    }
}
