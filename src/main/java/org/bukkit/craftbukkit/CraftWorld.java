package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.FeatureFlag;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameRule;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Raid;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.boss.CraftDragonBattle;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.generator.structure.CraftGeneratedStructure;
import org.bukkit.craftbukkit.generator.structure.CraftStructure;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.metadata.BlockMetadataStore;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.util.CraftBiomeSearchResult;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.CraftRayTraceResult;
import org.bukkit.craftbukkit.util.CraftSpawnCategory;
import org.bukkit.craftbukkit.util.CraftStructureSearchResult;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BiomeSearchResult;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.StructureSearchResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftWorld extends CraftRegionAccessor implements World {
    public static final int CUSTOM_DIMENSION_OFFSET = 10;
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();

    private final ServerLevel world;
    private WorldBorder worldBorder;
    private Environment environment;
    private final CraftServer server = (CraftServer) Bukkit.getServer();
    private final ChunkGenerator generator;
    private final BiomeProvider biomeProvider;
    private final List<BlockPopulator> populators = new ArrayList<BlockPopulator>();
    private final BlockMetadataStore blockMetadata = new BlockMetadataStore(this);
    private final Object2IntOpenHashMap<SpawnCategory> spawnCategoryLimit = new Object2IntOpenHashMap<>();
    private final CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);

    private static final Random rand = new Random();

    public CraftWorld(ServerLevel world, ChunkGenerator gen, BiomeProvider biomeProvider, Environment env) {
        this.world = world;
        this.generator = gen;
        this.biomeProvider = biomeProvider;

        environment = env;
    }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        return CraftBlock.at(world, new BlockPos(x, y, z));
    }

    @Override
    public Location getSpawnLocation() {
        LevelData.RespawnData respawnData = world.getRespawnData();
        ServerLevel spawnWorld = world.getServer().getLevel(respawnData.dimension());
        return CraftLocation.toBukkit(respawnData.pos(), (spawnWorld != null) ? spawnWorld.getWorld() : this, respawnData.yaw(), respawnData.pitch());
    }

    @Override
    public boolean setSpawnLocation(Location location) {
        Preconditions.checkArgument(location != null, "location");
        World spawnWorld = location.getWorld();

        return setSpawnLocation((spawnWorld != null) ? spawnWorld : this, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public boolean setSpawnLocation(int x, int y, int z, float angle) {
        return setSpawnLocation(this, x, y, z, angle, 0.0F);
    }

    private boolean setSpawnLocation(World spawnWorld, int x, int y, int z, float yaw, float pitch) {
        try {
            world.setRespawnData(LevelData.RespawnData.of(((CraftWorld) spawnWorld).getHandle().dimension(), new BlockPos(x, y, z), yaw, pitch));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean setSpawnLocation(int x, int y, int z) {
        return setSpawnLocation(x, y, z, 0.0F);
    }

    @Override
    public Chunk getChunkAt(int x, int z) {
        net.minecraft.world.level.chunk.LevelChunk levelchunk = (net.minecraft.world.level.chunk.LevelChunk) this.world.getChunk(x, z, ChunkStatus.FULL, true);
        return new CraftChunk(levelchunk);
    }

    @NotNull
    @Override
    public Chunk getChunkAt(int x, int z, boolean generate) {
        if (generate) {
            return getChunkAt(x, z);
        }

        return new CraftChunk(getHandle(), x, z);
    }

    @Override
    public Chunk getChunkAt(Block block) {
        Preconditions.checkArgument(block != null, "null block");

        return getChunkAt(block.getX() >> 4, block.getZ() >> 4);
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        return world.getChunkSource().isChunkLoaded(x, z);
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        try {
            return isChunkLoaded(x, z) || world.getChunkSource().chunkMap.read(new ChunkPos(x, z)).get().isPresent();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Chunk[] getLoadedChunks() {
        Long2ObjectLinkedOpenHashMap<ChunkHolder> chunks = world.getChunkSource().chunkMap.visibleChunkMap;
        return chunks.values().stream().map(ChunkHolder::getFullChunkNow).filter(Objects::nonNull).map(CraftChunk::new).toArray(Chunk[]::new);
    }

    @Override
    public void loadChunk(int x, int z) {
        loadChunk(x, z, true);
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        return unloadChunk(chunk.getX(), chunk.getZ());
    }

    @Override
    public boolean unloadChunk(int x, int z) {
        return unloadChunk(x, z, true);
    }

    @Override
    public boolean unloadChunk(int x, int z, boolean save) {
        return unloadChunk0(x, z, save);
    }

    @Override
    public boolean unloadChunkRequest(int x, int z) {
        if (isChunkLoaded(x, z)) {
            world.getChunkSource().removeTicketWithRadius(TicketType.PLUGIN, new ChunkPos(x, z), 1);
        }

        return true;
    }

    private boolean unloadChunk0(int x, int z, boolean save) {
        if (!isChunkLoaded(x, z)) {
            return true;
        }
        net.minecraft.world.level.chunk.LevelChunk levelchunk = world.getChunk(x, z);

        if (!save) {
            levelchunk.tryMarkSaved(); // Use method call to account for persistentDataContainer
        }
        unloadChunkRequest(x, z);

        world.getChunkSource().purgeUnload();
        return !isChunkLoaded(x, z);
    }

    @Override
    public boolean regenerateChunk(int x, int z) {
        throw new UnsupportedOperationException("Not supported in this Minecraft version! Unless you can fix it, this is not a bug :)");
        /*
        if (!unloadChunk0(x, z, false)) {
            return false;
        }

        final long chunkKey = ChunkCoordIntPair.pair(x, z);
        world.getChunkProvider().unloadQueue.remove(chunkKey);

        net.minecraft.server.Chunk chunk = world.getChunkProvider().generateChunk(x, z);
        PlayerChunk playerChunk = world.getPlayerChunkMap().getChunk(x, z);
        if (playerChunk != null) {
            playerChunk.chunk = chunk;
        }

        if (chunk != null) {
            refreshChunk(x, z);
        }

        return chunk != null;
        */
    }

    @Override
    public boolean refreshChunk(int x, int z) {
        ChunkHolder playerChunk = world.getChunkSource().chunkMap.visibleChunkMap.get(ChunkPos.pack(x, z));
        if (playerChunk == null) return false;

        playerChunk.getTickingChunkFuture().thenAccept(either -> {
            either.ifSuccess(levelchunk -> {
                List<ServerPlayer> playersInRange = playerChunk.playerProvider.getPlayers(playerChunk.getPos(), false);
                if (playersInRange.isEmpty()) return;

                ClientboundLevelChunkWithLightPacket refreshPacket = new ClientboundLevelChunkWithLightPacket(levelchunk, world.getLightEngine(), null, null);
                for (ServerPlayer player : playersInRange) {
                    if (player.connection == null) continue;

                    player.connection.send(refreshPacket);
                }
            });
        });

        return true;
    }

    @Override
    public Collection<Player> getPlayersSeeingChunk(Chunk chunk) {
        Preconditions.checkArgument(chunk != null, "chunk cannot be null");

        return getPlayersSeeingChunk(chunk.getX(), chunk.getZ());
    }

    @Override
    public Collection<Player> getPlayersSeeingChunk(int x, int z) {
        if (!isChunkLoaded(x, z)) {
            return Collections.emptySet();
        }

        List<ServerPlayer> players = world.getChunkSource().chunkMap.getPlayers(new ChunkPos(x, z), false);

        if (players.isEmpty()) {
            return Collections.emptySet();
        }

        return players.stream()
                .filter(Objects::nonNull)
                .map(ServerPlayer::getBukkitEntity)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isChunkInUse(int x, int z) {
        return isChunkLoaded(x, z);
    }

    @Override
    public boolean loadChunk(int x, int z, boolean generate) {
        ChunkAccess chunk = world.getChunkSource().getChunk(x, z, generate ? ChunkStatus.FULL : ChunkStatus.EMPTY, true);

        // If generate = false, but the chunk already exists, we will get this back.
        if (chunk instanceof ImposterProtoChunk) {
            // We then cycle through again to get the full chunk immediately, rather than after the ticket addition
            chunk = world.getChunkSource().getChunk(x, z, ChunkStatus.FULL, true);
        }

        if (chunk instanceof net.minecraft.world.level.chunk.LevelChunk) {
            world.getChunkSource().addTicketWithRadius(TicketType.PLUGIN, new ChunkPos(x, z), 1);
            return true;
        }

        return false;
    }

    @Override
    public boolean isChunkLoaded(Chunk chunk) {
        Preconditions.checkArgument(chunk != null, "null chunk");

        return isChunkLoaded(chunk.getX(), chunk.getZ());
    }

    @Override
    public void loadChunk(Chunk chunk) {
        Preconditions.checkArgument(chunk != null, "null chunk");

        loadChunk(chunk.getX(), chunk.getZ());
    }

    @Override
    public boolean addPluginChunkTicket(int x, int z, Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "null plugin");
        Preconditions.checkArgument(plugin.isEnabled(), "plugin is not enabled");

        TicketStorage chunkDistanceManager = this.world.getChunkSource().ticketStorage;

        if (chunkDistanceManager.addTicket(ChunkPos.pack(x, z), Ticket.of(TicketType.PLUGIN_TICKET, ChunkMap.FORCED_TICKET_LEVEL, plugin))) { // keep in-line with force loading, add at level 31
            this.getChunkAt(x, z); // ensure loaded
            return true;
        }

        return false;
    }

    @Override
    public boolean removePluginChunkTicket(int x, int z, Plugin plugin) {
        Preconditions.checkNotNull(plugin, "null plugin");

        TicketStorage chunkDistanceManager = this.world.getChunkSource().ticketStorage;
        return chunkDistanceManager.removeTicket(ChunkPos.pack(x, z), Ticket.of(TicketType.PLUGIN_TICKET, ChunkMap.FORCED_TICKET_LEVEL, plugin)); // keep in-line with force loading, remove at level 31
    }

    @Override
    public void removePluginChunkTickets(Plugin plugin) {
        Preconditions.checkNotNull(plugin, "null plugin");

        TicketStorage chunkDistanceManager = this.world.getChunkSource().ticketStorage;
        chunkDistanceManager.removeTicketIf((ticket, i) -> ticket.getType() == TicketType.PLUGIN_TICKET && Objects.equals(ticket.key, plugin), null);
    }

    @Override
    public Collection<Plugin> getPluginChunkTickets(int x, int z) {
        TicketStorage chunkDistanceManager = this.world.getChunkSource().ticketStorage;
        List<Ticket> tickets = chunkDistanceManager.getTickets(ChunkPos.pack(x, z));

        if (tickets == null) {
            return Collections.emptyList();
        }

        ImmutableList.Builder<Plugin> ret = ImmutableList.builder();
        for (Ticket ticket : tickets) {
            if (ticket.getType() == TicketType.PLUGIN_TICKET) {
                ret.add((Plugin) ticket.key);
            }
        }

        return ret.build();
    }

    @Override
    public Map<Plugin, Collection<Chunk>> getPluginChunkTickets() {
        Map<Plugin, ImmutableList.Builder<Chunk>> ret = new HashMap<>();
        TicketStorage chunkDistanceManager = this.world.getChunkSource().ticketStorage;

        for (Long2ObjectMap.Entry<List<Ticket>> chunkTickets : chunkDistanceManager.tickets.long2ObjectEntrySet()) {
            long chunkKey = chunkTickets.getLongKey();
            List<Ticket> tickets = chunkTickets.getValue();

            Chunk chunk = null;
            for (Ticket ticket : tickets) {
                if (ticket.getType() != TicketType.PLUGIN_TICKET) {
                    continue;
                }

                if (chunk == null) {
                    chunk = this.getChunkAt(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey));
                }

                ret.computeIfAbsent((Plugin) ticket.key, (key) -> ImmutableList.builder()).add(chunk);
            }
        }

        return ret.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry) -> entry.getValue().build()));
    }

    @NotNull
    @Override
    public Collection<Chunk> getIntersectingChunks(@NotNull BoundingBox boundingBox) {
        List<Chunk> chunks = new ArrayList<>();

        int minX = NumberConversions.floor(boundingBox.getMinX()) >> 4;
        int maxX = NumberConversions.floor(boundingBox.getMaxX()) >> 4;
        int minZ = NumberConversions.floor(boundingBox.getMinZ()) >> 4;
        int maxZ = NumberConversions.floor(boundingBox.getMaxZ()) >> 4;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(getChunkAt(x, z, false));
            }
        }

        return chunks;
    }

    @Override
    public boolean isChunkForceLoaded(int x, int z) {
        return getHandle().getForceLoadedChunks().contains(ChunkPos.pack(x, z));
    }

    @Override
    public void setChunkForceLoaded(int x, int z, boolean forced) {
        getHandle().setChunkForced(x, z, forced);
    }

    @Override
    public Collection<Chunk> getForceLoadedChunks() {
        Set<Chunk> chunks = new HashSet<>();

        for (long coord : getHandle().getForceLoadedChunks()) {
            chunks.add(getChunkAt(ChunkPos.getX(coord), ChunkPos.getZ(coord)));
        }

        return Collections.unmodifiableCollection(chunks);
    }

    public ServerLevel getHandle() {
        return world;
    }

    @Override
    public org.bukkit.entity.Item dropItem(Location loc, ItemStack item) {
        return dropItem(loc, item, null);
    }

    @Override
    public org.bukkit.entity.Item dropItem(Location loc, ItemStack item, Consumer<? super org.bukkit.entity.Item> function) {
        Preconditions.checkArgument(loc != null, "Location cannot be null");
        Preconditions.checkArgument(item != null, "ItemStack cannot be null");

        ItemEntity entity = new ItemEntity(world, loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
        org.bukkit.entity.Item itemEntity = (org.bukkit.entity.Item) entity.getBukkitEntity();
        entity.pickupDelay = 10;
        if (function != null) {
            function.accept(itemEntity);
        }
        world.addFreshEntity(entity, SpawnReason.CUSTOM);
        return itemEntity;
    }

    @Override
    public org.bukkit.entity.Item dropItemNaturally(Location loc, ItemStack item) {
        return dropItemNaturally(loc, item, null);
    }

    @Override
    public org.bukkit.entity.Item dropItemNaturally(Location loc, ItemStack item, Consumer<? super org.bukkit.entity.Item> function) {
        Preconditions.checkArgument(loc != null, "Location cannot be null");
        Preconditions.checkArgument(item != null, "ItemStack cannot be null");

        double xs = Mth.nextDouble(world.getRandom(), -0.25D, 0.25D);
        double ys = Mth.nextDouble(world.getRandom(), -0.25D, 0.25D) - ((double) EntityTypes.ITEM.getHeight() / 2.0D);
        double zs = Mth.nextDouble(world.getRandom(), -0.25D, 0.25D);
        loc = loc.clone().add(xs, ys, zs);
        return dropItem(loc, item, function);
    }

    @Override
    public Arrow spawnArrow(Location loc, Vector velocity, float speed, float spread) {
        return spawnArrow(loc, velocity, speed, spread, Arrow.class);
    }

    @Override
    public <T extends AbstractArrow> T spawnArrow(Location loc, Vector velocity, float speed, float spread, Class<T> clazz) {
        Preconditions.checkArgument(loc != null, "Location cannot be null");
        Preconditions.checkArgument(velocity != null, "Vector cannot be null");
        Preconditions.checkArgument(clazz != null, "clazz Entity for the arrow cannot be null");

        net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow;
        if (TippedArrow.class.isAssignableFrom(clazz)) {
            arrow = EntityTypes.ARROW.create(world, EntitySpawnReason.COMMAND);
            ((Arrow) arrow.getBukkitEntity()).setBasePotionType(PotionType.WATER);
        } else if (SpectralArrow.class.isAssignableFrom(clazz)) {
            arrow = EntityTypes.SPECTRAL_ARROW.create(world, EntitySpawnReason.COMMAND);
        } else if (Trident.class.isAssignableFrom(clazz)) {
            arrow = EntityTypes.TRIDENT.create(world, EntitySpawnReason.COMMAND);
        } else {
            arrow = EntityTypes.ARROW.create(world, EntitySpawnReason.COMMAND);
        }

        arrow.snapTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        arrow.shoot(velocity.getX(), velocity.getY(), velocity.getZ(), speed, spread);
        world.addFreshEntity(arrow);
        return (T) arrow.getBukkitEntity();
    }

    @Override
    public LightningStrike strikeLightning(Location loc) {
        return strikeLightning0(loc, false);
    }

    @Override
    public LightningStrike strikeLightningEffect(Location loc) {
        return strikeLightning0(loc, true);
    }

    private LightningStrike strikeLightning0(Location loc, boolean isVisual) {
        Preconditions.checkArgument(loc != null, "Location cannot be null");

        LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.COMMAND);
        lightning.snapTo(loc.getX(), loc.getY(), loc.getZ());
        lightning.setVisualOnly(isVisual);
        world.strikeLightning(lightning, LightningStrikeEvent.Cause.CUSTOM);
        return (LightningStrike) lightning.getBukkitEntity();
    }

    @Override
    public boolean generateTree(Location loc, TreeType type) {
        return generateTree(loc, rand, type);
    }

    @Override
    public boolean generateTree(Location loc, TreeType type, BlockChangeDelegate delegate) {
        world.captureTreeGeneration = true;
        world.captureBlockStates = true;
        boolean grownTree = generateTree(loc, type);
        world.captureBlockStates = false;
        world.captureTreeGeneration = false;
        if (grownTree) { // Copy block data to delegate
            for (BlockState blockstate : world.capturedBlockStates.values()) {
                BlockPos position = ((CraftBlockState) blockstate).getPosition();
                net.minecraft.world.level.block.state.BlockState oldBlock = world.getBlockState(position);
                int flag = ((CraftBlockState) blockstate).getFlag();
                delegate.setBlockData(blockstate.getX(), blockstate.getY(), blockstate.getZ(), blockstate.getBlockData());
                net.minecraft.world.level.block.state.BlockState newBlock = world.getBlockState(position);
                world.notifyAndUpdatePhysics(position, null, oldBlock, newBlock, newBlock, flag, 512);
            }
            world.capturedBlockStates.clear();
            return true;
        } else {
            world.capturedBlockStates.clear();
            return false;
        }
    }

    @Override
    public String getName() {
        return world.serverLevelData.getLevelName();
    }

    @Override
    public UUID getUID() {
        return world.uuid;
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(world.dimension().identifier());
    }

    @Override
    public String toString() {
        return "CraftWorld{name=" + getName() + '}';
    }

    @Override
    public long getTime() {
        long time = getFullTime() % 24000;
        if (time < 0) time += 24000;
        return time;
    }

    @Override
    public void setTime(long time) {
        long margin = (time - getFullTime()) % 24000;
        if (margin < 0) margin += 24000;
        setFullTime(getFullTime() + margin);
    }

    @Override
    public long getFullTime() {
        return world.getDefaultClockTime();
    }

    @Override
    public void setFullTime(long time) {
        world.dimensionType().defaultClock().ifPresent((clock) -> {
            world.clockManager().setTotalTicks(clock, time, TimeSkipEvent.SkipReason.CUSTOM);
        });
    }

    @Override
    public long getGameTime() {
        return world.levelData.getGameTime();
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power) {
        return createExplosion(x, y, z, power, false, true);
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire) {
        return createExplosion(x, y, z, power, setFire, true);
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        return createExplosion(x, y, z, power, setFire, breakBlocks, null);
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks, Entity source) {
        net.minecraft.world.level.Level.ExplosionInteraction explosionType;
        if (!breakBlocks) {
            explosionType = net.minecraft.world.level.Level.ExplosionInteraction.NONE; // Don't break blocks
        } else if (source == null) {
            explosionType = net.minecraft.world.level.Level.ExplosionInteraction.STANDARD; // Break blocks, don't decay drops
        } else {
            explosionType = net.minecraft.world.level.Level.ExplosionInteraction.MOB; // Respect mobGriefing gamerule
        }

        net.minecraft.world.entity.Entity entity = (source == null) ? null : ((CraftEntity) source).getHandle();
        return !world.explode0(entity, Explosion.getDefaultDamageSource(world, entity), null, x, y, z, power, setFire, explosionType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, net.minecraft.world.level.Level.DEFAULT_EXPLOSION_BLOCK_PARTICLES, SoundEvents.GENERIC_EXPLODE).wasCanceled;
    }

    @Override
    public boolean createExplosion(Location loc, float power) {
        return createExplosion(loc, power, false);
    }

    @Override
    public boolean createExplosion(Location loc, float power, boolean setFire) {
        return createExplosion(loc, power, setFire, true);
    }

    @Override
    public boolean createExplosion(Location loc, float power, boolean setFire, boolean breakBlocks) {
        return createExplosion(loc, power, setFire, breakBlocks, null);
    }

    @Override
    public boolean createExplosion(Location loc, float power, boolean setFire, boolean breakBlocks, Entity source) {
        Preconditions.checkArgument(loc != null, "Location is null");
        Preconditions.checkArgument(this.equals(loc.getWorld()), "Location not in world");

        return createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, setFire, breakBlocks, source);
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public Block getBlockAt(Location location) {
        return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public Chunk getChunkAt(Location location) {
        return getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    @Override
    public ChunkGenerator getGenerator() {
        return generator;
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    @Override
    public List<BlockPopulator> getPopulators() {
        return populators;
    }

    @NotNull
    @Override
    public <T extends LivingEntity> T spawn(@NotNull Location location, @NotNull Class<T> clazz, @NotNull SpawnReason spawnReason, boolean randomizeData, @Nullable Consumer<? super T> function) throws IllegalArgumentException {
        Preconditions.checkArgument(spawnReason != null, "Spawn reason cannot be null");
        return spawn(location, clazz, function, spawnReason, randomizeData);
    }

    @Override
    public Block getHighestBlockAt(int x, int z) {
        return getBlockAt(x, getHighestBlockYAt(x, z), z);
    }

    @Override
    public Block getHighestBlockAt(Location location) {
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public int getHighestBlockYAt(int x, int z, org.bukkit.HeightMap heightMap) {
        // Transient load for this tick
        return world.getChunk(x >> 4, z >> 4).getHeight(CraftHeightMap.toNMS(heightMap), x, z);
    }

    @Override
    public Block getHighestBlockAt(int x, int z, org.bukkit.HeightMap heightMap) {
        return getBlockAt(x, getHighestBlockYAt(x, z, heightMap), z);
    }

    @Override
    public Block getHighestBlockAt(Location location, org.bukkit.HeightMap heightMap) {
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ(), heightMap);
    }

    @Override
    public Biome getBiome(int x, int z) {
        return getBiome(x, 0, z);
    }

    @Override
    public void setBiome(int x, int z, Biome bio) {
        for (int y = getMinHeight(); y < getMaxHeight(); y++) {
            setBiome(x, y, z, bio);
        }
    }

    @Override
    public void setBiome(int x, int y, int z, Holder<net.minecraft.world.level.biome.Biome> bb) {
        BlockPos pos = new BlockPos(x, 0, z);
        if (this.world.hasChunkAt(pos)) {
            net.minecraft.world.level.chunk.LevelChunk levelchunk = this.world.getChunkAt(pos);

            if (levelchunk != null) {
                levelchunk.setBiome(x >> 2, y >> 2, z >> 2, bb);

                levelchunk.markUnsaved(); // SPIGOT-2890
            }
        }
    }

    @Override
    public double getTemperature(int x, int z) {
        return getTemperature(x, 0, z);
    }

    @Override
    public double getTemperature(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return this.world.getNoiseBiome(x >> 2, y >> 2, z >> 2).value().getTemperature(pos, this.world.getSeaLevel());
    }

    @Override
    public double getHumidity(int x, int z) {
        return getHumidity(x, 0, z);
    }

    @Override
    public double getHumidity(int x, int y, int z) {
        return this.world.getNoiseBiome(x >> 2, y >> 2, z >> 2).value().climateSettings.downfall();
    }

    @Override
    @SuppressWarnings("unchecked")
    @Deprecated
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... classes) {
        return (Collection<T>) getEntitiesByClasses(classes);
    }

    @Override
    public Iterable<net.minecraft.world.entity.Entity> getNMSEntities() {
        return getHandle().getEntities().getAll();
    }

    @Override
    public void addEntityToWorld(net.minecraft.world.entity.Entity entity, SpawnReason reason) {
        getHandle().addFreshEntity(entity, reason);
    }

    @Override
    public void addEntityWithPassengers(net.minecraft.world.entity.Entity entity, SpawnReason reason) {
        getHandle().tryAddFreshEntityWithPassengers(entity, reason);
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        return this.getNearbyEntities(location, x, y, z, null);
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z, Predicate<? super Entity> filter) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(this.equals(location.getWorld()), "Location cannot be in a different world");

        BoundingBox aabb = BoundingBox.of(location, x, y, z);
        return this.getNearbyEntities(aabb, filter);
    }

    @Override
    public Collection<Entity> getNearbyEntities(BoundingBox boundingBox) {
        return this.getNearbyEntities(boundingBox, null);
    }

    @Override
    public Collection<Entity> getNearbyEntities(BoundingBox boundingBox, Predicate<? super Entity> filter) {
        Preconditions.checkArgument(boundingBox != null, "BoundingBox cannot be null");

        AABB bb = new AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        List<net.minecraft.world.entity.Entity> entityList = getHandle().getEntities((net.minecraft.world.entity.Entity) null, bb, Predicates.alwaysTrue());
        List<Entity> bukkitEntityList = new ArrayList<Entity>(entityList.size());

        for (net.minecraft.world.entity.Entity entity : entityList) {
            Entity bukkitEntity = entity.getBukkitEntity();
            if (filter == null || filter.test(bukkitEntity)) {
                bukkitEntityList.add(bukkitEntity);
            }
        }

        return bukkitEntityList;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance) {
        return this.rayTraceEntities(start, direction, maxDistance, null);
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize) {
        return this.rayTraceEntities(start, direction, maxDistance, raySize, null);
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, Predicate<? super Entity> filter) {
        return this.rayTraceEntities(start, direction, maxDistance, 0.0D, filter);
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize, Predicate<? super Entity> filter) {
        Preconditions.checkArgument(start != null, "Location start cannot be null");
        Preconditions.checkArgument(this.equals(start.getWorld()), "Location start cannot be in a different world");
        start.checkFinite();

        Preconditions.checkArgument(direction != null, "Vector direction cannot be null");
        direction.checkFinite();

        Preconditions.checkArgument(direction.lengthSquared() > 0, "Direction's magnitude (%s) need to be greater than 0", direction.lengthSquared());

        if (maxDistance < 0.0D) {
            return null;
        }

        Vector startPos = start.toVector();
        Vector dir = direction.clone().normalize().multiply(maxDistance);
        BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(raySize);
        Collection<Entity> entities = this.getNearbyEntities(aabb, filter);

        Entity nearestHitEntity = null;
        RayTraceResult nearestHitResult = null;
        double nearestDistanceSq = Double.MAX_VALUE;

        for (Entity entity : entities) {
            BoundingBox boundingBox = entity.getBoundingBox().expand(raySize);
            RayTraceResult hitResult = boundingBox.rayTrace(startPos, direction, maxDistance);

            if (hitResult != null) {
                double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());

                if (distanceSq < nearestDistanceSq) {
                    nearestHitEntity = entity;
                    nearestHitResult = hitResult;
                    nearestDistanceSq = distanceSq;
                }
            }
        }

        return (nearestHitEntity == null) ? null : new RayTraceResult(nearestHitResult.getHitPosition(), nearestHitEntity, nearestHitResult.getHitBlockFace());
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance) {
        return this.rayTraceBlocks(start, direction, maxDistance, FluidCollisionMode.NEVER, false);
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
        return this.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, false);
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks) {
        Preconditions.checkArgument(start != null, "Location start cannot be null");
        Preconditions.checkArgument(this.equals(start.getWorld()), "Location start cannot be in a different world");
        start.checkFinite();

        Preconditions.checkArgument(direction != null, "Vector direction cannot be null");
        direction.checkFinite();

        Preconditions.checkArgument(direction.lengthSquared() > 0, "Direction's magnitude (%s) need to be greater than 0", direction.lengthSquared());
        Preconditions.checkArgument(fluidCollisionMode != null, "FluidCollisionMode cannot be null");

        if (maxDistance < 0.0D) {
            return null;
        }

        Vector dir = direction.clone().normalize().multiply(maxDistance);
        Vec3 startPos = CraftLocation.toVec3D(start);
        Vec3 endPos = startPos.add(dir.getX(), dir.getY(), dir.getZ());
        HitResult nmsHitResult = this.getHandle().clip(new ClipContext(startPos, endPos, ignorePassableBlocks ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE, CraftFluidCollisionMode.toNMS(fluidCollisionMode), CollisionContext.empty()));

        return CraftRayTraceResult.fromNMS(this, nmsHitResult);
    }

    @Override
    public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize, Predicate<? super Entity> filter) {
        RayTraceResult blockHit = this.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, ignorePassableBlocks);
        Vector startVec = null;
        double blockHitDistance = maxDistance;

        // limiting the entity search range if we found a block hit:
        if (blockHit != null) {
            startVec = start.toVector();
            blockHitDistance = startVec.distance(blockHit.getHitPosition());
        }

        RayTraceResult entityHit = this.rayTraceEntities(start, direction, blockHitDistance, raySize, filter);
        if (blockHit == null) {
            return entityHit;
        }

        if (entityHit == null) {
            return blockHit;
        }

        // Cannot be null as blockHit == null returns above
        double entityHitDistanceSquared = startVec.distanceSquared(entityHit.getHitPosition());
        if (entityHitDistanceSquared < (blockHitDistance * blockHitDistance)) {
            return entityHit;
        }

        return blockHit;
    }

    @Override
    public List<Player> getPlayers() {
        List<Player> list = new ArrayList<Player>(world.players().size());

        for (ServerPlayer human : world.players()) {
            Player bukkitEntity = human.getBukkitEntity();

            if (bukkitEntity != null) {
                list.add(bukkitEntity);
            }
        }

        return list;
    }

    @Override
    public void save() {
        this.server.checkSaveState();
        boolean oldSave = world.noSave;

        world.noSave = false;
        world.save(null, false, false);

        world.noSave = oldSave;
    }

    @Override
    public boolean isAutoSave() {
        return !world.noSave;
    }

    @Override
    public void setAutoSave(boolean value) {
        world.noSave = !value;
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.getHandle().serverLevelData.setDifficulty(net.minecraft.world.Difficulty.byId(difficulty.getValue()));
    }

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.getByValue(this.getHandle().getDifficulty().ordinal());
    }

    @Override
    public int getViewDistance() {
        return world.getChunkSource().chunkMap.serverViewDistance;
    }

    @Override
    public int getSimulationDistance() {
        return world.getChunkSource().chunkMap.getDistanceManager().simulationDistance;
    }

    public BlockMetadataStore getBlockMetadata() {
        return blockMetadata;
    }

    @Override
    public boolean hasStorm() {
        return world.getWeatherData().isRaining();
    }

    @Override
    public void setStorm(boolean hasStorm) {
        world.getWeatherData().setRaining(hasStorm);
        setWeatherDuration(0); // Reset weather duration (legacy behaviour)
        setClearWeatherDuration(0); // Reset clear weather duration (reset "/weather clear" commands)
    }

    @Override
    public int getWeatherDuration() {
        return world.getWeatherData().getRainTime();
    }

    @Override
    public void setWeatherDuration(int duration) {
        world.getWeatherData().setRainTime(duration);
    }

    @Override
    public boolean isThundering() {
        return world.getWeatherData().isThundering();
    }

    @Override
    public void setThundering(boolean thundering) {
        world.getWeatherData().setThundering(thundering);
        setThunderDuration(0); // Reset weather duration (legacy behaviour)
        setClearWeatherDuration(0); // Reset clear weather duration (reset "/weather clear" commands)
    }

    @Override
    public int getThunderDuration() {
        return world.getWeatherData().getThunderTime();
    }

    @Override
    public void setThunderDuration(int duration) {
        world.getWeatherData().setThunderTime(duration);
    }

    @Override
    public boolean isClearWeather() {
        return !this.hasStorm() && !this.isThundering();
    }

    @Override
    public void setClearWeatherDuration(int duration) {
        world.getWeatherData().setClearWeatherTime(duration);
    }

    @Override
    public int getClearWeatherDuration() {
        return world.getWeatherData().getClearWeatherTime();
    }

    @Override
    public long getSeed() {
        return world.getSeed();
    }

    @Override
    public boolean getPVP() {
        return world.getGameRules().get(GameRules.PVP);
    }

    @Override
    public void setPVP(boolean pvp) {
        world.getGameRules().set(GameRules.PVP, pvp, world);
    }

    public void playEffect(Player player, Effect effect, int data) {
        playEffect(player.getLocation(), effect, data, 0);
    }

    @Override
    public void playEffect(Location location, Effect effect, int data) {
        playEffect(location, effect, data, 64);
    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, T data) {
        playEffect(loc, effect, data, 64);
    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, T data, int radius) {
        if (data != null) {
            Preconditions.checkArgument(effect.getData() != null, "Effect.%s does not have a valid Data", effect);
            Preconditions.checkArgument(effect.getData().isAssignableFrom(data.getClass()), "%s data cannot be used for the %s effect", data.getClass().getName(), effect);
        } else {
            // Special case: the axis is optional for ELECTRIC_SPARK
            Preconditions.checkArgument(effect.getData() == null || effect == Effect.ELECTRIC_SPARK, "Wrong kind of data for the %s effect", effect);
        }

        int datavalue = CraftEffect.getDataValue(effect, data);
        playEffect(loc, effect, datavalue, radius);
    }

    @Override
    public void playEffect(Location location, Effect effect, int data, int radius) {
        Preconditions.checkArgument(effect != null, "Effect cannot be null");
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "World of Location cannot be null");
        int packetData = effect.getId();
        ClientboundLevelEventPacket packet = new ClientboundLevelEventPacket(packetData, CraftLocation.toBlockPosition(location), data, false);
        int distance;
        radius *= radius;

        for (Player player : getPlayers()) {
            if (((CraftPlayer) player).getHandle().connection == null) continue;
            if (!location.getWorld().equals(player.getWorld())) continue;

            distance = (int) player.getLocation().distanceSquared(location);
            if (distance <= radius) {
                ((CraftPlayer) player).getHandle().connection.send(packet);
            }
        }
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, MaterialData data) throws IllegalArgumentException {
        Preconditions.checkArgument(data != null, "MaterialData cannot be null");
        return spawnFallingBlock(location, data.getItemType(), data.getData());
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, org.bukkit.Material material, byte data) throws IllegalArgumentException {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(material != null, "Material cannot be null");
        Preconditions.checkArgument(material.isBlock(), "Material.%s must be a block", material);

        FallingBlockEntity entity = FallingBlockEntity.fall(world, BlockPos.containing(location.getX(), location.getY(), location.getZ()), CraftBlockType.bukkitToMinecraft(material).defaultBlockState(), SpawnReason.CUSTOM);
        return (FallingBlock) entity.getBukkitEntity();
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, BlockData data) throws IllegalArgumentException {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(data != null, "BlockData cannot be null");

        FallingBlockEntity entity = FallingBlockEntity.fall(world, BlockPos.containing(location.getX(), location.getY(), location.getZ()), ((CraftBlockData) data).getState(), SpawnReason.CUSTOM);
        return (FallingBlock) entity.getBukkitEntity();
    }

    @Override
    public ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome, boolean includeBiomeTempRain) {
        return CraftChunk.getEmptyChunkSnapshot(x, z, this, includeBiome, includeBiomeTempRain);
    }

    @Override
    public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals) {
        world.getChunkSource().setSpawnSettings(allowMonsters);
    }

    @Override
    public boolean getAllowAnimals() {
        return true;
    }

    @Override
    public boolean getAllowMonsters() {
        return world.getChunkSource().spawnEnemies;
    }

    @Override
    public int getMinHeight() {
        return world.getMinY();
    }

    @Override
    public int getMaxHeight() {
        return world.getMaxY() + 1;
    }

    @Override
    public int getLogicalHeight() {
        return world.dimensionType().logicalHeight();
    }

    @Override
    public boolean isNatural() {
        return world.environmentAttributes().getDimensionValue(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS);
    }

    @Override
    public boolean isBedWorks() {
        return !world.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE).explodes();
    }

    @Override
    public boolean hasSkyLight() {
        return world.dimensionType().hasSkyLight();
    }

    @Override
    public boolean hasCeiling() {
        return world.dimensionType().hasCeiling();
    }

    @Override
    public boolean isPiglinSafe() {
        return !world.environmentAttributes().getDimensionValue(EnvironmentAttributes.PIGLINS_ZOMBIFY);
    }

    @Override
    public boolean isRespawnAnchorWorks() {
        return world.environmentAttributes().getDimensionValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS);
    }

    @Override
    public boolean hasRaids() {
        return world.environmentAttributes().getDimensionValue(EnvironmentAttributes.CAN_START_RAID);
    }

    @Override
    public boolean isUltraWarm() {
        return world.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES) || world.environmentAttributes().getDimensionValue(EnvironmentAttributes.FAST_LAVA);
    }

    @Override
    public int getSeaLevel() {
        return world.getSeaLevel();
    }

    @Override
    public boolean getKeepSpawnInMemory() {
        return false;
    }

    @Override
    public void setKeepSpawnInMemory(boolean keepLoaded) {
    }

    @Override
    public int hashCode() {
        return getUID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final CraftWorld other = (CraftWorld) obj;

        return this.getUID() == other.getUID();
    }

    @Override
    public File getWorldFolder() {
        return world.storageSource.getLevelPath(LevelResource.ROOT).toFile().getParentFile();
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(server.getMessenger(), source, channel, message);

        for (Player player : getPlayers()) {
            player.sendPluginMessage(source, channel, message);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        Set<String> result = new HashSet<String>();

        for (Player player : getPlayers()) {
            result.addAll(player.getListeningPluginChannels());
        }

        return result;
    }

    @Override
    public org.bukkit.WorldType getWorldType() {
        return world.isFlat() ? org.bukkit.WorldType.FLAT : org.bukkit.WorldType.NORMAL;
    }

    @Override
    public boolean canGenerateStructures() {
        return world.getWorldGenSettings().options().generateStructures();
    }

    @Override
    public boolean isHardcore() {
        return world.getLevelData().isHardcore();
    }

    @Override
    public void setHardcore(boolean hardcore) {
        world.serverLevelData.settings.difficultySettings().hardcore = hardcore;
    }

    @Override
    @Deprecated
    public long getTicksPerAnimalSpawns() {
        return getTicksPerSpawns(SpawnCategory.ANIMAL);
    }

    @Override
    @Deprecated
    public void setTicksPerAnimalSpawns(int ticksPerAnimalSpawns) {
        setTicksPerSpawns(SpawnCategory.ANIMAL, ticksPerAnimalSpawns);
    }

    @Override
    @Deprecated
    public long getTicksPerMonsterSpawns() {
        return getTicksPerSpawns(SpawnCategory.MONSTER);
    }

    @Override
    @Deprecated
    public void setTicksPerMonsterSpawns(int ticksPerMonsterSpawns) {
        setTicksPerSpawns(SpawnCategory.MONSTER, ticksPerMonsterSpawns);
    }

    @Override
    @Deprecated
    public long getTicksPerWaterSpawns() {
        return getTicksPerSpawns(SpawnCategory.WATER_ANIMAL);
    }

    @Override
    @Deprecated
    public void setTicksPerWaterSpawns(int ticksPerWaterSpawns) {
        setTicksPerSpawns(SpawnCategory.WATER_ANIMAL, ticksPerWaterSpawns);
    }

    @Override
    @Deprecated
    public long getTicksPerWaterAmbientSpawns() {
        return getTicksPerSpawns(SpawnCategory.WATER_AMBIENT);
    }

    @Override
    @Deprecated
    public void setTicksPerWaterAmbientSpawns(int ticksPerWaterAmbientSpawns) {
        setTicksPerSpawns(SpawnCategory.WATER_AMBIENT, ticksPerWaterAmbientSpawns);
    }

    @Override
    @Deprecated
    public long getTicksPerWaterUndergroundCreatureSpawns() {
        return getTicksPerSpawns(SpawnCategory.WATER_UNDERGROUND_CREATURE);
    }

    @Override
    @Deprecated
    public void setTicksPerWaterUndergroundCreatureSpawns(int ticksPerWaterUndergroundCreatureSpawns) {
        setTicksPerSpawns(SpawnCategory.WATER_UNDERGROUND_CREATURE, ticksPerWaterUndergroundCreatureSpawns);
    }

    @Override
    @Deprecated
    public long getTicksPerAmbientSpawns() {
        return getTicksPerSpawns(SpawnCategory.AMBIENT);
    }

    @Override
    @Deprecated
    public void setTicksPerAmbientSpawns(int ticksPerAmbientSpawns) {
        setTicksPerSpawns(SpawnCategory.AMBIENT, ticksPerAmbientSpawns);
    }

    @Override
    public void setTicksPerSpawns(SpawnCategory spawnCategory, int ticksPerCategorySpawn) {
        Preconditions.checkArgument(spawnCategory != null, "SpawnCategory cannot be null");
        Preconditions.checkArgument(CraftSpawnCategory.isValidForLimits(spawnCategory), "SpawnCategory.%s are not supported", spawnCategory);

        world.ticksPerSpawnCategory.put(spawnCategory, (long) ticksPerCategorySpawn);
    }

    @Override
    public long getTicksPerSpawns(SpawnCategory spawnCategory) {
        Preconditions.checkArgument(spawnCategory != null, "SpawnCategory cannot be null");
        Preconditions.checkArgument(CraftSpawnCategory.isValidForLimits(spawnCategory), "SpawnCategory.%s are not supported", spawnCategory);

        return world.ticksPerSpawnCategory.getLong(spawnCategory);
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        server.getWorldMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return server.getWorldMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return server.getWorldMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        server.getWorldMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    @Deprecated
    public int getMonsterSpawnLimit() {
        return getSpawnLimit(SpawnCategory.MONSTER);
    }

    @Override
    @Deprecated
    public void setMonsterSpawnLimit(int limit) {
        setSpawnLimit(SpawnCategory.MONSTER, limit);
    }

    @Override
    @Deprecated
    public int getAnimalSpawnLimit() {
        return getSpawnLimit(SpawnCategory.ANIMAL);
    }

    @Override
    @Deprecated
    public void setAnimalSpawnLimit(int limit) {
        setSpawnLimit(SpawnCategory.ANIMAL, limit);
    }

    @Override
    @Deprecated
    public int getWaterAnimalSpawnLimit() {
        return getSpawnLimit(SpawnCategory.WATER_ANIMAL);
    }

    @Override
    @Deprecated
    public void setWaterAnimalSpawnLimit(int limit) {
        setSpawnLimit(SpawnCategory.WATER_ANIMAL, limit);
    }

    @Override
    @Deprecated
    public int getWaterAmbientSpawnLimit() {
        return getSpawnLimit(SpawnCategory.WATER_AMBIENT);
    }

    @Override
    @Deprecated
    public void setWaterAmbientSpawnLimit(int limit) {
        setSpawnLimit(SpawnCategory.WATER_AMBIENT, limit);
    }

    @Override
    @Deprecated
    public int getWaterUndergroundCreatureSpawnLimit() {
        return getSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE);
    }

    @Override
    @Deprecated
    public void setWaterUndergroundCreatureSpawnLimit(int limit) {
        setSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE, limit);
    }

    @Override
    @Deprecated
    public int getAmbientSpawnLimit() {
        return getSpawnLimit(SpawnCategory.AMBIENT);
    }

    @Override
    @Deprecated
    public void setAmbientSpawnLimit(int limit) {
        setSpawnLimit(SpawnCategory.AMBIENT, limit);
    }

    @Override
    public int getSpawnLimit(SpawnCategory spawnCategory) {
        Preconditions.checkArgument(spawnCategory != null, "SpawnCategory cannot be null");
        Preconditions.checkArgument(CraftSpawnCategory.isValidForLimits(spawnCategory), "SpawnCategory.%s are not supported", spawnCategory);

        int limit = spawnCategoryLimit.getOrDefault(spawnCategory, -1);
        if (limit < 0) {
            limit = server.getSpawnLimit(spawnCategory);
        }
        return limit;
    }

    @Override
    public void setSpawnLimit(SpawnCategory spawnCategory, int limit) {
        Preconditions.checkArgument(spawnCategory != null, "SpawnCategory cannot be null");
        Preconditions.checkArgument(CraftSpawnCategory.isValidForLimits(spawnCategory), "SpawnCategory.%s are not supported", spawnCategory);

        spawnCategoryLimit.put(spawnCategory, limit);
    }

    @Override
    public void playNote(@NotNull Location loc, @NotNull Instrument instrument, @NotNull Note note) {
        playSound(loc, instrument.getSound(), org.bukkit.SoundCategory.RECORDS, 3f, note.getPitch());
    }

    @Override
    public void playSound(Location loc, Sound sound, float volume, float pitch) {
        playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location loc, String sound, float volume, float pitch) {
        playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location loc, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        playSound(loc, sound, category, volume, pitch, getHandle().getRandom().nextLong());;
    }

    @Override
    public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        playSound(loc, sound, category, volume, pitch, getHandle().getRandom().nextLong());
    }

    @Override
    public void playSound(Location loc, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
        if (loc == null || sound == null || category == null) return;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        getHandle().playSeededSound(null, x, y, z, CraftSound.bukkitToMinecraft(sound), SoundSource.valueOf(category.name()), volume, pitch, seed);
    }

    @Override
    public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
        if (loc == null || sound == null || category == null) return;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        ClientboundSoundPacket packet = new ClientboundSoundPacket(Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.parse(sound))), SoundSource.valueOf(category.name()), x, y, z, volume, pitch, seed);
        world.getServer().getPlayerList().broadcast(null, x, y, z, volume > 1.0F ? 16.0F * volume : 16.0D, this.world.dimension(), packet);
    }

    @Override
    public void playSound(Entity entity, Sound sound, float volume, float pitch) {
        playSound(entity, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Entity entity, String sound, float volume, float pitch) {
        playSound(entity, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Entity entity, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        playSound(entity, sound, category, volume, pitch, getHandle().getRandom().nextLong());
    }

    @Override
    public void playSound(Entity entity, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        playSound(entity, sound, category, volume, pitch, getHandle().getRandom().nextLong());
    }

    @Override
    public void playSound(Entity entity, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
        if (!(entity instanceof CraftEntity craftEntity) || entity.getWorld() != this || sound == null || category == null) return;

        ClientboundSoundEntityPacket packet = new ClientboundSoundEntityPacket(CraftSound.bukkitToMinecraftHolder(sound), SoundSource.valueOf(category.name()), craftEntity.getHandle(), volume, pitch, seed);
        ChunkMap.TrackedEntity entityTracker = getHandle().getChunkSource().chunkMap.entityMap.get(entity.getEntityId());
        if (entityTracker != null) {
            entityTracker.sendToTrackingPlayersAndSelf(packet);
        }
    }

    @Override
    public void playSound(Entity entity, String sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
        if (!(entity instanceof CraftEntity craftEntity) || entity.getWorld() != this || sound == null || category == null) return;

        ClientboundSoundEntityPacket packet = new ClientboundSoundEntityPacket(Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.parse(sound))), SoundSource.valueOf(category.name()), craftEntity.getHandle(), volume, pitch, seed);
        ChunkMap.TrackedEntity entityTracker = getHandle().getChunkSource().chunkMap.entityMap.get(entity.getEntityId());
        if (entityTracker != null) {
            entityTracker.sendToTrackingPlayersAndSelf(packet);
        }
    }

    private Map<String, net.minecraft.world.level.gamerules.GameRule<?>> gamerules;
    public synchronized Map<String, net.minecraft.world.level.gamerules.GameRule<?>> getGameRulesNMS() {
        if (gamerules != null) {
            return gamerules;
        }

        return this.gamerules = getGameRulesNMS(getHandle().getGameRules());
    }

    public static Map<String, net.minecraft.world.level.gamerules.GameRule<?>> getGameRulesNMS(GameRules gameRules) {
        Map<String, net.minecraft.world.level.gamerules.GameRule<?>> gamerules = new HashMap<>();
        gameRules.visitGameRuleTypes(new GameRuleTypeVisitor() {
            @Override
            public <T> void visit(net.minecraft.world.level.gamerules.GameRule<T> gamerule) {
                gamerules.put(gamerule.getIdentifier().toString(), gamerule);
            }
        });
        return gamerules;
    }

    @Override
    public String[] getGameRules() {
        return getGameRulesNMS().keySet().toArray(new String[getGameRulesNMS().size()]);
    }

    @Override
    public boolean isGameRule(String rule) {
        Preconditions.checkArgument(rule != null, "String rule cannot be null");
        Preconditions.checkArgument(!rule.isEmpty(), "String rule cannot be empty");
        GameRule<?> resolved = GameRule.getByName(rule);
        return resolved != null && getGameRulesNMS().containsKey(resolved.getKey().toString());
    }

    @Override
    public <T> T getGameRuleValue(GameRule<T> rule) {
        Preconditions.checkArgument(rule != null, "GameRule cannot be null");
        return (T) getHandle().getGameRules().get(CraftGameRule.bukkitToMinecraft(rule));
    }

    @Override
    public <T> T getGameRuleDefault(GameRule<T> rule) {
        Preconditions.checkArgument(rule != null, "GameRule cannot be null");
        return (T) CraftGameRule.bukkitToMinecraft(rule).defaultValue();
    }

    @Override
    public <T> boolean setGameRule(GameRule<T> rule, T newValue) {
        Preconditions.checkArgument(rule != null, "GameRule cannot be null");
        Preconditions.checkArgument(newValue != null, "GameRule value cannot be null");

        net.minecraft.world.level.gamerules.GameRule handle = CraftGameRule.bukkitToMinecraft(rule);
        getHandle().getGameRules().set(handle, newValue, getHandle());
        return true;
    }

    @Override
    public WorldBorder getWorldBorder() {
        if (this.worldBorder == null) {
            this.worldBorder = new CraftWorldBorder(this);
        }

        return this.worldBorder;
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count) {
        spawnParticle(particle, x, y, z, count, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
        spawnParticle(particle, x, y, z, count, 0, 0, 0, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 1, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, false);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data, force);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
        getHandle().sendParticlesSource(
                null, // Sender
                CraftParticle.createParticleParam(particle, data), // Particle
                force, // force
                false, // override limiter for render - TODO: Expose this?
                x, y, z, // Position
                count,  // Count
                offsetX, offsetY, offsetZ, // Random offset
                extra // Speed?
        );

    }

    @Deprecated
    @Override
    public Location locateNearestStructure(Location origin, org.bukkit.StructureType structureType, int radius, boolean findUnexplored) {
        StructureSearchResult result = null;

        // Manually map the mess of the old StructureType to the new StructureType and normal Structure
        if (org.bukkit.StructureType.MINESHAFT == structureType) {
            result = locateNearestStructure(origin, StructureType.MINESHAFT, radius, findUnexplored);
        } else if (org.bukkit.StructureType.VILLAGE == structureType) {
            result = locateNearestStructure(origin, List.of(Structure.VILLAGE_DESERT, Structure.VILLAGE_PLAINS, Structure.VILLAGE_SAVANNA, Structure.VILLAGE_SNOWY, Structure.VILLAGE_TAIGA), radius, findUnexplored);
        } else if (org.bukkit.StructureType.NETHER_FORTRESS == structureType) {
            result = locateNearestStructure(origin, StructureType.FORTRESS, radius, findUnexplored);
        } else if (org.bukkit.StructureType.STRONGHOLD == structureType) {
            result = locateNearestStructure(origin, StructureType.STRONGHOLD, radius, findUnexplored);
        } else if (org.bukkit.StructureType.JUNGLE_PYRAMID == structureType) {
            result = locateNearestStructure(origin, StructureType.JUNGLE_TEMPLE, radius, findUnexplored);
        } else if (org.bukkit.StructureType.OCEAN_RUIN == structureType) {
            result = locateNearestStructure(origin, StructureType.OCEAN_RUIN, radius, findUnexplored);
        } else if (org.bukkit.StructureType.DESERT_PYRAMID == structureType) {
            result = locateNearestStructure(origin, StructureType.DESERT_PYRAMID, radius, findUnexplored);
        } else if (org.bukkit.StructureType.IGLOO == structureType) {
            result = locateNearestStructure(origin, StructureType.IGLOO, radius, findUnexplored);
        } else if (org.bukkit.StructureType.SWAMP_HUT == structureType) {
            result = locateNearestStructure(origin, StructureType.SWAMP_HUT, radius, findUnexplored);
        } else if (org.bukkit.StructureType.OCEAN_MONUMENT == structureType) {
            result = locateNearestStructure(origin, StructureType.OCEAN_MONUMENT, radius, findUnexplored);
        } else if (org.bukkit.StructureType.END_CITY == structureType) {
            result = locateNearestStructure(origin, StructureType.END_CITY, radius, findUnexplored);
        } else if (org.bukkit.StructureType.WOODLAND_MANSION == structureType) {
            result = locateNearestStructure(origin, StructureType.WOODLAND_MANSION, radius, findUnexplored);
        } else if (org.bukkit.StructureType.BURIED_TREASURE == structureType) {
            result = locateNearestStructure(origin, StructureType.BURIED_TREASURE, radius, findUnexplored);
        } else if (org.bukkit.StructureType.SHIPWRECK == structureType) {
            result = locateNearestStructure(origin, StructureType.SHIPWRECK, radius, findUnexplored);
        } else if (org.bukkit.StructureType.PILLAGER_OUTPOST == structureType) {
            result = locateNearestStructure(origin, Structure.PILLAGER_OUTPOST, radius, findUnexplored);
        } else if (org.bukkit.StructureType.NETHER_FOSSIL == structureType) {
            result = locateNearestStructure(origin, StructureType.NETHER_FOSSIL, radius, findUnexplored);
        } else if (org.bukkit.StructureType.RUINED_PORTAL == structureType) {
            result = locateNearestStructure(origin, StructureType.RUINED_PORTAL, radius, findUnexplored);
        } else if (org.bukkit.StructureType.BASTION_REMNANT == structureType) {
            result = locateNearestStructure(origin, Structure.BASTION_REMNANT, radius, findUnexplored);
        }

        return (result == null) ? null : result.getLocation();
    }

    @Override
    public StructureSearchResult locateNearestStructure(Location origin, StructureType structureType, int radius, boolean findUnexplored) {
        List<Structure> structures = new ArrayList<>();
        for (Structure structure : Registry.STRUCTURE) {
            if (structure.getStructureType() == structureType) {
                structures.add(structure);
            }
        }

        return locateNearestStructure(origin, structures, radius, findUnexplored);
    }

    @Override
    public StructureSearchResult locateNearestStructure(Location origin, Structure structure, int radius, boolean findUnexplored) {
        return locateNearestStructure(origin, List.of(structure), radius, findUnexplored);
    }

    public StructureSearchResult locateNearestStructure(Location origin, List<Structure> structures, int radius, boolean findUnexplored) {
        BlockPos originPos = BlockPos.containing(origin.getX(), origin.getY(), origin.getZ());
        List<Holder<net.minecraft.world.level.levelgen.structure.Structure>> holders = new ArrayList<>();

        for (Structure structure : structures) {
            holders.add(Holder.direct(CraftStructure.bukkitToMinecraft(structure)));
        }

        Pair<BlockPos, Holder<net.minecraft.world.level.levelgen.structure.Structure>> found = getHandle().getChunkSource().getGenerator().findNearestMapStructure(getHandle(), HolderSet.direct(holders), originPos, radius, findUnexplored);
        if (found == null) {
            return null;
        }

        return new CraftStructureSearchResult(CraftStructure.minecraftToBukkit(found.getSecond().value()), CraftLocation.toBukkit(found.getFirst(), this));
    }

    @Override
    public BiomeSearchResult locateNearestBiome(Location origin, int radius, Biome... biomes) {
        return locateNearestBiome(origin, radius, 32, 64, biomes);
    }

    @Override
    public BiomeSearchResult locateNearestBiome(Location origin, int radius, int horizontalInterval, int verticalInterval, Biome... biomes) {
        BlockPos originPos = BlockPos.containing(origin.getX(), origin.getY(), origin.getZ());
        Set<Holder<net.minecraft.world.level.biome.Biome>> holders = new HashSet<>();

        for (Biome biome : biomes) {
            holders.add(CraftBiome.bukkitToMinecraftHolder(biome));
        }

        Climate.Sampler sampler = getHandle().getChunkSource().randomState().sampler();
        // The given predicate is evaluated once at the start of the search, so performance isn't a large concern.
        Pair<BlockPos, Holder<net.minecraft.world.level.biome.Biome>> found = getHandle().getChunkSource().getGenerator().getBiomeSource().findClosestBiome3d(originPos, radius, horizontalInterval, verticalInterval, holders::contains, sampler, getHandle());
        if (found == null) {
            return null;
        }

        return new CraftBiomeSearchResult(CraftBiome.minecraftHolderToBukkit(found.getSecond()), new Location(this, found.getFirst().getX(), found.getFirst().getY(), found.getFirst().getZ()));
    }

    @Override
    public Raid locateNearestRaid(Location location, int radius) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(radius >= 0, "Radius value (%s) cannot be negative", radius);

        Raids persistentRaid = world.getRaids();
        net.minecraft.world.entity.raid.Raid raid = persistentRaid.getNearbyRaid(CraftLocation.toBlockPosition(location), radius * radius);
        return (raid == null) ? null : new CraftRaid(raid, world);
    }

    @Override
    public List<Raid> getRaids() {
        Raids persistentRaid = world.getRaids();
        return persistentRaid.raidMap.values().stream().map((raid) -> new CraftRaid(raid, world)).collect(Collectors.toList());
    }

    @Override
    public DragonBattle getEnderDragonBattle() {
        return (getHandle().getDragonFight() == null) ? null : new CraftDragonBattle(getHandle().getDragonFight());
    }

    @Override
    public Collection<GeneratedStructure> getStructures(int x, int z) {
        return getStructures(x, z, struct -> true);
    }

    @Override
    public Collection<GeneratedStructure> getStructures(int x, int z, Structure structure) {
        Preconditions.checkArgument(structure != null, "Structure cannot be null");

        net.minecraft.core.Registry<net.minecraft.world.level.levelgen.structure.Structure> registry = CraftRegistry.getMinecraftRegistry(Registries.STRUCTURE);
        Identifier key = registry.getKey(CraftStructure.bukkitToMinecraft(structure));

        return getStructures(x, z, struct -> registry.getKey(struct).equals(key));
    }

    private List<GeneratedStructure> getStructures(int x, int z, Predicate<net.minecraft.world.level.levelgen.structure.Structure> predicate) {
        List<GeneratedStructure> structures = new ArrayList<>();
        for (StructureStart start : getHandle().structureManager().startsForStructure(new ChunkPos(x, z), predicate)) {
            structures.add(new CraftGeneratedStructure(start));
        }

        return structures;
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return persistentDataContainer;
    }

    @Override
    public Set<FeatureFlag> getFeatureFlags() {
        return CraftFeatureFlag.getFromNMS(this.getHandle().enabledFeatures()).stream().map(FeatureFlag.class::cast).collect(Collectors.toUnmodifiableSet());
    }

    public void storeBukkitValues(CompoundTag c) {
        if (!this.persistentDataContainer.isEmpty()) {
            c.put("BukkitValues", this.persistentDataContainer.toTagCompound());
        }
    }

    public void readBukkitValues(Tag c) {
        if (c instanceof CompoundTag) {
            this.persistentDataContainer.putAll((CompoundTag) c);
        }
    }
}
