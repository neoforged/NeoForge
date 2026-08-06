package org.bukkit.craftbukkit.entity;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Minecart;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public abstract class CraftMinecart extends CraftVehicle implements Minecart {
    public CraftMinecart(CraftServer server, AbstractMinecart entity) {
        super(server, entity);
    }

    @Override
    public void setDamage(double damage) {
        getHandle().setDamage((float) damage);
    }

    @Override
    public double getDamage() {
        return getHandle().getDamage();
    }

    @Override
    public double getMaxSpeed() {
        return getHandle().getBehavior().getMaxSpeed((ServerLevel) getHandle().level());
    }

    @Override
    public void setMaxSpeed(double speed) {
        if (speed >= 0D) {
            getHandle().maxSpeed = speed;
        }
    }

    @Override
    public boolean isSlowWhenEmpty() {
        return getHandle().slowWhenEmpty;
    }

    @Override
    public void setSlowWhenEmpty(boolean slow) {
        getHandle().slowWhenEmpty = slow;
    }

    @Override
    public Vector getFlyingVelocityMod() {
        return getHandle().getFlyingVelocityMod();
    }

    @Override
    public void setFlyingVelocityMod(Vector flying) {
        getHandle().setFlyingVelocityMod(flying);
    }

    @Override
    public Vector getDerailedVelocityMod() {
        return getHandle().getDerailedVelocityMod();
    }

    @Override
    public void setDerailedVelocityMod(Vector derailed) {
        getHandle().setDerailedVelocityMod(derailed);
    }

    @Override
    public AbstractMinecart getHandle() {
        return (AbstractMinecart) entity;
    }

    @Override
    public void setDisplayBlock(MaterialData material) {
        if (material != null) {
            BlockState block = CraftMagicNumbers.getBlock(material);
            this.getHandle().setCustomDisplayBlockState(Optional.of(block));
        } else {
            // Set block to air (default) and set the flag to not have a display block.
            this.getHandle().setCustomDisplayBlockState(Optional.empty());
        }
    }

    @Override
    public void setDisplayBlockData(BlockData blockData) {
        if (blockData != null) {
            BlockState block = ((CraftBlockData) blockData).getState();
            this.getHandle().setCustomDisplayBlockState(Optional.of(block));
        } else {
            // Set block to air (default) and set the flag to not have a display block.
            this.getHandle().setCustomDisplayBlockState(Optional.empty());
        }
    }

    @Override
    public MaterialData getDisplayBlock() {
        BlockState blockData = getHandle().getDisplayBlockState();
        return CraftMagicNumbers.getMaterial(blockData);
    }

    @Override
    public BlockData getDisplayBlockData() {
        BlockState blockData = getHandle().getDisplayBlockState();
        return CraftBlockData.fromData(blockData);
    }

    @Override
    public void setDisplayBlockOffset(int offset) {
        getHandle().setDisplayOffset(offset);
    }

    @Override
    public int getDisplayBlockOffset() {
        return getHandle().getDisplayOffset();
    }

    @Override
    public double getPoweredRailAccelerationMultiplier() {
        return getHandle().powRailAccelMult;
    }

    @Override
    public void setPoweredRailAccelerationMultiplier(double multiplier) {
        getHandle().powRailAccelMult = multiplier;
    }
}
