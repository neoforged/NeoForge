/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event.Result;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.MutableHashedLinkedMap;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.brewing.PotionBrewEvent;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingPackSizeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent.AllowDespawn;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent.SpawnPlacementCheck;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.living.ZombieEvent.SummonAidEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent.AdvancementEarnEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent.AdvancementProgressEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent.AdvancementProgressEvent.ProgressType;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ArrowNockEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.EntityItemPickupEvent;
import net.neoforged.neoforge.event.entity.player.FillBucketEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PermissionsChangedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerFlyableFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSleepInBedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.entity.player.SleepingLocationCheckEvent;
import net.neoforged.neoforge.event.entity.player.SleepingTimeCheckEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.AlterGroundEvent;
import net.neoforged.neoforge.event.level.AlterGroundEvent.StateProvider;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BlockToolModificationEvent;
import net.neoforged.neoforge.event.level.BlockEvent.CreateFluidSourceEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityMultiPlaceEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.neoforged.neoforge.event.level.ChunkTicketLevelUpdatedEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.level.SaplingGrowTreeEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class EventHooks {
    public static boolean onMultiBlockPlace(@Nullable Entity entity, List<BlockSnapshot> blockSnapshots, Direction direction) {
        BlockSnapshot snap = blockSnapshots.get(0);
        BlockState placedAgainst = snap.getLevel().getBlockState(snap.getPos().relative(direction.getOpposite()));
        EntityMultiPlaceEvent event = new EntityMultiPlaceEvent(blockSnapshots, placedAgainst, entity);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onBlockPlace(@Nullable Entity entity, BlockSnapshot blockSnapshot, Direction direction) {
        BlockState placedAgainst = blockSnapshot.getLevel().getBlockState(blockSnapshot.getPos().relative(direction.getOpposite()));
        EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(blockSnapshot, placedAgainst, entity);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static NeighborNotifyEvent onNeighborNotify(Level level, BlockPos pos, BlockState state, EnumSet<Direction> notifiedSides, boolean forceRedstoneUpdate) {
        NeighborNotifyEvent event = new NeighborNotifyEvent(level, pos, state, notifiedSides, forceRedstoneUpdate);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static boolean doPlayerHarvestCheck(Player player, BlockState state, boolean success) {
        PlayerEvent.HarvestCheck event = new PlayerEvent.HarvestCheck(player, state, success);
        NeoForge.EVENT_BUS.post(event);
        return event.canHarvest();
    }

    public static float getBreakSpeed(Player player, BlockState state, float original, BlockPos pos) {
        PlayerEvent.BreakSpeed event = new PlayerEvent.BreakSpeed(player, state, original, pos);
        return (NeoForge.EVENT_BUS.post(event).isCanceled() ? -1 : event.getNewSpeed());
    }

    public static void onPlayerDestroyItem(Player player, ItemStack stack, @Nullable InteractionHand hand) {
        NeoForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, stack, hand));
    }

    /**
     * Internal, should only be called via {@link SpawnPlacements#checkSpawnRules}.
     * 
     * @see SpawnPlacementCheck
     */
    @ApiStatus.Internal
    public static boolean checkSpawnPlacements(EntityType<?> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, boolean defaultResult) {
        var event = new SpawnPlacementCheck(entityType, level, spawnType, pos, random, defaultResult);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult() == Result.DEFAULT ? defaultResult : event.getResult() == Result.ALLOW;
    }

    /**
     * Checks if the current position of the passed mob is valid for spawning, by firing {@link PositionCheck}.<br>
     * The default check is to perform the logical and of {@link Mob#checkSpawnRules} and {@link Mob#checkSpawnObstruction}.<br>
     * 
     * @param mob       The mob being spawned.
     * @param level     The level the mob will be added to, if successful.
     * @param spawnType The spawn type of the spawn.
     * @return True, if the position is valid, as determined by the contract of {@link PositionCheck}.
     * @see PositionCheck
     */
    public static boolean checkSpawnPosition(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType) {
        var event = new PositionCheck(mob, level, spawnType, null);
        NeoForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DEFAULT) {
            return mob.checkSpawnRules(level, spawnType) && mob.checkSpawnObstruction(level);
        }
        return event.getResult() == Result.ALLOW;
    }

    /**
     * Specialized variant of {@link #checkSpawnPosition} for spawners, as they have slightly different checks.
     * 
     * @see #CheckSpawnPosition
     * @implNote See in-line comments about custom spawn rules.
     */
    public static boolean checkSpawnPositionSpawner(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, SpawnData spawnData, BaseSpawner spawner) {
        var event = new PositionCheck(mob, level, spawnType, null);
        NeoForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DEFAULT) {
            // Spawners do not evaluate Mob#checkSpawnRules if any custom rules are present. This is despite the fact that these two methods do not check the same things.
            return (spawnData.getCustomSpawnRules().isPresent() || mob.checkSpawnRules(level, spawnType)) && mob.checkSpawnObstruction(level);
        }
        return event.getResult() == Result.ALLOW;
    }

    /**
     * Vanilla calls to {@link Mob#finalizeSpawn} are replaced with calls to this method via coremod.<br>
     * Mods should call this method in place of calling {@link Mob#finalizeSpawn}. Super calls (from within overrides) should not be wrapped.
     * <p>
     * When interfacing with this event, write all code as normal, and replace the call to {@link Mob#finalizeSpawn} with a call to this method.<p>
     * As an example, the following code block:
     * <code>
     * 
     * <pre>
     * var zombie = new Zombie(level);
     * zombie.finalizeSpawn(level, difficulty, spawnType, spawnData, spawnTag);
     * level.tryAddFreshEntityWithPassengers(zombie);
     * if (zombie.isAddedToWorld()) {
     *     // Do stuff with your new zombie
     * }
     * </pre>
     * 
     * </code>
     * Would become:
     * <code>
     * 
     * <pre>
     * var zombie = new Zombie(level);
     * EventHook.onFinalizeSpawn(zombie, level, difficulty, spawnType, spawnData, spawnTag);
     * level.tryAddFreshEntityWithPassengers(zombie);
     * if (zombie.isAddedToWorld()) {
     *     // Do stuff with your new zombie
     * }
     * </pre>
     * 
     * </code>
     * The only code that changes is the {@link Mob#finalizeSpawn} call.
     * 
     * @return The SpawnGroupData from this event, or null if it was canceled. The return value of this method has no bearing on if the entity will be spawned.
     * @see MobSpawnEvent.FinalizeSpawn
     * @see Mob#finalizeSpawn(ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData, CompoundTag)
     * @apiNote Callers do not need to check if the entity's spawn was cancelled, as the spawn will be blocked by Forge.
     * @implNote Changes to the signature of this method must be reflected in the method redirector coremod.
     */
    @Nullable
    @SuppressWarnings("deprecation") // Call to deprecated Mob#finalizeSpawn is expected.
    public static SpawnGroupData onFinalizeSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag spawnTag) {
        var event = new MobSpawnEvent.FinalizeSpawn(mob, level, mob.getX(), mob.getY(), mob.getZ(), difficulty, spawnType, spawnData, spawnTag, null);
        boolean cancel = NeoForge.EVENT_BUS.post(event).isCanceled();

        if (!cancel) {
            mob.finalizeSpawn(level, event.getDifficulty(), event.getSpawnType(), event.getSpawnData(), event.getSpawnTag());
        }

        return cancel ? null : event.getSpawnData();
    }

    /**
     * Returns the FinalizeSpawn event instance, or null if it was canceled.<br>
     * This is separate since mob spawners perform special finalizeSpawn handling when NBT data is present, but we still want to fire the event.<br>
     * This overload is also the only way to pass through a {@link BaseSpawner} instance.
     * 
     * @see #onFinalizeSpawn
     */
    @Nullable
    public static MobSpawnEvent.FinalizeSpawn onFinalizeSpawnSpawner(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag spawnTag, BaseSpawner spawner) {
        var event = new MobSpawnEvent.FinalizeSpawn(mob, level, mob.getX(), mob.getY(), mob.getZ(), difficulty, MobSpawnType.SPAWNER, spawnData, spawnTag, spawner);
        boolean cancel = NeoForge.EVENT_BUS.post(event).isCanceled();
        return cancel ? null : event;
    }

    public static PlayerSpawnPhantomsEvent onPhantomSpawn(ServerPlayer player, int phantomsToSpawn) {
        var event = new PlayerSpawnPhantomsEvent(player, phantomsToSpawn);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static Result canEntityDespawn(Mob entity, ServerLevelAccessor level) {
        AllowDespawn event = new AllowDespawn(entity, level);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    public static int getItemBurnTime(ItemStack itemStack, int burnTime, @Nullable RecipeType<?> recipeType) {
        FurnaceFuelBurnTimeEvent event = new FurnaceFuelBurnTimeEvent(itemStack, burnTime, recipeType);
        NeoForge.EVENT_BUS.post(event);
        return event.getBurnTime();
    }

    public static int getExperienceDrop(LivingEntity entity, Player attackingPlayer, int originalExperience) {
        LivingExperienceDropEvent event = new LivingExperienceDropEvent(entity, attackingPlayer, originalExperience);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return 0;
        }
        return event.getDroppedExperience();
    }

    public static int getMaxSpawnPackSize(Mob entity) {
        LivingPackSizeEvent maxCanSpawnEvent = new LivingPackSizeEvent(entity);
        NeoForge.EVENT_BUS.post(maxCanSpawnEvent);
        return maxCanSpawnEvent.getResult() == Result.ALLOW ? maxCanSpawnEvent.getMaxPackSize() : entity.getMaxSpawnClusterSize();
    }

    public static Component getPlayerDisplayName(Player player, Component username) {
        PlayerEvent.NameFormat event = new PlayerEvent.NameFormat(player, username);
        NeoForge.EVENT_BUS.post(event);
        return event.getDisplayname();
    }

    public static Component getPlayerTabListDisplayName(Player player) {
        PlayerEvent.TabListNameFormat event = new PlayerEvent.TabListNameFormat(player);
        NeoForge.EVENT_BUS.post(event);
        return event.getDisplayName();
    }

    public static BlockState fireFluidPlaceBlockEvent(LevelAccessor level, BlockPos pos, BlockPos liquidPos, BlockState state) {
        BlockEvent.FluidPlaceBlockEvent event = new BlockEvent.FluidPlaceBlockEvent(level, pos, liquidPos, state);
        NeoForge.EVENT_BUS.post(event);
        return event.getNewState();
    }

    public static ItemTooltipEvent onItemTooltip(ItemStack itemStack, @Nullable Player entityPlayer, List<Component> list, TooltipFlag flags) {
        ItemTooltipEvent event = new ItemTooltipEvent(itemStack, entityPlayer, list, flags);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static SummonAidEvent fireZombieSummonAid(Zombie zombie, Level level, int x, int y, int z, LivingEntity attacker, double summonChance) {
        SummonAidEvent summonEvent = new SummonAidEvent(zombie, level, x, y, z, attacker, summonChance);
        NeoForge.EVENT_BUS.post(summonEvent);
        return summonEvent;
    }

    public static boolean onEntityStruckByLightning(Entity entity, LightningBolt bolt) {
        return NeoForge.EVENT_BUS.post(new EntityStruckByLightningEvent(entity, bolt)).isCanceled();
    }

    public static int onItemUseStart(LivingEntity entity, ItemStack item, int duration) {
        var event = new LivingEntityUseItemEvent.Start(entity, item, duration);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? -1 : event.getDuration();
    }

    public static int onItemUseTick(LivingEntity entity, ItemStack item, int duration) {
        var event = new LivingEntityUseItemEvent.Tick(entity, item, duration);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? -1 : event.getDuration();
    }

    public static boolean onUseItemStop(LivingEntity entity, ItemStack item, int duration) {
        return NeoForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Stop(entity, item, duration)).isCanceled();
    }

    public static ItemStack onItemUseFinish(LivingEntity entity, ItemStack item, int duration, ItemStack result) {
        LivingEntityUseItemEvent.Finish event = new LivingEntityUseItemEvent.Finish(entity, item, duration, result);
        NeoForge.EVENT_BUS.post(event);
        return event.getResultStack();
    }

    public static void onStartEntityTracking(Entity entity, Player player) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.StartTracking(player, entity));
    }

    public static void onStopEntityTracking(Entity entity, Player player) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.StopTracking(player, entity));
    }

    public static void firePlayerLoadingEvent(Player player, File playerDirectory, String uuidString) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.LoadFromFile(player, playerDirectory, uuidString));
    }

    public static void firePlayerSavingEvent(Player player, File playerDirectory, String uuidString) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.SaveToFile(player, playerDirectory, uuidString));
    }

    public static void firePlayerLoadingEvent(Player player, PlayerDataStorage playerFileData, String uuidString) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.LoadFromFile(player, playerFileData.getPlayerDataFolder(), uuidString));
    }

    @Nullable
    public static BlockState onToolUse(BlockState originalState, UseOnContext context, ToolAction toolAction, boolean simulate) {
        BlockToolModificationEvent event = new BlockToolModificationEvent(originalState, context, toolAction, simulate);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? null : event.getFinalState();
    }

    public static int onApplyBonemeal(Player player, Level level, BlockPos pos, BlockState state, ItemStack stack) {
        BonemealEvent event = new BonemealEvent(player, level, pos, state, stack);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return -1;
        if (event.getResult() == Result.ALLOW) {
            if (!level.isClientSide)
                stack.shrink(1);
            return 1;
        }
        return 0;
    }

    @Nullable
    public static InteractionResultHolder<ItemStack> onBucketUse(Player player, Level level, ItemStack stack, @Nullable HitResult target) {
        FillBucketEvent event = new FillBucketEvent(player, stack, level, target);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, stack);

        if (event.getResult() == Result.ALLOW) {
            if (player.getAbilities().instabuild)
                return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);

            stack.shrink(1);
            if (stack.isEmpty())
                return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, event.getFilledBucket());

            if (!player.getInventory().add(event.getFilledBucket()))
                player.drop(event.getFilledBucket(), false);

            return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
        }
        return null;
    }

    public static PlayLevelSoundEvent.AtEntity onPlaySoundAtEntity(Entity entity, Holder<SoundEvent> name, SoundSource category, float volume, float pitch) {
        PlayLevelSoundEvent.AtEntity event = new PlayLevelSoundEvent.AtEntity(entity, name, category, volume, pitch);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static PlayLevelSoundEvent.AtPosition onPlaySoundAtPosition(Level level, double x, double y, double z, Holder<SoundEvent> name, SoundSource category, float volume, float pitch) {
        PlayLevelSoundEvent.AtPosition event = new PlayLevelSoundEvent.AtPosition(level, new Vec3(x, y, z), name, category, volume, pitch);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static int onItemExpire(ItemEntity entity, ItemStack item) {
        if (item.isEmpty()) return -1;
        ItemExpireEvent event = new ItemExpireEvent(entity, (item.isEmpty() ? 6000 : item.getItem().getEntityLifespan(item, entity.level())));
        if (!NeoForge.EVENT_BUS.post(event).isCanceled()) return -1;
        return event.getExtraLife();
    }

    public static int onItemPickup(ItemEntity entityItem, Player player) {
        var event = new EntityItemPickupEvent(player, entityItem);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return -1;
        return event.getResult() == Result.ALLOW ? 1 : 0;
    }

    public static boolean canMountEntity(Entity entityMounting, Entity entityBeingMounted, boolean isMounting) {
        boolean isCanceled = NeoForge.EVENT_BUS.post(new EntityMountEvent(entityMounting, entityBeingMounted, entityMounting.level(), isMounting)).isCanceled();

        if (isCanceled) {
            entityMounting.absMoveTo(entityMounting.getX(), entityMounting.getY(), entityMounting.getZ(), entityMounting.yRotO, entityMounting.xRotO);
            return false;
        } else
            return true;
    }

    public static boolean onAnimalTame(Animal animal, Player tamer) {
        return NeoForge.EVENT_BUS.post(new AnimalTameEvent(animal, tamer)).isCanceled();
    }

    public static Player.BedSleepingProblem onPlayerSleepInBed(Player player, Optional<BlockPos> pos) {
        PlayerSleepInBedEvent event = new PlayerSleepInBedEvent(player, pos);
        NeoForge.EVENT_BUS.post(event);
        return event.getResultStatus();
    }

    public static void onPlayerWakeup(Player player, boolean wakeImmediately, boolean updateLevel) {
        NeoForge.EVENT_BUS.post(new PlayerWakeUpEvent(player, wakeImmediately, updateLevel));
    }

    public static void onPlayerFall(Player player, float distance, float multiplier) {
        NeoForge.EVENT_BUS.post(new PlayerFlyableFallEvent(player, distance, multiplier));
    }

    public static boolean onPlayerSpawnSet(Player player, ResourceKey<Level> levelKey, BlockPos pos, boolean forced) {
        return NeoForge.EVENT_BUS.post(new PlayerSetSpawnEvent(player, levelKey, pos, forced)).isCanceled();
    }

    public static void onPlayerClone(Player player, Player oldPlayer, boolean wasDeath) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(player, oldPlayer, wasDeath));
    }

    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return NeoForge.EVENT_BUS.post(new ExplosionEvent.Start(level, explosion)).isCanceled();
    }

    public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> list, double diameter) {
        //Filter entities to only those who are effected, to prevent modders from seeing more then will be hurt.
        /* Enable this if we get issues with modders looping to much.
        Iterator<Entity> itr = list.iterator();
        Vec3 p = explosion.getPosition();
        while (itr.hasNext())
        {
            Entity e = itr.next();
            double dist = e.getDistance(p.xCoord, p.yCoord, p.zCoord) / diameter;
            if (e.isImmuneToExplosions() || dist > 1.0F) itr.remove();
        }
        */
        NeoForge.EVENT_BUS.post(new ExplosionEvent.Detonate(level, explosion, list));
    }

    public static boolean onCreateWorldSpawn(Level level, ServerLevelData settings) {
        return NeoForge.EVENT_BUS.post(new LevelEvent.CreateSpawnPosition(level, settings)).isCanceled();
    }

    public static float onLivingHeal(LivingEntity entity, float amount) {
        LivingHealEvent event = new LivingHealEvent(entity, amount);
        return (NeoForge.EVENT_BUS.post(event).isCanceled() ? 0 : event.getAmount());
    }

    public static boolean onPotionAttemptBrew(NonNullList<ItemStack> stacks) {
        NonNullList<ItemStack> tmp = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
        for (int x = 0; x < tmp.size(); x++)
            tmp.set(x, stacks.get(x).copy());

        PotionBrewEvent.Pre event = new PotionBrewEvent.Pre(tmp);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            boolean changed = false;
            for (int x = 0; x < stacks.size(); x++) {
                changed |= ItemStack.matches(tmp.get(x), stacks.get(x));
                stacks.set(x, event.getItem(x));
            }
            if (changed)
                onPotionBrewed(stacks);
            return true;
        }
        return false;
    }

    public static void onPotionBrewed(NonNullList<ItemStack> brewingItemStacks) {
        NeoForge.EVENT_BUS.post(new PotionBrewEvent.Post(brewingItemStacks));
    }

    public static void onPlayerBrewedPotion(Player player, ItemStack stack) {
        NeoForge.EVENT_BUS.post(new PlayerBrewedPotionEvent(player, stack));
    }

    public static boolean fireSleepingLocationCheck(LivingEntity player, BlockPos sleepingLocation) {
        SleepingLocationCheckEvent evt = new SleepingLocationCheckEvent(player, sleepingLocation);
        NeoForge.EVENT_BUS.post(evt);

        Result canContinueSleep = evt.getResult();
        if (canContinueSleep == Result.DEFAULT) {
            return player.getSleepingPos().map(pos -> {
                BlockState state = player.level().getBlockState(pos);
                return state.getBlock().isBed(state, player.level(), pos, player);
            }).orElse(false);
        } else
            return canContinueSleep == Result.ALLOW;
    }

    public static boolean fireSleepingTimeCheck(Player player, Optional<BlockPos> sleepingLocation) {
        SleepingTimeCheckEvent evt = new SleepingTimeCheckEvent(player, sleepingLocation);
        NeoForge.EVENT_BUS.post(evt);

        Result canContinueSleep = evt.getResult();
        if (canContinueSleep == Result.DEFAULT)
            return !player.level().isDay();
        else
            return canContinueSleep == Result.ALLOW;
    }

    public static InteractionResultHolder<ItemStack> onArrowNock(ItemStack item, Level level, Player player, InteractionHand hand, boolean hasAmmo) {
        ArrowNockEvent event = new ArrowNockEvent(player, item, hand, level, hasAmmo);
        if (NeoForge.EVENT_BUS.post(event).isCanceled())
            return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, item);
        return event.getAction();
    }

    public static int onArrowLoose(ItemStack stack, Level level, Player player, int charge, boolean hasAmmo) {
        ArrowLooseEvent event = new ArrowLooseEvent(player, stack, level, charge, hasAmmo);
        if (NeoForge.EVENT_BUS.post(event).isCanceled())
            return -1;
        return event.getCharge();
    }

    public static boolean onProjectileImpact(Projectile projectile, HitResult ray) {
        return NeoForge.EVENT_BUS.post(new ProjectileImpactEvent(projectile, ray)).isCanceled();
    }

    public static LootTable loadLootTable(ResourceLocation name, LootTable table) {
        if (table == LootTable.EMPTY) // Empty table has a null name, and shouldn't be modified anyway.
            return table;
        LootTableLoadEvent event = new LootTableLoadEvent(name, table);
        if (NeoForge.EVENT_BUS.post(event).isCanceled())
            return LootTable.EMPTY;
        return event.getTable();
    }

    public static boolean canCreateFluidSource(Level level, BlockPos pos, BlockState state, boolean def) {
        CreateFluidSourceEvent evt = new CreateFluidSourceEvent(level, pos, state);
        NeoForge.EVENT_BUS.post(evt);

        Result result = evt.getResult();
        return result == Result.DEFAULT ? def : result == Result.ALLOW;
    }

    public static Optional<PortalShape> onTrySpawnPortal(LevelAccessor level, BlockPos pos, Optional<PortalShape> size) {
        if (!size.isPresent()) return size;
        return !NeoForge.EVENT_BUS.post(new BlockEvent.PortalSpawnEvent(level, pos, level.getBlockState(pos), size.get())).isCanceled() ? size : Optional.empty();
    }

    public static int onEnchantmentLevelSet(Level level, BlockPos pos, int enchantRow, int power, ItemStack itemStack, int enchantmentLevel) {
        EnchantmentLevelSetEvent e = new EnchantmentLevelSetEvent(level, pos, enchantRow, power, itemStack, enchantmentLevel);
        NeoForge.EVENT_BUS.post(e);
        return e.getEnchantLevel();
    }

    public static boolean onEntityDestroyBlock(LivingEntity entity, BlockPos pos, BlockState state) {
        return !NeoForge.EVENT_BUS.post(new LivingDestroyBlockEvent(entity, pos, state)).isCanceled();
    }

    public static boolean getMobGriefingEvent(Level level, @Nullable Entity entity) {
        if (entity == null)
            return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);

        EntityMobGriefingEvent event = new EntityMobGriefingEvent(entity);
        NeoForge.EVENT_BUS.post(event);

        Result result = event.getResult();
        return result == Result.DEFAULT ? level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) : result == Result.ALLOW;
    }

    public static SaplingGrowTreeEvent blockGrowFeature(LevelAccessor level, RandomSource randomSource, BlockPos pos, @Nullable Holder<ConfiguredFeature<?, ?>> holder) {
        SaplingGrowTreeEvent event = new SaplingGrowTreeEvent(level, randomSource, pos, holder);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    /**
     * Fires the {@link AlterGroundEvent} and retrieves the resulting {@link StateProvider}.
     * 
     * @param ctx       The tree decoration context for the current alteration.
     * @param positions The list of positions that are considered roots.
     * @param provider  The original {@link BlockStateProvider} from the {@link AlterGroundDecorator}.
     * @return The (possibly event-modified) {@link StateProvider} to be used for ground alteration.
     * @apiNote This method is called off-thread during world generation.
     */
    public static StateProvider alterGround(TreeDecorator.Context ctx, List<BlockPos> positions, StateProvider provider) {
        if (positions.isEmpty()) return provider; // I don't think this list is ever empty, but if it is, firing the event is pointless anyway.
        AlterGroundEvent event = new AlterGroundEvent(ctx, positions, provider);
        NeoForge.EVENT_BUS.post(event);
        return event.getStateProvider();
    }

    public static void fireChunkTicketLevelUpdated(ServerLevel level, long chunkPos, int oldTicketLevel, int newTicketLevel, @Nullable ChunkHolder chunkHolder) {
        if (oldTicketLevel != newTicketLevel)
            NeoForge.EVENT_BUS.post(new ChunkTicketLevelUpdatedEvent(level, chunkPos, oldTicketLevel, newTicketLevel, chunkHolder));
    }

    public static void fireChunkWatch(ServerPlayer entity, LevelChunk chunk, ServerLevel level) {
        NeoForge.EVENT_BUS.post(new ChunkWatchEvent.Watch(entity, chunk, level));
    }

    public static void fireChunkSent(ServerPlayer entity, LevelChunk chunk, ServerLevel level) {
        NeoForge.EVENT_BUS.post(new ChunkWatchEvent.Sent(entity, chunk, level));
    }

    public static void fireChunkUnWatch(ServerPlayer entity, ChunkPos chunkpos, ServerLevel level) {
        NeoForge.EVENT_BUS.post(new ChunkWatchEvent.UnWatch(entity, chunkpos, level));
    }

    public static boolean onPistonMovePre(Level level, BlockPos pos, Direction direction, boolean extending) {
        return NeoForge.EVENT_BUS.post(new PistonEvent.Pre(level, pos, direction, extending ? PistonEvent.PistonMoveType.EXTEND : PistonEvent.PistonMoveType.RETRACT)).isCanceled();
    }

    public static void onPistonMovePost(Level level, BlockPos pos, Direction direction, boolean extending) {
        NeoForge.EVENT_BUS.post(new PistonEvent.Post(level, pos, direction, extending ? PistonEvent.PistonMoveType.EXTEND : PistonEvent.PistonMoveType.RETRACT));
    }

    public static long onSleepFinished(ServerLevel level, long newTime, long minTime) {
        SleepFinishedTimeEvent event = new SleepFinishedTimeEvent(level, newTime, minTime);
        NeoForge.EVENT_BUS.post(event);
        return event.getNewTime();
    }

    public static List<PreparableReloadListener> onResourceReload(ReloadableServerResources serverResources, RegistryAccess registryAccess) {
        AddReloadListenerEvent event = new AddReloadListenerEvent(serverResources, registryAccess);
        NeoForge.EVENT_BUS.post(event);
        return event.getListeners();
    }

    public static void onCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandBuildContext context) {
        RegisterCommandsEvent event = new RegisterCommandsEvent(dispatcher, environment, context);
        NeoForge.EVENT_BUS.post(event);
    }

    public static EntityEvent.Size getEntitySizeForge(Entity entity, Pose pose, EntityDimensions size, float eyeHeight) {
        EntityEvent.Size evt = new EntityEvent.Size(entity, pose, size, eyeHeight);
        NeoForge.EVENT_BUS.post(evt);
        return evt;
    }

    public static EntityEvent.Size getEntitySizeForge(Entity entity, Pose pose, EntityDimensions oldSize, EntityDimensions newSize, float newEyeHeight) {
        EntityEvent.Size evt = new EntityEvent.Size(entity, pose, oldSize, newSize, entity.getEyeHeight(), newEyeHeight);
        NeoForge.EVENT_BUS.post(evt);
        return evt;
    }

    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, Consumer<Integer> timer) {
        return !NeoForge.EVENT_BUS.post(new LivingConversionEvent.Pre(entity, outcome, timer)).isCanceled();
    }

    public static void onLivingConvert(LivingEntity entity, LivingEntity outcome) {
        NeoForge.EVENT_BUS.post(new LivingConversionEvent.Post(entity, outcome));
    }

    public static EntityTeleportEvent.TeleportCommand onEntityTeleportCommand(Entity entity, double targetX, double targetY, double targetZ) {
        EntityTeleportEvent.TeleportCommand event = new EntityTeleportEvent.TeleportCommand(entity, targetX, targetY, targetZ);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static EntityTeleportEvent.SpreadPlayersCommand onEntityTeleportSpreadPlayersCommand(Entity entity, double targetX, double targetY, double targetZ) {
        EntityTeleportEvent.SpreadPlayersCommand event = new EntityTeleportEvent.SpreadPlayersCommand(entity, targetX, targetY, targetZ);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static EntityTeleportEvent.EnderEntity onEnderTeleport(LivingEntity entity, double targetX, double targetY, double targetZ) {
        EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(entity, targetX, targetY, targetZ);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    @ApiStatus.Internal
    public static EntityTeleportEvent.EnderPearl onEnderPearlLand(ServerPlayer entity, double targetX, double targetY, double targetZ, ThrownEnderpearl pearlEntity, float attackDamage, HitResult hitResult) {
        EntityTeleportEvent.EnderPearl event = new EntityTeleportEvent.EnderPearl(entity, targetX, targetY, targetZ, pearlEntity, attackDamage, hitResult);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static EntityTeleportEvent.ChorusFruit onChorusFruitTeleport(LivingEntity entity, double targetX, double targetY, double targetZ) {
        EntityTeleportEvent.ChorusFruit event = new EntityTeleportEvent.ChorusFruit(entity, targetX, targetY, targetZ);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static boolean onPermissionChanged(GameProfile gameProfile, int newLevel, PlayerList playerList) {
        int oldLevel = playerList.getServer().getProfilePermissions(gameProfile);
        ServerPlayer player = playerList.getPlayer(gameProfile.getId());
        if (newLevel != oldLevel && player != null) {
            return NeoForge.EVENT_BUS.post(new PermissionsChangedEvent(player, newLevel, oldLevel)).isCanceled();
        }
        return false;
    }

    public static void firePlayerChangedDimensionEvent(Player player, ResourceKey<Level> fromDim, ResourceKey<Level> toDim) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerChangedDimensionEvent(player, fromDim, toDim));
    }

    public static void firePlayerLoggedIn(Player player) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(player));
    }

    public static void firePlayerLoggedOut(Player player) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(player));
    }

    public static void firePlayerRespawnEvent(Player player, boolean endConquered) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(player, endConquered));
    }

    public static void firePlayerItemPickupEvent(Player player, ItemEntity item, ItemStack clone) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemPickupEvent(player, item, clone));
    }

    public static void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, crafted, craftMatrix));
    }

    public static void firePlayerSmeltedEvent(Player player, ItemStack smelted) {
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemSmeltedEvent(player, smelted));
    }

    public static void onRenderTickStart(float timer) {
        NeoForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.START, timer));
    }

    public static void onRenderTickEnd(float timer) {
        NeoForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.END, timer));
    }

    public static void onPlayerPreTick(Player player) {
        NeoForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player));
    }

    public static void onPlayerPostTick(Player player) {
        NeoForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
    }

    public static void onPreLevelTick(Level level, BooleanSupplier haveTime) {
        NeoForge.EVENT_BUS.post(new TickEvent.LevelTickEvent(level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER, TickEvent.Phase.START, level, haveTime));
    }

    public static void onPostLevelTick(Level level, BooleanSupplier haveTime) {
        NeoForge.EVENT_BUS.post(new TickEvent.LevelTickEvent(level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER, TickEvent.Phase.END, level, haveTime));
    }

    public static void onPreClientTick() {
        NeoForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
    }

    public static void onPostClientTick() {
        NeoForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
    }

    public static void onPreServerTick(BooleanSupplier haveTime, MinecraftServer server) {
        NeoForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START, haveTime, server));
    }

    public static void onPostServerTick(BooleanSupplier haveTime, MinecraftServer server) {
        NeoForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END, haveTime, server));
    }

    public static WeightedRandomList<MobSpawnSettings.SpawnerData> getPotentialSpawns(LevelAccessor level, MobCategory category, BlockPos pos, WeightedRandomList<MobSpawnSettings.SpawnerData> oldList) {
        LevelEvent.PotentialSpawns event = new LevelEvent.PotentialSpawns(level, category, pos, oldList);
        if (NeoForge.EVENT_BUS.post(event).isCanceled())
            return WeightedRandomList.create();
        return WeightedRandomList.create(event.getSpawnerDataList());
    }

    public static StatAwardEvent onStatAward(Player player, Stat<?> stat, int value) {
        StatAwardEvent event = new StatAwardEvent(player, stat, value);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    @ApiStatus.Internal
    public static void onAdvancementEarnedEvent(Player player, AdvancementHolder earned) {
        NeoForge.EVENT_BUS.post(new AdvancementEarnEvent(player, earned));
    }

    @ApiStatus.Internal
    public static void onAdvancementProgressedEvent(Player player, AdvancementHolder progressed, AdvancementProgress advancementProgress, String criterion, ProgressType progressType) {
        NeoForge.EVENT_BUS.post(new AdvancementProgressEvent(player, progressed, advancementProgress, criterion, progressType));
    }

    public static boolean onEffectRemoved(LivingEntity entity, MobEffect effect, @Nullable EffectCure cure) {
        return NeoForge.EVENT_BUS.post(new MobEffectEvent.Remove(entity, effect, cure)).isCanceled();
    }

    public static boolean onEffectRemoved(LivingEntity entity, MobEffectInstance effectInstance, @Nullable EffectCure cure) {
        return NeoForge.EVENT_BUS.post(new MobEffectEvent.Remove(entity, effectInstance, cure)).isCanceled();
    }

    /**
     * Fires {@link GetEnchantmentLevelEvent} and for a single enchantment, returning the (possibly event-modified) level.
     * 
     * @param level The original level of the enchantment as provided by the Item.
     * @param stack The stack being queried against.
     * @param ench  The enchantment being queried for.
     * @return The new level of the enchantment.
     */
    public static int getEnchantmentLevelSpecific(int level, ItemStack stack, Enchantment ench) {
        Map<Enchantment, Integer> map = new HashMap<>();
        map.put(ench, level);
        var event = new GetEnchantmentLevelEvent(stack, map, ench);
        NeoForge.EVENT_BUS.post(event);
        return event.getEnchantments().getOrDefault(ench, 0);
    }

    /**
     * Fires {@link GetEnchantmentLevelEvent} and for all enchantments, returning the (possibly event-modified) enchantment map.
     * 
     * @param enchantments The original enchantment map as provided by the Item.
     * @param stack        The stack being queried against.
     * @return The new enchantment map.
     */
    public static Map<Enchantment, Integer> getEnchantmentLevel(Map<Enchantment, Integer> enchantments, ItemStack stack) {
        enchantments = new HashMap<>(enchantments);
        var event = new GetEnchantmentLevelEvent(stack, enchantments, null);
        NeoForge.EVENT_BUS.post(event);
        return enchantments;
    }

    /**
     * Fires the {@link BuildCreativeModeTabContentsEvent}.
     *
     * @param tab               The tab that contents are being collected for.
     * @param tabKey            The resource key of the tab.
     * @param originalGenerator The display items generator that populates vanilla entries.
     * @param params            Display parameters, controlling if certain items are hidden.
     * @param output            The output acceptor.
     * @apiNote Call via {@link CreativeModeTab#buildContents(CreativeModeTab.ItemDisplayParameters)}
     */
    @ApiStatus.Internal
    public static void onCreativeModeTabBuildContents(CreativeModeTab tab, ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.DisplayItemsGenerator originalGenerator, CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        final var entries = new MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility>(ItemStackLinkedSet.TYPE_AND_TAG,
                (key, left, right) -> {
                    //throw new IllegalStateException("Accidentally adding the same item stack twice " + key.getDisplayName().getString() + " to a Creative Mode Tab: " + tab.getDisplayName().getString());
                    // Vanilla adds enchanting books twice in both visibilities.
                    // This is just code cleanliness for them. For us lets just increase the visibility and merge the entries.
                    return CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
                });

        originalGenerator.accept(params, (stack, vis) -> {
            if (stack.getCount() != 1)
                throw new IllegalArgumentException("The stack count must be 1");
            entries.put(stack, vis);
        });

        ModLoader.get().postEvent(new BuildCreativeModeTabContentsEvent(tab, tabKey, params, entries));

        for (var entry : entries)
            output.accept(entry.getKey(), entry.getValue());
    }

    /**
     * Fires the mob split event. Returns the event for cancellation checking.
     * 
     * @param parent   The parent mob, which is in the process of being removed.
     * @param children All child mobs that would have normally spawned.
     * @return The event object.
     */
    public static MobSplitEvent onMobSplit(Mob parent, List<Mob> children) {
        var event = new MobSplitEvent(parent, children);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }
}
