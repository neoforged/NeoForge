package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.Raid.RaidStatus;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Raider;

public final class CraftRaid implements Raid {

    private final net.minecraft.world.entity.raid.Raid handle;
    private final Level world;

    public CraftRaid(net.minecraft.world.entity.raid.Raid handle, Level level) {
        this.handle = handle;
        this.world = level;
    }

    @Override
    public void stopRaid(boolean removeSpawnedRaiders) {
        org.bukkit.craftbukkit.event.CraftEventFactory.callRaidStopEvent(handle, world, org.bukkit.event.raid.RaidStopEvent.Reason.PLUGIN);
        handle.stop();

        if (removeSpawnedRaiders) {
            for (Raider raider : getRaiders()) {
                raider.remove();
            }
        }
    }

    @Override
    public boolean isStarted() {
        return handle.isStarted();
    }

    @Override
    public long getActiveTicks() {
        return handle.ticksActive;
    }

    @Override
    public int getBadOmenLevel() {
        return handle.raidOmenLevel;
    }

    @Override
    public void setBadOmenLevel(int badOmenLevel) {
        int max = handle.getMaxRaidOmenLevel();
        Preconditions.checkArgument(0 <= badOmenLevel && badOmenLevel <= max, "Bad Omen level must be between 0 and %s", max);
        handle.raidOmenLevel = badOmenLevel;
    }

    @Override
    public Location getLocation() {
        BlockPos pos = handle.getCenter();
        return CraftLocation.toBukkit(pos, world.getWorld());
    }

    @Override
    public RaidStatus getStatus() {
        if (handle.isStopped()) {
            return RaidStatus.STOPPED;
        } else if (handle.isVictory()) {
            return RaidStatus.VICTORY;
        } else if (handle.isLoss()) {
            return RaidStatus.LOSS;
        } else {
            return RaidStatus.ONGOING;
        }
    }

    @Override
    public int getSpawnedGroups() {
        return handle.getGroupsSpawned();
    }

    @Override
    public int getTotalGroups() {
        return handle.numGroups + (handle.raidOmenLevel > 1 ? 1 : 0);
    }

    @Override
    public int getTotalWaves() {
        return handle.numGroups;
    }

    @Override
    public void setTotalWaves(int waves) {
        Preconditions.checkArgument(waves > 0, "Total waves must be greater than 0");
        handle.numGroups = waves;
    }

    @Override
    public float getTotalHealth() {
        return handle.getHealthOfLivingRaiders();
    }

    @Override
    public Set<UUID> getHeroes() {
        return Collections.unmodifiableSet(handle.heroesOfTheVillage);
    }

    @Override
    public List<Raider> getRaiders() {
        return handle.getRaiders().stream().map(new Function<net.minecraft.world.entity.raid.Raider, Raider>() {
            @Override
            public Raider apply(net.minecraft.world.entity.raid.Raider entityRaider) {
                return (Raider) entityRaider.getBukkitEntity();
            }
        }).collect(ImmutableList.toImmutableList());
    }

    public net.minecraft.world.entity.raid.Raid getHandle() {
        return handle;
    }
}
