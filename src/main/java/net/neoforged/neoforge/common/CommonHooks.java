/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.syncher.EntityDataSerializer;
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
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.fixes.StructuresBecomeConfiguredFix;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.MavenVersionStringHelper;
import net.neoforged.neoforge.common.util.MutableHashedLinkedMap;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.DifficultyChangeEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.ModMismatchEvent;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.RegisterStructureConversionsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.StatAwardEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.brewing.PotionBrewEvent;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.EnderManAngerEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDrownEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingPackSizeEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.living.LootingLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.living.ShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.ZombieEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ArrowNockEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.EntityItemPickupEvent;
import net.neoforged.neoforge.event.entity.player.FillBucketEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PermissionsChangedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerFlyableFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSleepInBedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.entity.player.SleepingLocationCheckEvent;
import net.neoforged.neoforge.event.entity.player.SleepingTimeCheckEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.AlterGroundEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkTicketLevelUpdatedEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.level.SaplingGrowTreeEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Class for various common (i.e. client and server-side) hooks.
 */
public class CommonHooks {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker WORLDPERSISTENCE = MarkerManager.getMarker("WP");

    public static boolean canContinueUsing(ItemStack from, ItemStack to) {
        if (!from.isEmpty() && !to.isEmpty()) {
            return from.getItem().canContinueUsing(from, to);
        }
        return false;
    }

    public static boolean isCorrectToolForDrops(BlockState state, Player player) {
        if (!state.requiresCorrectToolForDrops())
            return CommonHooks.doPlayerHarvestCheck(player, state, true);

        return player.hasCorrectToolForDrops(state);
    }

    public static boolean onItemStackedOn(ItemStack carriedItem, ItemStack stackedOnItem, Slot slot, ClickAction action, Player player, SlotAccess carriedSlotAccess) {
        return NeoForge.EVENT_BUS.post(new ItemStackedOnOtherEvent(carriedItem, stackedOnItem, slot, action, player, carriedSlotAccess)).isCanceled();
    }

    public static void onDifficultyChange(Difficulty difficulty, Difficulty oldDifficulty) {
        NeoForge.EVENT_BUS.post(new DifficultyChangeEvent(difficulty, oldDifficulty));
    }

    public static LivingChangeTargetEvent onLivingChangeTarget(LivingEntity entity, LivingEntity originalTarget, LivingChangeTargetEvent.ILivingTargetType targetType) {
        LivingChangeTargetEvent event = new LivingChangeTargetEvent(entity, originalTarget, targetType);
        NeoForge.EVENT_BUS.post(event);

        return event;
    }

    public static boolean onLivingAttack(LivingEntity entity, DamageSource src, float amount) {
        return entity instanceof Player || !NeoForge.EVENT_BUS.post(new LivingAttackEvent(entity, src, amount)).isCanceled();
    }

    public static boolean onPlayerAttack(LivingEntity entity, DamageSource src, float amount) {
        return !NeoForge.EVENT_BUS.post(new LivingAttackEvent(entity, src, amount)).isCanceled();
    }

    public static LivingKnockBackEvent onLivingKnockBack(LivingEntity target, float strength, double ratioX, double ratioZ) {
        LivingKnockBackEvent event = new LivingKnockBackEvent(target, strength, ratioX, ratioZ);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static boolean onLivingUseTotem(LivingEntity entity, DamageSource damageSource, ItemStack totem, InteractionHand hand) {
        return !NeoForge.EVENT_BUS.post(new LivingUseTotemEvent(entity, damageSource, totem, hand)).isCanceled();
    }

    public static float onLivingHurt(LivingEntity entity, DamageSource src, float amount) {
        LivingHurtEvent event = new LivingHurtEvent(entity, src, amount);
        return (NeoForge.EVENT_BUS.post(event).isCanceled() ? 0 : event.getAmount());
    }

    public static float onLivingDamage(LivingEntity entity, DamageSource src, float amount) {
        LivingDamageEvent event = new LivingDamageEvent(entity, src, amount);
        return (NeoForge.EVENT_BUS.post(event).isCanceled() ? 0 : event.getAmount());
    }

    public static boolean onLivingDeath(LivingEntity entity, DamageSource src) {
        return NeoForge.EVENT_BUS.post(new LivingDeathEvent(entity, src)).isCanceled();
    }

    public static boolean onLivingDrops(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit) {
        return NeoForge.EVENT_BUS.post(new LivingDropsEvent(entity, source, drops, lootingLevel, recentlyHit)).isCanceled();
    }

    @Nullable
    public static float[] onLivingFall(LivingEntity entity, float distance, float damageMultiplier) {
        LivingFallEvent event = new LivingFallEvent(entity, distance, damageMultiplier);
        return (NeoForge.EVENT_BUS.post(event).isCanceled() ? null : new float[] { event.getDistance(), event.getDamageMultiplier() });
    }

    public static int getLootingLevel(Entity target, @Nullable Entity killer, @Nullable DamageSource cause) {
        int looting = 0;
        if (killer instanceof LivingEntity)
            looting = EnchantmentHelper.getMobLooting((LivingEntity) killer);
        if (target instanceof LivingEntity)
            looting = getLootingLevel((LivingEntity) target, cause, looting);
        return looting;
    }

    public static int getLootingLevel(LivingEntity target, @Nullable DamageSource cause, int level) {
        LootingLevelEvent event = new LootingLevelEvent(target, cause, level);
        NeoForge.EVENT_BUS.post(event);
        return event.getLootingLevel();
    }

    public static double getEntityVisibilityMultiplier(LivingEntity entity, Entity lookingEntity, double originalMultiplier) {
        LivingEvent.LivingVisibilityEvent event = new LivingEvent.LivingVisibilityEvent(entity, lookingEntity, originalMultiplier);
        NeoForge.EVENT_BUS.post(event);
        return Math.max(0, event.getVisibilityModifier());
    }

    public static Optional<BlockPos> isLivingOnLadder(BlockState state, Level level, BlockPos pos, LivingEntity entity) {
        boolean isSpectator = (entity instanceof Player && entity.isSpectator());
        if (isSpectator)
            return Optional.empty();
        if (!NeoForgeConfig.SERVER.fullBoundingBoxLadders.get()) {
            return state.isLadder(level, pos, entity) ? Optional.of(pos) : Optional.empty();
        } else {
            AABB bb = entity.getBoundingBox();
            int mX = Mth.floor(bb.minX);
            int mY = Mth.floor(bb.minY);
            int mZ = Mth.floor(bb.minZ);
            for (int y2 = mY; y2 < bb.maxY; y2++) {
                for (int x2 = mX; x2 < bb.maxX; x2++) {
                    for (int z2 = mZ; z2 < bb.maxZ; z2++) {
                        BlockPos tmp = new BlockPos(x2, y2, z2);
                        state = level.getBlockState(tmp);
                        if (state.isLadder(level, tmp, entity)) {
                            return Optional.of(tmp);
                        }
                    }
                }
            }
            return Optional.empty();
        }
    }

    public static void onLivingJump(LivingEntity entity) {
        NeoForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent(entity));
    }

    @Nullable
    public static ItemEntity onPlayerTossEvent(Player player, ItemStack item, boolean includeName) {
        player.captureDrops(Lists.newArrayList());
        ItemEntity ret = player.drop(item, false, includeName);
        player.captureDrops(null);

        if (ret == null)
            return null;

        ItemTossEvent event = new ItemTossEvent(ret, player);
        if (NeoForge.EVENT_BUS.post(event).isCanceled())
            return null;

        if (!player.level().isClientSide)
            player.getCommandSenderWorld().addFreshEntity(event.getEntity());
        return event.getEntity();
    }

    public static boolean onVanillaGameEvent(Level level, Holder<GameEvent> vanillaEvent, Vec3 pos, GameEvent.Context context) {
        return !NeoForge.EVENT_BUS.post(new VanillaGameEvent(level, vanillaEvent, pos, context)).isCanceled();
    }

    private static String getRawText(Component message) {
        return message.getContents() instanceof PlainTextContents plainTextContents ? plainTextContents.text() : "";
    }

    @Nullable
    public static Component onServerChatSubmittedEvent(ServerPlayer player, String plain, Component decorated) {
        ServerChatEvent event = new ServerChatEvent(player, plain, decorated);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? null : event.getMessage();
    }

