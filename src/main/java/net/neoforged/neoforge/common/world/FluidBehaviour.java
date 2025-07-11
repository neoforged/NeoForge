package net.neoforged.neoforge.common.world;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * A rough duplicate of {@link BlockBehaviour}, aimed at fluids.
 */
public abstract class FluidBehaviour implements FeatureElement {
    // block related properties
    private final float explosionResistance;
    private final boolean canConvertToSource;
    private final boolean canExtinguishBlocks;
    private final boolean canHydrateBlocks;

    // entity related properties
    private final float entityFallDistanceModifier;
    private final double entityMotionScale;
    private final boolean canPushEntities;
    private final boolean canEntitiesSwim;
    private final boolean canEntitiesDrown;
    private final boolean canExtinguishEntities;
    private final boolean canHydrateEntities;
    private final boolean supportsBoats;
    @Nullable
    private final PathType pathingType;
    @Nullable
    private final PathType adjacentPathingType;

    // misc. properties
    private final Optional<HolderSet<Fluid>> family;
    private final String descriptionId;
    private final FeatureFlagSet requiredFeatures;
    private final Properties properties;

    public FluidBehaviour(FluidBehaviour.Properties properties) {
        this.explosionResistance = properties.explosionResistance;
        this.canConvertToSource = properties.canConvertToSource;
        this.canExtinguishBlocks = properties.canExtinguishBlocks;
        this.canHydrateBlocks = properties.canHydrateBlocks;

        this.entityFallDistanceModifier = properties.entityFallDistanceModifier;
        this.entityMotionScale = properties.entityMotionScale;
        this.canPushEntities = properties.canPushEntities;
        this.canEntitiesSwim = properties.canEntitiesSwim;
        this.canEntitiesDrown = properties.canEntitiesDrown;
        this.canExtinguishEntities = properties.canExtinguishEntities;
        this.canHydrateEntities = properties.canHydrateEntities;
        this.supportsBoats = properties.supportsBoats;
        this.pathingType = properties.pathingType;
        this.adjacentPathingType = properties.adjacentPathingType;

        this.family = Optional.ofNullable(properties.family);
        this.descriptionId = properties.effectiveDescriptionId();
        this.requiredFeatures = properties.requiredFeatures;
        this.properties = properties;
    }

