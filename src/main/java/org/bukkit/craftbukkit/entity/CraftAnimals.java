package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.animal.Animal;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.inventory.ItemStack;

public class CraftAnimals extends CraftAgeable implements Animals {

    public CraftAnimals(CraftServer server, Animal entity) {
        super(server, entity);
    }

    @Override
    public Animal getHandle() {
        return (Animal) entity;
    }

    @Override
    public String toString() {
        return "CraftAnimals";
    }

    @Override
    public UUID getBreedCause() {
        EntityReference<ServerPlayer> loveCause = getHandle().loveCause;
        return (loveCause != null) ? loveCause.getUUID() : null;
    }

    @Override
    public void setBreedCause(UUID uuid) {
        getHandle().loveCause = (uuid != null) ? EntityReference.of(uuid) : null;
    }

    @Override
    public boolean isLoveMode() {
        return getHandle().isInLove();
    }

    @Override
    public void setLoveModeTicks(int ticks) {
        Preconditions.checkArgument(ticks >= 0, "Love mode ticks must be positive or 0");
        getHandle().setInLoveTime(ticks);
    }

    @Override
    public int getLoveModeTicks() {
        return getHandle().inLove;
    }

    @Override
    public boolean isBreedItem(ItemStack itemStack) {
        return getHandle().isFood(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public boolean isBreedItem(Material material) {
        return isBreedItem(new ItemStack(material));
    }
}
