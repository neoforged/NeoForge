package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.boat.CraftAcaciaBoat;
import org.bukkit.craftbukkit.entity.boat.CraftAcaciaChestBoat;
import org.bukkit.craftbukkit.entity.boat.CraftBambooChestRaft;
import org.bukkit.craftbukkit.entity.boat.CraftBambooRaft;
import org.bukkit.craftbukkit.entity.boat.CraftBirchBoat;
import org.bukkit.craftbukkit.entity.boat.CraftBirchChestBoat;
import org.bukkit.craftbukkit.entity.boat.CraftCherryBoat;
import org.bukkit.craftbukkit.entity.boat.CraftCherryChestBoat;
import org.bukkit.craftbukkit.entity.boat.CraftDarkOakBoat;
import org.bukkit.craftbukkit.entity.boat.CraftDarkOakChestBoat;
import org.bukkit.craftbukkit.entity.boat.CraftJungleBoat;
import org.bukkit.craftbukkit.entity.boat.CraftJungleChestBoat;
import org.bukkit.craftbukkit.entity.boat.CraftMangroveBoat;
import org.bukkit.craftbukkit.entity.boat.CraftMangroveChestBoat;
import org.bukkit.craftbukkit.entity.boat.CraftOakBoat;
import org.bukkit.craftbukkit.entity.boat.CraftOakChestBoat;
import org.bukkit.craftbukkit.entity.boat.CraftPaleOakBoat;
import org.bukkit.craftbukkit.entity.boat.CraftPaleOakChestBoat;
import org.bukkit.craftbukkit.entity.boat.CraftSpruceBoat;
import org.bukkit.craftbukkit.entity.boat.CraftSpruceChestBoat;
import org.bukkit.entity.Allay;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Armadillo;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Bogged;
import org.bukkit.entity.Breeze;
import org.bukkit.entity.BreezeWindCharge;
import org.bukkit.entity.Camel;
import org.bukkit.entity.CamelHusk;
import org.bukkit.entity.Cat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cod;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creaking;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Egg;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.Llama;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Mule;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Nautilus;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.OminousItemSpawner;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parched;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Slime;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Sniffer;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Spider;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Strider;
import org.bukkit.entity.SulfurCube;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tadpole;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.TraderLlama;
import org.bukkit.entity.Trident;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Warden;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.ZombieNautilus;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.boat.AcaciaBoat;
import org.bukkit.entity.boat.AcaciaChestBoat;
import org.bukkit.entity.boat.BambooChestRaft;
import org.bukkit.entity.boat.BambooRaft;
import org.bukkit.entity.boat.BirchBoat;
import org.bukkit.entity.boat.BirchChestBoat;
import org.bukkit.entity.boat.CherryBoat;
import org.bukkit.entity.boat.CherryChestBoat;
import org.bukkit.entity.boat.DarkOakBoat;
import org.bukkit.entity.boat.DarkOakChestBoat;
import org.bukkit.entity.boat.JungleBoat;
import org.bukkit.entity.boat.JungleChestBoat;
import org.bukkit.entity.boat.MangroveBoat;
import org.bukkit.entity.boat.MangroveChestBoat;
import org.bukkit.entity.boat.OakBoat;
import org.bukkit.entity.boat.OakChestBoat;
import org.bukkit.entity.boat.PaleOakBoat;
import org.bukkit.entity.boat.PaleOakChestBoat;
import org.bukkit.entity.boat.SpruceBoat;
import org.bukkit.entity.boat.SpruceChestBoat;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.util.Vector;

public final class CraftEntityTypes {

    public record EntityTypeData<E extends Entity, M extends net.minecraft.world.entity.Entity>(org.bukkit.entity.EntityType entityType,
                                                                                                Class<E> entityClass,
                                                                                                BiFunction<CraftServer, M, E> convertFunction,
                                                                                                Function<SpawnData, M> spawnFunction) {
    }

    public record SpawnData(WorldGenLevel world, Location location, boolean randomizeData, boolean normalWorld) {
        double x() {
            return location().getX();
        }

        double y() {
            return location().getY();
        }

        double z() {
            return location().getZ();
        }

        float yaw() {
            return location().getYaw();
        }

        float pitch() {
            return location().getPitch();
        }

        Level minecraftWorld() {
            return world().getMinecraftWorld();
        }
    }