    public FluidBehaviour.Properties properties() {
        return this.properties;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    public final String getDescriptionId() {
        return this.descriptionId;
    }

    public MutableComponent getName() {
        return Component.translatable(this.getDescriptionId());
    }

    public Optional<HolderSet<Fluid>> getFamily() {
        return this.family;
    }

    public float getExplosionResistance() {
        return this.explosionResistance;
    }

    public boolean canConvertToSource(ServerLevel level, BlockPos pos) {
        return this.canConvertToSource;
    }

    public boolean canExtinguish(BlockGetter level, BlockPos pos) {
        return this.canExtinguishBlocks;
    }

    public boolean canHydrate(BlockGetter level, BlockPos pos, BlockState source, BlockPos sourcePos) {
        return this.canHydrateBlocks;
    }

    public boolean handleCauldronDrip(BlockGetter level, BlockPos pos) {
        return false;
    }

    public abstract FluidState getStateForPlacement(BlockGetter level, BlockPos pos, FluidStack stack);

    public abstract BlockState getBlockStateForPlacement(BlockGetter level, BlockPos pos);

    public boolean shouldHideAdjacentFluidFace(Direction selfFace, FluidState adjacentFluid) {
        return false;
    }

    @Nullable
    public DripstoneDripInfo getDripInfo() {
        return null;
    }

    public boolean move(LivingEntity entity, Vec3 movementVector, double gravity) {
        return false;
    }

    public boolean move(ItemEntity entity, Vec3 movementVector, double gravity) {
        return false;
    }

    @Nullable
    public SoundEvent getSound(LivingEntity entity, BlockGetter level, BlockPos pos, SoundAction action) {
        return null;
    }

    @Nullable
    public SoundEvent getSound(FluidStack stack, SoundAction action) {
        return null;
    }

    @Nullable
    public SoundEvent getSound(Entity entity, SoundAction action) {
        return null;
    }

    public boolean isVaporizedOnPlacement(BlockGetter level, BlockPos pos, FluidStack stack) {
        return false;
    }

    public void onVaporize(LivingEntity player, BlockGetter level, BlockPos pos, FluidStack stack) {
    }

    public boolean canVehicleRideUnder(BlockGetter level, BlockPos pos, Entity entity, Entity rider)  {
        return false;
    }

    public float getFallDistanceModifier(Entity entity) {
        return this.entityFallDistanceModifier;
    }

    public double motionScale(Entity entity) {
        return this.entityMotionScale;
    }

    public boolean canPushEntity(Entity entity) {
        return this.canPushEntities;
    }

    public boolean canStartSwimming(Entity entity) {
        return this.canEntitiesSwim;
    }

    public boolean canContinueSwimming(Entity entity) {
        return this.canEntitiesSwim;
    }

    public boolean canDrownIn(LivingEntity livingEntity) {
        return this.canEntitiesDrown;
    }

    public boolean canExtinguish(Entity entity) {
        return this.canExtinguishEntities;
    }

    public boolean canHydrate(Entity entity) {
        return this.canHydrateEntities;
    }

    public boolean supportsBoating(AbstractBoat boat) {
        return this.supportsBoats;
    }

    @Nullable
    public PathType getBlockPathType(BlockGetter level, BlockPos pos, @Nullable Mob mob, boolean canFluidLog) {
        return this.pathingType;
    }

    @Nullable
    public PathType getAdjacentBlockPathType(BlockGetter level, BlockPos pos, @Nullable Mob mob, PathType originalType) {
        return this.adjacentPathingType;
    }

    public static class Properties {
        // block related properties
        private float explosionResistance = 0f;
        private boolean canConvertToSource = false;
        private boolean canExtinguishBlocks = false;
        private boolean canHydrateBlocks = false;

        // entity related properties
        private float entityFallDistanceModifier = 1f;
        private double entityMotionScale = 1f;
        private boolean canPushEntities = false;
        private boolean canEntitiesSwim = false;
        private boolean canEntitiesDrown = false;
        private boolean canExtinguishEntities = false;
        private boolean canHydrateEntities = false;
        private boolean supportsBoats = false;
        @Nullable
        private PathType pathingType = PathType.BLOCKED;
        @Nullable
        private PathType adjacentPathingType = PathType.BLOCKED;

        // misc. properties
        @Nullable
        private HolderSet<Fluid> family;
        @Nullable
        private ResourceKey<Fluid> id;
        private DependantName<Fluid, String> descriptionId = fluid -> Util.makeDescriptionId("fluid", fluid.location());
        private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

        private Properties() {
        }

        public static FluidBehaviour.Properties of() {
            return new FluidBehaviour.Properties();
        }

        public FluidBehaviour.Properties explosionResistance(float explosionResistance) {
            this.explosionResistance = Math.max(0.0F, explosionResistance);
            return this;
        }

        public FluidBehaviour.Properties convertsToSource() {
            this.canConvertToSource = true;
            return this;
        }

        public FluidBehaviour.Properties extinguishBlocks() {
            this.canExtinguishBlocks = true;
            return this;
        }

        public FluidBehaviour.Properties hydrateBlocks() {
            this.canHydrateBlocks = true;
            return this;
        }

        public FluidBehaviour.Properties fallDistanceModifier(float fallDistanceModifier) {
            this.entityFallDistanceModifier = fallDistanceModifier;
            return this;
        }

        public FluidBehaviour.Properties motionScale(double motionScale) {
            this.entityMotionScale = motionScale;
            return this;
        }

        public FluidBehaviour.Properties pushEntities() {
            this.canPushEntities = true;
            return this;
        }

        public FluidBehaviour.Properties swimmable() {
            this.canEntitiesSwim = true;
            return this;
        }

        public FluidBehaviour.Properties drownable() {
            this.canEntitiesDrown = true;
            return this;
        }

        public FluidBehaviour.Properties extinguishEntities() {
            this.canExtinguishEntities = true;
            return this;
        }

        public FluidBehaviour.Properties hydrateEntities() {
            this.canHydrateEntities = true;
            return this;
        }

        public FluidBehaviour.Properties supportsBoats() {
            this.supportsBoats = true;
            return this;
        }

        public FluidBehaviour.Properties pathType(@Nullable PathType pathingType) {
            this.pathingType = pathingType;
            return this;
        }

        public FluidBehaviour.Properties adjacentPathType(@Nullable PathType pathingType) {
            this.adjacentPathingType = pathingType;
            return this;
        }

        public FluidBehaviour.Properties family(HolderSet<Fluid> family) {
            this.family = family;
            return this;
        }

        public FluidBehaviour.Properties family(Holder<Fluid>... family) {
            this.family = net.minecraft.core.HolderSet.direct(family);
            return this;
        }

        public FluidBehaviour.Properties family(TagKey<Fluid> family) {
            this.family = HolderSet.emptyNamed(BuiltInRegistries.FLUID, family);
            return this;
        }

        public FluidBehaviour.Properties setId(ResourceKey<Fluid> fluidId) {
            this.id = fluidId;
            return this;
        }

        public FluidBehaviour.Properties overrideDescription(String description) {
            this.descriptionId = DependantName.fixed(description);
            return this;
        }

        public FluidBehaviour.Properties requiredFeatures(FeatureFlag... featureFlags) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
            return this;
        }

        protected String effectiveDescriptionId() {
            return this.descriptionId.get(Objects.requireNonNull(this.id, "Fluid id not set"));
        }
    }

    /**
     * A record that holds some information to let a fluid drip from Pointed Dripstone stalactites and fill cauldrons below.
     *
     * @param chance         the chance that the cauldron below will be filled every time the Pointed Dripstone is randomly ticked. This number should be some value between 0.0 and 1.0
     * @param dripParticle   the particle that spawns randomly from the tip of the Pointed Dripstone when this fluid is above it
     * @param filledCauldron the block the Pointed Dripstone should replace an empty cauldron with when it successfully tries to fill the cauldron
     */
    public record DripstoneDripInfo(float chance, @Nullable ParticleOptions dripParticle, Holder<Block> filledCauldron) {}

}
