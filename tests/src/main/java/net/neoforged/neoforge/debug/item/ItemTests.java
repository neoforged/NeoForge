package net.neoforged.neoforge.debug.item;

import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.eventtest.internal.TestsMod;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ForEachTest(groups = ItemTests.GROUP)
public class ItemTests {
    public static final String GROUP = "level.item";

    @GameTest
    @TestHolder(description = {
            "Tests if custom mob buckets work"
    })
    static void customMobBucket(final DynamicTest test, final RegistrationHelper reg) {
        final var cowBucket = reg.items().register("cow_bucket", () -> new MobBucketItem(
                EntityType.COW,
                Fluids.WATER,
                SoundEvents.BUCKET_EMPTY_FISH,
                (new Item.Properties()).stacksTo(1)))
                .withLang("Cow bucket");
        test.framework().modEventBus().addListener((final FMLCommonSetupEvent event) -> {
            DispenserBlock.registerBehavior(cowBucket, new DefaultDispenseItemBehavior() {
                private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

                @Override
                public ItemStack execute(BlockSource p_302435_, ItemStack p_123562_) {
                    DispensibleContainerItem dispensiblecontaineritem = (DispensibleContainerItem)p_123562_.getItem();
                    BlockPos blockpos = p_302435_.pos().relative(p_302435_.state().getValue(DispenserBlock.FACING));
                    Level level = p_302435_.level();
                    if (dispensiblecontaineritem.emptyContents(null, level, blockpos, null, p_123562_)) {
                        dispensiblecontaineritem.checkExtraContent(null, level, p_123562_, blockpos);
                        return new ItemStack(Items.BUCKET);
                    } else {
                        return this.defaultDispenseItemBehavior.dispense(p_302435_, p_123562_);
                    }
                }
            });
        });

        test.registerGameTestTemplate(StructureTemplateBuilder.withSize(3, 4, 3)
                .placeWaterConfinement(1, 1, 1, Blocks.IRON_BLOCK.defaultBlockState(), true)
                .placeWaterConfinement(1, 2, 1, Blocks.GOLD_BLOCK.defaultBlockState(), false));

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP)))
                .thenExecute(() -> ((DispenserBlockEntity) helper.getBlockEntity(new BlockPos(1, 1, 1))).setItem(0, cowBucket.get().getDefaultInstance()))
                .thenExecute(() -> helper.pulseRedstone(new BlockPos(1, 1, 2), 3))
//                .thenExecute(() -> cowBucket.get().emptyContents(
//                        helper.makeMockPlayer(), helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)),
//                        new BlockHitResult(helper.absoluteVec(new BlockPos(1, 2, 1).getCenter()), Direction.UP, helper.absolutePos(new BlockPos(1, 2, 1)), true),
//                        cowBucket.get().getDefaultInstance()
//                ))
                .thenIdle(5)
                .thenExecute(() -> helper.assertBlockPresent(Blocks.WATER, new BlockPos(1, 2, 1)))
                .thenExecute(() -> helper.assertEntityPresent(EntityType.COW, 1, 3, 1))
                .thenExecute(() -> helper.killAllEntitiesOfClass(Cow.class))
                .thenSucceed());
    }

    @TestHolder(description = {
            "Tests if the forge spawn egg works"
    })
    @GameTest(template = TestsMod.TEMPLATE_3x3)
    static void forgeSpawnEggTest(final DynamicTest test, final RegistrationHelper reg) {
        final var testEntity = reg.entityTypes().registerType("test_entity", () -> EntityType.Builder.of(Pig::new, MobCategory.CREATURE)
                .sized(1, 1)).withAttributes(() -> {
            AttributeSupplier.Builder attributes = Pig.createAttributes();
            //Remove step height attribute to validate that things are handled properly when an entity doesn't have it
            Map<Attribute, AttributeInstance> builder = ObfuscationReflectionHelper.getPrivateValue(AttributeSupplier.Builder.class, attributes, "builder");
            if (builder != null) {
                builder.remove(NeoForgeMod.STEP_HEIGHT.value());
            }
            return attributes;
        }).withRenderer(() -> PigRenderer::new).withLang("Test Pig spawn egg");

        final var egg = reg.items().register("test_spawn_egg", () -> new DeferredSpawnEggItem(testEntity, 0x0000FF, 0xFF0000, new Item.Properties()) {

                    @Override
                    public InteractionResult useOn(UseOnContext ctx) {
                        final var result = super.useOn(ctx);
                        if (result.consumesAction()) {
                            test.pass();
                        }
                        return result;
                    }

                    @Override
                    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
                        final var sup = super.use(level, player, hand);
                        if (sup.getResult().consumesAction()) {
                            test.pass();
                        }
                        return sup;
                    }

                    @Override
                    public boolean spawnsEntity(@Nullable CompoundTag p_43231_, EntityType<?> p_43232_) {
                        return super.spawnsEntity(p_43231_, p_43232_);
                    }
                })
                .tab(CreativeModeTabs.SPAWN_EGGS).withModel(builder ->
                        builder.parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft:item/template_spawn_egg"))));

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.IRON_BLOCK))
                .thenExecute(() -> helper.useBlock(new BlockPos(1, 1, 1), helper.makeMockPlayer(), egg.get().getDefaultInstance(), Direction.UP))
                .thenExecuteAfter(10, () -> helper.assertEntityPresent(testEntity.get(), 1, 2, 1))
                .thenExecute(() -> helper.killAllEntitiesOfClass(Pig.class))
                .thenSucceed());
    }
}
