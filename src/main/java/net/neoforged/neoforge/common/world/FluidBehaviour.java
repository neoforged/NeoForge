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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A rough duplicate of {@link BlockBehaviour}, aimed at fluids.
 */
public abstract class FluidBehaviour implements FeatureElement {
    protected final float explosionResistance;
    protected final float fallDistanceModifier;
    protected final double motionScale;
    protected final Optional<HolderSet<Fluid>> family;
    protected final FeatureFlagSet requiredFeatures;
    protected final String descriptionId;
    protected final FluidBehaviour.Properties properties;

    public FluidBehaviour(FluidBehaviour.Properties properties) {
        this.explosionResistance = properties.explosionResistance;
        this.fallDistanceModifier = properties.fallDistanceModifier;
        this.motionScale = properties.motionScale;
        this.family = Optional.ofNullable(properties.family);
        this.requiredFeatures = properties.requiredFeatures;
        this.descriptionId = properties.effectiveDescriptionId();

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

    @Nullable
    public SoundEvent getSound(FluidStack stack, SoundAction action) {
        return null;
    }

    @Nullable
    public SoundEvent getSound(Entity entity, SoundAction action) {
        return null;
    }

    @Nullable
    public SoundEvent getSound(LivingEntity entity, LevelAccessor level, BlockPos pos, SoundAction action) {
        return null;
    }

    public boolean isVaporizedOnPlacement(BlockGetter level, BlockPos pos, FluidStack stack) {
        return false;
    }

    public void onVaporize(LivingEntity player, BlockGetter level, BlockPos pos, FluidStack stack) {

    }

    public boolean handleCauldronDrip(BlockGetter level, BlockPos pos) {
        return false;
    }

    public BlockState getBlockForFluidState(BlockGetter level, BlockPos pos, FluidState state) {
        return state.createLegacyBlock();
    }

    public float getFallDistanceModifier(Entity entity) {
        return this.fallDistanceModifier;
    }

    public boolean canStartSwimming(Entity entity) {
        return true;
    }

    public boolean canContinueSwimming(Entity entity) {
        return true;
    }

    public float getExplosionResistance() {
        return this.explosionResistance;
    }

    public double motionScale(Entity entity) {
        return this.motionScale;
    }

    public boolean canPushEntity(Entity entity) {
        return true;
    }

    public boolean canHydrate(Entity entity) {
        return true;
    }

    public boolean canExtinguish(Entity entity) {
        return true;
    }

    public boolean shouldHideAdjacentFluidFace(Direction selfFace, FluidState adjacentFluid) {
        return false;
    }

    @Nullable
    public DripstoneDripInfo getDripInfo() {
        return null;
    }

    public static class Properties {
        private float explosionResistance = 100.0F;
        private float fallDistanceModifier = 0.5f;
        private double motionScale = 0.014D;
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

        public FluidBehaviour.Properties fallDistanceModifier(float fallDistanceModifier) {
            this.fallDistanceModifier = fallDistanceModifier;
            return this;
        }

        public FluidBehaviour.Properties motionScale(double motionScale) {
            this.motionScale = motionScale;
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

        public FluidBehaviour.Properties requiredFeatures(FeatureFlag... featureFlags) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
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
