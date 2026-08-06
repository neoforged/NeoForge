package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.monster.creaking.Creaking;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Player;

public class CraftCreaking extends CraftMonster implements org.bukkit.entity.Creaking {

    public CraftCreaking(CraftServer server, Creaking entity) {
        super(server, entity);
    }

    @Override
    public Creaking getHandle() {
        return (Creaking) entity;
    }

    @Override
    public Location getHome() {
        return CraftLocation.toBukkit(this.getHandle().getHomePos(), this.getHandle().level());
    }

    @Override
    public void setHome(Location location) {
        Preconditions.checkArgument(location != null, "location cannot be null");
        Preconditions.checkArgument(this.getWorld().equals(location.getWorld()), "Home must be in the same world as the creaking");
        this.getHandle().setHomePos(CraftLocation.toBlockPosition(location));
    }

    @Override
    public void activate(Player player) {
        Preconditions.checkArgument(player != null, "player cannot be null");
        this.getHandle().activate(((CraftPlayer) player).getHandle());
    }

    @Override
    public void deactivate() {
        this.getHandle().deactivate();
    }

    @Override
    public boolean isActive() {
        return this.getHandle().isActive();
    }

    @Override
    public String toString() {
        return "CraftCreaking";
    }
}