    public static ChatDecorator getServerChatSubmittedDecorator() {
        return (sender, message) -> {
            if (sender == null)
                return message; // Vanilla should never get here with the patches we use, but let's be safe with dumb mods

            return onServerChatSubmittedEvent(sender, getRawText(message), message);
        };
    }

    static final Pattern URL_PATTERN = Pattern.compile(
            //         schema                          ipv4            OR        namespace                 port     path         ends
            //   |-----------------|        |-------------------------|  |-------------------------|    |---------| |--|   |---------------|
            "((?:[a-z0-9]{2,}:\\/\\/)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|(?:[-\\w_]{1,}\\.[a-z]{2,}?))(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))",
            Pattern.CASE_INSENSITIVE);

    public static Component newChatWithLinks(String string) {
        return newChatWithLinks(string, true);
    }

    public static Component newChatWithLinks(String string, boolean allowMissingHeader) {
        // Includes ipv4 and domain pattern
        // Matches an ip (xx.xxx.xx.xxx) or a domain (something.com) with or
        // without a protocol or path.
        MutableComponent ichat = null;
        Matcher matcher = URL_PATTERN.matcher(string);
        int lastEnd = 0;

        // Find all urls
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Append the previous left overs.
            String part = string.substring(lastEnd, start);
            if (part.length() > 0) {
                if (ichat == null)
                    ichat = Component.literal(part);
                else
                    ichat.append(part);
            }
            lastEnd = end;
            String url = string.substring(start, end);
            MutableComponent link = Component.literal(url);

            try {
                // Add schema so client doesn't crash.
                if ((new URI(url)).getScheme() == null) {
                    if (!allowMissingHeader) {
                        if (ichat == null)
                            ichat = Component.literal(url);
                        else
                            ichat.append(url);
                        continue;
                    }
                    url = "http://" + url;
                }
            } catch (URISyntaxException e) {
                // Bad syntax bail out!
                if (ichat == null)
                    ichat = Component.literal(url);
                else
                    ichat.append(url);
                continue;
            }

            // Set the click event and append the link.
            ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            link.setStyle(link.getStyle().withClickEvent(click).withUnderlined(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)));
            if (ichat == null)
                ichat = Component.literal("");
            ichat.append(link);
        }

        // Append the rest of the message.
        String end = string.substring(lastEnd);
        if (ichat == null)
            ichat = Component.literal(end);
        else if (end.length() > 0)
            ichat.append(Component.literal(string.substring(lastEnd)));
        return ichat;
    }

    public static void dropXpForBlock(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack) {
        int fortuneLevel = stack.getEnchantmentLevel(Enchantments.FORTUNE);
        int silkTouchLevel = stack.getEnchantmentLevel(Enchantments.SILK_TOUCH);
        int exp = state.getExpDrop(level, level.random, pos, fortuneLevel, silkTouchLevel);
        if (exp > 0)
            state.getBlock().popExperience(level, pos, exp);
    }

    public static int onBlockBreakEvent(Level level, GameType gameType, ServerPlayer entityPlayer, BlockPos pos) {
        // Logic from tryHarvestBlock for pre-canceling the event
        boolean preCancelEvent = false;
        ItemStack itemstack = entityPlayer.getMainHandItem();
        if (!itemstack.isEmpty() && !itemstack.getItem().canAttackBlock(level.getBlockState(pos), level, pos, entityPlayer)) {
            preCancelEvent = true;
        }

        if (gameType.isBlockPlacingRestricted()) {
            if (gameType == GameType.SPECTATOR)
                preCancelEvent = true;

            if (!entityPlayer.mayBuild()) {
                AdventureModePredicate adventureModePredicate = itemstack.get(DataComponents.CAN_BREAK);
                if (itemstack.isEmpty() || adventureModePredicate == null || !adventureModePredicate.test(new BlockInWorld(level, pos, false))) {
                    preCancelEvent = true;
                }
            }
        }

        // Tell client the block is gone immediately then process events
        if (level.getBlockEntity(pos) == null) {
            entityPlayer.connection.send(new ClientboundBlockUpdatePacket(pos, level.getFluidState(pos).createLegacyBlock()));
        }

        // Post the block break event
        BlockState state = level.getBlockState(pos);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, entityPlayer);
        event.setCanceled(preCancelEvent);
        NeoForge.EVENT_BUS.post(event);

        // Handle if the event is canceled
        if (event.isCanceled()) {
            // Let the client know the block still exists
            entityPlayer.connection.send(new ClientboundBlockUpdatePacket(level, pos));

            // Update any tile entity data for this block
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                Packet<?> pkt = blockEntity.getUpdatePacket();
                if (pkt != null) {
                    entityPlayer.connection.send(pkt);
                }
            }
        }
        return event.isCanceled() ? -1 : event.getExpToDrop();
    }

    public static InteractionResult onPlaceItemIntoWorld(UseOnContext context) {
        ItemStack itemstack = context.getItemInHand();
        Level level = context.getLevel();

        Player player = context.getPlayer();
        if (player != null && !player.getAbilities().mayBuild) {
            AdventureModePredicate adventureModePredicate = itemstack.get(DataComponents.CAN_PLACE_ON);
            if (adventureModePredicate == null || !adventureModePredicate.test(new BlockInWorld(level, context.getClickedPos(), false))) {
                return net.minecraft.world.InteractionResult.PASS;
            }
        }

        // handle all placement events here
        Item item = itemstack.getItem();
        int size = itemstack.getCount();
        // Porting 1.20.5 redo this for components?
        //CompoundTag nbt = null;
        //if (itemstack.getTag() != null)
        //    nbt = itemstack.getTag().copy();

        if (!(itemstack.getItem() instanceof BucketItem)) // if not bucket
            level.captureBlockSnapshots = true;

        ItemStack copy = itemstack.copy();
        InteractionResult ret = itemstack.getItem().useOn(context);
        if (itemstack.isEmpty())
            CommonHooks.onPlayerDestroyItem(player, copy, context.getHand());

        level.captureBlockSnapshots = false;

        if (ret.consumesAction()) {
            // save new item data
            int newSize = itemstack.getCount();
            //CompoundTag newNBT = null;
            //if (itemstack.getTag() != null) {
            //    newNBT = itemstack.getTag().copy();
            //}
            @SuppressWarnings("unchecked")
            List<BlockSnapshot> blockSnapshots = (List<BlockSnapshot>) level.capturedBlockSnapshots.clone();
            level.capturedBlockSnapshots.clear();

            // make sure to set pre-placement item data for event
            itemstack.setCount(size);
            //itemstack.setTag(nbt);
            //TODO: Set pre-placement item attachments?

            Direction side = context.getClickedFace();

            boolean eventResult = false;
            if (blockSnapshots.size() > 1) {
                eventResult = CommonHooks.onMultiBlockPlace(player, blockSnapshots, side);
            } else if (blockSnapshots.size() == 1) {
                eventResult = CommonHooks.onBlockPlace(player, blockSnapshots.get(0), side);
            }

            if (eventResult) {
                ret = InteractionResult.FAIL; // cancel placement
                // revert back all captured blocks
                for (BlockSnapshot blocksnapshot : Lists.reverse(blockSnapshots)) {
                    level.restoringBlockSnapshots = true;
                    blocksnapshot.restore(true, false);
                    level.restoringBlockSnapshots = false;
                }
            } else {
                // Change the stack to its new content
                itemstack.setCount(newSize);
                //itemstack.setTag(newNBT);

                for (BlockSnapshot snap : blockSnapshots) {
                    int updateFlag = snap.getFlag();
                    BlockState oldBlock = snap.getReplacedBlock();
                    BlockState newBlock = level.getBlockState(snap.getPos());
                    newBlock.onPlace(level, snap.getPos(), oldBlock, false);

                    level.markAndNotifyBlock(snap.getPos(), level.getChunkAt(snap.getPos()), oldBlock, newBlock, updateFlag, 512);
                }
                if (player != null)
                    player.awardStat(Stats.ITEM_USED.get(item));
            }
        }
        level.capturedBlockSnapshots.clear();

        return ret;
    }

    public static boolean onAnvilChange(AnvilMenu container, ItemStack left, ItemStack right, Container outputSlot, String name, long baseCost, Player player) {
        AnvilUpdateEvent e = new AnvilUpdateEvent(left, right, name, baseCost, player);
        if (NeoForge.EVENT_BUS.post(e).isCanceled())
            return false;
        if (e.getOutput().isEmpty())
            return true;

        outputSlot.setItem(0, e.getOutput());
        container.setMaximumCost(e.getCost());
        container.repairItemCountCost = e.getMaterialCost();
        return false;
    }

    public static float onAnvilRepair(Player player, ItemStack output, ItemStack left, ItemStack right) {
        AnvilRepairEvent e = new AnvilRepairEvent(player, left, right, output);
        NeoForge.EVENT_BUS.post(e);
        return e.getBreakChance();
    }

    public static int onGrindstoneChange(ItemStack top, ItemStack bottom, Container outputSlot, int xp) {
        GrindstoneEvent.OnPlaceItem e = new GrindstoneEvent.OnPlaceItem(top, bottom, xp);
        if (NeoForge.EVENT_BUS.post(e).isCanceled()) {
            outputSlot.setItem(0, ItemStack.EMPTY);
            return -1;
        }
        if (e.getOutput().isEmpty())
            return Integer.MIN_VALUE;

        outputSlot.setItem(0, e.getOutput());
        return e.getXp();
    }

    public static boolean onGrindstoneTake(Container inputSlots, ContainerLevelAccess access, Function<Level, Integer> xpFunction) {
        access.execute((l, p) -> {
            int xp = xpFunction.apply(l);
            GrindstoneEvent.OnTakeItem e = new GrindstoneEvent.OnTakeItem(inputSlots.getItem(0), inputSlots.getItem(1), xp);
            if (NeoForge.EVENT_BUS.post(e).isCanceled()) {
                return;
            }
            if (l instanceof ServerLevel) {
                ExperienceOrb.award((ServerLevel) l, Vec3.atCenterOf(p), e.getXp());
            }
            l.levelEvent(1042, p, 0);
            inputSlots.setItem(0, e.getNewTopItem());
            inputSlots.setItem(1, e.getNewBottomItem());
            inputSlots.setChanged();
        });
        return true;
    }

    private static ThreadLocal<Player> craftingPlayer = new ThreadLocal<Player>();

    public static void setCraftingPlayer(Player player) {
        craftingPlayer.set(player);
    }

    public static Player getCraftingPlayer() {
        return craftingPlayer.get();
    }

    public static ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (stack.getItem().hasCraftingRemainingItem(stack)) {
            stack = stack.getItem().getCraftingRemainingItem(stack);
            if (!stack.isEmpty() && stack.isDamageableItem() && stack.getDamageValue() > stack.getMaxDamage()) {
                CommonHooks.onPlayerDestroyItem(craftingPlayer.get(), stack, null);
                return ItemStack.EMPTY;
            }
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public static boolean onPlayerAttackTarget(Player player, Entity target) {
        if (NeoForge.EVENT_BUS.post(new AttackEntityEvent(player, target)).isCanceled())
            return false;
        ItemStack stack = player.getMainHandItem();
        return stack.isEmpty() || !stack.getItem().onLeftClickEntity(stack, player, target);
    }

    public static boolean onTravelToDimension(Entity entity, ResourceKey<Level> dimension) {
        EntityTravelToDimensionEvent event = new EntityTravelToDimensionEvent(entity, dimension);
        NeoForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    @Nullable
    public static InteractionResult onInteractEntityAt(Player player, Entity entity, HitResult ray, InteractionHand hand) {
        Vec3 vec3d = ray.getLocation().subtract(entity.position());
        return onInteractEntityAt(player, entity, vec3d, hand);
    }

    @Nullable
    public static InteractionResult onInteractEntityAt(Player player, Entity entity, Vec3 vec3d, InteractionHand hand) {
        PlayerInteractEvent.EntityInteractSpecific evt = new PlayerInteractEvent.EntityInteractSpecific(player, hand, entity, vec3d);
        NeoForge.EVENT_BUS.post(evt);
        return evt.isCanceled() ? evt.getCancellationResult() : null;
    }

    @Nullable
    public static InteractionResult onInteractEntity(Player player, Entity entity, InteractionHand hand) {
        PlayerInteractEvent.EntityInteract evt = new PlayerInteractEvent.EntityInteract(player, hand, entity);
        NeoForge.EVENT_BUS.post(evt);
        return evt.isCanceled() ? evt.getCancellationResult() : null;
    }

    @Nullable
    public static InteractionResult onItemRightClick(Player player, InteractionHand hand) {
        PlayerInteractEvent.RightClickItem evt = new PlayerInteractEvent.RightClickItem(player, hand);
        NeoForge.EVENT_BUS.post(evt);
        return evt.isCanceled() ? evt.getCancellationResult() : null;
    }

    /**
     * @deprecated Use {@link #onLeftClickBlock(Player, BlockPos, Direction, ServerboundPlayerActionPacket.Action)} instead
     */
    @Deprecated(since = "1.20.1", forRemoval = true)
    public static PlayerInteractEvent.LeftClickBlock onLeftClickBlock(Player player, BlockPos pos, Direction face) {
        return onLeftClickBlock(player, pos, face, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
    }

    public static PlayerInteractEvent.LeftClickBlock onLeftClickBlock(Player player, BlockPos pos, Direction face, ServerboundPlayerActionPacket.Action action) {
        PlayerInteractEvent.LeftClickBlock evt = new PlayerInteractEvent.LeftClickBlock(player, pos, face, PlayerInteractEvent.LeftClickBlock.Action.convert(action));
        NeoForge.EVENT_BUS.post(evt);
        return evt;
    }

    public static PlayerInteractEvent.LeftClickBlock onClientMineHold(Player player, BlockPos pos, Direction face) {
        PlayerInteractEvent.LeftClickBlock evt = new PlayerInteractEvent.LeftClickBlock(player, pos, face, PlayerInteractEvent.LeftClickBlock.Action.CLIENT_HOLD);
        NeoForge.EVENT_BUS.post(evt);
        return evt;
    }

    public static PlayerInteractEvent.RightClickBlock onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
        PlayerInteractEvent.RightClickBlock evt = new PlayerInteractEvent.RightClickBlock(player, hand, pos, hitVec);
        NeoForge.EVENT_BUS.post(evt);
        return evt;
    }

    public static void onEmptyClick(Player player, InteractionHand hand) {
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickEmpty(player, hand));
    }

    public static void onEmptyLeftClick(Player player) {
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickEmpty(player));
    }

    /**
     * @return null if game type should not be changed, desired new GameType otherwise
     */
    @Nullable
    public static GameType onChangeGameType(Player player, GameType currentGameType, GameType newGameType) {
        if (currentGameType != newGameType) {
            PlayerEvent.PlayerChangeGameModeEvent evt = new PlayerEvent.PlayerChangeGameModeEvent(player, currentGameType, newGameType);
            NeoForge.EVENT_BUS.post(evt);
            return evt.isCanceled() ? null : evt.getNewGameMode();
        }
        return newGameType;
    }

    @ApiStatus.Internal
    public static Codec<List<LootPool>> lootPoolsCodec(BiConsumer<LootPool, String> nameSetter) {
        var decoder = ConditionalOps.createConditionalCodec(LootPool.CODEC).listOf()
                .map(pools -> {
                    if (pools.size() == 1) {
                        if (pools.get(0).isPresent() && pools.get(0).get().getName() == null) {
                            nameSetter.accept(pools.get(0).get(), "main");
                        }
                    } else {
                        for (int i = 0; i < pools.size(); ++i) {
                            if (pools.get(i).isPresent() && pools.get(i).get().getName() == null) {
                                nameSetter.accept(pools.get(i).get(), "pool" + i);
                            }
                        }
                    }

                    return pools.stream().filter(Optional::isPresent).map(Optional::get).toList();
                });
        return Codec.of(LootPool.CODEC.listOf(), decoder);
    }

    /**
     * Returns a vanilla fluid type for the given fluid.
     *
     * @param fluid the fluid looking for its type
     * @return the type of the fluid if vanilla
     * @throws RuntimeException if the fluid is not a vanilla one
     */
    public static FluidType getVanillaFluidType(Fluid fluid) {
        if (fluid == Fluids.EMPTY)
            return NeoForgeMod.EMPTY_TYPE.value();
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER)
            return NeoForgeMod.WATER_TYPE.value();
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA)
            return NeoForgeMod.LAVA_TYPE.value();
        if (NeoForgeMod.MILK.asOptional().filter(milk -> milk == fluid).isPresent() || NeoForgeMod.FLOWING_MILK.asOptional().filter(milk -> milk == fluid).isPresent())
            return NeoForgeMod.MILK_TYPE.value();
        throw new RuntimeException("Mod fluids must override getFluidType.");
    }

    public static TagKey<Block> getTagFromVanillaTier(Tiers tier) {
        return switch (tier) {
            case WOOD -> Tags.Blocks.NEEDS_WOOD_TOOL;
            case GOLD -> Tags.Blocks.NEEDS_GOLD_TOOL;
            case STONE -> BlockTags.NEEDS_STONE_TOOL;
            case IRON -> BlockTags.NEEDS_IRON_TOOL;
            case DIAMOND -> BlockTags.NEEDS_DIAMOND_TOOL;
            case NETHERITE -> Tags.Blocks.NEEDS_NETHERITE_TOOL;
        };
    }

    public static Collection<CreativeModeTab> onCheckCreativeTabs(CreativeModeTab... vanillaTabs) {
        final List<CreativeModeTab> tabs = new ArrayList<>(Arrays.asList(vanillaTabs));
        return tabs;
    }

    @FunctionalInterface
    public interface BiomeCallbackFunction {
        Biome apply(final Biome.ClimateSettings climate, final BiomeSpecialEffects effects, final BiomeGenerationSettings gen, final MobSpawnSettings spawns);
    }

    public static boolean onCropsGrowPre(Level level, BlockPos pos, BlockState state, boolean def) {
        BlockEvent ev = new BlockEvent.CropGrowEvent.Pre(level, pos, state);
        NeoForge.EVENT_BUS.post(ev);
        return (ev.getResult() == net.neoforged.bus.api.Event.Result.ALLOW || (ev.getResult() == net.neoforged.bus.api.Event.Result.DEFAULT && def));
    }

    public static void onCropsGrowPost(Level level, BlockPos pos, BlockState state) {
        NeoForge.EVENT_BUS.post(new BlockEvent.CropGrowEvent.Post(level, pos, state, level.getBlockState(pos)));
    }

    @Nullable
    public static CriticalHitEvent getCriticalHit(Player player, Entity target, boolean vanillaCritical, float damageModifier) {
        CriticalHitEvent hitResult = new CriticalHitEvent(player, target, damageModifier, vanillaCritical);
        NeoForge.EVENT_BUS.post(hitResult);
        if (hitResult.getResult() == net.neoforged.bus.api.Event.Result.ALLOW || (vanillaCritical && hitResult.getResult() == net.neoforged.bus.api.Event.Result.DEFAULT)) {
            return hitResult;
        }
        return null;
    }

    /**
     * Hook to fire {@link ItemAttributeModifierEvent}. Modders should use {@link ItemStack#forEachModifier(EquipmentSlot, BiConsumer)} instead.
     */
    public static Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot equipmentSlot, Multimap<Holder<Attribute>, AttributeModifier> attributes) {
        ItemAttributeModifierEvent event = new ItemAttributeModifierEvent(stack, equipmentSlot, attributes);
        NeoForge.EVENT_BUS.post(event);
        return event.getModifiers();
    }

    /**
     * Hook to fire {@link LivingGetProjectileEvent}. Returns the ammo to be used.
     */
    public static ItemStack getProjectile(LivingEntity entity, ItemStack projectileWeaponItem, ItemStack projectile) {
        LivingGetProjectileEvent event = new LivingGetProjectileEvent(entity, projectileWeaponItem, projectile);
        NeoForge.EVENT_BUS.post(event);
        return event.getProjectileItemStack();
    }

    /**
     * Used as the default implementation of {@link Item#getCreatorModId}. Call that method instead.
     */
    @Nullable
    public static String getDefaultCreatorModId(ItemStack itemStack) {
        Item item = itemStack.getItem();
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        String modId = registryName == null ? null : registryName.getNamespace();
        if ("minecraft".equals(modId)) {
            if (item instanceof EnchantedBookItem) {
                Set<Holder<Enchantment>> enchantments = itemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).keySet();
                if (enchantments.size() == 1) {
                    Holder<Enchantment> enchantmentHolder = enchantments.iterator().next();
                    Optional<ResourceKey<Enchantment>> key = enchantmentHolder.unwrapKey();
                    if (key.isPresent()) {
                        return key.get().location().getNamespace();
                    }
                }
            } else if (item instanceof PotionItem || item instanceof TippedArrowItem) {
                PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                Optional<Holder<Potion>> potionType = potionContents.potion();
                Optional<ResourceKey<Potion>> key = potionType.flatMap(Holder::unwrapKey);
                if (key.isPresent()) {
                    return key.get().location().getNamespace();
                }
            } else if (item instanceof SpawnEggItem spawnEggItem) {
                Optional<ResourceKey<EntityType<?>>> key = BuiltInRegistries.ENTITY_TYPE.getResourceKey(spawnEggItem.getType(itemStack));
                if (key.isPresent()) {
                    return key.get().location().getNamespace();
                }
            }
        }
        return modId;
    }

    public static boolean onFarmlandTrample(Level level, BlockPos pos, BlockState state, float fallDistance, Entity entity) {
        if (entity.canTrample(state, pos, fallDistance)) {
            BlockEvent.FarmlandTrampleEvent event = new BlockEvent.FarmlandTrampleEvent(level, pos, state, fallDistance, entity);
            NeoForge.EVENT_BUS.post(event);
            return !event.isCanceled();
        }
        return false;
    }

    public static int onNoteChange(Level level, BlockPos pos, BlockState state, int old, int _new) {
        NoteBlockEvent.Change event = new NoteBlockEvent.Change(level, pos, state, old, _new);
        if (NeoForge.EVENT_BUS.post(event).isCanceled())
            return -1;
        return event.getVanillaNoteId();
    }

    @Deprecated(forRemoval = true, since = "1.20.1") // Tags use a codec now This was never used in 1.20
    public static <T> void deserializeTagAdditions(List<TagEntry> list, JsonObject json, List<TagEntry> allList) {}

    public static final int VANILLA_SERIALIZER_LIMIT = 256;

    @Nullable
    public static EntityDataSerializer<?> getSerializer(int id, CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> vanilla) {
        EntityDataSerializer<?> serializer = vanilla.byId(id);
        if (serializer == null) {
            return NeoForgeRegistries.ENTITY_DATA_SERIALIZERS.byId(id - VANILLA_SERIALIZER_LIMIT);
        }
        return serializer;
    }

    public static int getSerializerId(EntityDataSerializer<?> serializer, CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> vanilla) {
        int id = vanilla.getId(serializer);
        if (id < 0) {
            id = NeoForgeRegistries.ENTITY_DATA_SERIALIZERS.getId(serializer);
            if (id >= 0) {
                return id + VANILLA_SERIALIZER_LIMIT;
            }
        }
        return id;
    }

    public static boolean canEntityDestroy(Level level, BlockPos pos, LivingEntity entity) {
        if (!level.isLoaded(pos))
            return false;
        BlockState state = level.getBlockState(pos);
        return CommonHooks.getMobGriefingEvent(level, entity) && state.canEntityDestroy(level, pos, entity) && CommonHooks.onEntityDestroyBlock(entity, pos, state);
    }

    /**
     * Gets the burn time of this item stack.
     * 
     * @deprecated Use {@link IItemStackExtension#getBurnTime(RecipeType)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.20.5")
    public static int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return stack.getBurnTime(recipeType);
    }

    /**
     * All loot table drops should be passed to this function so that mod added effects (e.g. smelting enchantments) can be processed.
     *
     * @param list    The loot generated
     * @param context The loot context that generated that loot
     * @return The modified list
     *
     * @deprecated Use {@link #modifyLoot(ResourceLocation, ObjectArrayList, LootContext)} instead.
     *
     * @implNote This method will use the {@linkplain LootTableIdCondition#UNKNOWN_LOOT_TABLE unknown loot table marker} when redirecting.
     */
    @Deprecated
    public static List<ItemStack> modifyLoot(List<ItemStack> list, LootContext context) {
        return modifyLoot(LootTableIdCondition.UNKNOWN_LOOT_TABLE, ObjectArrayList.wrap((ItemStack[]) list.toArray()), context);
    }

    /**
     * Handles the modification of loot table drops via the registered Global Loot Modifiers, so that custom effects can be processed.
     *
     * <p>
     * All loot-table generated loot should be passed to this function.
     * </p>
     *
     * @param lootTableId   The ID of the loot table currently being queried
     * @param generatedLoot The loot generated by the loot table
     * @param context       The loot context that generated the loot, unmodified
     * @return The modified list of drops
     *
     * @apiNote The given context will be modified by this method to also store the ID of the loot table being queried.
     */
    public static ObjectArrayList<ItemStack> modifyLoot(ResourceLocation lootTableId, ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        context.setQueriedLootTableId(lootTableId); // In case the ID was set via copy constructor, this will be ignored: intended
        LootModifierManager man = NeoForgeEventHandler.getLootModifierManager();
        for (IGlobalLootModifier mod : man.getAllLootMods()) {
            generatedLoot = mod.apply(generatedLoot, context);
        }
        return generatedLoot;
    }

    public static List<String> getModDataPacks() {
        List<String> modpacks = ResourcePackLoader.getDataPackNames();
        if (modpacks.isEmpty())
            throw new IllegalStateException("Attempted to retrieve mod packs before they were loaded in!");
        return modpacks;
    }

    public static List<String> getModDataPacksWithVanilla() {
        List<String> modpacks = getModDataPacks();
        modpacks.add("vanilla");
        return modpacks;
    }

    private static final Set<String> VANILLA_DIMS = Sets.newHashSet("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
    private static final String DIMENSIONS_KEY = "dimensions";
    private static final String SEED_KEY = "seed";

    private static final Map<EntityType<? extends LivingEntity>, AttributeSupplier> FORGE_ATTRIBUTES = new HashMap<>();

    /** FOR INTERNAL USE ONLY, DO NOT CALL DIRECTLY */
    @Deprecated
    public static Map<EntityType<? extends LivingEntity>, AttributeSupplier> getAttributesView() {
        return Collections.unmodifiableMap(FORGE_ATTRIBUTES);
    }

    /** FOR INTERNAL USE ONLY, DO NOT CALL DIRECTLY */
    @Deprecated
    public static void modifyAttributes() {
        ModLoader.postEvent(new EntityAttributeCreationEvent(FORGE_ATTRIBUTES));
        Map<EntityType<? extends LivingEntity>, AttributeSupplier.Builder> finalMap = new HashMap<>();
        ModLoader.postEvent(new EntityAttributeModificationEvent(finalMap));

        finalMap.forEach((k, v) -> {
            AttributeSupplier supplier = DefaultAttributes.getSupplier(k);
            AttributeSupplier.Builder newBuilder = supplier != null ? new AttributeSupplier.Builder(supplier) : new AttributeSupplier.Builder();
            newBuilder.combine(v);
            FORGE_ATTRIBUTES.put(k, newBuilder.build());
        });
    }

    public static void onEntityEnterSection(Entity entity, long packedOldPos, long packedNewPos) {
        NeoForge.EVENT_BUS.post(new EntityEvent.EnteringSection(entity, packedOldPos, packedNewPos));
    }

    public static ShieldBlockEvent onShieldBlock(LivingEntity blocker, DamageSource source, float blocked) {
        ShieldBlockEvent e = new ShieldBlockEvent(blocker, source, blocked);
        NeoForge.EVENT_BUS.post(e);
        return e;
    }

    public static LivingSwapItemsEvent.Hands onLivingSwapHandItems(LivingEntity livingEntity) {
        LivingSwapItemsEvent.Hands event = new LivingSwapItemsEvent.Hands(livingEntity);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    @ApiStatus.Internal
    public static void writeAdditionalLevelSaveData(WorldData worldData, CompoundTag levelTag) {
        CompoundTag fmlData = new CompoundTag();
        ListTag modList = new ListTag();
        ModList.get().getMods().forEach(mi -> {
            final CompoundTag mod = new CompoundTag();
            mod.putString("ModId", mi.getModId());
            mod.putString("ModVersion", MavenVersionStringHelper.artifactVersionToString(mi.getVersion()));
            modList.add(mod);
        });
        fmlData.put("LoadingModList", modList);

        LOGGER.debug(WORLDPERSISTENCE, "Gathered mod list to write to world save {}", worldData.getLevelName());
        levelTag.put("fml", fmlData);
    }

    /**
     * @param rootTag        Level data file contents.
     * @param levelDirectory Level currently being loaded.
     */
    @ApiStatus.Internal
    public static void readAdditionalLevelSaveData(CompoundTag rootTag, LevelStorageSource.LevelDirectory levelDirectory) {
        CompoundTag tag = rootTag.getCompound("fml");
        if (tag.contains("LoadingModList")) {
            ListTag modList = tag.getList("LoadingModList", net.minecraft.nbt.Tag.TAG_COMPOUND);
            Map<String, ArtifactVersion> mismatchedVersions = new HashMap<>(modList.size());
            Map<String, ArtifactVersion> missingVersions = new HashMap<>(modList.size());
            for (int i = 0; i < modList.size(); i++) {
                CompoundTag mod = modList.getCompound(i);
                String modId = mod.getString("ModId");
                if (Objects.equals("minecraft", modId)) {
                    continue;
                }

                String modVersion = mod.getString("ModVersion");
                final var previousVersion = new DefaultArtifactVersion(modVersion);
                ModList.get().getModContainerById(modId).ifPresentOrElse(container -> {
                    final var loadingVersion = container.getModInfo().getVersion();
                    if (!loadingVersion.equals(previousVersion)) {
                        // Enqueue mismatched versions for bulk event
                        mismatchedVersions.put(modId, previousVersion);
                    }
                }, () -> missingVersions.put(modId, previousVersion));
            }

            final var mismatchEvent = new ModMismatchEvent(levelDirectory, mismatchedVersions, missingVersions);
            ModLoader.postEvent(mismatchEvent);

            StringBuilder resolved = new StringBuilder("The following mods have version differences that were marked resolved:");
            StringBuilder unresolved = new StringBuilder("The following mods have version differences that were not resolved:");

            // For mods that were marked resolved, log the version resolution and the mod that resolved the mismatch
            mismatchEvent.getResolved().forEachOrdered((res) -> {
                final var modid = res.modid();
                final var diff = res.versionDifference();
                if (res.wasSelfResolved()) {
                    resolved.append(System.lineSeparator())
                            .append(diff.isMissing()
                                    ? "%s (version %s -> MISSING, self-resolved)".formatted(modid, diff.oldVersion())
                                    : "%s (version %s -> %s, self-resolved)".formatted(modid, diff.oldVersion(), diff.newVersion()));
                } else {
                    final var resolver = res.resolver().getModId();
                    resolved.append(System.lineSeparator())
                            .append(diff.isMissing()
                                    ? "%s (version %s -> MISSING, resolved by %s)".formatted(modid, diff.oldVersion(), resolver)
                                    : "%s (version %s -> %s, resolved by %s)".formatted(modid, diff.oldVersion(), diff.newVersion(), resolver));
                }
            });

            // For mods that did not specify handling, show a warning to users that errors may occur
            mismatchEvent.getUnresolved().forEachOrdered((unres) -> {
                final var modid = unres.modid();
                final var diff = unres.versionDifference();
                unresolved.append(System.lineSeparator())
                        .append(diff.isMissing()
                                ? "%s (version %s -> MISSING)".formatted(modid, diff.oldVersion())
                                : "%s (version %s -> %s)".formatted(modid, diff.oldVersion(), diff.newVersion()));
            });

            if (mismatchEvent.anyResolved()) {
                resolved.append(System.lineSeparator()).append("Things may not work well.");
                LOGGER.debug(WORLDPERSISTENCE, resolved.toString());
            }

            if (mismatchEvent.anyUnresolved()) {
                unresolved.append(System.lineSeparator()).append("Things may not work well.");
                LOGGER.warn(WORLDPERSISTENCE, unresolved.toString());
            }
        }
    }

    public static String encodeLifecycle(Lifecycle lifecycle) {
        if (lifecycle == Lifecycle.stable())
            return "stable";
        if (lifecycle == Lifecycle.experimental())
            return "experimental";
        if (lifecycle instanceof Lifecycle.Deprecated dep)
            return "deprecated=" + dep.since();
        throw new IllegalArgumentException("Unknown lifecycle.");
    }

    public static Lifecycle parseLifecycle(String lifecycle) {
        if (lifecycle.equals("stable"))
            return Lifecycle.stable();
        if (lifecycle.equals("experimental"))
            return Lifecycle.experimental();
        if (lifecycle.startsWith("deprecated="))
            return Lifecycle.deprecated(Integer.parseInt(lifecycle.substring(lifecycle.indexOf('=') + 1)));
        throw new IllegalArgumentException("Unknown lifecycle.");
    }

    public static void saveMobEffect(CompoundTag nbt, String key, MobEffect effect) {
        var registryName = BuiltInRegistries.MOB_EFFECT.getKey(effect);
        if (registryName != null) {
            nbt.putString(key, registryName.toString());
        }
    }

    @Nullable
    public static MobEffect loadMobEffect(CompoundTag nbt, String key, @Nullable MobEffect fallback) {
        var registryName = nbt.getString(key);
        if (Strings.isNullOrEmpty(registryName)) {
            return fallback;
        }
        try {
            return BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(registryName));
        } catch (ResourceLocationException e) {
            return fallback;
        }
    }

    public static boolean shouldSuppressEnderManAnger(EnderMan enderMan, Player player, ItemStack mask) {
        return mask.isEnderMask(player, enderMan) || NeoForge.EVENT_BUS.post(new EnderManAngerEvent(enderMan, player)).isCanceled();
    }

    private static final Lazy<Map<String, StructuresBecomeConfiguredFix.Conversion>> FORGE_CONVERSION_MAP = Lazy.concurrentOf(() -> {
        Map<String, StructuresBecomeConfiguredFix.Conversion> map = new HashMap<>();
        NeoForge.EVENT_BUS.post(new RegisterStructureConversionsEvent(map));
        return ImmutableMap.copyOf(map);
    });

    // DO NOT CALL from within RegisterStructureConversionsEvent, otherwise you'll get a deadlock
    /**
     * @hidden For internal use only.
     */
    @Nullable
    public static StructuresBecomeConfiguredFix.Conversion getStructureConversion(String originalBiome) {
        return FORGE_CONVERSION_MAP.get().get(originalBiome);
    }

    /**
     * @hidden For internal use only.
     */
    public static boolean checkStructureNamespace(String biome) {
        @Nullable
        ResourceLocation biomeLocation = ResourceLocation.tryParse(biome);
        return biomeLocation != null && !biomeLocation.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE);
    }

    /**
     * <p>
     * This method is used to prefix the path, where elements of the associated registry are stored, with their namespace, if it is not minecraft
     * </p>
     * <p>
     * This rules conflicts with equal paths out. If for example the mod {@code fancy_cheese} adds a registry named {@code cheeses}, but the mod {@code awesome_cheese} also adds a registry called {@code cheeses}, they are going to have the
     * same path {@code cheeses}, just with different namespaces. If {@code additional_cheese} wants to add additional cheese to {@code awesome_cheese}, but not {@code fancy_cheese}, it can not differentiate both. Both paths will look like
     * {@code data/additional_cheese/cheeses}.
     * </p>
     * <p>
     * The fix, which is applied here prefixes the path of the registry with the namespace, so {@code fancy_cheese}'s registry stores its elements in {@code data/<namespace>/fancy_cheese/cheeses} and {@code awesome_cheese}'s registry stores
     * its elements in {@code data/namespace/awesome_cheese/cheeses}
     * </p>
     *
     * @param registryKey key of the registry
     * @return path of the registry key. Prefixed with the namespace if it is not "minecraft"
     */
    public static String prefixNamespace(ResourceLocation registryKey) {
        return registryKey.getNamespace().equals("minecraft") ? registryKey.getPath() : registryKey.getNamespace() + "/" + registryKey.getPath();
    }

    public static boolean canUseEntitySelectors(SharedSuggestionProvider provider) {
        if (provider.hasPermission(Commands.LEVEL_GAMEMASTERS)) {
            return true;
        } else if (provider instanceof CommandSourceStack source && source.source instanceof ServerPlayer player) {
            return PermissionAPI.getPermission(player, NeoForgeMod.USE_SELECTORS_PERMISSION);
        }
        return false;
    }

    @ApiStatus.Internal
    public static <T> HolderLookup.RegistryLookup<T> wrapRegistryLookup(final HolderLookup.RegistryLookup<T> lookup) {
        return new HolderLookup.RegistryLookup.Delegate<>() {
            @Override
            public RegistryLookup<T> parent() {
                return lookup;
            }

            @Override
            public Stream<HolderSet.Named<T>> listTags() {
                return Stream.empty();
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> key) {
                return Optional.of(HolderSet.emptyNamed(lookup, key));
            }
        };
    }

    /**
     * Handles living entities being underwater. This fires the {@link LivingBreatheEvent} and if the entity's air supply is less than or equal to zero also the {@link LivingDrownEvent}. Additionally, when the entity is underwater it will
     * dismount if {@link IEntityExtension#canBeRiddenUnderFluidType(FluidType, Entity)} returns false.
     *
     * @param entity           The living entity which is currently updated
     * @param consumeAirAmount The amount of air to consume when the entity is unable to breathe
     * @param refillAirAmount  The amount of air to refill when the entity is able to breathe
     * @implNote This method needs to closely replicate the logic found right after the call site in {@link LivingEntity#baseTick()} as it overrides it.
     */
    public static void onLivingBreathe(LivingEntity entity, int consumeAirAmount, int refillAirAmount) {
        // Check things that vanilla considers to be air - these will cause the air supply to be increased.
        boolean isAir = entity.getEyeInFluidType().isAir() || entity.level().getBlockState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ())).is(Blocks.BUBBLE_COLUMN);
        boolean canBreathe = isAir;
        // The following effects cause the entity to not drown, but do not cause the air supply to be increased.
        if (!isAir && (MobEffectUtil.hasWaterBreathing(entity) || !entity.canDrownInFluidType(entity.getEyeInFluidType()) || (entity instanceof Player player && player.getAbilities().invulnerable))) {
            canBreathe = true;
            refillAirAmount = 0;
        }
        LivingBreatheEvent breatheEvent = new LivingBreatheEvent(entity, canBreathe, consumeAirAmount, refillAirAmount);
        NeoForge.EVENT_BUS.post(breatheEvent);
        if (breatheEvent.canBreathe()) {
            entity.setAirSupply(Math.min(entity.getAirSupply() + breatheEvent.getRefillAirAmount(), entity.getMaxAirSupply()));
        } else {
            entity.setAirSupply(entity.getAirSupply() - breatheEvent.getConsumeAirAmount());
        }

        if (entity.getAirSupply() <= 0) {
            LivingDrownEvent drownEvent = new LivingDrownEvent(entity);
            if (!NeoForge.EVENT_BUS.post(drownEvent).isCanceled() && drownEvent.isDrowning()) {
                entity.setAirSupply(0);
                Vec3 vec3 = entity.getDeltaMovement();

                for (int i = 0; i < drownEvent.getBubbleCount(); ++i) {
                    double d2 = entity.getRandom().nextDouble() - entity.getRandom().nextDouble();
                    double d3 = entity.getRandom().nextDouble() - entity.getRandom().nextDouble();
                    double d4 = entity.getRandom().nextDouble() - entity.getRandom().nextDouble();
                    entity.level().addParticle(ParticleTypes.BUBBLE, entity.getX() + d2, entity.getY() + d3, entity.getZ() + d4, vec3.x, vec3.y, vec3.z);
                }

                if (drownEvent.getDamageAmount() > 0) entity.hurt(entity.damageSources().drown(), drownEvent.getDamageAmount());
            }
        }

        if (!isAir && !entity.level().isClientSide && entity.isPassenger() && entity.getVehicle() != null && !entity.getVehicle().canBeRiddenUnderFluidType(entity.getEyeInFluidType(), entity)) {
            entity.stopRiding();
        }
    }

    private static final Set<Class<?>> checkedComponentClasses = ConcurrentHashMap.newKeySet();

    /**
     * Marks a class as being safe to use as a {@link DataComponents data component}.
     * Keep in mind that data components are compared with {@link Object#equals(Object)}
     * and hashed with {@link Object#hashCode()}.
     * <b>They must also be immutable.</b>
     *
     * <p>Only call this method if the default implementations of {@link Object#equals(Object)}
     * and {@link Object#hashCode()} are suitable for this class,
     * and if instances of this class are immutable.
     * Typically, this is only the case for singletons such as {@link Block} instances.
     */
    public static void markComponentClassAsValid(Class<?> clazz) {
        if (clazz.isRecord() || clazz.isEnum()) {
            throw new IllegalArgumentException("Records and enums are always valid components");
        }

        if (overridesEqualsAndHashCode(clazz)) {
            throw new IllegalArgumentException("Class " + clazz + " already overrides equals and hashCode");
        }

        checkedComponentClasses.add(clazz);
    }

    static {
        // Mark common singletons as valid
        markComponentClassAsValid(BlockState.class);
        markComponentClassAsValid(FluidState.class);
        // Block, Fluid, Item, etc. are handled via the registry check further down
    }

    /**
     * Checks that all data components override equals and hashCode.
     */
    @ApiStatus.Internal
    public static void validateComponent(@Nullable Object dataComponent) {
        if (!SharedConstants.IS_RUNNING_IN_IDE || dataComponent == null) {
            return;
        }

        Class<?> clazz = dataComponent.getClass();
        if (!checkedComponentClasses.contains(clazz)) {
            if (clazz.isRecord() || clazz.isEnum()) {
                checkedComponentClasses.add(clazz);
                return; // records and enums are always ok
            }

            if (overridesEqualsAndHashCode(clazz)) {
                checkedComponentClasses.add(clazz);
                return;
            }

            // By far the slowest check: Is this a registry object?
            // If it is, we assume it must be usable as a singleton...
            if (isPotentialRegistryObject(dataComponent)) {
                checkedComponentClasses.add(clazz);
                return;
            }

            throw new IllegalArgumentException("Data components must implement equals and hashCode. Keep in mind they must also be immutable. Problematic class: " + clazz);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean isPotentialRegistryObject(Object value) {
        for (Registry registry : BuiltInRegistries.REGISTRY) {
            if (registry.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean overridesEqualsAndHashCode(Class<?> clazz) {
        try {
            Method equals = clazz.getMethod("equals", Object.class);
            Method hashCode = clazz.getMethod("hashCode");
            return equals.getDeclaringClass() != Object.class && hashCode.getDeclaringClass() != Object.class;
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Failed to check for component equals and hashCode", exception);
        }
    }

    public static boolean onMultiBlockPlace(@Nullable Entity entity, List<BlockSnapshot> blockSnapshots, Direction direction) {
        BlockSnapshot snap = blockSnapshots.get(0);
        BlockState placedAgainst = snap.getLevel().getBlockState(snap.getPos().relative(direction.getOpposite()));
        BlockEvent.EntityMultiPlaceEvent event = new BlockEvent.EntityMultiPlaceEvent(blockSnapshots, placedAgainst, entity);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onBlockPlace(@Nullable Entity entity, BlockSnapshot blockSnapshot, Direction direction) {
        BlockState placedAgainst = blockSnapshot.getLevel().getBlockState(blockSnapshot.getPos().relative(direction.getOpposite()));
        BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(blockSnapshot, placedAgainst, entity);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static BlockEvent.NeighborNotifyEvent onNeighborNotify(Level level, BlockPos pos, BlockState state, EnumSet<Direction> notifiedSides, boolean forceRedstoneUpdate) {
        BlockEvent.NeighborNotifyEvent event = new BlockEvent.NeighborNotifyEvent(level, pos, state, notifiedSides, forceRedstoneUpdate);
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
     * @see MobSpawnEvent.SpawnPlacementCheck
     */
    @ApiStatus.Internal
    public static boolean checkSpawnPlacements(EntityType<?> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, boolean defaultResult) {
        var event = new MobSpawnEvent.SpawnPlacementCheck(entityType, level, spawnType, pos, random, defaultResult);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult() == Event.Result.DEFAULT ? defaultResult : event.getResult() == Event.Result.ALLOW;
    }

    /**
     * Checks if the current position of the passed mob is valid for spawning, by firing {@link MobSpawnEvent.PositionCheck}.<br>
     * The default check is to perform the logical and of {@link Mob#checkSpawnRules} and {@link Mob#checkSpawnObstruction}.<br>
     *
     * @param mob       The mob being spawned.
     * @param level     The level the mob will be added to, if successful.
     * @param spawnType The spawn type of the spawn.
     * @return True, if the position is valid, as determined by the contract of {@link MobSpawnEvent.PositionCheck}.
     * @see MobSpawnEvent.PositionCheck
     */
    public static boolean checkSpawnPosition(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType) {
        var event = new MobSpawnEvent.PositionCheck(mob, level, spawnType, null);
        NeoForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DEFAULT) {
            return mob.checkSpawnRules(level, spawnType) && mob.checkSpawnObstruction(level);
        }
        return event.getResult() == Event.Result.ALLOW;
    }

    /**
     * Specialized variant of {@link #checkSpawnPosition} for spawners, as they have slightly different checks.
     *
     * @see #CheckSpawnPosition
     * @implNote See in-line comments about custom spawn rules.
     */
    public static boolean checkSpawnPositionSpawner(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, SpawnData spawnData, BaseSpawner spawner) {
        var event = new MobSpawnEvent.PositionCheck(mob, level, spawnType, null);
        NeoForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DEFAULT) {
            // Spawners do not evaluate Mob#checkSpawnRules if any custom rules are present. This is despite the fact that these two methods do not check the same things.
            return (spawnData.getCustomSpawnRules().isPresent() || mob.checkSpawnRules(level, spawnType)) && mob.checkSpawnObstruction(level);
        }
        return event.getResult() == Event.Result.ALLOW;
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
     * zombie.finalizeSpawn(level, difficulty, spawnType, spawnData);
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
     * EventHook.onFinalizeSpawn(zombie, level, difficulty, spawnType, spawnData);
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
     * @see Mob#finalizeSpawn(ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData)
     * @apiNote Callers do not need to check if the entity's spawn was cancelled, as the spawn will be blocked by Forge.
     * @implNote Changes to the signature of this method must be reflected in the method redirector coremod.
     */
    @Nullable
    @SuppressWarnings("deprecation") // Call to deprecated Mob#finalizeSpawn is expected.
    public static SpawnGroupData onFinalizeSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        var event = new MobSpawnEvent.FinalizeSpawn(mob, level, mob.getX(), mob.getY(), mob.getZ(), difficulty, spawnType, spawnData, null);
        boolean cancel = NeoForge.EVENT_BUS.post(event).isCanceled();

        if (!cancel) {
            mob.finalizeSpawn(level, event.getDifficulty(), event.getSpawnType(), event.getSpawnData());
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
    public static MobSpawnEvent.FinalizeSpawn onFinalizeSpawnSpawner(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, @Nullable SpawnGroupData spawnData, BaseSpawner spawner) {
        var event = new MobSpawnEvent.FinalizeSpawn(mob, level, mob.getX(), mob.getY(), mob.getZ(), difficulty, MobSpawnType.SPAWNER, spawnData, spawner);
        boolean cancel = NeoForge.EVENT_BUS.post(event).isCanceled();
        return cancel ? null : event;
    }

    public static PlayerSpawnPhantomsEvent onPhantomSpawn(ServerPlayer player, int phantomsToSpawn) {
        var event = new PlayerSpawnPhantomsEvent(player, phantomsToSpawn);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static Event.Result canEntityDespawn(Mob entity, ServerLevelAccessor level) {
        MobSpawnEvent.AllowDespawn event = new MobSpawnEvent.AllowDespawn(entity, level);
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
        return maxCanSpawnEvent.getResult() == Event.Result.ALLOW ? maxCanSpawnEvent.getMaxPackSize() : entity.getMaxSpawnClusterSize();
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

    public static ItemTooltipEvent onItemTooltip(ItemStack itemStack, @Nullable Player entityPlayer, List<Component> list, TooltipFlag flags, Item.TooltipContext context) {
        ItemTooltipEvent event = new ItemTooltipEvent(itemStack, entityPlayer, list, flags, context);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static ZombieEvent.SummonAidEvent fireZombieSummonAid(Zombie zombie, Level level, int x, int y, int z, LivingEntity attacker, double summonChance) {
        ZombieEvent.SummonAidEvent summonEvent = new ZombieEvent.SummonAidEvent(zombie, level, x, y, z, attacker, summonChance);
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
        NeoForge.EVENT_BUS.post(new PlayerEvent.LoadFromFile(player, playerFileData.getPlayerDir(), uuidString));
    }

    @Nullable
    public static BlockState onToolUse(BlockState originalState, UseOnContext context, ToolAction toolAction, boolean simulate) {
        BlockEvent.BlockToolModificationEvent event = new BlockEvent.BlockToolModificationEvent(originalState, context, toolAction, simulate);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? null : event.getFinalState();
    }

    public static int onApplyBonemeal(Player player, Level level, BlockPos pos, BlockState state, ItemStack stack) {
        BonemealEvent event = new BonemealEvent(player, level, pos, state, stack);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return -1;
        if (event.getResult() == Event.Result.ALLOW) {
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

        if (event.getResult() == Event.Result.ALLOW) {
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
        return event.getResult() == Event.Result.ALLOW ? 1 : 0;
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

        Event.Result canContinueSleep = evt.getResult();
        if (canContinueSleep == Event.Result.DEFAULT) {
            return player.getSleepingPos().map(pos -> {
                BlockState state = player.level().getBlockState(pos);
                return state.getBlock().isBed(state, player.level(), pos, player);
            }).orElse(false);
        } else
            return canContinueSleep == Event.Result.ALLOW;
    }

    public static boolean fireSleepingTimeCheck(Player player, Optional<BlockPos> sleepingLocation) {
        SleepingTimeCheckEvent evt = new SleepingTimeCheckEvent(player, sleepingLocation);
        NeoForge.EVENT_BUS.post(evt);

        Event.Result canContinueSleep = evt.getResult();
        if (canContinueSleep == Event.Result.DEFAULT)
            return !player.level().isDay();
        else
            return canContinueSleep == Event.Result.ALLOW;
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
        BlockEvent.CreateFluidSourceEvent evt = new BlockEvent.CreateFluidSourceEvent(level, pos, state);
        NeoForge.EVENT_BUS.post(evt);

        Event.Result result = evt.getResult();
        return result == Event.Result.DEFAULT ? def : result == Event.Result.ALLOW;
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

        Event.Result result = event.getResult();
        return result == Event.Result.DEFAULT ? level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) : result == Event.Result.ALLOW;
    }

    public static SaplingGrowTreeEvent blockGrowFeature(LevelAccessor level, RandomSource randomSource, BlockPos pos, @Nullable Holder<ConfiguredFeature<?, ?>> holder) {
        SaplingGrowTreeEvent event = new SaplingGrowTreeEvent(level, randomSource, pos, holder);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    /**
     * Fires the {@link AlterGroundEvent} and retrieves the resulting {@link AlterGroundEvent.StateProvider}.
     *
     * @param ctx       The tree decoration context for the current alteration.
     * @param positions The list of positions that are considered roots.
     * @param provider  The original {@link BlockStateProvider} from the {@link AlterGroundDecorator}.
     * @return The (possibly event-modified) {@link AlterGroundEvent.StateProvider} to be used for ground alteration.
     * @apiNote This method is called off-thread during world generation.
     */
    public static AlterGroundEvent.StateProvider alterGround(TreeDecorator.Context ctx, List<BlockPos> positions, AlterGroundEvent.StateProvider provider) {
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

    /**
     * Fires {@link EntityTickEvent.Pre}. Called from the head of {@link LivingEntity#tick()}.
     *
     * @param entity The entity being ticked
     * @return The event
     */
    public static EntityTickEvent.Pre fireEntityTickPre(Entity entity) {
        return NeoForge.EVENT_BUS.post(new EntityTickEvent.Pre(entity));
    }

    /**
     * Fires {@link EntityTickEvent.Post}. Called from the tail of {@link LivingEntity#tick()}.
     *
     * @param entity The entity being ticked
     */
    public static void fireEntityTickPost(Entity entity) {
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(entity));
    }

    /**
     * Fires {@link PlayerTickEvent.Pre}. Called from the head of {@link Player#tick()}.
     *
     * @param player The player being ticked
     */
    public static void firePlayerTickPre(Player player) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(player));
    }

    /**
     * Fires {@link PlayerTickEvent.Post}. Called from the tail of {@link Player#tick()}.
     *
     * @param player The player being ticked
     */
    public static void firePlayerTickPost(Player player) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
    }

    /**
     * Fires {@link LevelTickEvent.Pre}. Called from {@link Minecraft#tick()} and {@link MinecraftServer#tickChildren(BooleanSupplier)} just before the try block for level tick is entered.
     *
     * @param level    The level being ticked
     * @param haveTime The time supplier, indicating if there is remaining time to do work in the current tick.
     */
    public static void fireLevelTickPre(Level level, BooleanSupplier haveTime) {
        NeoForge.EVENT_BUS.post(new LevelTickEvent.Pre(haveTime, level));
    }

    /**
     * Fires {@link LevelTickEvent.Post}. Called from {@link Minecraft#tick()} and {@link MinecraftServer#tickChildren(BooleanSupplier)} just after the try block for level tick is exited.
     *
     * @param level    The level being ticked
     * @param haveTime The time supplier, indicating if there is remaining time to do work in the current tick.
     */
    public static void fireLevelTickPost(Level level, BooleanSupplier haveTime) {
        NeoForge.EVENT_BUS.post(new LevelTickEvent.Post(haveTime, level));
    }

    /**
     * Fires {@link ServerTickEvent.Pre}. Called from the head of {@link MinecraftServer#tickServer(BooleanSupplier)}.
     *
     * @param haveTime The time supplier, indicating if there is remaining time to do work in the current tick.
     * @param server   The current server
     */
    public static void fireServerTickPre(BooleanSupplier haveTime, MinecraftServer server) {
        NeoForge.EVENT_BUS.post(new ServerTickEvent.Pre(haveTime, server));
    }

    /**
     * Fires {@link ServerTickEvent.Post}. Called from the tail of {@link MinecraftServer#tickServer(BooleanSupplier)}.
     *
     * @param haveTime The time supplier, indicating if there is remaining time to do work in the current tick.
     * @param server   The current server
     */
    public static void fireServerTickPost(BooleanSupplier haveTime, MinecraftServer server) {
        NeoForge.EVENT_BUS.post(new ServerTickEvent.Post(haveTime, server));
    }

    private static final WeightedRandomList<MobSpawnSettings.SpawnerData> NO_SPAWNS = WeightedRandomList.create();

    public static WeightedRandomList<MobSpawnSettings.SpawnerData> getPotentialSpawns(LevelAccessor level, MobCategory category, BlockPos pos, WeightedRandomList<MobSpawnSettings.SpawnerData> oldList) {
        LevelEvent.PotentialSpawns event = new LevelEvent.PotentialSpawns(level, category, pos, oldList);
        if (NeoForge.EVENT_BUS.post(event).isCanceled())
            return NO_SPAWNS;
        else if (event.getSpawnerDataList() == oldList.unwrap())
            return oldList;
        return WeightedRandomList.create(event.getSpawnerDataList());
    }

    public static StatAwardEvent onStatAward(Player player, Stat<?> stat, int value) {
        StatAwardEvent event = new StatAwardEvent(player, stat, value);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    @ApiStatus.Internal
    public static void onAdvancementEarnedEvent(Player player, AdvancementHolder earned) {
        NeoForge.EVENT_BUS.post(new AdvancementEvent.AdvancementEarnEvent(player, earned));
    }

    @ApiStatus.Internal
    public static void onAdvancementProgressedEvent(Player player, AdvancementHolder progressed, AdvancementProgress advancementProgress, String criterion, AdvancementEvent.AdvancementProgressEvent.ProgressType progressType) {
        NeoForge.EVENT_BUS.post(new AdvancementEvent.AdvancementProgressEvent(player, progressed, advancementProgress, criterion, progressType));
    }

    public static boolean onEffectRemoved(LivingEntity entity, Holder<MobEffect> effect, @Nullable EffectCure cure) {
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
        var enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(ench, level);
        var event = new GetEnchantmentLevelEvent(stack, enchantments, ench);
        NeoForge.EVENT_BUS.post(event);
        return enchantments.getLevel(ench);
    }

    /**
     * Fires {@link GetEnchantmentLevelEvent} and for all enchantments, returning the (possibly event-modified) enchantment map.
     *
     * @param enchantments The original enchantment map as provided by the Item.
     * @param stack        The stack being queried against.
     * @return The new enchantment map.
     */
    public static ItemEnchantments getEnchantmentLevel(ItemEnchantments enchantments, ItemStack stack) {
        var mutableEnchantments = new ItemEnchantments.Mutable(enchantments);
        var event = new GetEnchantmentLevelEvent(stack, mutableEnchantments, null);
        NeoForge.EVENT_BUS.post(event);
        return mutableEnchantments.toImmutable();
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

        ModLoader.postEvent(new BuildCreativeModeTabContentsEvent(tab, tabKey, params, entries));

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
