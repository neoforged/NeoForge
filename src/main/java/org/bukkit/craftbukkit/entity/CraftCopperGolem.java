package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.world.level.block.WeatheringCopper;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.CopperGolem;

public class CraftCopperGolem extends CraftGolem implements CopperGolem {

    public CraftCopperGolem(CraftServer server, net.minecraft.world.entity.animal.golem.CopperGolem entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.golem.CopperGolem getHandle() {
        return (net.minecraft.world.entity.animal.golem.CopperGolem) entity;
    }

    @Override
    public String toString() {
        return "CraftCopperGolem";
    }

    @Override
    public long getNextWeatheringTick() {
        return getHandle().nextWeatheringTick;
    }

    @Override
    public void setNextWeatheringTick(long tick) {
        getHandle().nextWeatheringTick = tick;
    }

    @Override
    public CopperWeatherState getWeatherState() {
        return CopperWeatherState.valueOf(getHandle().getWeatherState().name());
    }

    @Override
    public void setWeatherState(CopperWeatherState state) {
        Preconditions.checkArgument(state != null, "state cannot be null");

        getHandle().setWeatherState(WeatheringCopper.WeatherState.valueOf(state.name()));
    }
}
