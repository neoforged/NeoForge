package org.bukkit.craftbukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.phys.AABB;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Conduit;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;

public class CraftConduit extends CraftBlockEntityState<ConduitBlockEntity> implements Conduit {

    public CraftConduit(World world, ConduitBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftConduit(CraftConduit state, Location location) {
        super(state, location);
    }

    @Override
    public CraftConduit copy() {
        return new CraftConduit(this, null);
    }

    @Override
    public CraftConduit copy(Location location) {
        return new CraftConduit(this, location);
    }

    @Override
    public boolean isActive() {
        ensureNoWorldGeneration();
        ConduitBlockEntity conduit = (ConduitBlockEntity) getTileEntityFromWorld();
        return conduit != null && conduit.isActive();
    }

    @Override
    public boolean isHunting() {
        ensureNoWorldGeneration();
        ConduitBlockEntity conduit = (ConduitBlockEntity) getTileEntityFromWorld();
        return conduit != null && conduit.isHunting();
    }

    @Override
    public Collection<Block> getFrameBlocks() {
        ensureNoWorldGeneration();
        Collection<Block> blocks = new ArrayList<>();

        ConduitBlockEntity conduit = (ConduitBlockEntity) getTileEntityFromWorld();
        if (conduit != null) {
            for (BlockPos position : conduit.effectBlocks) {
                blocks.add(CraftBlock.at(getWorldHandle(), position));
            }
        }

        return blocks;
    }

    @Override
    public int getFrameBlockCount() {
        ensureNoWorldGeneration();
        ConduitBlockEntity conduit = (ConduitBlockEntity) getTileEntityFromWorld();
        return (conduit != null) ? conduit.effectBlocks.size() : 0;
    }

    @Override
    public int getRange() {
        ensureNoWorldGeneration();
        ConduitBlockEntity conduit = (ConduitBlockEntity) getTileEntityFromWorld();
        return (conduit != null) ? ConduitBlockEntity.getRange(conduit.effectBlocks) : 0;
    }

    @Override
    public boolean setTarget(LivingEntity target) {
        ConduitBlockEntity conduit = (ConduitBlockEntity) getTileEntityFromWorld();
        if (conduit == null) {
            return false;
        }

        EntityReference<net.minecraft.world.entity.LivingEntity> currentTarget = conduit.destroyTarget;

        if (target == null) {
            if (currentTarget == null) {
                return false;
            }

            conduit.destroyTarget = null;
        } else {
            net.minecraft.world.entity.LivingEntity newTarget = ((CraftLivingEntity) target).getHandle();
            if (currentTarget != null && currentTarget.matches(newTarget)) {
                return false;
            }

            conduit.destroyTarget = EntityReference.of(newTarget);
        }

        ConduitBlockEntity.updateAndAttackTarget((ServerLevel) conduit.getLevel(), getPosition(), data, conduit, conduit.effectBlocks.size() >= 42, false);
        return true;
    }

    @Override
    public LivingEntity getTarget() {
        ConduitBlockEntity conduit = (ConduitBlockEntity) getTileEntityFromWorld();
        if (conduit == null) {
            return null;
        }

        net.minecraft.world.entity.LivingEntity nmsEntity = EntityReference.get(conduit.destroyTarget, conduit.getLevel(), net.minecraft.world.entity.LivingEntity.class);
        return (nmsEntity != null) ? (LivingEntity) nmsEntity.getBukkitEntity() : null;
    }

    @Override
    public boolean hasTarget() {
        ConduitBlockEntity conduit = (ConduitBlockEntity) getTileEntityFromWorld();
        net.minecraft.world.entity.LivingEntity destroyTarget = (conduit != null) ? EntityReference.get(conduit.destroyTarget, conduit.getLevel(), net.minecraft.world.entity.LivingEntity.class) : null;
        return destroyTarget != null && destroyTarget.isAlive();
    }

    @Override
    public BoundingBox getHuntingArea() {
        AABB bounds = ConduitBlockEntity.getDestroyRangeAABB(getPosition());
        return new BoundingBox(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ);
    }
}
