package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.registry.CraftOldEnumRegistryItem;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;

public class CraftVillager extends CraftAbstractVillager implements Villager {

    public CraftVillager(CraftServer server, net.minecraft.world.entity.npc.villager.Villager entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.npc.villager.Villager getHandle() {
        return (net.minecraft.world.entity.npc.villager.Villager) entity;
    }

    @Override
    public String toString() {
        return "CraftVillager";
    }

    @Override
    public void remove() {
        getHandle().releaseAllPois();

        super.remove();
    }

    @Override
    public Profession getProfession() {
        return CraftProfession.minecraftHolderToBukkit(getHandle().getVillagerData().profession());
    }

    @Override
    public void setProfession(Profession profession) {
        Preconditions.checkArgument(profession != null, "Profession cannot be null");
        getHandle().setVillagerData(getHandle().getVillagerData().withProfession(CraftProfession.bukkitToMinecraftHolder(profession)));
    }

    @Override
    public Type getVillagerType() {
        return CraftType.minecraftHolderToBukkit(getHandle().getVillagerData().type());
    }

    @Override
    public void setVillagerType(Type type) {
        Preconditions.checkArgument(type != null, "Type cannot be null");
        getHandle().setVillagerData(getHandle().getVillagerData().withType(CraftType.bukkitToMinecraftHolder(type)));
    }

    @Override
    public int getVillagerLevel() {
        return getHandle().getVillagerData().level();
    }

    @Override
    public void setVillagerLevel(int level) {
        Preconditions.checkArgument(1 <= level && level <= 5, "level (%s) must be between [1, 5]", level);

        getHandle().setVillagerData(getHandle().getVillagerData().withLevel(level));
    }

    @Override
    public int getVillagerExperience() {
        return getHandle().getVillagerXp();
    }

    @Override
    public void setVillagerExperience(int experience) {
        Preconditions.checkArgument(experience >= 0, "Experience (%s) must be positive", experience);

        getHandle().setVillagerXp(experience);
    }

    @Override
    public boolean sleep(Location location) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "Location needs to be in a world");
        Preconditions.checkArgument(location.getWorld().equals(getWorld()), "Cannot sleep across worlds");
        Preconditions.checkState(!getHandle().generation, "Cannot sleep during world generation");

        BlockPos position = CraftLocation.toBlockPosition(location);
        BlockState blockstate = getHandle().level().getBlockState(position);
        if (!(blockstate.getBlock() instanceof BedBlock)) {
            return false;
        }

