/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;
import net.neoforged.neoforge.common.conditions.NeoForgeConditions;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.GeneratingOverlayMetadataSection;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber
@Mod(DataGeneratorTest.MODID)
public class DataGeneratorTest {
    static final String MODID = "data_gen_test";

    private static Gson GSON = null;

    // Datapack registry objects
    private static final ResourceKey<NoiseGeneratorSettings> TEST_SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS, ResourceLocation.fromNamespaceAndPath(MODID, "test_settings"));
    private static final ResourceKey<LevelStem> TEST_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, ResourceLocation.fromNamespaceAndPath(MODID, "test_level_stem"));
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.NOISE_SETTINGS, context -> context.register(TEST_SETTINGS, NoiseGeneratorSettings.floatingIslands(context)))
            .add(Registries.LEVEL_STEM, DataGeneratorTest::levelStem);

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        GSON = new GsonBuilder()
                .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
                .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
                .create();

        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        gen.addProvider(true, new PackMetadataGenerator(packOutput)
                .add(GeneratingOverlayMetadataSection.neoforgeType(PackType.SERVER_DATA), new GeneratingOverlayMetadataSection(List.of(
                        new WithConditions<>(new OverlayMetadataSection.OverlayEntry(new InclusiveRange<>(PackFormat.of(0), PackFormat.of(Integer.MAX_VALUE)), "neoforge_overlays_test")))))
                .add(GeneratingOverlayMetadataSection.type(PackType.SERVER_DATA), new GeneratingOverlayMetadataSection(List.of(
                        new WithConditions<>(new OverlayMetadataSection.OverlayEntry(new InclusiveRange<>(PackFormat.of(0), PackFormat.of(Integer.MAX_VALUE)), "pack_overlays_test")),
                        new WithConditions<>(new OverlayMetadataSection.OverlayEntry(new InclusiveRange<>(PackFormat.of(0), PackFormat.of(Integer.MAX_VALUE)), "conditional_overlays_enabled"), NeoForgeConditions.modLoaded(NeoForgeVersion.MOD_ID)),
                        new WithConditions<>(new OverlayMetadataSection.OverlayEntry(new InclusiveRange<>(PackFormat.of(0), PackFormat.of(Integer.MAX_VALUE)), "conditional_overlays_disabled"), NeoForgeConditions.modLoaded("does_not_exist")))))
                .add(PackMetadataSection.CLIENT_TYPE, new PackMetadataSection(
                        Component.literal("NeoForge tests resource pack"),
                        new InclusiveRange<>(PackFormat.of(0), PackFormat.of(Integer.MAX_VALUE, Integer.MAX_VALUE)))));
        gen.addProvider(true, new Lang(packOutput));
        gen.addProvider(true, new SoundDefinitions(packOutput, event.getResourceManager(PackType.CLIENT_RESOURCES)));
        gen.addProvider(true, new ParticleDescriptions(packOutput, event.getResourceManager(PackType.CLIENT_RESOURCES)));

        gen.addProvider(true, new Recipes.Runner(packOutput, lookupProvider));
        gen.addProvider(true, new Tags(packOutput, lookupProvider));
        gen.addProvider(true, new AdvancementProvider(packOutput, lookupProvider, List.of(new Advancements())));
        gen.addProvider(true, new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, BUILDER, Set.of(MODID)));
    }

    public static void levelStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGeneratorSettings = context.lookup(Registries.NOISE_SETTINGS);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        Holder<DimensionType> holder2 = dimensionTypes.getOrThrow(BuiltinDimensionTypes.END);
        Holder<NoiseGeneratorSettings> holder3 = noiseGeneratorSettings.getOrThrow(NoiseGeneratorSettings.END);
        LevelStem levelStem = new LevelStem(holder2, new NoiseBasedChunkGenerator(TheEndBiomeSource.create(biomes), holder3));
        context.register(TEST_LEVEL_STEM, levelStem);
    }

    public static class Recipes extends RecipeProvider {
        public Recipes(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
        }

        private static ResourceKey<Recipe<?>> recipeKey(String path) {
            return ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath("data_gen_test", path));
        }

        @Override
        protected void buildRecipes() {
            // conditional recipe
            this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DIAMOND_BLOCK, 64)
                    .pattern("XXX")
                    .pattern("XXX")
                    .pattern("XXX")
                    .define('X', Blocks.DIRT)
                    .group("")
                    .unlockedBy("has_dirt", has(Blocks.DIRT))
                    .save(
                            output.withConditions(
                                    NeoForgeConditions.and(
                                            NeoForgeConditions.not(NeoForgeConditions.modLoaded("minecraft")),
                                            NeoForgeConditions.itemRegistered("minecraft", "dirt"),
                                            NeoForgeConditions.never())),
                            recipeKey("conditional"));

            this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DIAMOND_BLOCK, 64)
                    .pattern("XXX")
                    .pattern("XXX")
                    .pattern("XXX")
                    .define('X', Blocks.DIRT)
                    .group("")
                    .unlockedBy("has_dirt", has(Blocks.DIRT))
                    .save(
                            output.withConditions(
                                    NeoForgeConditions.not(
                                            NeoForgeConditions.and(
                                                    NeoForgeConditions.not(NeoForgeConditions.modLoaded("minecraft")),
                                                    NeoForgeConditions.itemRegistered("minecraft", "dirt"),
                                                    NeoForgeConditions.never()))),
                            recipeKey("conditional2"));

            this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.NETHERITE_BLOCK, 1)
                    .pattern("XX")
                    .pattern("XX")
                    .define('X', Blocks.DIAMOND_BLOCK)
                    .group("")
                    .unlockedBy("has_diamond_block", has(Blocks.DIAMOND_BLOCK))
                    .save(
                            output.withConditions(
                                    NeoForgeConditions.tagEmpty(ItemTags.PLANKS)),
                            recipeKey("conditional3"));

            this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.NETHERITE_BLOCK, 9)
                    .pattern("XX")
                    .pattern("XX")
                    .define('X', Blocks.DIAMOND_BLOCK)
                    .group("")
                    .unlockedBy("has_diamond_block", has(Blocks.DIAMOND_BLOCK))
                    .save(
                            output.withConditions(
                                    NeoForgeConditions.not(NeoForgeConditions.tagEmpty(ItemTags.PLANKS))),
                            recipeKey("conditional4"));

            // intersection - should match all non-flammable planks
            this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.NETHERRACK)
                    .pattern("###")
                    .pattern("###")
                    .pattern(" # ")
                    .define('#', IntersectionIngredient.of(tag(ItemTags.PLANKS), tag(ItemTags.NON_FLAMMABLE_WOOD)))
                    .unlockedBy("has_planks", has(Items.CRIMSON_PLANKS))
                    .save(output, recipeKey("intersection_ingredient"));

            // difference - should match all flammable fences
            this.shaped(RecipeCategory.TOOLS, Items.FLINT_AND_STEEL)
                    .pattern(" # ")
                    .pattern("###")
                    .pattern(" # ")
                    .define('#', DifferenceIngredient.of(tag(ItemTags.FENCES), tag(ItemTags.NON_FLAMMABLE_WOOD)))
                    .unlockedBy("has_fence", has(Items.CRIMSON_FENCE))
                    .save(output, recipeKey("difference_ingredient"));

            // compound - should match planks, logs, or bedrock
            this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.DIRT)
                    .pattern("###")
                    .pattern(" # ")
                    .define('#', CompoundIngredient.of(tag(ItemTags.PLANKS), tag(ItemTags.LOGS), Ingredient.of(Blocks.BEDROCK)))
                    .unlockedBy("has_planks", has(Items.CRIMSON_PLANKS))
                    .save(output, recipeKey("compound_ingredient_only_vanilla"));

            // compound - should match planks, logs, or a stone pickaxe with 3 damage
            this.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.GOLD_BLOCK)
                    .pattern("#")
                    .pattern("#")
                    .define('#', CompoundIngredient.of(tag(ItemTags.PLANKS), tag(ItemTags.LOGS), net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(true, Util.make(() -> {
                        ItemStack stack = new ItemStack(Items.STONE_PICKAXE);
                        stack.setDamageValue(3);
                        return stack;
                    }))))
                    .unlockedBy("has_planks", has(Items.CRIMSON_PLANKS))
                    .save(output, recipeKey("compound_ingredient_custom_types"));
        }

        private static class Runner extends RecipeProvider.Runner {
            protected Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
                super(packOutput, registries);
            }

            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
                return new Recipes(registries, output);
            }

            @Override
            public String getName() {
                return "DataGeneratorTest Recipes";
            }
        }
    }

    public static class SoundDefinitions extends SoundDefinitionsProvider {
        private static final Logger LOGGER = LogManager.getLogger();

        private final ResourceManager resourceManager;

        public SoundDefinitions(final PackOutput output, final ResourceManager resourceManager) {
            super(output, MODID);

            this.resourceManager = resourceManager;
        }

        @Override
        public void registerSounds() {
            // ambient.underwater.loop.additions
            this.add(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS, definition().with(
                    sound("ambient/underwater/additions/bubbles1"),
                    sound("ambient/underwater/additions/bubbles2"),
                    sound("ambient/underwater/additions/bubbles3"),
                    sound("ambient/underwater/additions/bubbles4"),
                    sound("ambient/underwater/additions/bubbles5"),
                    sound("ambient/underwater/additions/bubbles6"),
                    sound("ambient/underwater/additions/water1"),
                    sound("ambient/underwater/additions/water2")));

            //ambient.underwater.loop.additions.ultra_rare
            this.add(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, definition().with(
                    sound("ambient/underwater/additions/animal2"),
                    sound("ambient/underwater/additions/dark1"),
                    sound("ambient/underwater/additions/dark2").volume(0.7),
                    sound("ambient/underwater/additions/dark3"),
                    sound("ambient/underwater/additions/dark4")));

            //block.lava.ambient
            this.add(SoundEvents.LAVA_AMBIENT, definition().with(sound("liquid/lava")).subtitle("subtitles.block.lava.ambient"));

            //entity.dolphin.ambient_water
            this.add(SoundEvents.DOLPHIN_AMBIENT_WATER, definition().with(
                    sound("mob/dolphin/idle_water1").volume(0.8),
                    sound("mob/dolphin/idle_water2"),
                    sound("mob/dolphin/idle_water3"),
                    sound("mob/dolphin/idle_water4"),
                    sound("mob/dolphin/idle_water5"),
                    sound("mob/dolphin/idle_water6"),
                    sound("mob/dolphin/idle_water7").volume(0.75),
                    sound("mob/dolphin/idle_water8").volume(0.75),
                    sound("mob/dolphin/idle_water9"),
                    sound("mob/dolphin/idle_water10").volume(0.8)).subtitle("subtitles.entity.dolphin.ambient_water"));

            //entity.parrot.imitate.drowned
            this.add(SoundEvents.PARROT_IMITATE_DROWNED, definition().with(
                    sound("entity.drowned.ambient", SoundDefinition.SoundType.EVENT).pitch(1.8).volume(0.6)).subtitle("subtitles.entity.parrot.imitate.drowned"));

            //item.trident.return
            this.add(SoundEvents.TRIDENT_RETURN, definition().with(
                    sound("item/trident/return1").volume(0.8),
                    sound("item/trident/return2").volume(0.8),
                    sound("item/trident/return2").pitch(0.8).volume(0.8),
                    sound("item/trident/return2").pitch(1.2).volume(0.8),
                    sound("item/trident/return2").pitch(1.2).volume(0.8),
                    sound("item/trident/return3").volume(0.8),
                    sound("item/trident/return3").pitch(0.8).volume(0.8),
                    sound("item/trident/return3").pitch(0.8).volume(0.8),
                    sound("item/trident/return3").pitch(1.2).volume(0.8)).subtitle("subtitles.item.trident.return"));

            //music_disc.blocks
            this.add(SoundEvents.MUSIC_DISC_BLOCKS.value(), definition().with(sound("records/blocks").stream()));
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            return super.run(cache).thenRun(this::test);
        }

        private void test() {
            final JsonObject generated;
            try {
                generated = reflect();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Unable to test for errors due to reflection error", e);
            }
            final JsonObject actual;
            try {
                List<Resource> resourceStack = this.resourceManager.getResourceStack(ResourceLocation.withDefaultNamespace("sounds.json"));
                // Get the first resource in the stack
                // This guarantees vanilla even when a forge sounds.json is present because getResourceStack reverses the list
                // so that the lower priority resources are first (to allow overwriting data in later entries)
                Resource vanillaSoundResource = resourceStack.get(0);
                actual = GSON.fromJson(
                        vanillaSoundResource.openAsReader(),
                        JsonObject.class);
            } catch (IOException e) {
                throw new RuntimeException("Unable to test for errors due to missing sounds.json", e);
            }

            final JsonObject filtered = new JsonObject();
            generated.entrySet().forEach(it -> filtered.add(it.getKey(), Optional.ofNullable(actual.get(it.getKey())).orElseGet(JsonNull::new)));

            final List<String> errors = this.compareObjects(filtered, generated);

            if (!errors.isEmpty()) {
                LOGGER.error("Found {} discrepancies between generated and vanilla sound definitions: ", errors.size());
                for (String s : errors) {
                    LOGGER.error("    {}", s);
                }
                throw new RuntimeException("Generated sounds.json differed from vanilla equivalent, check above errors.");
            }
        }

        private JsonObject reflect() throws ReflectiveOperationException {
            // This is not supposed to be done by client code, so we just run with reflection to avoid exposing
            // something that shouldn't be exposed in the first place
            final Method mapToJson = this.getClass().getSuperclass().getDeclaredMethod("mapToJson", Map.class);
            mapToJson.setAccessible(true);
            final Field map = this.getClass().getSuperclass().getDeclaredField("sounds");
            map.setAccessible(true);
            //noinspection JavaReflectionInvocation
            return (JsonObject) mapToJson.invoke(this, map.get(this));
        }

        private List<String> compareAndGatherErrors(final Triple<String, JsonElement, JsonElement> triple) {
            return this.compare(triple.getMiddle(), triple.getRight()).stream().map(it -> triple.getLeft() + ": " + it).collect(Collectors.toList());
        }

        private List<String> compare(final JsonElement vanilla, @Nullable final JsonElement generated) {
            if (generated == null) {
                return Collections.singletonList("vanilla element has no generated counterpart");
            } else if (vanilla.isJsonPrimitive()) {
                return this.comparePrimitives(vanilla.getAsJsonPrimitive(), generated);
            } else if (vanilla.isJsonObject()) {
                return this.compareObjects(vanilla.getAsJsonObject(), generated);
            } else if (vanilla.isJsonArray()) {
                return this.compareArrays(vanilla.getAsJsonArray(), generated);
            } else if (vanilla.isJsonNull() && !generated.isJsonNull()) {
                return Collections.singletonList("null value in vanilla doesn't match non-null value in generated");
            }
            throw new RuntimeException("Unable to match " + vanilla + " to any JSON type");
        }

        private List<String> comparePrimitives(final JsonPrimitive vanilla, final JsonElement generated) {
            if (!generated.isJsonPrimitive()) return Collections.singletonList("Primitive in vanilla isn't matched by generated " + generated);

            final JsonPrimitive generatedPrimitive = generated.getAsJsonPrimitive();

            if (vanilla.isBoolean()) {
                if (!generatedPrimitive.isBoolean()) return Collections.singletonList("Boolean in vanilla isn't matched by non-boolean " + generatedPrimitive);

                if (vanilla.getAsBoolean() != generated.getAsBoolean()) {
                    return Collections.singletonList("Boolean '" + vanilla.getAsBoolean() + "' does not match generated '" + generatedPrimitive.getAsBoolean() + "'");
                }
            } else if (vanilla.isNumber()) {
                if (!generatedPrimitive.isNumber()) return Collections.singletonList("Number in vanilla isn't matched by non-number " + generatedPrimitive);

                // Handle numbers via big decimal so we are sure there isn't any sort of errors due to float/long
                final BigDecimal vanillaNumber = vanilla.getAsBigDecimal();
                final BigDecimal generatedNumber = vanilla.getAsBigDecimal();

                if (vanillaNumber.compareTo(generatedNumber) != 0) {
                    return Collections.singletonList("Number '" + vanillaNumber + "' does not match generated '" + generatedNumber + "'");
                }
            } else if (vanilla.isString()) {
                if (!generatedPrimitive.isString()) return Collections.singletonList("String in vanilla isn't matched by non-string " + generatedPrimitive);

                if (!vanilla.getAsString().equals(generatedPrimitive.getAsString())) {
                    return Collections.singletonList("String '" + vanilla.getAsString() + "' does not match generated '" + generatedPrimitive.getAsString() + "'");
                }
            }

            return new ArrayList<>();
        }

        private List<String> compareObjects(final JsonObject vanilla, final JsonElement generated) {
            if (!generated.isJsonObject()) return Collections.singletonList("Object in vanilla isn't matched by generated " + generated);

            final JsonObject generatedObject = generated.getAsJsonObject();

            return vanilla.entrySet().stream()
                    .map(it -> Triple.of(it.getKey(), it.getValue(), generatedObject.get(it.getKey())))
                    .map(this::compareAndGatherErrors)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        private List<String> compareArrays(final JsonArray vanilla, final JsonElement generated) {
            if (!generated.isJsonArray()) return Collections.singletonList("Array in vanilla isn't matched by generated " + generated);

            final JsonArray generatedArray = generated.getAsJsonArray();

            return IntStream.range(0, vanilla.size())
                    .mapToObj(it -> Triple.of("[" + it + "]", vanilla.get(it), generatedArray.get(it)))
                    .map(this::compareAndGatherErrors)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
    }

    public static class Tags extends BlockTagsProvider {
        public Tags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider, MODID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "test")))
                    .add(Blocks.DIAMOND_BLOCK)
                    .addTag(BlockTags.STONE_BRICKS)
                    .addTag(net.neoforged.neoforge.common.Tags.Blocks.COBBLESTONES)
                    .add(TagEntry.optionalElement(ResourceLocation.fromNamespaceAndPath("chisel", "marble/raw")))
                    .add(TagEntry.optionalTag(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "storage_blocks/ruby")));

            // Hopefully sorting issues
            tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "thing/one")))
                    .add(Blocks.COBBLESTONE);
            tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "thing/two")))
                    .add(Blocks.DIORITE);
            tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "thing/three")))
                    .add(Blocks.ANDESITE);

            tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath(MODID, "things")))
                    .add(Blocks.COBBLESTONE)
                    .add(Blocks.DIORITE)
                    .add(Blocks.ANDESITE);
        }
    }

    public static class Lang extends LanguageProvider {
        public Lang(PackOutput gen) {
            super(gen, MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add(Blocks.STONE, "Stone");
            add(Items.DIAMOND, "Diamond");
            //add(Biomes.BEACH, "Beach");
            add(MobEffects.POISON.value(), "Poison");
            add(EntityType.CAT, "Cat");
            add(MODID + ".test.unicode", "\u0287s\u01DD\u2534 \u01DDpo\u0254\u1D09u\u2229");
        }
    }

    private static class Advancements implements AdvancementSubProvider {
        @Override
        @SuppressWarnings("removal")
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
            var obtainDirt = Advancement.Builder.advancement()
                    .display(Items.DIRT,
                            Component.translatable(Items.DIRT.getDescriptionId()),
                            Component.translatable("dirt_description"),
                            ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
                            AdvancementType.TASK,
                            true,
                            true,
                            false)
                    .addCriterion("has_dirt", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIRT))
                    .save(saver, ResourceLocation.fromNamespaceAndPath(MODID, "obtain_dirt"));

            Advancement.Builder.advancement()
                    .parent(obtainDirt)
                    .display(Items.DIAMOND_BLOCK,
                            Component.translatable(Items.DIAMOND_BLOCK.getDescriptionId()),
                            Component.literal("You obtained a DiamondBlock"),
                            ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
                            AdvancementType.CHALLENGE,
                            true,
                            true,
                            false)
                    .addCriterion("obtained_diamond_block", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_BLOCK))
                    .save(saver, ResourceLocation.withDefaultNamespace("obtain_diamond_block"));

            Advancement.Builder.advancement()
                    .display(Blocks.GRASS_BLOCK,
                            Component.translatable("advancements.story.root.title"),
                            Component.literal("Changed Description"),
                            ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
                            AdvancementType.TASK,
                            false,
                            false,
                            false)
                    .addCriterion("crafting_table", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.CRAFTING_TABLE))
                    .save(saver, ResourceLocation.withDefaultNamespace("story/root"));

            // This should cause an error because of the parent not existing
