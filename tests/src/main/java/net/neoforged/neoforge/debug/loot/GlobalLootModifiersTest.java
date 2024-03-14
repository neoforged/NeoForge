/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.condition.TestEnabledLootCondition;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "loot", idPrefix = "glm_")
public class GlobalLootModifiersTest {
    public static final RegistrationHelper HELPER = RegistrationHelper.create("neotests_glm_test");

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM = HELPER.registrar(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS);
    private static final DeferredRegister<Enchantment> ENCHANTS = HELPER.registrar(Registries.ENCHANTMENT);

    private static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<DungeonLootEnhancerModifier>> DUNGEON_LOOT = GLM.register("dungeon_loot", DungeonLootEnhancerModifier.CODEC);
    private static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<SmeltingEnchantmentModifier>> SMELTING = GLM.register("smelting", SmeltingEnchantmentModifier.CODEC);
    private static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<WheatSeedsConverterModifier>> WHEATSEEDS = GLM.register("wheat_harvest", WheatSeedsConverterModifier.CODEC);
    private static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<SilkTouchTestModifier>> SILKTOUCH = GLM.register("silk_touch_bamboo", SilkTouchTestModifier.CODEC);
    private static final DeferredHolder<Enchantment, Enchantment> SMELT = ENCHANTS.register("smelt", () -> new SmelterEnchantment(Rarity.UNCOMMON, ItemTags.MINING_ENCHANTABLE, EquipmentSlot.MAINHAND));

    @OnInit
    static void init(final TestFramework framework) {
        HELPER.register(framework.modEventBus());
    }

    private static class SmelterEnchantment extends Enchantment {
        protected SmelterEnchantment(Rarity rarityIn, TagKey<Item> typeIn, EquipmentSlot... slots) {
            super(rarityIn, typeIn, slots);
        }
    }

