package org.bukkit.craftbukkit;

import com.mojang.serialization.Codec;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.entity.memory.CraftMemoryMapper;
import org.bukkit.craftbukkit.profile.CraftPlayerProfile;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;

@SerializableAs("Player")
public class CraftOfflinePlayer implements OfflinePlayer, ConfigurationSerializable {
    private final NameAndId profile;
    private final CraftServer server;
    private final PlayerDataStorage storage;

    protected CraftOfflinePlayer(CraftServer server, NameAndId profile) {
        this.server = server;
        this.profile = profile;
        this.storage = server.console.playerDataStorage;

    }

    @Override
    public boolean isOnline() {
        return getPlayer() != null;
    }

    @Override
    public String getName() {
        Player player = getPlayer();
        if (player != null) {
            return player.getName();
        }

        // This might not match lastKnownName but if not it should be more correct
        if (!profile.name().isEmpty()) {
            return profile.name();
        }

        ValueInput data = getBukkitData();

        if (data != null) {
            return data.getStringOr("lastKnownName", null);
        }

        return null;
    }

    @Override
    public UUID getUniqueId() {
        return profile.id();
    }

    @Override
    public PlayerProfile getPlayerProfile() {
        return new CraftPlayerProfile(profile);
    }

    public Server getServer() {
        return server;
    }

    @Override
    public boolean isOp() {
        return server.getHandle().isOp(profile);
    }

    @Override
    public void setOp(boolean value) {
        if (value == isOp()) {
            return;
        }

        if (value) {
            server.getHandle().op(profile);
        } else {
            server.getHandle().deop(profile);
        }
    }

    @Override
    public boolean isBanned() {
        return ((ProfileBanList) server.getBanList(BanList.Type.PROFILE)).isBanned(getPlayerProfile());
    }

    @Override
    public BanEntry<PlayerProfile> ban(String reason, Date expires, String source) {
        return ((ProfileBanList) server.getBanList(BanList.Type.PROFILE)).addBan(getPlayerProfile(), reason, expires, source);
    }

    @Override
    public BanEntry<PlayerProfile> ban(String reason, Instant expires, String source) {
        return ((ProfileBanList) server.getBanList(BanList.Type.PROFILE)).addBan(getPlayerProfile(), reason, expires, source);
    }

    @Override
    public BanEntry<PlayerProfile> ban(String reason, Duration duration, String source) {
        return ((ProfileBanList) server.getBanList(BanList.Type.PROFILE)).addBan(getPlayerProfile(), reason, duration, source);
    }

    public void setBanned(boolean value) {
        if (value) {
            ((ProfileBanList) server.getBanList(BanList.Type.PROFILE)).addBan(getPlayerProfile(), null, (Date) null, null);
        } else {
            ((ProfileBanList) server.getBanList(BanList.Type.PROFILE)).pardon(getPlayerProfile());
        }
    }

    @Override
    public boolean isWhitelisted() {
        return server.getHandle().getWhiteList().isWhiteListed(profile);
    }

    @Override
    public void setWhitelisted(boolean value) {
        if (value) {
            server.getHandle().getWhiteList().add(new UserWhiteListEntry(profile));
        } else {
            server.getHandle().getWhiteList().remove(profile);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("UUID", profile.id().toString());

        return result;
    }

    public static OfflinePlayer deserialize(Map<String, Object> args) {
        // Backwards comparability
        if (args.get("name") != null) {
            return Bukkit.getServer().getOfflinePlayer((String) args.get("name"));
        }

        return Bukkit.getServer().getOfflinePlayer(UUID.fromString((String) args.get("UUID")));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[UUID=" + profile.id() + "]";
    }

    @Override
    public Player getPlayer() {
        return server.getPlayer(getUniqueId());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OfflinePlayer other)) {
            return false;
        }

        if ((this.getUniqueId() == null) || (other.getUniqueId() == null)) {
            return false;
        }