        getHandle().startSleeping(position);
        return true;
    }

    @Override
    public void wakeup() {
        Preconditions.checkState(isSleeping(), "Cannot wakeup if not sleeping");
        Preconditions.checkState(!getHandle().generation, "Cannot wakeup during world generation");

        getHandle().stopSleeping();
    }

    @Override
    public void shakeHead() {
        getHandle().setUnhappy();
    }

    @Override
    public ZombieVillager zombify() {
        net.minecraft.world.entity.monster.zombie.ZombieVillager zombievillager = Zombie.convertVillagerToZombieVillager(getHandle().level().getMinecraftWorld(), getHandle(), getHandle().blockPosition(), isSilent(), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (zombievillager != null) ? (ZombieVillager) zombievillager.getBukkitEntity() : null;
    }

    @Override
    public int getReputation(UUID uuid, ReputationType reputationType) {
        Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        Preconditions.checkArgument(reputationType != null, "Reputation type cannot be null");
        return getHandle().getGossips().getReputation(uuid,
                Predicate.isEqual(CraftReputationType.bukkitToMinecraft(reputationType)),
                false);
    }

    @Override
    public int getWeightedReputation(UUID uuid, ReputationType reputationType) {
        Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        Preconditions.checkArgument(reputationType != null, "Reputation type cannot be null");
        return getHandle().getGossips().getReputation(uuid,
                Predicate.isEqual(CraftReputationType.bukkitToMinecraft(reputationType)),
                true);
    }

    @Override
    public int getReputation(UUID uuid) {
        Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        return getHandle().getGossips().getReputation(uuid, reputationType -> true);
    }

    @Override
    public void addReputation(UUID uuid, ReputationType reputationType, int amount) {
        addReputation(uuid, reputationType, amount, ReputationEvent.UNSPECIFIED);
    }

    @Override
    public void addReputation(UUID uuid, ReputationType reputationType, int amount, ReputationEvent changeReason) {
        Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        Preconditions.checkArgument(reputationType != null, "Reputation type cannot be null");
        Preconditions.checkArgument(changeReason != null, "Change reason cannot be null");
        getHandle().getGossips().add(uuid, CraftReputationType.bukkitToMinecraft(reputationType), amount, changeReason);
    }

    @Override
    public void removeReputation(UUID uuid, ReputationType reputationType, int amount) {
        removeReputation(uuid, reputationType, amount, ReputationEvent.UNSPECIFIED);
    }

    @Override
    public void removeReputation(UUID uuid, ReputationType reputationType, int amount, ReputationEvent changeReason) {
        Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        Preconditions.checkArgument(reputationType != null, "Reputation type cannot be null");
        Preconditions.checkArgument(changeReason != null, "Change reason cannot be null");
        getHandle().getGossips().remove(uuid, CraftReputationType.bukkitToMinecraft(reputationType), amount, changeReason);
    }

    @Override
    public void setReputation(UUID uuid, ReputationType reputationType, int amount) {
        setReputation(uuid, reputationType, amount, ReputationEvent.UNSPECIFIED);
    }

    @Override
    public void setReputation(UUID uuid, ReputationType reputationType, int amount, ReputationEvent changeReason) {
        Preconditions.checkArgument(uuid != null, "UUID cannot be null");
        Preconditions.checkArgument(reputationType != null, "Reputation type cannot be null");
        Preconditions.checkArgument(changeReason != null, "Change reason cannot be null");
        getHandle().getGossips().set(uuid, CraftReputationType.bukkitToMinecraft(reputationType), amount, changeReason);
    }

    @Override
    public void setGossipDecayTime(long ticks) {
        getHandle().gossipDecayInterval = ticks;
    }

    @Override
    public long getGossipDecayTime() {
        return getHandle().gossipDecayInterval;
    }

    public static class CraftType extends CraftOldEnumRegistryItem<Type, VillagerType> implements Type {
        private static int count = 0;

        public static Type minecraftToBukkit(VillagerType minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, Registries.VILLAGER_TYPE, Registry.VILLAGER_TYPE);
        }

        public static Type minecraftHolderToBukkit(Holder<VillagerType> minecraft) {
            return minecraftToBukkit(minecraft.value());
        }

        public static VillagerType bukkitToMinecraft(Type bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        public static Holder<VillagerType> bukkitToMinecraftHolder(Type bukkit) {
            Preconditions.checkArgument(bukkit != null);

            net.minecraft.core.Registry<VillagerType> registry = CraftRegistry.getMinecraftRegistry(Registries.VILLAGER_TYPE);

            if (registry.wrapAsHolder(bukkitToMinecraft(bukkit)) instanceof Holder.Reference<VillagerType> holder) {
                return holder;
            }

            throw new IllegalArgumentException("No Reference holder found for " + bukkit
                    + ", this can happen if a plugin creates its own villager type without properly registering it.");
        }

        public CraftType(NamespacedKey key, Holder<VillagerType> handle) {
            super(key, handle, count++);
        }

        @Override
        public NamespacedKey getKey() {
            return getKeyOrThrow();
        }
    }

    public static class CraftProfession extends CraftOldEnumRegistryItem<Profession, VillagerProfession> implements Profession {
        private static int count = 0;

        public static Profession minecraftToBukkit(VillagerProfession minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, Registries.VILLAGER_PROFESSION, Registry.VILLAGER_PROFESSION);
        }

        public static Profession minecraftHolderToBukkit(Holder<VillagerProfession> minecraft) {
            return minecraftToBukkit(minecraft.value());
        }

        public static VillagerProfession bukkitToMinecraft(Profession bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        public static Holder<VillagerProfession> bukkitToMinecraftHolder(Profession bukkit) {
            Preconditions.checkArgument(bukkit != null);

            net.minecraft.core.Registry<VillagerProfession> registry = CraftRegistry.getMinecraftRegistry(Registries.VILLAGER_PROFESSION);

            if (registry.wrapAsHolder(bukkitToMinecraft(bukkit)) instanceof Holder.Reference<VillagerProfession> holder) {
                return holder;
            }

            throw new IllegalArgumentException("No Reference holder found for " + bukkit
                    + ", this can happen if a plugin creates its own villager profession without properly registering it.");
        }

        public CraftProfession(NamespacedKey key, Holder<VillagerProfession> handle) {
            super(key, handle, count++);
        }

        @Override
        public NamespacedKey getKey() {
            return getKeyOrThrow();
        }
    }

    public static class CraftReputationType implements ReputationType, Handleable<net.minecraft.world.entity.ai.gossip.GossipType> {

        public static final Map<String, CraftReputationType> BY_ID = Stream
                .of(net.minecraft.world.entity.ai.gossip.GossipType.values())
                .collect(Collectors.toMap(reputationType -> reputationType.id, CraftReputationType::new));
        private final net.minecraft.world.entity.ai.gossip.GossipType handle;

        public CraftReputationType(net.minecraft.world.entity.ai.gossip.GossipType handle) {
            this.handle = handle;
        }

        @Override
        public net.minecraft.world.entity.ai.gossip.GossipType getHandle() {
            return handle;
        }

        @Override
        public int getMaxValue() {
            return handle.max;
        }

        @Override
        public int getWeight() {
            return handle.weight;
        }

        public static net.minecraft.world.entity.ai.gossip.GossipType bukkitToMinecraft(ReputationType bukkit) {
            Preconditions.checkArgument(bukkit != null);

            return ((CraftReputationType) bukkit).getHandle();
        }

        public static ReputationType minecraftToBukkit(net.minecraft.world.entity.ai.gossip.GossipType minecraft) {
            Preconditions.checkArgument(minecraft != null);

            return switch (minecraft) {
                case MAJOR_NEGATIVE -> ReputationType.MAJOR_NEGATIVE;
                case MINOR_NEGATIVE -> ReputationType.MINOR_NEGATIVE;
                case MINOR_POSITIVE -> ReputationType.MINOR_POSITIVE;
                case MAJOR_POSITIVE -> ReputationType.MAJOR_POSITIVE;
                case TRADING -> ReputationType.TRADING;
            };
        }
    }

    public static class CraftReputationEvent implements ReputationEvent, Handleable<net.minecraft.world.entity.ai.village.ReputationEventType> {

        private static final Map<String, ReputationEvent> ALL = Maps.newHashMap();
        private final net.minecraft.world.entity.ai.village.ReputationEventType handle;

        public CraftReputationEvent(net.minecraft.world.entity.ai.village.ReputationEventType handle) {
            this.handle = handle;
            ALL.put(handle.toString(), this);
        }

        @Override
        public net.minecraft.world.entity.ai.village.ReputationEventType getHandle() {
            return handle;
        }

        public static net.minecraft.world.entity.ai.village.ReputationEventType bukkitToMinecraft(ReputationEvent bukkit) {
            Preconditions.checkArgument(bukkit != null);

            return ((CraftReputationEvent) bukkit).getHandle();
        }

        public static ReputationEvent minecraftToBukkit(net.minecraft.world.entity.ai.village.ReputationEventType minecraft) {
            Preconditions.checkArgument(minecraft != null);

            ReputationEvent bukkit = ALL.get(minecraft.toString());
            return bukkit == null ? new CraftReputationEvent(minecraft) : bukkit;
        }
    }
}