/*            Advancement.Builder.advancement().display(Blocks.COBBLESTONE,
        new TranslationTextComponent(Items.COBBLESTONE.getDescriptionId()),
        new StringTextComponent("You got cobblestone"),
        ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
        AdvancementType.TASK,
        false,
        false,
        false)
        .addCriterion("get_cobbleStone", InventoryChangeTrigger.Instance.hasItems(Items.COBBLESTONE))
        .parent(ResourceLocation.withDefaultNamespace("not_there/not_here"))
        .save(consumer, ResourceLocation.withDefaultNamespace("illegal_parent"), fileHelper);*/

            Advancement.Builder.advancement().display(Blocks.COBBLESTONE,
                    Component.translatable(Items.COBBLESTONE.getDescriptionId()),
                    Component.literal("You got cobblestone"),
                    ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
                    AdvancementType.TASK,
                    false,
                    false,
                    false)
                    .addCriterion("get_cobbleStone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COBBLESTONE))
                    .parent(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "dummy_parent"))
                    .save(saver, ResourceLocation.withDefaultNamespace("good_parent"));
        }
    }

    private static class ParticleDescriptions extends ParticleDescriptionProvider {
        private final ResourceManager resourceManager;

        public ParticleDescriptions(PackOutput output, ResourceManager resourceManager) {
            super(output);

            this.resourceManager = resourceManager;
        }

        @Override
        protected void addDescriptions() {
            this.spriteSet(ParticleTypes.DRIPPING_LAVA, ResourceLocation.withDefaultNamespace("drip_hang"));

            this.spriteSet(ParticleTypes.CLOUD, ResourceLocation.withDefaultNamespace("generic"), 8, true);

            this.spriteSet(ParticleTypes.FISHING,
                    ResourceLocation.withDefaultNamespace("splash_0"),
                    ResourceLocation.withDefaultNamespace("splash_1"),
                    ResourceLocation.withDefaultNamespace("splash_2"),
                    ResourceLocation.withDefaultNamespace("splash_3"));

            this.spriteSet(ParticleTypes.ENCHANT, () -> new Iterator<>() {
                private final ResourceLocation base = ResourceLocation.withDefaultNamespace("sga");
                private char suffix = 'a';

                @Override
                public boolean hasNext() {
                    return this.suffix <= 'z';
                }

                @Override
                public ResourceLocation next() {
                    return this.base.withSuffix("_" + this.suffix++);
                }
            });
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            return super.run(cache).thenRun(this::validateResults);
        }

        private void validateResults() {
            var errors = Stream.of(ParticleTypes.DRIPPING_LAVA, ParticleTypes.CLOUD, ParticleTypes.FISHING, ParticleTypes.ENCHANT)
                    .map(BuiltInRegistries.PARTICLE_TYPE::getKey).map(particle -> {
                        try (var resource = this.resourceManager.openAsReader(particle.withPath(path -> "particles/" + path + ".json"))) {
                            var existingTextures = GSON.fromJson(resource, JsonObject.class).get("textures").getAsJsonArray();
                            var generatedTextures = this.descriptions.get(particle);

                            // Check texture size
                            if (existingTextures.size() != generatedTextures.size()) {
                                LOGGER.error("{} had a different number of sprites, expected {}, actual {}", particle, existingTextures.size(), generatedTextures.size());
                                return particle;
                            }

                            boolean error = false;
                            for (int i = 0; i < generatedTextures.size(); ++i) {
                                if (!existingTextures.get(i).getAsString().equals(generatedTextures.get(i))) {
                                    LOGGER.error("{} index {}: expected {}, actual {}", particle, i, existingTextures.get(i).getAsString(), generatedTextures.get(i));
                                    error = true;
                                }
                            }

                            return error ? particle : null;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).filter(Objects::nonNull).toList();

            if (!errors.isEmpty()) {
                throw new AssertionError(String.format("Validation errors found in %s; see above for details", errors.stream().reduce("", (str, rl) -> str + ", " + rl, (str1, str2) -> str1 + ", " + str2)));
            }
        }
    }
}