    /**
     * The smelting enchantment causes this modifier to be invoked, via the smelting loot_modifier json
     */
    private static class SmeltingEnchantmentModifier extends LootModifier {
        public static final Supplier<Codec<SmeltingEnchantmentModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SmeltingEnchantmentModifier::new)));

        public SmeltingEnchantmentModifier(LootItemCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Override
        public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            ObjectArrayList<ItemStack> ret = new ObjectArrayList<ItemStack>();
            generatedLoot.forEach((stack) -> ret.add(smelt(stack, context)));
            return ret;
        }

        private static ItemStack smelt(ItemStack stack, LootContext context) {
            return context.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), context.getLevel())
                    .map(smeltingRecipe -> smeltingRecipe.value().getResultItem(context.getLevel().registryAccess()))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(itemStack -> ItemHandlerHelper.copyStackWithSize(itemStack, stack.getCount() * itemStack.getCount()))
                    .orElse(stack);
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    /**
     * When harvesting blocks with bamboo, this modifier is invoked, via the silk_touch_bamboo loot_modifier json
     */
    private static class SilkTouchTestModifier extends LootModifier {
        public static final Supplier<Codec<SilkTouchTestModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SilkTouchTestModifier::new)));

        public SilkTouchTestModifier(LootItemCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Override
        public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            ItemStack ctxTool = context.getParamOrNull(LootContextParams.TOOL);
            //return early if silk-touch is already applied (otherwise we'll get stuck in an infinite loop).
            if (ctxTool == null || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, ctxTool) > 0)
                return generatedLoot;
            ItemStack fakeTool = ctxTool.copy();
            fakeTool.enchant(Enchantments.SILK_TOUCH, 1);
            LootParams.Builder builder = new LootParams.Builder(context.getLevel());
            builder.withParameter(LootContextParams.TOOL, fakeTool);
            LootTable loottable = context.getLevel().getServer().getLootData().getLootTable(context.getParamOrNull(LootContextParams.BLOCK_STATE).getBlock().getLootTable());
            return loottable.getRandomItems(builder.create(LootContextParamSets.EMPTY)); // TODO - porting: we need an AT
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    /**
     * When harvesting wheat with shears, this modifier is invoked via the wheat_harvest loot_modifier json<br/>
     * This modifier checks how many seeds were harvested and turns X seeds into Y wheat (3:1)
     */
    private static class WheatSeedsConverterModifier extends LootModifier {
        public static final Supplier<Codec<WheatSeedsConverterModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).and(
                inst.group(
                        Codec.INT.fieldOf("numSeeds").forGetter(m -> m.numSeedsToConvert),
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("seedItem").forGetter(m -> m.itemToCheck),
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("replacement").forGetter(m -> m.itemReward)))
                .apply(inst, WheatSeedsConverterModifier::new)));

        private final int numSeedsToConvert;
        private final Item itemToCheck;
        private final Item itemReward;

        public WheatSeedsConverterModifier(LootItemCondition[] conditionsIn, int numSeeds, Item itemCheck, Item reward) {
            super(conditionsIn);
            numSeedsToConvert = numSeeds;
            itemToCheck = itemCheck;
            itemReward = reward;
        }

        @Override
        public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            //
            // Additional conditions can be checked, though as much as possible should be parameterized via JSON data.
            // It is better to write a new ILootCondition implementation than to do things here.
            //
            int numSeeds = 0;
            for (ItemStack stack : generatedLoot) {
                if (stack.getItem() == itemToCheck)
                    numSeeds += stack.getCount();
            }
            if (numSeeds >= numSeedsToConvert) {
                generatedLoot.removeIf(x -> x.getItem() == itemToCheck);
                generatedLoot.add(new ItemStack(itemReward, (numSeeds / numSeedsToConvert)));
                numSeeds = numSeeds % numSeedsToConvert;
                if (numSeeds > 0)
                    generatedLoot.add(new ItemStack(itemToCheck, numSeeds));
            }
            return generatedLoot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    private static class DungeonLootEnhancerModifier extends LootModifier {
        public static final Supplier<Codec<DungeonLootEnhancerModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst)
                .and(ExtraCodecs.POSITIVE_INT.optionalFieldOf("multiplication_factor", 2).forGetter(m -> m.multiplicationFactor))
                .apply(inst, DungeonLootEnhancerModifier::new)));

        private final int multiplicationFactor;

        public DungeonLootEnhancerModifier(final LootItemCondition[] conditionsIn, final int multiplicationFactor) {
            super(conditionsIn);
            this.multiplicationFactor = multiplicationFactor;
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            if (context.hasParam(LootContextParams.THIS_ENTITY)) {
                // Only modify if a player attempts to open it
                return generatedLoot.stream()
                        .map(ItemStack::copy)
                        .peek(stack -> stack.setCount(Math.min(stack.getMaxStackSize(), stack.getCount() * this.multiplicationFactor)))
                        .collect(Collectors.toCollection(ObjectArrayList::new));
            }
            return generatedLoot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if a GLM smelting the loot table rolls works")
    static void smeltingModifierTest(final DynamicTest test) {
        HELPER.provider(GlobalLootModifierProvider.class, prov -> prov.add("smelting", new SmeltingEnchantmentModifier(
                new LootItemCondition[] {
                        MatchTool.toolMatches(ItemPredicate.Builder.item().withSubPredicate(
                                ItemSubPredicates.ENCHANTMENTS,
                                ItemEnchantmentsPredicate.enchantments(
                                        List.of(new EnchantmentPredicate(SMELT.get(), MinMaxBounds.Ints.atLeast(1)))
                                )
                        )).build(),
                        new TestEnabledLootCondition(test)
                })));

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL).preventItemPickup())
                .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, Items.DIAMOND_PICKAXE.getDefaultInstance()))
                .thenExecute(player -> player.getItemInHand(InteractionHand.MAIN_HAND).enchant(SMELT.get(), 1))

                .thenSequence((sq, player) -> sq.thenMap(() -> new BlockPos(1, 2, 1))
                        .thenExecute(pos -> helper.setBlock(pos, Blocks.IRON_ORE))
                        .thenExecute(pos -> player.get().gameMode.destroyBlock(helper.absolutePos(pos)))
                        .thenIdle(5))

                .thenIdle(5)
                .thenSequence((sq, player) -> sq.thenMap(() -> new BlockPos(1, 3, 1))
                        .thenExecute(pos -> helper.setBlock(pos, Blocks.EMERALD_BLOCK))
                        .thenExecute(pos -> player.get().gameMode.destroyBlock(helper.absolutePos(pos)))
                        .thenIdle(5))

                .thenIdle(5)
                .thenMap(() -> new BlockPos(1, 2, 1))
                .thenExecute(pos -> helper.assertItemEntityCountIs(Items.IRON_INGOT, pos, 1d, 1))
                .thenExecute(pos -> helper.assertItemEntityCountIs(Items.EMERALD_BLOCK, pos, 1d, 1))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if a GLM replacing loot table values works, by replacing seeds with wheat when harvesting wheat")
    static void wheatSeedReplacerTest(final DynamicTest test) {
        HELPER.provider(GlobalLootModifierProvider.class, prov -> prov.add("wheat_harvest", new WheatSeedsConverterModifier(
                new LootItemCondition[] {
                        MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)).build(),
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.WHEAT).build(),
                        new TestEnabledLootCondition(test)
                },
                1, Items.WHEAT_SEEDS, Items.WHEAT)));

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL).preventItemPickup())
                .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, Items.SHEARS.getDefaultInstance()))

                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.FARMLAND))
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7)))

                .thenIdle(5)
                .thenExecute(player -> player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(1, 2, 1))))
                .thenIdle(5)
                // At least one seed will be dropped (which will be converted to wheat), and one wheat
                .thenExecute(player -> helper.assertItemEntityCountIsAtLeast(Items.WHEAT, new BlockPos(1, 2, 1), 1d, 2))
                .thenExecute(player -> helper.assertItemEntityNotPresent(Items.WHEAT_SEEDS, new BlockPos(1, 2, 1), 1d))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if dungeon loot modifiers work, by rolling the simple_dungeon loot table")
    static void dungeonLootTest(final DynamicTest test) {
        HELPER.provider(GlobalLootModifierProvider.class, prov -> prov.add("dungeon_loot", new DungeonLootEnhancerModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(new ResourceLocation("chests/simple_dungeon")).build(),
                        new TestEnabledLootCondition(test)
                },
                2)));

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST.defaultBlockState()))
                .thenMap(() -> helper.requireBlockEntity(1, 2, 1, ChestBlockEntity.class))
                .thenExecute(chest -> chest.setLootTable(new ResourceLocation("chests/simple_dungeon"), 124424))

                .thenExecute(chest -> chest.unpackLootTable(helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL)))

                .thenMap(chest -> IntStream.range(0, 27)
                        .mapToObj(chest::getItem)
                        .filter(Predicate.not(ItemStack::isEmpty))
                        .collect(Collectors.toMap(ItemStack::getItem, ItemStack::getCount, Integer::sum)))

                .thenMapToSequence(stacks -> helper
                        .startSequence(() -> helper.getLevel().getServer().getLootData().getLootTable(new ResourceLocation("chests/simple_dungeon"))
                                .getRandomItems(new LootParams.Builder(helper.getLevel())
                                        .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(new Vec3(1, 3, 1)))
                                        .create(LootContextParamSets.CHEST), 124424))
                        .thenMap(base -> base.stream()
                                .collect(Collectors.toMap(ItemStack::getItem, stack -> Math.min(stack.getMaxStackSize(), stack.getCount() * 2))))
                        .thenExecute(expected -> helper.assertTrue(
                                stacks.get().equals(expected),
                                "Stacks weren't as expected")))
                .thenSucceed());
    }
}