    private static final BiConsumer<SpawnData, net.minecraft.world.entity.Entity> POS = (spawnData, entity) -> entity.setPos(spawnData.x(), spawnData.y(), spawnData.z());
    private static final BiConsumer<SpawnData, net.minecraft.world.entity.Entity> ABS_MOVE = (spawnData, entity) -> {
        entity.absSnapTo(spawnData.x(), spawnData.y(), spawnData.z(), spawnData.yaw(), spawnData.pitch());
        entity.setYHeadRot(spawnData.yaw()); // SPIGOT-3587
    };
    private static final BiConsumer<SpawnData, net.minecraft.world.entity.Entity> MOVE = (spawnData, entity) -> entity.snapTo(spawnData.x(), spawnData.y(), spawnData.z(), spawnData.yaw(), spawnData.pitch());
    private static final BiConsumer<SpawnData, net.minecraft.world.entity.Entity> MOVE_EMPTY_ROT = (spawnData, entity) -> entity.snapTo(spawnData.x(), spawnData.y(), spawnData.z(), 0, 0);
    private static final BiConsumer<SpawnData, AbstractHurtingProjectile> DIRECTION = (spawnData, entity) -> {
        Vector direction = spawnData.location().getDirection();
        entity.assignDirectionalMovement(new Vec3(direction.getX(), direction.getY(), direction.getZ()), 1.0);
    };
    private static final Map<Class<?>, EntityTypeData<?, ?>> CLASS_TYPE_DATA = new HashMap<>();
    private static final Map<org.bukkit.entity.EntityType, EntityTypeData<?, ?>> ENTITY_TYPE_DATA = new HashMap<>();

