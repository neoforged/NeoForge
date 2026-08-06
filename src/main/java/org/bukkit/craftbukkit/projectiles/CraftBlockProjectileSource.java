package org.bukkit.craftbukkit.projectiles;

import com.google.common.base.Preconditions;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.entity.BreezeWindCharge;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.WitherSkull;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.Vector;

public class CraftBlockProjectileSource implements BlockProjectileSource {
    private final DispenserBlockEntity dispenserBlock;

    public CraftBlockProjectileSource(DispenserBlockEntity dispenserBlock) {
        this.dispenserBlock = dispenserBlock;
    }

    @Override
    public Block getBlock() {
        return dispenserBlock.getLevel().getWorld().getBlockAt(dispenserBlock.getBlockPos().getX(), dispenserBlock.getBlockPos().getY(), dispenserBlock.getBlockPos().getZ());
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
        return launchProjectile(projectile, null);
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {
        Preconditions.checkArgument(getBlock().getType() == Material.DISPENSER, "Block is no longer dispenser");
        // Copied from BlockDispenser.dispense()
        BlockSource blocksource = new BlockSource((ServerLevel) dispenserBlock.getLevel(), dispenserBlock.getBlockPos(), dispenserBlock.getBlockState(), dispenserBlock);
        // Copied from DispenseBehaviorProjectile
        Direction direction = (Direction) blocksource.state().getValue(DispenserBlock.FACING);
        net.minecraft.world.level.Level level = dispenserBlock.getLevel();
        net.minecraft.world.item.Item item = null;

        if (Snowball.class.isAssignableFrom(projectile)) {
            item = Items.SNOWBALL;
        } else if (Egg.class.isAssignableFrom(projectile)) {
            item = Items.EGG;
        } else if (EnderPearl.class.isAssignableFrom(projectile)) {
            item = Items.ENDER_PEARL;
        } else if (ThrownExpBottle.class.isAssignableFrom(projectile)) {
            item = Items.EXPERIENCE_BOTTLE;
        } else if (ThrownPotion.class.isAssignableFrom(projectile)) {
            if (LingeringPotion.class.isAssignableFrom(projectile)) {
                item = Items.LINGERING_POTION;
            } else {
                item = Items.SPLASH_POTION;
            }
        } else if (AbstractArrow.class.isAssignableFrom(projectile)) {
            if (TippedArrow.class.isAssignableFrom(projectile)) {
                item = Items.TIPPED_ARROW;
            } else if (SpectralArrow.class.isAssignableFrom(projectile)) {
                item = Items.SPECTRAL_ARROW;
            } else {
                item = Items.ARROW;
            }
        } else if (Fireball.class.isAssignableFrom(projectile)) {
            if (AbstractWindCharge.class.isAssignableFrom(projectile)) {
                item = Items.WIND_CHARGE;
            } else {
                item = Items.FIRE_CHARGE;
            }
        } else if (Firework.class.isAssignableFrom(projectile)) {
            item = Items.FIREWORK_ROCKET;
        }

        Preconditions.checkArgument(item instanceof ProjectileItem, "Projectile not supported");

        ItemStack itemstack = new ItemStack(item);
        ProjectileItem projectileItem = (ProjectileItem) item;
        ProjectileItem.DispenseConfig dispenseConfig = projectileItem.createDispenseConfig();

        Position position = dispenseConfig.positionFunction().getDispensePosition(blocksource, direction);
        net.minecraft.world.entity.projectile.Projectile launch = projectileItem.asProjectile(level, position, itemstack, direction);

        if (Fireball.class.isAssignableFrom(projectile)) {
            AbstractHurtingProjectile customFireball = null;
            if (WitherSkull.class.isAssignableFrom(projectile)) {
                launch = customFireball = EntityTypes.WITHER_SKULL.create(level, EntitySpawnReason.TRIGGERED);
            } else if (DragonFireball.class.isAssignableFrom(projectile)) {
                launch = EntityTypes.DRAGON_FIREBALL.create(level, EntitySpawnReason.TRIGGERED);
            } else if (BreezeWindCharge.class.isAssignableFrom(projectile)) {
                launch = customFireball = EntityTypes.BREEZE_WIND_CHARGE.create(level, EntitySpawnReason.TRIGGERED);
            } else if (LargeFireball.class.isAssignableFrom(projectile)) {
                launch = customFireball = EntityTypes.FIREBALL.create(level, EntitySpawnReason.TRIGGERED);
            }

            if (customFireball != null) {
                customFireball.setPos(position.x(), position.y(), position.z());

                // Values from ItemFireball
                RandomSource randomsource = level.getRandom();
                double d0 = randomsource.triangle((double) direction.getStepX(), 0.11485000000000001D);
                double d1 = randomsource.triangle((double) direction.getStepY(), 0.11485000000000001D);
                double d2 = randomsource.triangle((double) direction.getStepZ(), 0.11485000000000001D);
                Vec3 vec3 = new Vec3(d0, d1, d2);
                customFireball.assignDirectionalMovement(vec3, 0.1D);
            }
        }

        if (launch instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow) {
            arrow.pickup = net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.ALLOWED;
        }
        launch.projectileSource = this;
        projectileItem.shoot(launch, (double) direction.getStepX(), (double) direction.getStepY(), (double) direction.getStepZ(), dispenseConfig.power(), dispenseConfig.uncertainty());

        if (velocity != null) {
            ((T) launch.getBukkitEntity()).setVelocity(velocity);
        }

        level.addFreshEntity(launch);
        return (T) launch.getBukkitEntity();
    }
}
