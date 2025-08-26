package net.neoforged.neoforge.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

import java.util.Optional;

public abstract class DefaultMilkFluid extends FlowingFluid {
    private static final TagKey<Fluid> MILK = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "milk"));
    private static final HolderSet<Fluid> FAMILY = HolderSet.emptyNamed(BuiltInRegistries.FLUID, MILK);

    protected DefaultMilkFluid(String id) {
        super(net.neoforged.neoforge.common.world.FluidBehaviour.Properties.of()
            .convertsToSource()
            .extinguishBlocks()
            .hydrateBlocks()
            .fallDistanceModifier(0f)
            .motionScale(0.014d)
            .pushEntities()
            .swimmable()
            .drownable()
            .extinguishEntities()
            .hydrateEntities()
            .supportsBoats()
            .family(FAMILY)
            .setId(ResourceKey.create(BuiltInRegistries.FLUID.key(), ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, id))));
    }

    @Override
    public Fluid getFlowing() {
        return NeoForgeMod.FLOWING_MILK.get();
    }

    @Override
    public Fluid getSource() {
        return NeoForgeMod.MILK.get();
    }

    @Override
    public Item getBucket() {
        return Items.MILK_BUCKET;
    }

    @Override
    protected boolean canConvertToSource(ServerLevel p_376722_) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor p_76450_, BlockPos p_76451_, BlockState p_76452_) {
        BlockEntity blockentity = p_76452_.hasBlockEntity() ? p_76450_.getBlockEntity(p_76451_) : null;
        Block.dropResources(p_76452_, p_76450_, p_76451_, blockentity);
    }

    @Override
    public int getSlopeFindDistance(LevelReader p_76464_) {
        return 4;
    }

    @Override
    public BlockState createLegacyBlock(FluidState p_76466_) {
        return NeoForgeMod.MILK_BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(p_76466_));
    }

    @Override
    public boolean isSame(Fluid p_76456_) {
        return p_76456_ == NeoForgeMod.MILK.get() || p_76456_ == NeoForgeMod.FLOWING_MILK.get();
    }

    @Override
    public int getDropOff(LevelReader p_76469_) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader p_76454_) {
        return 5;
    }

    @Override
    public boolean canBeReplacedWith(FluidState p_76458_, BlockGetter p_76459_, BlockPos p_76460_, Fluid p_76461_, Direction p_76462_) {
        return p_76462_ == Direction.DOWN && !p_76461_.is(MILK);
    }

    @Override
    public float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    public boolean move(final LivingEntity entity, final Vec3 movementVector, final double gravity)
    {
        // TODO: this might be better off having an API to say "move like water" or "move like lava"
        boolean flag = entity.getDeltaMovement().y <= 0.0;
        float f = entity.isSprinting() ? 0.9F : 0.8f;
        float f1 = 0.02F;
        float f2 = (float)entity.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
        if (!entity.onGround()) {
            f2 *= 0.5F;
        }

        if (f2 > 0.0F) {
            f += (0.54600006F - f) * f2;
            f1 += (entity.getSpeed() - f1) * f2;
        }

        if (entity.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            f = 0.96F;
        }

        f1 *= (float)entity.getAttributeValue(NeoForgeMod.SWIM_SPEED);
        entity.moveRelative(f1, movementVector);
        entity.move(MoverType.SELF, entity.getDeltaMovement());
        Vec3 vec3 = entity.getDeltaMovement();
        if (entity.horizontalCollision && entity.onClimbable()) {
            vec3 = new Vec3(vec3.x, 0.2, vec3.z);
        }

        vec3 = vec3.multiply(f, 0.8F, f);
        entity.setDeltaMovement(entity.getFluidFallingAdjustedMovement(gravity, flag, vec3));
        return true;
    }

    @Override
    public boolean move(final ItemEntity entity, final Vec3 movementVector, final double gravity)
    {
        // TODO: this might be better off having an API to say "move like water" or "move like lava"
        entity.setDeltaMovement(movementVector.x * 0.99, movementVector.y + (movementVector.y < 0.06F ? 5.0E-4F : 0.0F), movementVector.z * 0.99);
        return true;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    public static class Flowing extends DefaultMilkFluid {
        public Flowing() {
            super("flowing_milk");
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> p_76476_) {
            super.createFluidStateDefinition(p_76476_);
            p_76476_.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState p_76480_) {
            return p_76480_.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState p_76478_) {
            return false;
        }
    }

    public static class Source extends DefaultMilkFluid {
        public Source() {
            super("milk");
        }

        @Override
        public int getAmount(FluidState p_76485_) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState p_76483_) {
            return true;
        }
    }
}
