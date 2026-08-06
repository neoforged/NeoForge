package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAssets;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.memory.CraftMemoryKey;
import org.bukkit.craftbukkit.entity.memory.CraftMemoryMapper;
import org.bukkit.craftbukkit.inventory.CraftEntityEquipment;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.BreezeWindCharge;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class CraftLivingEntity extends CraftEntity implements LivingEntity {
    private CraftEntityEquipment equipment;

    public CraftLivingEntity(final CraftServer server, final net.minecraft.world.entity.LivingEntity entity) {
        super(server, entity);

        if (entity instanceof Mob || entity instanceof ArmorStand || entity instanceof Mannequin) {
            equipment = new CraftEntityEquipment(this);
        }
    }

    @Override
    public double getHealth() {
        return Math.min(Math.max(0, getHandle().getHealth()), getMaxHealth());
    }

    @Override
    public void setHealth(double health) {
        health = (float) health;
        Preconditions.checkArgument(health >= 0 && health <= this.getMaxHealth(), "Health value (%s) must be between 0 and %s", health, this.getMaxHealth());

        // during world generation, we don't want to run logic for dropping items and xp
        if (getHandle().generation && health == 0) {
            getHandle().discard(null); // Add Bukkit remove cause
            return;
        }

        getHandle().setHealth((float) health);

        if (health == 0) {
            getHandle().die(getHandle().damageSources().generic());
        }
    }

    @Override
    public double getAbsorptionAmount() {
        return getHandle().getAbsorptionAmount();
    }

    @Override
    public void setAbsorptionAmount(double amount) {
        Preconditions.checkArgument(amount >= 0 && Double.isFinite(amount), "amount < 0 or non-finite");

        getHandle().setAbsorptionAmount((float) amount);
    }

    @Override
    public double getMaxHealth() {
        return getHandle().getMaxHealth();
    }

    @Override
    public void setMaxHealth(double amount) {
        Preconditions.checkArgument(amount > 0, "Max health amount (%s) must be greater than 0", amount);

        getHandle().getAttribute(Attributes.MAX_HEALTH).setBaseValue(amount);

        if (getHealth() > amount) {
            setHealth(amount);
        }
    }

    @Override
    public void resetMaxHealth() {
        setMaxHealth(getHandle().getAttribute(Attributes.MAX_HEALTH).getAttribute().value().getDefaultValue());
    }

    @Override
    public double getEyeHeight() {
        return getHandle().getEyeHeight();
    }

    @Override
    public double getEyeHeight(boolean ignorePose) {
        return getEyeHeight();
    }

    private List<Block> getLineOfSight(Set<Material> transparent, int maxDistance, int maxLength) {
        Preconditions.checkState(!getHandle().generation, "Cannot get line of sight during world generation");

        if (transparent == null) {
            transparent = Sets.newHashSet(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);
        }
        if (maxDistance > 120) {
            maxDistance = 120;
        }
        ArrayList<Block> blocks = new ArrayList<Block>();
        Iterator<Block> itr = new BlockIterator(this, maxDistance);
        while (itr.hasNext()) {
            Block block = itr.next();
            blocks.add(block);
            if (maxLength != 0 && blocks.size() > maxLength) {
                blocks.remove(0);
            }
            Material material = block.getType();
            if (!transparent.contains(material)) {
                break;
            }
        }
        return blocks;
    }

    @Override
    public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
        return getLineOfSight(transparent, maxDistance, 0);
    }

    @Override
    public Block getTargetBlock(Set<Material> transparent, int maxDistance) {
        List<Block> blocks = getLineOfSight(transparent, maxDistance, 1);
        return blocks.get(0);
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> transparent, int maxDistance) {
        return getLineOfSight(transparent, maxDistance, 2);
    }

    @Override
    public Block getTargetBlockExact(int maxDistance) {
        return this.getTargetBlockExact(maxDistance, FluidCollisionMode.NEVER);
    }

    @Override
    public Block getTargetBlockExact(int maxDistance, FluidCollisionMode fluidCollisionMode) {
        RayTraceResult hitResult = this.rayTraceBlocks(maxDistance, fluidCollisionMode);
        return (hitResult != null ? hitResult.getHitBlock() : null);
    }

    @Override
    public RayTraceResult rayTraceBlocks(double maxDistance) {
        return this.rayTraceBlocks(maxDistance, FluidCollisionMode.NEVER);
    }

    @Override
    public RayTraceResult rayTraceBlocks(double maxDistance, FluidCollisionMode fluidCollisionMode) {
        Preconditions.checkState(!getHandle().generation, "Cannot ray tray blocks during world generation");

        Location eyeLocation = this.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        return this.getWorld().rayTraceBlocks(eyeLocation, direction, maxDistance, fluidCollisionMode, false);
    }

    @Override
    public int getRemainingAir() {
        return getHandle().getAirSupply();
    }

    @Override
    public void setRemainingAir(int ticks) {
        getHandle().setAirSupply(ticks);
    }

    @Override
    public int getMaximumAir() {
        return getHandle().maxAirTicks;
    }

    @Override
    public void setMaximumAir(int ticks) {
        getHandle().maxAirTicks = ticks;
    }

    @Override
    public ItemStack getItemInUse() {
        net.minecraft.world.item.ItemStack item = getHandle().getUseItem();
        return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
    }

    @Override
    public int getItemInUseTicks() {
        return getHandle().getUseItemRemainingTicks();
    }

    @Override
    public void setItemInUseTicks(int ticks) {
        getHandle().useItemRemaining = ticks;
    }

    @Override
    public int getArrowCooldown() {
        return getHandle().removeArrowTime;
    }

    @Override
    public void setArrowCooldown(int ticks) {
        getHandle().removeArrowTime = ticks;
    }

    @Override
    public int getArrowsInBody() {
        return getHandle().getArrowCount();
    }

    @Override
    public void setArrowsInBody(int count) {
        Preconditions.checkArgument(count >= 0, "New arrow amount must be >= 0");
        getHandle().getEntityData().set(net.minecraft.world.entity.LivingEntity.DATA_ARROW_COUNT_ID, count);
    }

    @Override
    public boolean isInvulnerable() {
        return getHandle().isInvulnerableTo((ServerLevel) getHandle().level(), getHandle().damageSources().generic());
    }

    @Override
    public void damage(double amount) {
        damage(amount, getHandle().damageSources().generic());
    }

    @Override
    public void damage(double amount, Entity source) {
        DamageSource reason = getHandle().damageSources().generic();

        if (source instanceof HumanEntity) {
            reason = getHandle().damageSources().playerAttack(((CraftHumanEntity) source).getHandle());
        } else if (source instanceof LivingEntity) {
            reason = getHandle().damageSources().mobAttack(((CraftLivingEntity) source).getHandle());
        }

        damage(amount, reason);
    }

    @Override
    public void damage(double amount, org.bukkit.damage.DamageSource damageSource) {
        Preconditions.checkArgument(damageSource != null, "damageSource cannot be null");

        damage(amount, ((CraftDamageSource) damageSource).getHandle());
    }

    private void damage(double amount, DamageSource damageSource) {
        Preconditions.checkArgument(damageSource != null, "damageSource cannot be null");
        Preconditions.checkState(!getHandle().generation, "Cannot damage entity during world generation");

        entity.hurt(damageSource, (float) amount);
    }

    @Override
    public Location getEyeLocation() {
        Location loc = getLocation();
        loc.setY(loc.getY() + getEyeHeight());
        return loc;
    }

    @Override
    public int getMaximumNoDamageTicks() {
        return getHandle().invulnerableDuration;
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
        getHandle().invulnerableDuration = ticks;
    }

    @Override
    public double getLastDamage() {
        return getHandle().lastHurt;
    }

    @Override
    public void setLastDamage(double damage) {
        getHandle().lastHurt = (float) damage;
    }

    @Override
    public int getNoDamageTicks() {
        return getHandle().invulnerableTime;
    }

    @Override
    public void setNoDamageTicks(int ticks) {
        getHandle().invulnerableTime = ticks;
    }

    @Override
    public int getNoActionTicks() {
        return getHandle().getNoActionTime();
    }

    @Override
    public void setNoActionTicks(int ticks) {
        Preconditions.checkArgument(ticks >= 0, "ticks must be >= 0");
        getHandle().setNoActionTime(ticks);
    }

    @Override
    public net.minecraft.world.entity.LivingEntity getHandle() {
        return (net.minecraft.world.entity.LivingEntity) entity;
    }

    public void setHandle(final net.minecraft.world.entity.LivingEntity entity) {
        super.setHandle(entity);
    }

    @Override
    public String toString() {
        return "CraftLivingEntity{" + "id=" + getEntityId() + '}';
    }

    @Override
    public Player getKiller() {
        net.minecraft.world.entity.player.Player lastHurtByPlayer = getHandle().getLastHurtByPlayer();
        return lastHurtByPlayer == null ? null : (Player) lastHurtByPlayer.getBukkitEntity();
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        return addPotionEffect(effect, false);
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        getHandle().addEffect(new MobEffectInstance(CraftPotionEffectType.bukkitToMinecraftHolder(effect.getType()), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles()), EntityPotionEffectEvent.Cause.PLUGIN);
        return true;
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        boolean success = true;
        for (PotionEffect effect : effects) {
            success &= addPotionEffect(effect);
        }
        return success;
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        return getHandle().hasEffect(CraftPotionEffectType.bukkitToMinecraftHolder(type));
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType type) {
        MobEffectInstance handle = getHandle().getEffect(CraftPotionEffectType.bukkitToMinecraftHolder(type));
        return (handle == null) ? null : new PotionEffect(CraftPotionEffectType.minecraftHolderToBukkit(handle.getEffect()), handle.getDuration(), handle.getAmplifier(), handle.isAmbient(), handle.isVisible());
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        getHandle().removeEffect(CraftPotionEffectType.bukkitToMinecraftHolder(type), EntityPotionEffectEvent.Cause.PLUGIN);
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        List<PotionEffect> effects = new ArrayList<PotionEffect>();
        for (MobEffectInstance handle : getHandle().activeEffects.values()) {
            effects.add(new PotionEffect(CraftPotionEffectType.minecraftHolderToBukkit(handle.getEffect()), handle.getDuration(), handle.getAmplifier(), handle.isAmbient(), handle.isVisible()));
        }
        return effects;
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
        return launchProjectile(projectile, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {
        Preconditions.checkState(!getHandle().generation, "Cannot launch projectile during world generation");

        net.minecraft.world.level.Level level = ((CraftWorld) getWorld()).getHandle();
        net.minecraft.world.entity.Entity launch = null;

        if (Snowball.class.isAssignableFrom(projectile)) {
            launch = new net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball(level, getHandle(), new net.minecraft.world.item.ItemStack(Items.SNOWBALL));
            ((ThrowableProjectile) launch).shootFromRotation(getHandle(), getHandle().getXRot(), getHandle().getYRot(), 0.0F, 1.5F, 1.0F); // ItemSnowball
        } else if (Egg.class.isAssignableFrom(projectile)) {
            launch = new ThrownEgg(level, getHandle(), new net.minecraft.world.item.ItemStack(Items.EGG));
            ((ThrowableProjectile) launch).shootFromRotation(getHandle(), getHandle().getXRot(), getHandle().getYRot(), 0.0F, 1.5F, 1.0F); // ItemEgg
        } else if (EnderPearl.class.isAssignableFrom(projectile)) {
            launch = new ThrownEnderpearl(level, getHandle(), new net.minecraft.world.item.ItemStack(Items.ENDER_PEARL));
            ((ThrowableProjectile) launch).shootFromRotation(getHandle(), getHandle().getXRot(), getHandle().getYRot(), 0.0F, 1.5F, 1.0F); // ItemEnderPearl
        } else if (AbstractArrow.class.isAssignableFrom(projectile)) {
            if (TippedArrow.class.isAssignableFrom(projectile)) {
                launch = new net.minecraft.world.entity.projectile.arrow.Arrow(level, getHandle(), new net.minecraft.world.item.ItemStack(Items.ARROW), null);
                ((Arrow) launch.getBukkitEntity()).setBasePotionType(PotionType.WATER);
            } else if (SpectralArrow.class.isAssignableFrom(projectile)) {
                launch = new net.minecraft.world.entity.projectile.arrow.SpectralArrow(level, getHandle(), new net.minecraft.world.item.ItemStack(Items.SPECTRAL_ARROW), null);
            } else if (Trident.class.isAssignableFrom(projectile)) {
                launch = new ThrownTrident(level, getHandle(), new net.minecraft.world.item.ItemStack(Items.TRIDENT));
            } else {
                launch = new net.minecraft.world.entity.projectile.arrow.Arrow(level, getHandle(), new net.minecraft.world.item.ItemStack(Items.ARROW), null);
            }
            ((net.minecraft.world.entity.projectile.arrow.AbstractArrow) launch).shootFromRotation(getHandle(), getHandle().getXRot(), getHandle().getYRot(), 0.0F, 3.0F, 1.0F); // ItemBow
        } else if (ThrownPotion.class.isAssignableFrom(projectile)) {
            if (LingeringPotion.class.isAssignableFrom(projectile)) {
                launch = new ThrownLingeringPotion(level, getHandle(), new net.minecraft.world.item.ItemStack(Items.LINGERING_POTION));
            } else {
                launch = new ThrownSplashPotion(level, getHandle(), new net.minecraft.world.item.ItemStack(Items.SPLASH_POTION));
            }
            ((ThrowableProjectile) launch).shootFromRotation(getHandle(), getHandle().getXRot(), getHandle().getYRot(), -20.0F, 0.5F, 1.0F); // ItemSplashPotion
        } else if (ThrownExpBottle.class.isAssignableFrom(projectile)) {
            launch = new ThrownExperienceBottle(level, getHandle(), new net.minecraft.world.item.ItemStack(Items.EXPERIENCE_BOTTLE));
            ((ThrowableProjectile) launch).shootFromRotation(getHandle(), getHandle().getXRot(), getHandle().getYRot(), -20.0F, 0.7F, 1.0F); // ItemExpBottle
        } else if (FishHook.class.isAssignableFrom(projectile) && getHandle() instanceof Player) {
            launch = new FishingHook((net.minecraft.world.entity.player.Player) getHandle(), level, 0, 0);
        } else if (Fireball.class.isAssignableFrom(projectile)) {
            Location location = getEyeLocation();
            Vector direction = location.getDirection().multiply(10);
            Vec3 vec = new Vec3(direction.getX(), direction.getY(), direction.getZ());

            if (SmallFireball.class.isAssignableFrom(projectile)) {
                launch = new net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball(level, getHandle(), vec);
            } else if (WitherSkull.class.isAssignableFrom(projectile)) {
                launch = new net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull(level, getHandle(), vec);
            } else if (DragonFireball.class.isAssignableFrom(projectile)) {
                launch = new net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball(level, getHandle(), vec);
            } else if (AbstractWindCharge.class.isAssignableFrom(projectile)) {
                if (BreezeWindCharge.class.isAssignableFrom(projectile)) {
                    launch = EntityTypes.BREEZE_WIND_CHARGE.create(level, EntitySpawnReason.TRIGGERED);
                } else {
                    launch = EntityTypes.WIND_CHARGE.create(level, EntitySpawnReason.TRIGGERED);
                }

                ((net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge) launch).setOwner(getHandle());
                ((net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge) launch).shootFromRotation(getHandle(), getHandle().getXRot(), getHandle().getYRot(), 0.0F, 1.5F, 1.0F); // WindChargeItem
            } else {
                launch = new LargeFireball(level, getHandle(), vec, 1);
            }

            ((AbstractHurtingProjectile) launch).projectileSource = this;
            launch.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } else if (LlamaSpit.class.isAssignableFrom(projectile)) {
            Location location = getEyeLocation();
            Vector direction = location.getDirection();

            launch = EntityTypes.LLAMA_SPIT.create(level, EntitySpawnReason.TRIGGERED);

            ((net.minecraft.world.entity.projectile.LlamaSpit) launch).setOwner(getHandle());
            ((net.minecraft.world.entity.projectile.LlamaSpit) launch).shoot(direction.getX(), direction.getY(), direction.getZ(), 1.5F, 10.0F); // EntityLlama
            launch.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } else if (ShulkerBullet.class.isAssignableFrom(projectile)) {
            Location location = getEyeLocation();

            launch = new net.minecraft.world.entity.projectile.ShulkerBullet(level, getHandle(), null, null);
            launch.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } else if (Firework.class.isAssignableFrom(projectile)) {
            Location location = getEyeLocation();

            launch = new FireworkRocketEntity(level, net.minecraft.world.item.ItemStack.EMPTY, getHandle());
            launch.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        }

        Preconditions.checkArgument(launch != null, "Projectile (%s) not supported", projectile.getName());

        if (velocity != null) {
            ((T) launch.getBukkitEntity()).setVelocity(velocity);
        }

        level.addFreshEntity(launch);
        return (T) launch.getBukkitEntity();
    }

    @Override
    public boolean hasLineOfSight(Entity other) {
        Preconditions.checkState(!getHandle().generation, "Cannot check line of sight during world generation");

        return getHandle().hasLineOfSight(((CraftEntity) other).getHandle());
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        return getHandle() instanceof Mob && !((Mob) getHandle()).isPersistenceRequired();
    }

    @Override
    public void setRemoveWhenFarAway(boolean remove) {
        if (getHandle() instanceof Mob) {
            ((Mob) getHandle()).persistenceRequired = !remove;
        }
    }

    @Override
    public EntityEquipment getEquipment() {
        return equipment;
    }

    @Override
    public void setCanPickupItems(boolean pickup) {
        if (getHandle() instanceof Mob) {
            ((Mob) getHandle()).setCanPickUpLoot(pickup);
        } else {
            getHandle().bukkitPickUpLoot = pickup;
        }
    }

    @Override
    public boolean getCanPickupItems() {
        if (getHandle() instanceof Mob) {
            return ((Mob) getHandle()).canPickUpLoot();
        } else {
            return getHandle().bukkitPickUpLoot;
        }
    }

    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause) {
        if (getHealth() == 0) {
            return false;
        }

        return super.teleport(location, cause);
    }

    @Override
    public boolean isLeashed() {
        if (!(getHandle() instanceof Leashable)) {
            return false;
        }
        return ((Leashable) getHandle()).getLeashHolder() != null;
    }

    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        Preconditions.checkState(isLeashed(), "Entity not leashed");
        return ((Leashable) getHandle()).getLeashHolder().getBukkitEntity();
    }

    private boolean unleash() {
        if (!isLeashed()) {
            return false;
        }
        ((Leashable) getHandle()).removeLeash();
        return true;
    }

    @Override
    public boolean setLeashHolder(Entity holder) {
        if (getHandle().generation || !(getHandle() instanceof Leashable)) {
            return false;
        }

        if (holder == null) {
            return unleash();
        }

        if (holder.isDead()) {
            return false;
        }

        unleash();
        ((Leashable) getHandle()).setLeashedTo(((CraftEntity) holder).getHandle(), true);
        return true;
    }

    @Override
    public boolean isGliding() {
        return getHandle().getSharedFlag(7);
    }

    @Override
    public void setGliding(boolean gliding) {
        getHandle().setSharedFlag(7, gliding);
    }

    @Override
    public boolean isSwimming() {
        return getHandle().isSwimming();
    }

    @Override
    public void setSwimming(boolean swimming) {
        getHandle().setSwimming(swimming);
    }

    @Override
    public boolean isRiptiding() {
        return getHandle().isAutoSpinAttack();
    }

    @Override
    public void setRiptiding(boolean riptiding) {
        getHandle().setLivingEntityFlag(net.minecraft.world.entity.LivingEntity.LIVING_ENTITY_FLAG_SPIN_ATTACK, riptiding);
    }

    @Override
    public boolean isSleeping() {
        return getHandle().isSleeping();
    }

    @Override
    public boolean isClimbing() {
        Preconditions.checkState(!getHandle().generation, "Cannot check if climbing during world generation");

        return getHandle().onClimbable();
    }

    @Override
    public AttributeInstance getAttribute(Attribute attribute) {
        return getHandle().craftAttributes.getAttribute(attribute);
    }

    @Override
    public void setAI(boolean ai) {
        if (this.getHandle() instanceof Mob) {
            ((Mob) this.getHandle()).setNoAi(!ai);
        }
    }

    @Override
    public boolean hasAI() {
        return (this.getHandle() instanceof Mob) ? !((Mob) this.getHandle()).isNoAi() : false;
    }

    @Override
    public void attack(Entity target) {
        Preconditions.checkArgument(target != null, "target == null");
        Preconditions.checkState(!getHandle().generation, "Cannot attack during world generation");

        if (getHandle() instanceof net.minecraft.world.entity.player.Player) {
            ((net.minecraft.world.entity.player.Player) getHandle()).attack(((CraftEntity) target).getHandle());
        } else {
            getHandle().doHurtTarget((ServerLevel) ((CraftEntity) target).getHandle().level(), ((CraftEntity) target).getHandle());
        }
    }

    @Override
    public void swingMainHand() {
        Preconditions.checkState(!getHandle().generation, "Cannot swing hand during world generation");

        getHandle().swing(InteractionHand.MAIN_HAND, true);
    }

    @Override
    public void swingOffHand() {
        Preconditions.checkState(!getHandle().generation, "Cannot swing hand during world generation");

        getHandle().swing(InteractionHand.OFF_HAND, true);
    }

    @Override
    public void playHurtAnimation(float yaw) {
        if (getHandle().level() instanceof ServerLevel world) {
            /*
             * Vanilla degrees state that 0 = left, 90 = front, 180 = right, and 270 = behind.
             * This makes no sense. We'll add 90 to it so that 0 = front, clockwise from there.
             */
            float actualYaw = yaw + 90;
            ClientboundHurtAnimationPacket packet = new ClientboundHurtAnimationPacket(getEntityId(), actualYaw);

            world.getChunkSource().sendToTrackingPlayersAndSelf(getHandle(), packet);
        }
    }

    @Override
    public void setCollidable(boolean collidable) {
        getHandle().collides = collidable;
    }

    @Override
    public boolean isCollidable() {
        return getHandle().collides;
    }

    @Override
    public Set<UUID> getCollidableExemptions() {
        return getHandle().collidableExemptions;
    }

    @Override
    public <T> T getMemory(MemoryKey<T> memoryKey) {
        return (T) getHandle().getBrain().getMemory(CraftMemoryKey.bukkitToMinecraft(memoryKey)).map(CraftMemoryMapper::fromNms).orElse(null);
    }

    @Override
    public <T> void setMemory(MemoryKey<T> memoryKey, T t) {
        getHandle().getBrain().setMemory(CraftMemoryKey.bukkitToMinecraft(memoryKey), CraftMemoryMapper.toNms(t));
    }

    @Override
    public Sound getHurtSound() {
        SoundEvent sound = getHandle().getHurtSound0(getHandle().damageSources().generic());
        return (sound != null) ? CraftSound.minecraftToBukkit(sound) : null;
    }

    @Override
    public Sound getDeathSound() {
        SoundEvent sound = getHandle().getDeathSound0();
        return (sound != null) ? CraftSound.minecraftToBukkit(sound) : null;
    }

    @Override
    public Sound getFallDamageSound(int fallHeight) {
        return CraftSound.minecraftToBukkit(getHandle().getFallDamageSound0(fallHeight));
    }

    @Override
    public Sound getFallDamageSoundSmall() {
        return CraftSound.minecraftToBukkit(getHandle().getFallSounds().small());
    }

    @Override
    public Sound getFallDamageSoundBig() {
        return CraftSound.minecraftToBukkit(getHandle().getFallSounds().big());
    }

    @Override
    public Sound getDrinkingSound(ItemStack itemStack) {
        return getEatingSound(itemStack);
    }

    @Override
    public Sound getEatingSound(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null, "itemStack must not be null");

        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
        Consumable consumable = nms.get(DataComponents.CONSUMABLE);
        SoundEvent soundevent = SoundEvents.GENERIC_DRINK.value();

        if (consumable != null) {
            if (getHandle() instanceof Consumable.OverrideConsumeSound consumable_b) {
                soundevent = consumable_b.getConsumeSound(nms);
            } else {
                soundevent = (SoundEvent) consumable.sound().value();
            }
        }

        return CraftSound.minecraftToBukkit(soundevent);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return getHandle().canBreatheUnderwater();
    }

    @Override
    public EntityCategory getCategory() {
        throw new UnsupportedOperationException("Method no longer applicable. Use Tags instead.");
    }

    @Override
    public boolean isInvisible() {
        return getHandle().isInvisible();
    }

    @Override
    public void setInvisible(boolean invisible) {
        getHandle().persistentInvisibility = invisible;
        getHandle().setSharedFlag(5, invisible);
    }

    @Override
    public Color getWaypointColor() {
        return getHandle().waypointIcon().color.map(Color::fromRGB).orElse(null);
    }

    @Override
    public void setWaypointColor(Color color) {
        mutateIcon((icon) -> icon.color = Optional.ofNullable(color).map(Color::asRGB));
    }

    @Override
    public NamespacedKey getWaypointStyle() {
        return CraftNamespacedKey.fromMinecraft(getHandle().waypointIcon().style.identifier());
    }

    @Override
    public void setWaypointStyle(NamespacedKey key) {
        mutateIcon((icon) -> icon.style = Optional.ofNullable(key).map((k) -> ResourceKey.create(WaypointStyleAssets.ROOT_ID, CraftNamespacedKey.toMinecraft(k))).orElse(WaypointStyleAssets.DEFAULT));
    }

    private void mutateIcon(Consumer<Waypoint.Icon> consumer) {
        net.minecraft.world.entity.LivingEntity handle = getHandle();
        ServerLevel serverlevel = (ServerLevel) handle.level();

        serverlevel.getWaypointManager().untrackWaypoint(handle);
        consumer.accept(handle.waypointIcon());
        serverlevel.getWaypointManager().trackWaypoint(handle);
    }
}