    static {
        // Living
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ELDER_GUARDIAN, ElderGuardian.class, CraftElderGuardian::new, createLiving(EntityTypes.ELDER_GUARDIAN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.WITHER_SKELETON, WitherSkeleton.class, CraftWitherSkeleton::new, createLiving(EntityTypes.WITHER_SKELETON)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.STRAY, Stray.class, CraftStray::new, createLiving(EntityTypes.STRAY)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BOGGED, Bogged.class, CraftBogged::new, createLiving(EntityTypes.BOGGED)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.HUSK, Husk.class, CraftHusk::new, createLiving(EntityTypes.HUSK)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ZOMBIE_VILLAGER, ZombieVillager.class, CraftVillagerZombie::new, createLiving(EntityTypes.ZOMBIE_VILLAGER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SKELETON_HORSE, SkeletonHorse.class, CraftSkeletonHorse::new, createLiving(EntityTypes.SKELETON_HORSE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ZOMBIE_HORSE, ZombieHorse.class, CraftZombieHorse::new, createLiving(EntityTypes.ZOMBIE_HORSE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ARMOR_STAND, ArmorStand.class, CraftArmorStand::new, createLiving(EntityTypes.ARMOR_STAND)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.MANNEQUIN, Mannequin.class, CraftMannequin::new, createLiving(EntityTypes.MANNEQUIN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.DONKEY, Donkey.class, CraftDonkey::new, createLiving(EntityTypes.DONKEY)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.MULE, Mule.class, CraftMule::new, createLiving(EntityTypes.MULE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.EVOKER, Evoker.class, CraftEvoker::new, createLiving(EntityTypes.EVOKER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.VEX, Vex.class, CraftVex::new, createLiving(EntityTypes.VEX)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.VINDICATOR, Vindicator.class, CraftVindicator::new, createLiving(EntityTypes.VINDICATOR)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ILLUSIONER, Illusioner.class, CraftIllusioner::new, createLiving(EntityTypes.ILLUSIONER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.CREEPER, Creeper.class, CraftCreeper::new, createLiving(EntityTypes.CREEPER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SKELETON, Skeleton.class, CraftSkeleton::new, createLiving(EntityTypes.SKELETON)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SPIDER, Spider.class, CraftSpider::new, createLiving(EntityTypes.SPIDER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.GIANT, Giant.class, CraftGiant::new, createLiving(EntityTypes.GIANT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ZOMBIE, Zombie.class, CraftZombie::new, createLiving(EntityTypes.ZOMBIE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SLIME, Slime.class, CraftSlime::new, createLiving(EntityTypes.SLIME)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.GHAST, Ghast.class, CraftGhast::new, createLiving(EntityTypes.GHAST)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.HAPPY_GHAST, HappyGhast.class, CraftHappyGhast::new, createLiving(EntityTypes.HAPPY_GHAST)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ZOMBIFIED_PIGLIN, PigZombie.class, CraftPigZombie::new, createLiving(EntityTypes.ZOMBIFIED_PIGLIN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ENDERMAN, Enderman.class, CraftEnderman::new, createLiving(EntityTypes.ENDERMAN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.CAVE_SPIDER, CaveSpider.class, CraftCaveSpider::new, createLiving(EntityTypes.CAVE_SPIDER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SILVERFISH, Silverfish.class, CraftSilverfish::new, createLiving(EntityTypes.SILVERFISH)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BLAZE, Blaze.class, CraftBlaze::new, createLiving(EntityTypes.BLAZE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.MAGMA_CUBE, MagmaCube.class, CraftMagmaCube::new, createLiving(EntityTypes.MAGMA_CUBE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SULFUR_CUBE, SulfurCube.class, CraftSulfurCube::new, createLiving(EntityTypes.SULFUR_CUBE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.WITHER, Wither.class, CraftWither::new, createLiving(EntityTypes.WITHER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BAT, Bat.class, CraftBat::new, createLiving(EntityTypes.BAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.WITCH, Witch.class, CraftWitch::new, createLiving(EntityTypes.WITCH)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ENDERMITE, Endermite.class, CraftEndermite::new, createLiving(EntityTypes.ENDERMITE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.GUARDIAN, Guardian.class, CraftGuardian::new, createLiving(EntityTypes.GUARDIAN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SHULKER, Shulker.class, CraftShulker::new, createLiving(EntityTypes.SHULKER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PIG, Pig.class, CraftPig::new, createLiving(EntityTypes.PIG)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SHEEP, Sheep.class, CraftSheep::new, createLiving(EntityTypes.SHEEP)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.COW, Cow.class, CraftCow::new, createLiving(EntityTypes.COW)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.CHICKEN, Chicken.class, CraftChicken::new, createLiving(EntityTypes.CHICKEN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SQUID, Squid.class, CraftSquid::new, createLiving(EntityTypes.SQUID)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.WOLF, Wolf.class, CraftWolf::new, createLiving(EntityTypes.WOLF)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.MOOSHROOM, MushroomCow.class, CraftMushroomCow::new, createLiving(EntityTypes.MOOSHROOM)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SNOW_GOLEM, Snowman.class, CraftSnowman::new, createLiving(EntityTypes.SNOW_GOLEM)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.OCELOT, Ocelot.class, CraftOcelot::new, createLiving(EntityTypes.OCELOT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.IRON_GOLEM, IronGolem.class, CraftIronGolem::new, createLiving(EntityTypes.IRON_GOLEM)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.COPPER_GOLEM, CopperGolem.class, CraftCopperGolem::new, createLiving(EntityTypes.COPPER_GOLEM)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.HORSE, Horse.class, CraftHorse::new, createLiving(EntityTypes.HORSE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.RABBIT, Rabbit.class, CraftRabbit::new, createLiving(EntityTypes.RABBIT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.POLAR_BEAR, PolarBear.class, CraftPolarBear::new, createLiving(EntityTypes.POLAR_BEAR)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.LLAMA, Llama.class, CraftLlama::new, createLiving(EntityTypes.LLAMA)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PARROT, Parrot.class, CraftParrot::new, createLiving(EntityTypes.PARROT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.VILLAGER, Villager.class, CraftVillager::new, createLiving(EntityTypes.VILLAGER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.TURTLE, Turtle.class, CraftTurtle::new, createLiving(EntityTypes.TURTLE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PHANTOM, Phantom.class, CraftPhantom::new, createLiving(EntityTypes.PHANTOM)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.COD, Cod.class, CraftCod::new, createLiving(EntityTypes.COD)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SALMON, Salmon.class, CraftSalmon::new, createLiving(EntityTypes.SALMON)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PUFFERFISH, PufferFish.class, CraftPufferFish::new, createLiving(EntityTypes.PUFFERFISH)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.TROPICAL_FISH, TropicalFish.class, CraftTropicalFish::new, createLiving(EntityTypes.TROPICAL_FISH)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.DROWNED, Drowned.class, CraftDrowned::new, createLiving(EntityTypes.DROWNED)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.DOLPHIN, Dolphin.class, CraftDolphin::new, createLiving(EntityTypes.DOLPHIN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.CAT, Cat.class, CraftCat::new, createLiving(EntityTypes.CAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PANDA, Panda.class, CraftPanda::new, createLiving(EntityTypes.PANDA)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PILLAGER, Pillager.class, CraftPillager::new, createLiving(EntityTypes.PILLAGER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.RAVAGER, Ravager.class, CraftRavager::new, createLiving(EntityTypes.RAVAGER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.TRADER_LLAMA, TraderLlama.class, CraftTraderLlama::new, createLiving(EntityTypes.TRADER_LLAMA)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.WANDERING_TRADER, WanderingTrader.class, CraftWanderingTrader::new, createLiving(EntityTypes.WANDERING_TRADER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.FOX, Fox.class, CraftFox::new, createLiving(EntityTypes.FOX)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BEE, Bee.class, CraftBee::new, createLiving(EntityTypes.BEE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.HOGLIN, Hoglin.class, CraftHoglin::new, createLiving(EntityTypes.HOGLIN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PIGLIN, Piglin.class, CraftPiglin::new, createLiving(EntityTypes.PIGLIN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.STRIDER, Strider.class, CraftStrider::new, createLiving(EntityTypes.STRIDER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ZOGLIN, Zoglin.class, CraftZoglin::new, createLiving(EntityTypes.ZOGLIN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PIGLIN_BRUTE, PiglinBrute.class, CraftPiglinBrute::new, createLiving(EntityTypes.PIGLIN_BRUTE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.AXOLOTL, Axolotl.class, CraftAxolotl::new, createLiving(EntityTypes.AXOLOTL)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.GLOW_SQUID, GlowSquid.class, CraftGlowSquid::new, createLiving(EntityTypes.GLOW_SQUID)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.GOAT, Goat.class, CraftGoat::new, createLiving(EntityTypes.GOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ALLAY, Allay.class, CraftAllay::new, createLiving(EntityTypes.ALLAY)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.FROG, Frog.class, CraftFrog::new, createLiving(EntityTypes.FROG)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.TADPOLE, Tadpole.class, CraftTadpole::new, createLiving(EntityTypes.TADPOLE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.WARDEN, Warden.class, CraftWarden::new, createLiving(EntityTypes.WARDEN)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.CAMEL, Camel.class, CraftCamel::new, createLiving(EntityTypes.CAMEL)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SNIFFER, Sniffer.class, CraftSniffer::new, createLiving(EntityTypes.SNIFFER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BREEZE, Breeze.class, CraftBreeze::new, createLiving(EntityTypes.BREEZE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ARMADILLO, Armadillo.class, CraftArmadillo::new, createLiving(EntityTypes.ARMADILLO)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.CREAKING, Creaking.class, CraftCreaking::new, createLiving(EntityTypes.CREAKING)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.CAMEL_HUSK, CamelHusk.class, CraftCamelHusk::new, createLiving(EntityTypes.CAMEL_HUSK)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.NAUTILUS, Nautilus.class, CraftNautilus::new, createLiving(EntityTypes.NAUTILUS)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PARCHED, Parched.class, CraftParched::new, createLiving(EntityTypes.PARCHED)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ZOMBIE_NAUTILUS, ZombieNautilus.class, CraftZombieNautilus::new, createLiving(EntityTypes.ZOMBIE_NAUTILUS)));

        Function<SpawnData, net.minecraft.world.entity.boss.enderdragon.EnderDragon> dragonFunction = createLiving(EntityTypes.ENDER_DRAGON);
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ENDER_DRAGON, EnderDragon.class, CraftEnderDragon::new, spawnData -> {
            Preconditions.checkArgument(spawnData.normalWorld(), "Cannot spawn entity %s during world generation", EnderDragon.class.getName());
            return dragonFunction.apply(spawnData);
        }));

        // Fireball
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.FIREBALL, LargeFireball.class, CraftLargeFireball::new, createFireball(EntityTypes.FIREBALL)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SMALL_FIREBALL, SmallFireball.class, CraftSmallFireball::new, createFireball(EntityTypes.SMALL_FIREBALL)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.WITHER_SKULL, WitherSkull.class, CraftWitherSkull::new, createFireball(EntityTypes.WITHER_SKULL)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.DRAGON_FIREBALL, DragonFireball.class, CraftDragonFireball::new, createFireball(EntityTypes.DRAGON_FIREBALL)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.WIND_CHARGE, WindCharge.class, CraftWindCharge::new, createFireball(EntityTypes.WIND_CHARGE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BREEZE_WIND_CHARGE, BreezeWindCharge.class, CraftBreezeWindCharge::new, createFireball(EntityTypes.BREEZE_WIND_CHARGE)));

        // Hanging
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PAINTING, Painting.class, CraftPainting::new, createHanging(Painting.class, (spawnData, hangingData) -> {
                    if (spawnData.normalWorld && hangingData.randomize()) {
                        return net.minecraft.world.entity.decoration.painting.Painting.create(spawnData.minecraftWorld(), hangingData.position(), hangingData.direction()).orElse(null);
                    } else {
                        net.minecraft.world.entity.decoration.painting.Painting entity = new net.minecraft.world.entity.decoration.painting.Painting(EntityTypes.PAINTING, spawnData.minecraftWorld());
                        entity.absSnapTo(spawnData.x(), spawnData.y(), spawnData.z(), spawnData.yaw(), spawnData.pitch());
                        entity.setDirection(hangingData.direction());
                        return entity;
                    }
                }
        )));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ITEM_FRAME, ItemFrame.class, CraftItemFrame::new, createHanging(ItemFrame.class, (spawnData, hangingData) -> new net.minecraft.world.entity.decoration.ItemFrame(spawnData.minecraftWorld(), hangingData.position(), hangingData.direction()))));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.GLOW_ITEM_FRAME, GlowItemFrame.class, CraftGlowItemFrame::new, createHanging(GlowItemFrame.class, (spawnData, hangingData) -> new net.minecraft.world.entity.decoration.GlowItemFrame(spawnData.minecraftWorld(), hangingData.position(), hangingData.direction()))));

        // Move no rotation
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ARROW, Arrow.class, CraftArrow::new, createAndMoveEmptyRot(EntityTypes.ARROW)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ENDER_PEARL, EnderPearl.class, CraftEnderPearl::new, createAndMoveEmptyRot(EntityTypes.ENDER_PEARL)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.EXPERIENCE_BOTTLE, ThrownExpBottle.class, CraftThrownExpBottle::new, createAndMoveEmptyRot(EntityTypes.EXPERIENCE_BOTTLE)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SPECTRAL_ARROW, SpectralArrow.class, CraftSpectralArrow::new, createAndMoveEmptyRot(EntityTypes.SPECTRAL_ARROW)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.END_CRYSTAL, EnderCrystal.class, CraftEnderCrystal::new, createAndMoveEmptyRot(EntityTypes.END_CRYSTAL)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.TRIDENT, Trident.class, CraftTrident::new, createAndMoveEmptyRot(EntityTypes.TRIDENT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.LIGHTNING_BOLT, LightningStrike.class, CraftLightningStrike::new, createAndMoveEmptyRot(EntityTypes.LIGHTNING_BOLT)));

        // Move
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SHULKER_BULLET, ShulkerBullet.class, CraftShulkerBullet::new, createAndMove(EntityTypes.SHULKER_BULLET)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.LLAMA_SPIT, LlamaSpit.class, CraftLlamaSpit::new, createAndMove(EntityTypes.LLAMA_SPIT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.OMINOUS_ITEM_SPAWNER, OminousItemSpawner.class, CraftOminousItemSpawner::new, createAndMove(EntityTypes.OMINOUS_ITEM_SPAWNER)));
        // Move (boats)
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ACACIA_BOAT, AcaciaBoat.class, CraftAcaciaBoat::new, createAndMove(EntityTypes.ACACIA_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ACACIA_CHEST_BOAT, AcaciaChestBoat.class, CraftAcaciaChestBoat::new, createAndMove(EntityTypes.ACACIA_CHEST_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BAMBOO_RAFT, BambooRaft.class, CraftBambooRaft::new, createAndMove(EntityTypes.BAMBOO_RAFT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BAMBOO_CHEST_RAFT, BambooChestRaft.class, CraftBambooChestRaft::new, createAndMove(EntityTypes.BAMBOO_CHEST_RAFT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BIRCH_BOAT, BirchBoat.class, CraftBirchBoat::new, createAndMove(EntityTypes.BIRCH_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BIRCH_CHEST_BOAT, BirchChestBoat.class, CraftBirchChestBoat::new, createAndMove(EntityTypes.BIRCH_CHEST_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.CHERRY_BOAT, CherryBoat.class, CraftCherryBoat::new, createAndMove(EntityTypes.CHERRY_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.CHERRY_CHEST_BOAT, CherryChestBoat.class, CraftCherryChestBoat::new, createAndMove(EntityTypes.CHERRY_CHEST_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.DARK_OAK_BOAT, DarkOakBoat.class, CraftDarkOakBoat::new, createAndMove(EntityTypes.DARK_OAK_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.DARK_OAK_CHEST_BOAT, DarkOakChestBoat.class, CraftDarkOakChestBoat::new, createAndMove(EntityTypes.DARK_OAK_CHEST_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.JUNGLE_BOAT, JungleBoat.class, CraftJungleBoat::new, createAndMove(EntityTypes.JUNGLE_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.JUNGLE_CHEST_BOAT, JungleChestBoat.class, CraftJungleChestBoat::new, createAndMove(EntityTypes.JUNGLE_CHEST_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.MANGROVE_BOAT, MangroveBoat.class, CraftMangroveBoat::new, createAndMove(EntityTypes.MANGROVE_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.MANGROVE_CHEST_BOAT, MangroveChestBoat.class, CraftMangroveChestBoat::new, createAndMove(EntityTypes.MANGROVE_CHEST_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.OAK_BOAT, OakBoat.class, CraftOakBoat::new, createAndMove(EntityTypes.OAK_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.OAK_CHEST_BOAT, OakChestBoat.class, CraftOakChestBoat::new, createAndMove(EntityTypes.OAK_CHEST_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PALE_OAK_BOAT, PaleOakBoat.class, CraftPaleOakBoat::new, createAndMove(EntityTypes.PALE_OAK_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PALE_OAK_CHEST_BOAT, PaleOakChestBoat.class, CraftPaleOakChestBoat::new, createAndMove(EntityTypes.PALE_OAK_CHEST_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SPRUCE_BOAT, SpruceBoat.class, CraftSpruceBoat::new, createAndMove(EntityTypes.SPRUCE_BOAT)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SPRUCE_CHEST_BOAT, SpruceChestBoat.class, CraftSpruceChestBoat::new, createAndMove(EntityTypes.SPRUCE_CHEST_BOAT)));

        // Set pos
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.MARKER, Marker.class, CraftMarker::new, createAndSetPos(EntityTypes.MARKER)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.BLOCK_DISPLAY, BlockDisplay.class, CraftBlockDisplay::new, createAndSetPos(EntityTypes.BLOCK_DISPLAY)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.INTERACTION, Interaction.class, CraftInteraction::new, createAndSetPos(EntityTypes.INTERACTION)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ITEM_DISPLAY, ItemDisplay.class, CraftItemDisplay::new, createAndSetPos(EntityTypes.ITEM_DISPLAY)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.TEXT_DISPLAY, TextDisplay.class, CraftTextDisplay::new, createAndSetPos(EntityTypes.TEXT_DISPLAY)));

        // MISC
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.ITEM, Item.class, CraftItem::new, spawnData -> {
            // We use stone instead of empty, to give the plugin developer a visual clue, that the spawn method is working,
            // and that the item stack should probably be changed.
            ItemStack itemStack = new ItemStack(Items.STONE);
            ItemEntity item = new ItemEntity(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z(), itemStack);
            item.setPickUpDelay(10);

            return item;
        }));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.EXPERIENCE_ORB, ExperienceOrb.class, CraftExperienceOrb::new,
                spawnData -> new net.minecraft.world.entity.ExperienceOrb(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z(), 0)
        ));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.AREA_EFFECT_CLOUD, AreaEffectCloud.class, CraftAreaEffectCloud::new, spawnData -> new net.minecraft.world.entity.AreaEffectCloud(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z())));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.EGG, Egg.class, CraftEgg::new, spawnData -> new ThrownEgg(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z(), new ItemStack(Items.EGG))));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.LEASH_KNOT, LeashHitch.class, CraftLeash::new, spawnData -> new LeashFenceKnotEntity(spawnData.minecraftWorld(), BlockPos.containing(spawnData.x(), spawnData.y(), spawnData.z())))); // SPIGOT-5732: LeashHitch has no direction and is always centered at a block
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SNOWBALL, Snowball.class, CraftSnowball::new, spawnData -> new net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z(), new ItemStack(Items.SNOWBALL))));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.EYE_OF_ENDER, EnderSignal.class, CraftEnderSignal::new, spawnData -> new EyeOfEnder(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z())));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SPLASH_POTION, SplashPotion.class, CraftSplashPotion::new, spawnData -> {
            AbstractThrownPotion entity = new ThrownSplashPotion(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z(), new ItemStack(Items.SPLASH_POTION));
            return entity;
        }));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.LINGERING_POTION, LingeringPotion.class, CraftLingeringPotion::new, spawnData -> {
            AbstractThrownPotion entity = new ThrownLingeringPotion(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z(), new ItemStack(Items.LINGERING_POTION));
            return entity;
        }));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.TNT, TNTPrimed.class, CraftTNTPrimed::new, spawnData -> new PrimedTnt(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z(), null)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.FALLING_BLOCK, FallingBlock.class, CraftFallingBlock::new, spawnData -> {
            BlockPos pos = BlockPos.containing(spawnData.x(), spawnData.y(), spawnData.z());
            return FallingBlockEntity.fall(spawnData.minecraftWorld(), pos, spawnData.world().getBlockState(pos));
        }));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.FIREWORK_ROCKET, Firework.class, CraftFirework::new, spawnData -> new FireworkRocketEntity(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z(), ItemStack.EMPTY)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.EVOKER_FANGS, EvokerFangs.class, CraftEvokerFangs::new, spawnData -> new net.minecraft.world.entity.projectile.EvokerFangs(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z(), (float) Math.toRadians(spawnData.yaw()), 0, null)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.COMMAND_BLOCK_MINECART, CommandMinecart.class, CraftMinecartCommand::new, createMinecart(EntityTypes.COMMAND_BLOCK_MINECART)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.MINECART, RideableMinecart.class, CraftMinecartRideable::new, createMinecart(EntityTypes.MINECART)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.CHEST_MINECART, StorageMinecart.class, CraftMinecartChest::new, createMinecart(EntityTypes.CHEST_MINECART)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.FURNACE_MINECART, PoweredMinecart.class, CraftMinecartFurnace::new, createMinecart(EntityTypes.FURNACE_MINECART)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.TNT_MINECART, ExplosiveMinecart.class, CraftMinecartTNT::new, createMinecart(EntityTypes.TNT_MINECART)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.HOPPER_MINECART, HopperMinecart.class, CraftMinecartHopper::new, createMinecart(EntityTypes.HOPPER_MINECART)));
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.SPAWNER_MINECART, SpawnerMinecart.class, CraftMinecartMobSpawner::new, createMinecart(EntityTypes.SPAWNER_MINECART)));

        // None spawn able
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.FISHING_BOBBER, FishHook.class, CraftFishHook::new, null)); // Cannot spawn a fish hook
        register(new EntityTypeData<>(org.bukkit.entity.EntityType.PLAYER, Player.class, CraftPlayer::new, null)); // Cannot spawn a player
    }

    private static void register(EntityTypeData<?, ?> typeData) {
        EntityTypeData<?, ?> other = CLASS_TYPE_DATA.put(typeData.entityClass(), typeData);
        if (other != null) {
            Bukkit.getLogger().warning(String.format("Found multiple entity type data for class %s, replacing '%s' with new value '%s'", typeData.entityClass().getName(), other, typeData));
        }

        other = ENTITY_TYPE_DATA.put(typeData.entityType(), typeData);
        if (other != null) {
            Bukkit.getLogger().warning(String.format("Found multiple entity type data for entity type %s, replacing '%s' with new value '%s'", typeData.entityType().getKey(), other, typeData));
        }
    }

    private static <R extends net.minecraft.world.entity.Entity> Function<SpawnData, R> fromEntityType(EntityType<R> entityTypes) {
        return spawnData -> entityTypes.create(spawnData.minecraftWorld(), EntitySpawnReason.COMMAND);
    }

    private static <R extends net.minecraft.world.entity.LivingEntity> Function<SpawnData, R> createLiving(EntityType<R> entityTypes) {
        return combine(fromEntityType(entityTypes), ABS_MOVE);
    }

    private static <R extends AbstractHurtingProjectile> Function<SpawnData, R> createFireball(EntityType<R> entityTypes) {
        return combine(createAndMove(entityTypes), DIRECTION);
    }

    private static <R extends AbstractMinecart> Function<SpawnData, R> createMinecart(EntityType<R> entityTypes) {
        return spawnData -> {
            if (spawnData.normalWorld()) {
                return AbstractMinecart.createMinecart(spawnData.minecraftWorld(), spawnData.x(), spawnData.y(), spawnData.z(), entityTypes, EntitySpawnReason.TRIGGERED, ItemStack.EMPTY, null);
            } else {
                return combine(fromEntityType(entityTypes), (spawnData2, entity) -> entity.setInitialPos(spawnData.x(), spawnData.y(), spawnData.z())).apply(spawnData);
            }
        };
    }

    private static <R extends net.minecraft.world.entity.Entity> Function<SpawnData, R> createAndMove(EntityType<R> entityTypes) {
        return combine(fromEntityType(entityTypes), MOVE);
    }

    private static <R extends net.minecraft.world.entity.Entity> Function<SpawnData, R> createAndMoveEmptyRot(EntityType<R> entityTypes) {
        return combine(fromEntityType(entityTypes), MOVE_EMPTY_ROT);
    }

    private static <R extends net.minecraft.world.entity.Entity> Function<SpawnData, R> createAndSetPos(EntityType<R> entityTypes) {
        return combine(fromEntityType(entityTypes), POS);
    }

    private record HangingData(boolean randomize, BlockPos position, Direction direction) {
    }

    private static <E extends Hanging, R extends HangingEntity> Function<SpawnData, R> createHanging(Class<E> clazz, BiFunction<SpawnData, HangingData, R> spawnFunction) {
        return spawnData -> {
            boolean randomizeData = spawnData.randomizeData();
            BlockFace face = BlockFace.SELF;
            BlockFace[] faces = new BlockFace[]{BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};

            int width = 16; // 1 full block, also painting smallest size.
            int height = 16; // 1 full block, also painting smallest size.

            if (ItemFrame.class.isAssignableFrom(clazz)) {
                width = 12;
                height = 12;
                faces = new BlockFace[]{BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};
            }

            final BlockPos pos = BlockPos.containing(spawnData.x(), spawnData.y(), spawnData.z());
            for (BlockFace dir : faces) {
                BlockState nmsBlock = spawnData.world().getBlockState(pos.relative(CraftBlock.blockFaceToNotch(dir)));
                if (nmsBlock.isSolid() || DiodeBlock.isDiode(nmsBlock)) {
                    boolean taken = false;
                    AABB bb = (ItemFrame.class.isAssignableFrom(clazz))
                            ? net.minecraft.world.entity.decoration.ItemFrame.createBoundingBox(pos, CraftBlock.blockFaceToNotch(dir).getOpposite(), false)
                            : net.minecraft.world.entity.decoration.painting.Painting.calculateBoundingBoxStatic(pos, CraftBlock.blockFaceToNotch(dir).getOpposite(), width, height);
                    List<net.minecraft.world.entity.Entity> list = spawnData.world().getEntities(null, bb);
                    for (Iterator<net.minecraft.world.entity.Entity> it = list.iterator(); !taken && it.hasNext(); ) {
                        net.minecraft.world.entity.Entity e = it.next();
                        if (e instanceof HangingEntity) {
                            taken = true; // Hanging entities do not like hanging entities which intersect them.
                        }
                    }

                    if (!taken) {
                        face = dir;
                        break;
                    }
                }
            }

            // No valid face found
            if (face == BlockFace.SELF) {
                // SPIGOT-6387: Allow hanging entities to be placed in midair
                face = BlockFace.SOUTH;
                randomizeData = false; // Don't randomize if no valid face is found, prevents null painting
            }

            Direction dir = CraftBlock.blockFaceToNotch(face).getOpposite();
            return spawnFunction.apply(spawnData, new HangingData(randomizeData, pos, dir));
        };
    }

    private static <T, R> Function<T, R> combine(Function<T, R> before, BiConsumer<T, ? super R> after) {
        return (t) -> {
            R r = before.apply(t);
            after.accept(t, r);
            return r;
        };
    }

    public static <E extends Entity, M extends net.minecraft.world.entity.Entity> EntityTypeData<E, M> getEntityTypeData(org.bukkit.entity.EntityType entityType) {
        return (EntityTypeData<E, M>) ENTITY_TYPE_DATA.get(entityType);
    }

    public static <E extends Entity, M extends net.minecraft.world.entity.Entity> EntityTypeData<E, M> getEntityTypeData(Class<E> entityClass) {
        return (EntityTypeData<E, M>) CLASS_TYPE_DATA.get(entityClass);
    }

    private CraftEntityTypes() {
    }
}
