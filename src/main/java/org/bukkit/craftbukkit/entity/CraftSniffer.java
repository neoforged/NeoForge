package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Sniffer;

public class CraftSniffer extends CraftAnimals implements Sniffer {

    public CraftSniffer(CraftServer server, net.minecraft.world.entity.animal.sniffer.Sniffer entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.sniffer.Sniffer getHandle() {
        return (net.minecraft.world.entity.animal.sniffer.Sniffer) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftSniffer";
    }

    @Override
    public Collection<Location> getExploredLocations() {
        return this.getHandle().getExploredPositions().map(blockPosition -> CraftLocation.toBukkit(blockPosition.pos(), this.server.getServer().getLevel(blockPosition.dimension()))).collect(Collectors.toList());
    }

    @Override
    public void removeExploredLocation(Location location) {
        Preconditions.checkArgument(location != null, "location cannot be null");
        if (location.getWorld() != getWorld()) {
            return;
        }

        BlockPos blockPosition = CraftLocation.toBlockPosition(location);
        this.getHandle().getBrain().setMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS, this.getHandle().getExploredPositions().filter(blockPositionExplored -> !blockPositionExplored.equals(blockPosition)).collect(Collectors.toList()));
    }

    @Override
    public void addExploredLocation(Location location) {
        Preconditions.checkArgument(location != null, "location cannot be null");
        if (location.getWorld() != getWorld()) {
            return;
        }

        this.getHandle().storeExploredPosition(CraftLocation.toBlockPosition(location));
    }

    @Override
    public State getState() {
        return this.stateToBukkit(this.getHandle().getState());
    }

    @Override
    public void setState(State state) {
        Preconditions.checkArgument(state != null, "state cannot be null");
        this.getHandle().transitionTo(this.stateToNMS(state));
    }

    @Override
    public Location findPossibleDigLocation() {
        return this.getHandle().calculateDigPosition().map(blockPosition -> CraftLocation.toBukkit(blockPosition, this.getLocation().getWorld())).orElse(null);
    }

    @Override
    public boolean canDig() {
        return this.getHandle().canDig();
    }

    private net.minecraft.world.entity.animal.sniffer.Sniffer.State stateToNMS(State state) {
        return switch (state) {
            case IDLING -> net.minecraft.world.entity.animal.sniffer.Sniffer.State.IDLING;
            case FEELING_HAPPY -> net.minecraft.world.entity.animal.sniffer.Sniffer.State.FEELING_HAPPY;
            case SCENTING -> net.minecraft.world.entity.animal.sniffer.Sniffer.State.SCENTING;
            case SNIFFING -> net.minecraft.world.entity.animal.sniffer.Sniffer.State.SNIFFING;
            case SEARCHING -> net.minecraft.world.entity.animal.sniffer.Sniffer.State.SEARCHING;
            case DIGGING -> net.minecraft.world.entity.animal.sniffer.Sniffer.State.DIGGING;
            case RISING -> net.minecraft.world.entity.animal.sniffer.Sniffer.State.RISING;
        };
    }

    private State stateToBukkit(net.minecraft.world.entity.animal.sniffer.Sniffer.State state) {
        return switch (state) {
            case IDLING -> State.IDLING;
            case FEELING_HAPPY -> State.FEELING_HAPPY;
            case SCENTING -> State.SCENTING;
            case SNIFFING -> State.SNIFFING;
            case SEARCHING -> State.SEARCHING;
            case DIGGING -> State.DIGGING;
            case RISING -> State.RISING;
        };
    }
}
