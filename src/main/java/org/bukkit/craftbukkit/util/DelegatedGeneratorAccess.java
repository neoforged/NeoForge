package org.bukkit.craftbukkit.util;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

public abstract class DelegatedGeneratorAccess implements WorldGenLevel {

    private WorldGenLevel handle;

    public void setHandle(WorldGenLevel worldAccess) {
        this.handle = worldAccess;
    }

    public WorldGenLevel getHandle() {
        return handle;
    }

    @Override
    public long getSeed() {
        return handle.getSeed();
    }

    @Override
    public boolean ensureCanWrite(BlockPos blockpos) {
        return handle.ensureCanWrite(blockpos);
    }

    @Override
    public void setCurrentlyGenerating(Supplier<String> supplier) {
        handle.setCurrentlyGenerating(supplier);
    }

    @Override
    public ServerLevel getLevel() {
        return handle.getLevel();
    }

    @Override
    public void addFreshEntityWithPassengers(Entity entity) {
        handle.addFreshEntityWithPassengers(entity);
    }

    @Override
    public void addFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        handle.addFreshEntityWithPassengers(entity, reason);
    }

    @Override
    public ServerLevel getMinecraftWorld() {
        return handle.getMinecraftWorld();
    }

    @Override
    public long nextSubTickCount() {
        return handle.nextSubTickCount();
    }

    @Override
    public <T> ScheduledTick<T> createTick(BlockPos blockpos, T t0, int i, TickPriority tickpriority) {
        return handle.createTick(blockpos, t0, i, tickpriority);
    }

    @Override
    public <T> ScheduledTick<T> createTick(BlockPos blockpos, T t0, int i) {
        return handle.createTick(blockpos, t0, i);
    }

    @Override
    public LevelData getLevelData() {
        return handle.getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos blockpos) {
        return handle.getCurrentDifficultyAt(blockpos);
    }

    @Override
    public MinecraftServer getServer() {
        return handle.getServer();
    }

    @Override
    public Difficulty getDifficulty() {
        return handle.getDifficulty();
    }

    @Override
    public ChunkSource getChunkSource() {
        return handle.getChunkSource();
    }

    @Override
    public boolean hasChunk(int i, int j) {
        return handle.hasChunk(i, j);
    }

    @Override
    public RandomSource getRandom() {
        return handle.getRandom();
    }

    @Override
    public void updateNeighborsAt(BlockPos blockpos, Block block) {
        handle.updateNeighborsAt(blockpos, block);
    }

    @Override
    public void neighborShapeChanged(Direction direction, BlockPos blockpos, BlockPos blockpos1, BlockState blockstate, int i, int j) {
        handle.neighborShapeChanged(direction, blockpos, blockpos1, blockstate, i, j);
    }

    @Override
    public void playSound(Entity entity, BlockPos blockpos, SoundEvent soundevent, SoundSource soundsource) {
        handle.playSound(entity, blockpos, soundevent, soundsource);
    }

    @Override
    public void playSound(Entity entity, BlockPos blockpos, SoundEvent soundevent, SoundSource soundsource, float f, float f1) {
        handle.playSound(entity, blockpos, soundevent, soundsource, f, f1);
    }

    @Override
    public void addParticle(ParticleOptions particleoptions, double d0, double d1, double d2, double d3, double d4, double d5) {
        handle.addParticle(particleoptions, d0, d1, d2, d3, d4, d5);
    }

    @Override
    public void levelEvent(Entity entity, int i, BlockPos blockpos, int j) {
        handle.levelEvent(entity, i, blockpos, j);
    }

    @Override
    public void levelEvent(int i, BlockPos blockpos, int j) {
        handle.levelEvent(i, blockpos, j);
    }

    @Override
    public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context gameevent_a) {
        handle.gameEvent(holder, vec3, gameevent_a);
    }

    @Override
    public void gameEvent(Entity entity, Holder<GameEvent> holder, Vec3 vec3) {
        handle.gameEvent(entity, holder, vec3);
    }

    @Override
    public void gameEvent(Entity entity, Holder<GameEvent> holder, BlockPos blockpos) {
        handle.gameEvent(entity, holder, blockpos);
    }

    @Override
    public void gameEvent(Holder<GameEvent> holder, BlockPos blockpos, GameEvent.Context gameevent_a) {
        handle.gameEvent(holder, blockpos, gameevent_a);
    }

    @Override
    public void gameEvent(ResourceKey<GameEvent> resourcekey, BlockPos blockpos, GameEvent.Context gameevent_a) {
        handle.gameEvent(resourcekey, blockpos, gameevent_a);
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockpos, BlockEntityType<T> blockentitytype) {
        return handle.getBlockEntity(blockpos, blockentitytype);
    }

    @Override
    public List<VoxelShape> getEntityCollisions(Entity entity, AABB aabb) {
        return handle.getEntityCollisions(entity, aabb);
    }

    @Override
    public boolean isUnobstructed(Entity entity, VoxelShape voxelshape) {
        return handle.isUnobstructed(entity, voxelshape);
    }

    @Override
    public BlockPos getHeightmapPos(Heightmap.Types heightmap_type, BlockPos blockpos) {
        return handle.getHeightmapPos(heightmap_type, blockpos);
    }

    @Override
    public ChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag) {
        return handle.getChunk(i, j, chunkstatus, flag);
    }

    @Override
    public int getHeight(Heightmap.Types heightmap_type, int i, int j) {
        return handle.getHeight(heightmap_type, i, j);
    }

    @Override
    public int getSkyDarken() {
        return handle.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return handle.getBiomeManager();
    }

    @Override
    public Holder<Biome> getBiome(BlockPos blockpos) {
        return handle.getBiome(blockpos);
    }

    @Override
    public Stream<BlockState> getBlockStatesIfLoaded(AABB aabb) {
        return handle.getBlockStatesIfLoaded(aabb);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int i, int j, int k) {
        return handle.getNoiseBiome(i, j, k);
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k) {
        return handle.getUncachedNoiseBiome(i, j, k);
    }

    @Override
    public boolean isClientSide() {
        return handle.isClientSide();
    }

    @Override
    public int getSeaLevel() {
        return handle.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return handle.dimensionType();
    }

    @Override
    public int getMinY() {
        return handle.getMinY();
    }

    @Override
    public int getHeight() {
        return handle.getHeight();
    }

    @Override
    public boolean isEmptyBlock(BlockPos blockpos) {
        return handle.isEmptyBlock(blockpos);
    }

    @Override
    public boolean canSeeSkyFromBelowWater(BlockPos blockpos) {
        return handle.canSeeSkyFromBelowWater(blockpos);
    }

    @Override
    public float getPathfindingCostFromLightLevels(BlockPos blockpos) {
        return handle.getPathfindingCostFromLightLevels(blockpos);
    }

    @Override
    public float getLightLevelDependentMagicValue(BlockPos blockpos) {
        return handle.getLightLevelDependentMagicValue(blockpos);
    }

    @Override
    public ChunkAccess getChunk(BlockPos blockpos) {
        return handle.getChunk(blockpos);
    }

    @Override
    public ChunkAccess getChunk(int i, int j) {
        return handle.getChunk(i, j);
    }

    @Override
    public ChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus) {
        return handle.getChunk(i, j, chunkstatus);
    }

    @Override
    public BlockGetter getChunkForCollisions(int i, int j) {
        return handle.getChunkForCollisions(i, j);
    }

    @Override
    public boolean isWaterAt(BlockPos blockpos) {
        return handle.isWaterAt(blockpos);
    }

    @Override
    public boolean containsAnyLiquid(AABB aabb) {
        return handle.containsAnyLiquid(aabb);
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos blockpos) {
        return handle.getMaxLocalRawBrightness(blockpos);
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos blockpos, int i) {
        return handle.getMaxLocalRawBrightness(blockpos, i);
    }

    @Override
    public boolean hasChunkAt(int i, int j) {
        return handle.hasChunkAt(i, j);
    }

    @Override
    public boolean hasChunkAt(BlockPos blockpos) {
        return handle.hasChunkAt(blockpos);
    }

    @Override
    public boolean hasChunksAt(BlockPos blockpos, BlockPos blockpos1) {
        return handle.hasChunksAt(blockpos, blockpos1);
    }

    @Override
    public boolean hasChunksAt(int i, int j, int k, int l, int i1, int j1) {
        return handle.hasChunksAt(i, j, k, l, i1, j1);
    }

    @Override
    public boolean hasChunksAt(int i, int j, int k, int l) {
        return handle.hasChunksAt(i, j, k, l);
    }

    @Override
    public RegistryAccess registryAccess() {
        return handle.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return handle.enabledFeatures();
    }

    @Override
    public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<? extends T>> resourcekey) {
        return handle.holderLookup(resourcekey);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return handle.getLightEngine();
    }

    @Override
    public int getBrightness(LightLayer lightlayer, BlockPos blockpos) {
        return handle.getBrightness(lightlayer, blockpos);
    }

    @Override
    public int getRawBrightness(BlockPos blockpos, int i) {
        return handle.getRawBrightness(blockpos, i);
    }

    @Override
    public boolean canSeeSky(BlockPos blockpos) {
        return handle.canSeeSky(blockpos);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return handle.getWorldBorder();
    }

    @Override
    public boolean isUnobstructed(BlockState blockstate, BlockPos blockpos, CollisionContext collisioncontext) {
        return handle.isUnobstructed(blockstate, blockpos, collisioncontext);
    }

    @Override
    public boolean isUnobstructed(Entity entity) {
        return handle.isUnobstructed(entity);
    }

    @Override
    public boolean noCollision(AABB aabb) {
        return handle.noCollision(aabb);
    }

    @Override
    public boolean noCollision(Entity entity) {
        return handle.noCollision(entity);
    }

    @Override
    public boolean noCollision(Entity entity, AABB aabb) {
        return handle.noCollision(entity, aabb);
    }

    @Override
    public boolean noCollision(Entity entity, AABB aabb, boolean flag) {
        return handle.noCollision(entity, aabb, flag);
    }

    @Override
    public boolean noBlockCollision(Entity entity, AABB aabb) {
        return handle.noBlockCollision(entity, aabb);
    }

    @Override
    public Iterable<VoxelShape> getCollisions(Entity entity, AABB aabb) {
        return handle.getCollisions(entity, aabb);
    }

    @Override
    public Iterable<VoxelShape> getBlockCollisions(Entity entity, AABB aabb) {
        return handle.getBlockCollisions(entity, aabb);
    }

    @Override
    public Iterable<VoxelShape> getBlockAndLiquidCollisions(Entity entity, AABB aabb) {
        return handle.getBlockAndLiquidCollisions(entity, aabb);
    }

    @Override
    public BlockHitResult clipIncludingBorder(ClipContext clipcontext) {
        return handle.clipIncludingBorder(clipcontext);
    }

    @Override
    public boolean collidesWithSuffocatingBlock(Entity entity, AABB aabb) {
        return handle.collidesWithSuffocatingBlock(entity, aabb);
    }

    @Override
    public Optional<BlockPos> findSupportingBlock(Entity entity, AABB aabb) {
        return handle.findSupportingBlock(entity, aabb);
    }

    @Override
    public Optional<Vec3> findFreePosition(Entity entity, VoxelShape voxelshape, Vec3 vec3, double d0, double d1, double d2) {
        return handle.findFreePosition(entity, voxelshape, vec3, d0, d1, d2);
    }

    @Override
    public int getDirectSignal(BlockPos blockpos, Direction direction) {
        return handle.getDirectSignal(blockpos, direction);
    }

    @Override
    public int getDirectSignalTo(BlockPos blockpos) {
        return handle.getDirectSignalTo(blockpos);
    }

    @Override
    public int getControlInputSignal(BlockPos blockpos, Direction direction, boolean flag) {
        return handle.getControlInputSignal(blockpos, direction, flag);
    }

    @Override
    public boolean hasSignal(BlockPos blockpos, Direction direction) {
        return handle.hasSignal(blockpos, direction);
    }

    @Override
    public int getSignal(BlockPos blockpos, Direction direction) {
        return handle.getSignal(blockpos, direction);
    }

    @Override
    public boolean hasNeighborSignal(BlockPos blockpos) {
        return handle.hasNeighborSignal(blockpos);
    }

    @Override
    public int getBestNeighborSignal(BlockPos blockpos) {
        return handle.getBestNeighborSignal(blockpos);
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos blockpos) {
        return handle.getBlockEntity(blockpos);
    }

    @Override
    public BlockState getBlockState(BlockPos blockpos) {
        return handle.getBlockState(blockpos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockpos) {
        return handle.getFluidState(blockpos);
    }

    @Override
    public int getLightEmission(BlockPos blockpos) {
        return handle.getLightEmission(blockpos);
    }

    @Override
    public Stream<BlockState> getBlockStates(AABB aabb) {
        return handle.getBlockStates(aabb);
    }

    @Override
    public BlockHitResult isBlockInLine(ClipBlockStateContext clipblockstatecontext) {
        return handle.isBlockInLine(clipblockstatecontext);
    }

    @Override
    public BlockHitResult clip(ClipContext clipcontext1, BlockPos blockpos) {
        return handle.clip(clipcontext1, blockpos);
    }

    @Override
    public BlockHitResult clip(ClipContext clipcontext) {
        return handle.clip(clipcontext);
    }

    @Override
    public BlockHitResult clipWithInteractionOverride(Vec3 vec3, Vec3 vec31, BlockPos blockpos, VoxelShape voxelshape, BlockState blockstate) {
        return handle.clipWithInteractionOverride(vec3, vec31, blockpos, voxelshape, blockstate);
    }

    @Override
    public double getBlockFloorHeight(VoxelShape voxelshape, Supplier<VoxelShape> supplier) {
        return handle.getBlockFloorHeight(voxelshape, supplier);
    }

    @Override
    public double getBlockFloorHeight(BlockPos blockpos) {
        return handle.getBlockFloorHeight(blockpos);
    }

    @Override
    public List<Entity> getEntities(Entity entity, AABB aabb, Predicate<? super Entity> predicate) {
        return handle.getEntities(entity, aabb, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entitytypetest, AABB aabb, Predicate<? super T> predicate) {
        return handle.getEntities(entitytypetest, aabb, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<T> oclass, AABB aabb, Predicate<? super T> predicate) {
        return handle.getEntitiesOfClass(oclass, aabb, predicate);
    }

    @Override
    public List<? extends Player> players() {
        return handle.players();
    }

    @Override
    public List<Entity> getEntities(Entity entity, AABB aabb) {
        return handle.getEntities(entity, aabb);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<T> oclass, AABB aabb) {
        return handle.getEntitiesOfClass(oclass, aabb);
    }

    @Override
    public Player getNearestPlayer(double d0, double d1, double d2, double d3, Predicate<Entity> predicate) {
        return handle.getNearestPlayer(d0, d1, d2, d3, predicate);
    }

    @Override
    public Player getNearestPlayer(Entity entity, double d0) {
        return handle.getNearestPlayer(entity, d0);
    }

    @Override
    public Player getNearestPlayer(double d0, double d1, double d2, double d3, boolean flag) {
        return handle.getNearestPlayer(d0, d1, d2, d3, flag);
    }

    @Override
    public boolean hasNearbyAlivePlayer(double d0, double d1, double d2, double d3) {
        return handle.hasNearbyAlivePlayer(d0, d1, d2, d3);
    }

    @Override
    public Player getPlayerByUUID(UUID uuid) {
        return handle.getPlayerByUUID(uuid);
    }

    @Override
    public boolean setBlock(BlockPos blockpos, BlockState blockstate, int i, int j) {
        return handle.setBlock(blockpos, blockstate, i, j);
    }

    @Override
    public boolean setBlock(BlockPos blockpos, BlockState blockstate, int i) {
        return handle.setBlock(blockpos, blockstate, i);
    }

    @Override
    public boolean removeBlock(BlockPos blockpos, boolean flag) {
        return handle.removeBlock(blockpos, flag);
    }

    @Override
    public boolean destroyBlock(BlockPos blockpos, boolean flag) {
        return handle.destroyBlock(blockpos, flag);
    }

    @Override
    public boolean destroyBlock(BlockPos blockpos, boolean flag, Entity entity) {
        return handle.destroyBlock(blockpos, flag, entity);
    }

    @Override
    public boolean destroyBlock(BlockPos blockpos, boolean flag, Entity entity, int i) {
        return handle.destroyBlock(blockpos, flag, entity, i);
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        return handle.addFreshEntity(entity);
    }

    @Override
    public boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return handle.addFreshEntity(entity, reason);
    }

    @Override
    public int getMaxY() {
        return handle.getMaxY();
    }

    @Override
    public int getSectionsCount() {
        return handle.getSectionsCount();
    }

    @Override
    public int getMinSectionY() {
        return handle.getMinSectionY();
    }

    @Override
    public int getMaxSectionY() {
        return handle.getMaxSectionY();
    }

    @Override
    public boolean isInsideBuildHeight(int i) {
        return handle.isInsideBuildHeight(i);
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos blockpos) {
        return handle.isOutsideBuildHeight(blockpos);
    }

    @Override
    public boolean isOutsideBuildHeight(int i) {
        return handle.isOutsideBuildHeight(i);
    }

    @Override
    public int getSectionIndex(int i) {
        return handle.getSectionIndex(i);
    }

    @Override
    public int getSectionIndexFromSectionY(int i) {
        return handle.getSectionIndexFromSectionY(i);
    }

    @Override
    public int getSectionYFromSectionIndex(int i) {
        return handle.getSectionYFromSectionIndex(i);
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return handle.getBlockTicks();
    }

    @Override
    public void scheduleTick(BlockPos blockpos, Block block, int i, TickPriority tickpriority) {
        handle.scheduleTick(blockpos, block, i, tickpriority);
    }

    @Override
    public void scheduleTick(BlockPos blockpos, Block block, int i) {
        handle.scheduleTick(blockpos, block, i);
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return handle.getFluidTicks();
    }

    @Override
    public void scheduleTick(BlockPos blockpos, Fluid fluid, int i, TickPriority tickpriority) {
        handle.scheduleTick(blockpos, fluid, i, tickpriority);
    }

    @Override
    public void scheduleTick(BlockPos blockpos, Fluid fluid, int i) {
        handle.scheduleTick(blockpos, fluid, i);
    }

    @Override
    public boolean isStateAtPosition(BlockPos blockpos, Predicate<BlockState> predicate) {
        return handle.isStateAtPosition(blockpos, predicate);
    }

    @Override
    public boolean isFluidAtPosition(BlockPos blockpos, Predicate<FluidState> predicate) {
        return handle.isFluidAtPosition(blockpos, predicate);
    }

    @Override
    public EnvironmentAttributeReader environmentAttributes() {
        return handle.environmentAttributes();
    }
}