        return this.getUniqueId().equals(other.getUniqueId());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.getUniqueId() != null ? this.getUniqueId().hashCode() : 0);
        return hash;
    }

    private ValueInput getData() {
        return storage.load(profile).map((compoundtag) -> {
            return TagValueInput.create(ProblemReporter.DISCARDING, server.console.registryAccess(), compoundtag);
        }).orElse(null);
    }

    private ValueInput getBukkitData() {
        ValueInput result = getData();

        if (result != null) {
            result = result.childOrEmpty("bukkit");
        }

        return result;
    }

    private File getDataFile() {
        return new File(storage.getPlayerDir(), getUniqueId() + ".dat");
    }

    @Override
    public long getFirstPlayed() {
        Player player = getPlayer();
        if (player != null) return player.getFirstPlayed();

        ValueInput data = getBukkitData();

        if (data != null) {
            Optional<Long> firstPlayed = data.getLong("firstPlayed");
            if (firstPlayed.isPresent()) {
                return firstPlayed.get();
            } else {
                File file = getDataFile();
                return file.lastModified();
            }
        } else {
            return 0;
        }
    }

    @Override
    public long getLastPlayed() {
        Player player = getPlayer();
        if (player != null) return player.getLastPlayed();

        ValueInput data = getBukkitData();

        if (data != null) {
            Optional<Long> lastPlayed = data.getLong("lastPlayed");
            if (lastPlayed.isPresent()) {
                return lastPlayed.get();
            } else {
                File file = getDataFile();
                return file.lastModified();
            }
        } else {
            return 0;
        }
    }

    @Override
    public boolean hasPlayedBefore() {
        return getData() != null;
    }

    @Override
    public Location getLastDeathLocation() {
        ValueInput data = getData();
        if (data == null) {
            return null;
        }

        return data.read("LastDeathLocation", GlobalPos.CODEC).map(CraftMemoryMapper::fromNms).orElse(null);
    }

    @Override
    public Location getLocation() {
        ValueInput data = getData();
        if (data == null) {
            return null;
        }
        Vec3 position = data.read("Pos", Vec3.CODEC).orElse(null);
        Vec2 rotation = data.read("Rotation", Vec2.CODEC).orElse(null);
        if (position != null && rotation != null) {
            UUID uuid = new UUID(data.getLongOr("WorldUUIDMost", 0), data.getLongOr("WorldUUIDLeast", 0));

            return CraftLocation.toBukkit(position, server.getWorld(uuid), rotation.x, rotation.y);
        }

        return null;
    }

    @Override
    public Location getBedSpawnLocation() {
        return getRespawnLocation();
    }

    @Override
    public Location getRespawnLocation() {
        ValueInput data = getData();
        if (data == null) return null;

        ServerPlayer.RespawnConfig respawnConfig = data.read("respawn", ServerPlayer.RespawnConfig.CODEC).orElse(null);
        if (respawnConfig != null) {
            LevelData.RespawnData respawn = respawnConfig.respawnData();
            ServerLevel world = server.getServer().getLevel(respawn.dimension());
            if (world == null) {
                world = server.getServer().overworld();
            }

            return CraftLocation.toBukkit(respawn.pos(), world.getWorld(), respawn.yaw(), respawn.pitch());
        }

        if (data.read("SpawnX", Codec.INT).isPresent() && data.read("SpawnY", Codec.INT).isPresent() && data.read("SpawnZ", Codec.INT).isPresent()) {
            String spawnWorld = data.getStringOr("SpawnWorld", "");
            if (spawnWorld.equals("")) {
                spawnWorld = server.getWorlds().get(0).getName();
            }
            return new Location(server.getWorld(spawnWorld), data.getIntOr("SpawnX", 0), data.getIntOr("SpawnY", 0), data.getIntOr("SpawnZ", 0));
        }
        return null;
    }

    public void setMetadata(String metadataKey, MetadataValue metadataValue) {
        server.getPlayerMetadata().setMetadata(this, metadataKey, metadataValue);
    }

    public List<MetadataValue> getMetadata(String metadataKey) {
        return server.getPlayerMetadata().getMetadata(this, metadataKey);
    }

    public boolean hasMetadata(String metadataKey) {
        return server.getPlayerMetadata().hasMetadata(this, metadataKey);
    }

    public void removeMetadata(String metadataKey, Plugin plugin) {
        server.getPlayerMetadata().removeMetadata(this, metadataKey, plugin);
    }

    private ServerStatsCounter getStatisticManager() {
        return server.getHandle().getPlayerStats(profile);
    }

    @Override
    public void incrementStatistic(Statistic statistic) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.incrementStatistic(manager, statistic, null);
            manager.save();
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.decrementStatistic(manager, statistic, null);
            manager.save();
        }
    }

    @Override
    public int getStatistic(Statistic statistic) {
        if (isOnline()) {
            return getPlayer().getStatistic(statistic);
        } else {
            return CraftStatistic.getStatistic(getStatisticManager(), statistic);
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic, int amount) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic, amount);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.incrementStatistic(manager, statistic, amount, null);
            manager.save();
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic, int amount) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic, amount);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.decrementStatistic(manager, statistic, amount, null);
            manager.save();
        }
    }

    @Override
    public void setStatistic(Statistic statistic, int newValue) {
        if (isOnline()) {
            getPlayer().setStatistic(statistic, newValue);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.setStatistic(manager, statistic, newValue, null);
            manager.save();
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic, material);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.incrementStatistic(manager, statistic, material, null);
            manager.save();
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic, material);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.decrementStatistic(manager, statistic, material, null);
            manager.save();
        }
    }

    @Override
    public int getStatistic(Statistic statistic, Material material) {
        if (isOnline()) {
            return getPlayer().getStatistic(statistic, material);
        } else {
            return CraftStatistic.getStatistic(getStatisticManager(), statistic, material);
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int amount) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic, material, amount);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.incrementStatistic(manager, statistic, material, amount, null);
            manager.save();
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int amount) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic, material, amount);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.decrementStatistic(manager, statistic, material, amount, null);
            manager.save();
        }
    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int newValue) {
        if (isOnline()) {
            getPlayer().setStatistic(statistic, material, newValue);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.setStatistic(manager, statistic, material, newValue, null);
            manager.save();
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic, entityType);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.incrementStatistic(manager, statistic, entityType, null);
            manager.save();
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic, entityType);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.decrementStatistic(manager, statistic, entityType, null);
            manager.save();
        }
    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) {
        if (isOnline()) {
            return getPlayer().getStatistic(statistic, entityType);
        } else {
            return CraftStatistic.getStatistic(getStatisticManager(), statistic, entityType);
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        if (isOnline()) {
            getPlayer().incrementStatistic(statistic, entityType, amount);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.incrementStatistic(manager, statistic, entityType, amount, null);
            manager.save();
        }
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        if (isOnline()) {
            getPlayer().decrementStatistic(statistic, entityType, amount);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.decrementStatistic(manager, statistic, entityType, amount, null);
            manager.save();
        }
    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
        if (isOnline()) {
            getPlayer().setStatistic(statistic, entityType, newValue);
        } else {
            ServerStatsCounter manager = getStatisticManager();
            CraftStatistic.setStatistic(manager, statistic, entityType, newValue, null);
            manager.save();
        }
    }
}
