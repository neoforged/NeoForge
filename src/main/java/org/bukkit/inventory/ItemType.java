package org.bukkit.inventory;

import com.google.common.collect.Multimap;
import java.util.function.Consumer;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Translatable;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.registry.RegistryAware;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * While this API is in a public interface, it is not intended for use by
 * plugins until further notice. The purpose of these types is to make
 * {@link Material} more maintenance friendly, but will in due time be the
 * official replacement for the aforementioned enum. Entirely incompatible
 * changes may occur. Do not use this API in plugins.
 */
@ApiStatus.Internal
public interface ItemType extends Keyed, Translatable, RegistryAware {

    /**
     * Typed represents a subtype of {@link ItemType}s that have a known item meta type
     * at compile time.
     *
     * @param <M> the generic type of the item meta that represents the item type.
     */
    interface Typed<M extends ItemMeta> extends ItemType {

        /**
         * Gets the ItemMeta class of this ItemType
         *
         * @return the ItemMeta class of this ItemType
         */
        @Override
        @NotNull
        Class<M> getItemMetaClass();

        /**
         * Constructs a new item stack with this item type with the amount 1.
         *
         * @param metaConfigurator an optional consumer of the items {@link ItemMeta} that is called.
         *                         May be null if no intent exists to mutate the item meta at this point.
         * @return the created and configured item stack.
         */
        @NotNull
        ItemStack createItemStack(@Nullable Consumer<? super M> metaConfigurator);

        /**
         * Constructs a new item stack with this item type.
         *
         * @param amount           the amount of itemstack.
         * @param metaConfigurator an optional consumer of the items {@link ItemMeta} that is called.
         *                         May be null if no intent exists to mutate the item meta at this point.
         * @return the created and configured item stack.
         */
        @NotNull
        ItemStack createItemStack(int amount, @Nullable Consumer<? super M> metaConfigurator);
    }

    //<editor-fold desc="ItemTypes" defaultstate="collapsed">
    /**
     * Air does not have any ItemMeta
     */
    ItemType AIR = getItemType("air");
    Typed<ItemMeta> STONE = getItemType("stone");
    Typed<ItemMeta> GRANITE = getItemType("granite");
    Typed<ItemMeta> POLISHED_GRANITE = getItemType("polished_granite");
    Typed<ItemMeta> DIORITE = getItemType("diorite");
    Typed<ItemMeta> POLISHED_DIORITE = getItemType("polished_diorite");
    Typed<ItemMeta> ANDESITE = getItemType("andesite");
    Typed<ItemMeta> POLISHED_ANDESITE = getItemType("polished_andesite");
    Typed<ItemMeta> DEEPSLATE = getItemType("deepslate");
    Typed<ItemMeta> COBBLED_DEEPSLATE = getItemType("cobbled_deepslate");
    Typed<ItemMeta> POLISHED_DEEPSLATE = getItemType("polished_deepslate");
    Typed<ItemMeta> CALCITE = getItemType("calcite");
    Typed<ItemMeta> TUFF = getItemType("tuff");
    Typed<ItemMeta> TUFF_SLAB = getItemType("tuff_slab");
    Typed<ItemMeta> TUFF_STAIRS = getItemType("tuff_stairs");
    Typed<ItemMeta> TUFF_WALL = getItemType("tuff_wall");
    Typed<ItemMeta> CHISELED_TUFF = getItemType("chiseled_tuff");
    Typed<ItemMeta> POLISHED_TUFF = getItemType("polished_tuff");
    Typed<ItemMeta> POLISHED_TUFF_SLAB = getItemType("polished_tuff_slab");
    Typed<ItemMeta> POLISHED_TUFF_STAIRS = getItemType("polished_tuff_stairs");
    Typed<ItemMeta> POLISHED_TUFF_WALL = getItemType("polished_tuff_wall");
    Typed<ItemMeta> TUFF_BRICKS = getItemType("tuff_bricks");
    Typed<ItemMeta> TUFF_BRICK_SLAB = getItemType("tuff_brick_slab");
    Typed<ItemMeta> TUFF_BRICK_STAIRS = getItemType("tuff_brick_stairs");
    Typed<ItemMeta> TUFF_BRICK_WALL = getItemType("tuff_brick_wall");
    Typed<ItemMeta> CHISELED_TUFF_BRICKS = getItemType("chiseled_tuff_bricks");
    Typed<ItemMeta> SULFUR = getItemType("sulfur");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> POTENT_SULFUR = getItemType("potent_sulfur");
    Typed<ItemMeta> SULFUR_SLAB = getItemType("sulfur_slab");
    Typed<ItemMeta> SULFUR_STAIRS = getItemType("sulfur_stairs");
    Typed<ItemMeta> SULFUR_WALL = getItemType("sulfur_wall");
    Typed<ItemMeta> POLISHED_SULFUR = getItemType("polished_sulfur");
    Typed<ItemMeta> POLISHED_SULFUR_SLAB = getItemType("polished_sulfur_slab");
    Typed<ItemMeta> POLISHED_SULFUR_STAIRS = getItemType("polished_sulfur_stairs");
    Typed<ItemMeta> POLISHED_SULFUR_WALL = getItemType("polished_sulfur_wall");
    Typed<ItemMeta> SULFUR_BRICKS = getItemType("sulfur_bricks");
    Typed<ItemMeta> SULFUR_BRICK_SLAB = getItemType("sulfur_brick_slab");
    Typed<ItemMeta> SULFUR_BRICK_STAIRS = getItemType("sulfur_brick_stairs");
    Typed<ItemMeta> SULFUR_BRICK_WALL = getItemType("sulfur_brick_wall");
    Typed<ItemMeta> CHISELED_SULFUR = getItemType("chiseled_sulfur");
    Typed<ItemMeta> CINNABAR = getItemType("cinnabar");
    Typed<ItemMeta> CINNABAR_SLAB = getItemType("cinnabar_slab");
    Typed<ItemMeta> CINNABAR_STAIRS = getItemType("cinnabar_stairs");
    Typed<ItemMeta> CINNABAR_WALL = getItemType("cinnabar_wall");
    Typed<ItemMeta> POLISHED_CINNABAR = getItemType("polished_cinnabar");
    Typed<ItemMeta> POLISHED_CINNABAR_SLAB = getItemType("polished_cinnabar_slab");
    Typed<ItemMeta> POLISHED_CINNABAR_STAIRS = getItemType("polished_cinnabar_stairs");
    Typed<ItemMeta> POLISHED_CINNABAR_WALL = getItemType("polished_cinnabar_wall");
    Typed<ItemMeta> CINNABAR_BRICKS = getItemType("cinnabar_bricks");
    Typed<ItemMeta> CINNABAR_BRICK_SLAB = getItemType("cinnabar_brick_slab");
    Typed<ItemMeta> CINNABAR_BRICK_STAIRS = getItemType("cinnabar_brick_stairs");
    Typed<ItemMeta> CINNABAR_BRICK_WALL = getItemType("cinnabar_brick_wall");
    Typed<ItemMeta> CHISELED_CINNABAR = getItemType("chiseled_cinnabar");
    Typed<ItemMeta> DRIPSTONE_BLOCK = getItemType("dripstone_block");
    Typed<ItemMeta> GRASS_BLOCK = getItemType("grass_block");
    Typed<ItemMeta> DIRT = getItemType("dirt");
    Typed<ItemMeta> COARSE_DIRT = getItemType("coarse_dirt");
    Typed<ItemMeta> PODZOL = getItemType("podzol");
    Typed<ItemMeta> ROOTED_DIRT = getItemType("rooted_dirt");
    Typed<ItemMeta> MUD = getItemType("mud");
    Typed<ItemMeta> CRIMSON_NYLIUM = getItemType("crimson_nylium");
    Typed<ItemMeta> WARPED_NYLIUM = getItemType("warped_nylium");
    Typed<ItemMeta> COBBLESTONE = getItemType("cobblestone");
    Typed<ItemMeta> OAK_PLANKS = getItemType("oak_planks");
    Typed<ItemMeta> SPRUCE_PLANKS = getItemType("spruce_planks");
    Typed<ItemMeta> BIRCH_PLANKS = getItemType("birch_planks");
    Typed<ItemMeta> JUNGLE_PLANKS = getItemType("jungle_planks");
    Typed<ItemMeta> ACACIA_PLANKS = getItemType("acacia_planks");
    Typed<ItemMeta> CHERRY_PLANKS = getItemType("cherry_planks");
    Typed<ItemMeta> DARK_OAK_PLANKS = getItemType("dark_oak_planks");
    Typed<ItemMeta> PALE_OAK_PLANKS = getItemType("pale_oak_planks");
    Typed<ItemMeta> MANGROVE_PLANKS = getItemType("mangrove_planks");
    Typed<ItemMeta> BAMBOO_PLANKS = getItemType("bamboo_planks");
    Typed<ItemMeta> CRIMSON_PLANKS = getItemType("crimson_planks");
    Typed<ItemMeta> WARPED_PLANKS = getItemType("warped_planks");
    Typed<ItemMeta> BAMBOO_MOSAIC = getItemType("bamboo_mosaic");
    Typed<ItemMeta> OAK_SAPLING = getItemType("oak_sapling");
    Typed<ItemMeta> SPRUCE_SAPLING = getItemType("spruce_sapling");
    Typed<ItemMeta> BIRCH_SAPLING = getItemType("birch_sapling");
    Typed<ItemMeta> JUNGLE_SAPLING = getItemType("jungle_sapling");
    Typed<ItemMeta> ACACIA_SAPLING = getItemType("acacia_sapling");
    Typed<ItemMeta> CHERRY_SAPLING = getItemType("cherry_sapling");
    Typed<ItemMeta> DARK_OAK_SAPLING = getItemType("dark_oak_sapling");
    Typed<ItemMeta> PALE_OAK_SAPLING = getItemType("pale_oak_sapling");
    Typed<ItemMeta> MANGROVE_PROPAGULE = getItemType("mangrove_propagule");
    Typed<ItemMeta> BEDROCK = getItemType("bedrock");
    Typed<ItemMeta> SAND = getItemType("sand");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SUSPICIOUS_SAND = getItemType("suspicious_sand");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SUSPICIOUS_GRAVEL = getItemType("suspicious_gravel");
    Typed<ItemMeta> RED_SAND = getItemType("red_sand");
    Typed<ItemMeta> GRAVEL = getItemType("gravel");
    Typed<ItemMeta> COAL_ORE = getItemType("coal_ore");
    Typed<ItemMeta> DEEPSLATE_COAL_ORE = getItemType("deepslate_coal_ore");
    Typed<ItemMeta> IRON_ORE = getItemType("iron_ore");
    Typed<ItemMeta> DEEPSLATE_IRON_ORE = getItemType("deepslate_iron_ore");
    Typed<ItemMeta> COPPER_ORE = getItemType("copper_ore");
    Typed<ItemMeta> DEEPSLATE_COPPER_ORE = getItemType("deepslate_copper_ore");
    Typed<ItemMeta> GOLD_ORE = getItemType("gold_ore");
    Typed<ItemMeta> DEEPSLATE_GOLD_ORE = getItemType("deepslate_gold_ore");
    Typed<ItemMeta> REDSTONE_ORE = getItemType("redstone_ore");
    Typed<ItemMeta> DEEPSLATE_REDSTONE_ORE = getItemType("deepslate_redstone_ore");
    Typed<ItemMeta> EMERALD_ORE = getItemType("emerald_ore");
    Typed<ItemMeta> DEEPSLATE_EMERALD_ORE = getItemType("deepslate_emerald_ore");
    Typed<ItemMeta> LAPIS_ORE = getItemType("lapis_ore");
    Typed<ItemMeta> DEEPSLATE_LAPIS_ORE = getItemType("deepslate_lapis_ore");
    Typed<ItemMeta> DIAMOND_ORE = getItemType("diamond_ore");
    Typed<ItemMeta> DEEPSLATE_DIAMOND_ORE = getItemType("deepslate_diamond_ore");
    Typed<ItemMeta> NETHER_GOLD_ORE = getItemType("nether_gold_ore");
    Typed<ItemMeta> NETHER_QUARTZ_ORE = getItemType("nether_quartz_ore");
    Typed<ItemMeta> ANCIENT_DEBRIS = getItemType("ancient_debris");
    Typed<ItemMeta> COAL_BLOCK = getItemType("coal_block");
    Typed<ItemMeta> RAW_IRON_BLOCK = getItemType("raw_iron_block");
    Typed<ItemMeta> RAW_COPPER_BLOCK = getItemType("raw_copper_block");
    Typed<ItemMeta> RAW_GOLD_BLOCK = getItemType("raw_gold_block");
    Typed<ItemMeta> HEAVY_CORE = getItemType("heavy_core");
    Typed<ItemMeta> AMETHYST_BLOCK = getItemType("amethyst_block");
    Typed<ItemMeta> BUDDING_AMETHYST = getItemType("budding_amethyst");
    Typed<ItemMeta> IRON_BLOCK = getItemType("iron_block");
    Typed<ItemMeta> COPPER_BLOCK = getItemType("copper_block");
    Typed<ItemMeta> GOLD_BLOCK = getItemType("gold_block");
    Typed<ItemMeta> DIAMOND_BLOCK = getItemType("diamond_block");
    Typed<ItemMeta> NETHERITE_BLOCK = getItemType("netherite_block");
    Typed<ItemMeta> EXPOSED_COPPER = getItemType("exposed_copper");
    Typed<ItemMeta> WEATHERED_COPPER = getItemType("weathered_copper");
    Typed<ItemMeta> OXIDIZED_COPPER = getItemType("oxidized_copper");
    Typed<ItemMeta> CHISELED_COPPER = getItemType("chiseled_copper");
    Typed<ItemMeta> EXPOSED_CHISELED_COPPER = getItemType("exposed_chiseled_copper");
    Typed<ItemMeta> WEATHERED_CHISELED_COPPER = getItemType("weathered_chiseled_copper");
    Typed<ItemMeta> OXIDIZED_CHISELED_COPPER = getItemType("oxidized_chiseled_copper");
    Typed<ItemMeta> CUT_COPPER = getItemType("cut_copper");
    Typed<ItemMeta> EXPOSED_CUT_COPPER = getItemType("exposed_cut_copper");
    Typed<ItemMeta> WEATHERED_CUT_COPPER = getItemType("weathered_cut_copper");
    Typed<ItemMeta> OXIDIZED_CUT_COPPER = getItemType("oxidized_cut_copper");
    Typed<ItemMeta> CUT_COPPER_STAIRS = getItemType("cut_copper_stairs");
    Typed<ItemMeta> EXPOSED_CUT_COPPER_STAIRS = getItemType("exposed_cut_copper_stairs");
    Typed<ItemMeta> WEATHERED_CUT_COPPER_STAIRS = getItemType("weathered_cut_copper_stairs");
    Typed<ItemMeta> OXIDIZED_CUT_COPPER_STAIRS = getItemType("oxidized_cut_copper_stairs");
    Typed<ItemMeta> CUT_COPPER_SLAB = getItemType("cut_copper_slab");
    Typed<ItemMeta> EXPOSED_CUT_COPPER_SLAB = getItemType("exposed_cut_copper_slab");
    Typed<ItemMeta> WEATHERED_CUT_COPPER_SLAB = getItemType("weathered_cut_copper_slab");
    Typed<ItemMeta> OXIDIZED_CUT_COPPER_SLAB = getItemType("oxidized_cut_copper_slab");
    Typed<ItemMeta> WAXED_COPPER_BLOCK = getItemType("waxed_copper_block");
    Typed<ItemMeta> WAXED_EXPOSED_COPPER = getItemType("waxed_exposed_copper");
    Typed<ItemMeta> WAXED_WEATHERED_COPPER = getItemType("waxed_weathered_copper");
    Typed<ItemMeta> WAXED_OXIDIZED_COPPER = getItemType("waxed_oxidized_copper");
    Typed<ItemMeta> WAXED_CHISELED_COPPER = getItemType("waxed_chiseled_copper");
    Typed<ItemMeta> WAXED_EXPOSED_CHISELED_COPPER = getItemType("waxed_exposed_chiseled_copper");
    Typed<ItemMeta> WAXED_WEATHERED_CHISELED_COPPER = getItemType("waxed_weathered_chiseled_copper");
    Typed<ItemMeta> WAXED_OXIDIZED_CHISELED_COPPER = getItemType("waxed_oxidized_chiseled_copper");
    Typed<ItemMeta> WAXED_CUT_COPPER = getItemType("waxed_cut_copper");
    Typed<ItemMeta> WAXED_EXPOSED_CUT_COPPER = getItemType("waxed_exposed_cut_copper");
    Typed<ItemMeta> WAXED_WEATHERED_CUT_COPPER = getItemType("waxed_weathered_cut_copper");
    Typed<ItemMeta> WAXED_OXIDIZED_CUT_COPPER = getItemType("waxed_oxidized_cut_copper");
    Typed<ItemMeta> WAXED_CUT_COPPER_STAIRS = getItemType("waxed_cut_copper_stairs");
    Typed<ItemMeta> WAXED_EXPOSED_CUT_COPPER_STAIRS = getItemType("waxed_exposed_cut_copper_stairs");
    Typed<ItemMeta> WAXED_WEATHERED_CUT_COPPER_STAIRS = getItemType("waxed_weathered_cut_copper_stairs");
    Typed<ItemMeta> WAXED_OXIDIZED_CUT_COPPER_STAIRS = getItemType("waxed_oxidized_cut_copper_stairs");
    Typed<ItemMeta> WAXED_CUT_COPPER_SLAB = getItemType("waxed_cut_copper_slab");
    Typed<ItemMeta> WAXED_EXPOSED_CUT_COPPER_SLAB = getItemType("waxed_exposed_cut_copper_slab");
    Typed<ItemMeta> WAXED_WEATHERED_CUT_COPPER_SLAB = getItemType("waxed_weathered_cut_copper_slab");
    Typed<ItemMeta> WAXED_OXIDIZED_CUT_COPPER_SLAB = getItemType("waxed_oxidized_cut_copper_slab");
    Typed<ItemMeta> OAK_LOG = getItemType("oak_log");
    Typed<ItemMeta> SPRUCE_LOG = getItemType("spruce_log");
    Typed<ItemMeta> BIRCH_LOG = getItemType("birch_log");
    Typed<ItemMeta> JUNGLE_LOG = getItemType("jungle_log");
    Typed<ItemMeta> ACACIA_LOG = getItemType("acacia_log");
    Typed<ItemMeta> CHERRY_LOG = getItemType("cherry_log");
    Typed<ItemMeta> DARK_OAK_LOG = getItemType("dark_oak_log");
    Typed<ItemMeta> PALE_OAK_LOG = getItemType("pale_oak_log");
    Typed<ItemMeta> MANGROVE_LOG = getItemType("mangrove_log");
    Typed<ItemMeta> MANGROVE_ROOTS = getItemType("mangrove_roots");
    Typed<ItemMeta> MUDDY_MANGROVE_ROOTS = getItemType("muddy_mangrove_roots");
    Typed<ItemMeta> CRIMSON_STEM = getItemType("crimson_stem");
    Typed<ItemMeta> WARPED_STEM = getItemType("warped_stem");
    Typed<ItemMeta> BAMBOO_BLOCK = getItemType("bamboo_block");
    Typed<ItemMeta> STRIPPED_OAK_LOG = getItemType("stripped_oak_log");
    Typed<ItemMeta> STRIPPED_SPRUCE_LOG = getItemType("stripped_spruce_log");
    Typed<ItemMeta> STRIPPED_BIRCH_LOG = getItemType("stripped_birch_log");
    Typed<ItemMeta> STRIPPED_JUNGLE_LOG = getItemType("stripped_jungle_log");
    Typed<ItemMeta> STRIPPED_ACACIA_LOG = getItemType("stripped_acacia_log");
    Typed<ItemMeta> STRIPPED_CHERRY_LOG = getItemType("stripped_cherry_log");
    Typed<ItemMeta> STRIPPED_DARK_OAK_LOG = getItemType("stripped_dark_oak_log");
    Typed<ItemMeta> STRIPPED_PALE_OAK_LOG = getItemType("stripped_pale_oak_log");
    Typed<ItemMeta> STRIPPED_MANGROVE_LOG = getItemType("stripped_mangrove_log");
    Typed<ItemMeta> STRIPPED_CRIMSON_STEM = getItemType("stripped_crimson_stem");
    Typed<ItemMeta> STRIPPED_WARPED_STEM = getItemType("stripped_warped_stem");
    Typed<ItemMeta> STRIPPED_OAK_WOOD = getItemType("stripped_oak_wood");
    Typed<ItemMeta> STRIPPED_SPRUCE_WOOD = getItemType("stripped_spruce_wood");
    Typed<ItemMeta> STRIPPED_BIRCH_WOOD = getItemType("stripped_birch_wood");
    Typed<ItemMeta> STRIPPED_JUNGLE_WOOD = getItemType("stripped_jungle_wood");
    Typed<ItemMeta> STRIPPED_ACACIA_WOOD = getItemType("stripped_acacia_wood");
    Typed<ItemMeta> STRIPPED_CHERRY_WOOD = getItemType("stripped_cherry_wood");
    Typed<ItemMeta> STRIPPED_DARK_OAK_WOOD = getItemType("stripped_dark_oak_wood");
    Typed<ItemMeta> STRIPPED_PALE_OAK_WOOD = getItemType("stripped_pale_oak_wood");
    Typed<ItemMeta> STRIPPED_MANGROVE_WOOD = getItemType("stripped_mangrove_wood");
    Typed<ItemMeta> STRIPPED_CRIMSON_HYPHAE = getItemType("stripped_crimson_hyphae");
    Typed<ItemMeta> STRIPPED_WARPED_HYPHAE = getItemType("stripped_warped_hyphae");
    Typed<ItemMeta> STRIPPED_BAMBOO_BLOCK = getItemType("stripped_bamboo_block");
    Typed<ItemMeta> OAK_WOOD = getItemType("oak_wood");
    Typed<ItemMeta> SPRUCE_WOOD = getItemType("spruce_wood");
    Typed<ItemMeta> BIRCH_WOOD = getItemType("birch_wood");
    Typed<ItemMeta> JUNGLE_WOOD = getItemType("jungle_wood");
    Typed<ItemMeta> ACACIA_WOOD = getItemType("acacia_wood");
    Typed<ItemMeta> CHERRY_WOOD = getItemType("cherry_wood");
    Typed<ItemMeta> DARK_OAK_WOOD = getItemType("dark_oak_wood");
    Typed<ItemMeta> PALE_OAK_WOOD = getItemType("pale_oak_wood");
    Typed<ItemMeta> MANGROVE_WOOD = getItemType("mangrove_wood");
    Typed<ItemMeta> CRIMSON_HYPHAE = getItemType("crimson_hyphae");
    Typed<ItemMeta> WARPED_HYPHAE = getItemType("warped_hyphae");
    Typed<ItemMeta> OAK_LEAVES = getItemType("oak_leaves");
    Typed<ItemMeta> SPRUCE_LEAVES = getItemType("spruce_leaves");
    Typed<ItemMeta> BIRCH_LEAVES = getItemType("birch_leaves");
    Typed<ItemMeta> JUNGLE_LEAVES = getItemType("jungle_leaves");
    Typed<ItemMeta> ACACIA_LEAVES = getItemType("acacia_leaves");
    Typed<ItemMeta> CHERRY_LEAVES = getItemType("cherry_leaves");
    Typed<ItemMeta> DARK_OAK_LEAVES = getItemType("dark_oak_leaves");
    Typed<ItemMeta> PALE_OAK_LEAVES = getItemType("pale_oak_leaves");
    Typed<ItemMeta> MANGROVE_LEAVES = getItemType("mangrove_leaves");
    Typed<ItemMeta> AZALEA_LEAVES = getItemType("azalea_leaves");
    Typed<ItemMeta> FLOWERING_AZALEA_LEAVES = getItemType("flowering_azalea_leaves");
    Typed<ItemMeta> SPONGE = getItemType("sponge");
    Typed<ItemMeta> WET_SPONGE = getItemType("wet_sponge");
    Typed<ItemMeta> GLASS = getItemType("glass");
    Typed<ItemMeta> TINTED_GLASS = getItemType("tinted_glass");
    Typed<ItemMeta> LAPIS_BLOCK = getItemType("lapis_block");
    Typed<ItemMeta> SANDSTONE = getItemType("sandstone");
    Typed<ItemMeta> CHISELED_SANDSTONE = getItemType("chiseled_sandstone");
    Typed<ItemMeta> CUT_SANDSTONE = getItemType("cut_sandstone");
    Typed<ItemMeta> COBWEB = getItemType("cobweb");
    Typed<ItemMeta> SHORT_GRASS = getItemType("short_grass");
    Typed<ItemMeta> FERN = getItemType("fern");
    Typed<ItemMeta> AZALEA = getItemType("azalea");
    Typed<ItemMeta> BUSH = getItemType("bush");
    Typed<ItemMeta> FLOWERING_AZALEA = getItemType("flowering_azalea");
    Typed<ItemMeta> DEAD_BUSH = getItemType("dead_bush");
    Typed<ItemMeta> FIREFLY_BUSH = getItemType("firefly_bush");
    Typed<ItemMeta> SHORT_DRY_GRASS = getItemType("short_dry_grass");
    Typed<ItemMeta> TALL_DRY_GRASS = getItemType("tall_dry_grass");
    Typed<ItemMeta> SEAGRASS = getItemType("seagrass");
    Typed<ItemMeta> SEA_PICKLE = getItemType("sea_pickle");
    Typed<ItemMeta> WHITE_WOOL = getItemType("white_wool");
    Typed<ItemMeta> ORANGE_WOOL = getItemType("orange_wool");
    Typed<ItemMeta> MAGENTA_WOOL = getItemType("magenta_wool");
    Typed<ItemMeta> LIGHT_BLUE_WOOL = getItemType("light_blue_wool");
    Typed<ItemMeta> YELLOW_WOOL = getItemType("yellow_wool");
    Typed<ItemMeta> LIME_WOOL = getItemType("lime_wool");
    Typed<ItemMeta> PINK_WOOL = getItemType("pink_wool");
    Typed<ItemMeta> GRAY_WOOL = getItemType("gray_wool");
    Typed<ItemMeta> LIGHT_GRAY_WOOL = getItemType("light_gray_wool");
    Typed<ItemMeta> CYAN_WOOL = getItemType("cyan_wool");
    Typed<ItemMeta> PURPLE_WOOL = getItemType("purple_wool");
    Typed<ItemMeta> BLUE_WOOL = getItemType("blue_wool");
    Typed<ItemMeta> BROWN_WOOL = getItemType("brown_wool");
    Typed<ItemMeta> GREEN_WOOL = getItemType("green_wool");
    Typed<ItemMeta> RED_WOOL = getItemType("red_wool");
    Typed<ItemMeta> BLACK_WOOL = getItemType("black_wool");
    Typed<ItemMeta> DANDELION = getItemType("dandelion");
    Typed<ItemMeta> GOLDEN_DANDELION = getItemType("golden_dandelion");
    Typed<ItemMeta> OPEN_EYEBLOSSOM = getItemType("open_eyeblossom");
    Typed<ItemMeta> CLOSED_EYEBLOSSOM = getItemType("closed_eyeblossom");
    Typed<ItemMeta> POPPY = getItemType("poppy");
    Typed<ItemMeta> BLUE_ORCHID = getItemType("blue_orchid");
    Typed<ItemMeta> ALLIUM = getItemType("allium");
    Typed<ItemMeta> AZURE_BLUET = getItemType("azure_bluet");
    Typed<ItemMeta> RED_TULIP = getItemType("red_tulip");
    Typed<ItemMeta> ORANGE_TULIP = getItemType("orange_tulip");
    Typed<ItemMeta> WHITE_TULIP = getItemType("white_tulip");
    Typed<ItemMeta> PINK_TULIP = getItemType("pink_tulip");
    Typed<ItemMeta> OXEYE_DAISY = getItemType("oxeye_daisy");
    Typed<ItemMeta> CORNFLOWER = getItemType("cornflower");
    Typed<ItemMeta> LILY_OF_THE_VALLEY = getItemType("lily_of_the_valley");
    Typed<ItemMeta> WITHER_ROSE = getItemType("wither_rose");
    Typed<ItemMeta> TORCHFLOWER = getItemType("torchflower");
    Typed<ItemMeta> PITCHER_PLANT = getItemType("pitcher_plant");
    Typed<ItemMeta> SPORE_BLOSSOM = getItemType("spore_blossom");
    Typed<ItemMeta> BROWN_MUSHROOM = getItemType("brown_mushroom");
    Typed<ItemMeta> RED_MUSHROOM = getItemType("red_mushroom");
    Typed<ItemMeta> CRIMSON_FUNGUS = getItemType("crimson_fungus");
    Typed<ItemMeta> WARPED_FUNGUS = getItemType("warped_fungus");
    Typed<ItemMeta> CRIMSON_ROOTS = getItemType("crimson_roots");
    Typed<ItemMeta> WARPED_ROOTS = getItemType("warped_roots");
    Typed<ItemMeta> NETHER_SPROUTS = getItemType("nether_sprouts");
    Typed<ItemMeta> WEEPING_VINES = getItemType("weeping_vines");
    Typed<ItemMeta> TWISTING_VINES = getItemType("twisting_vines");
    Typed<ItemMeta> SUGAR_CANE = getItemType("sugar_cane");
    Typed<ItemMeta> KELP = getItemType("kelp");
    Typed<ItemMeta> PINK_PETALS = getItemType("pink_petals");
    Typed<ItemMeta> WILDFLOWERS = getItemType("wildflowers");
    Typed<ItemMeta> LEAF_LITTER = getItemType("leaf_litter");
    Typed<ItemMeta> MOSS_CARPET = getItemType("moss_carpet");
    Typed<ItemMeta> MOSS_BLOCK = getItemType("moss_block");
    Typed<ItemMeta> PALE_MOSS_CARPET = getItemType("pale_moss_carpet");
    Typed<ItemMeta> PALE_HANGING_MOSS = getItemType("pale_hanging_moss");
    Typed<ItemMeta> PALE_MOSS_BLOCK = getItemType("pale_moss_block");
    Typed<ItemMeta> HANGING_ROOTS = getItemType("hanging_roots");
    Typed<ItemMeta> BIG_DRIPLEAF = getItemType("big_dripleaf");
    Typed<ItemMeta> SMALL_DRIPLEAF = getItemType("small_dripleaf");
    Typed<ItemMeta> BAMBOO = getItemType("bamboo");
    Typed<ItemMeta> OAK_SLAB = getItemType("oak_slab");
    Typed<ItemMeta> SPRUCE_SLAB = getItemType("spruce_slab");
    Typed<ItemMeta> BIRCH_SLAB = getItemType("birch_slab");
    Typed<ItemMeta> JUNGLE_SLAB = getItemType("jungle_slab");
    Typed<ItemMeta> ACACIA_SLAB = getItemType("acacia_slab");
    Typed<ItemMeta> CHERRY_SLAB = getItemType("cherry_slab");
    Typed<ItemMeta> DARK_OAK_SLAB = getItemType("dark_oak_slab");
    Typed<ItemMeta> PALE_OAK_SLAB = getItemType("pale_oak_slab");
    Typed<ItemMeta> MANGROVE_SLAB = getItemType("mangrove_slab");
    Typed<ItemMeta> BAMBOO_SLAB = getItemType("bamboo_slab");
    Typed<ItemMeta> BAMBOO_MOSAIC_SLAB = getItemType("bamboo_mosaic_slab");
    Typed<ItemMeta> CRIMSON_SLAB = getItemType("crimson_slab");
    Typed<ItemMeta> WARPED_SLAB = getItemType("warped_slab");
    Typed<ItemMeta> STONE_SLAB = getItemType("stone_slab");
    Typed<ItemMeta> SMOOTH_STONE_SLAB = getItemType("smooth_stone_slab");
    Typed<ItemMeta> SANDSTONE_SLAB = getItemType("sandstone_slab");
    Typed<ItemMeta> CUT_SANDSTONE_SLAB = getItemType("cut_sandstone_slab");
    Typed<ItemMeta> PETRIFIED_OAK_SLAB = getItemType("petrified_oak_slab");
    Typed<ItemMeta> COBBLESTONE_SLAB = getItemType("cobblestone_slab");
    Typed<ItemMeta> BRICK_SLAB = getItemType("brick_slab");
    Typed<ItemMeta> STONE_BRICK_SLAB = getItemType("stone_brick_slab");
    Typed<ItemMeta> MUD_BRICK_SLAB = getItemType("mud_brick_slab");
    Typed<ItemMeta> NETHER_BRICK_SLAB = getItemType("nether_brick_slab");
    Typed<ItemMeta> QUARTZ_SLAB = getItemType("quartz_slab");
    Typed<ItemMeta> RED_SANDSTONE_SLAB = getItemType("red_sandstone_slab");
    Typed<ItemMeta> CUT_RED_SANDSTONE_SLAB = getItemType("cut_red_sandstone_slab");
    Typed<ItemMeta> PURPUR_SLAB = getItemType("purpur_slab");
    Typed<ItemMeta> PRISMARINE_SLAB = getItemType("prismarine_slab");
    Typed<ItemMeta> PRISMARINE_BRICK_SLAB = getItemType("prismarine_brick_slab");
    Typed<ItemMeta> DARK_PRISMARINE_SLAB = getItemType("dark_prismarine_slab");
    Typed<ItemMeta> SMOOTH_QUARTZ = getItemType("smooth_quartz");
    Typed<ItemMeta> SMOOTH_RED_SANDSTONE = getItemType("smooth_red_sandstone");
    Typed<ItemMeta> SMOOTH_SANDSTONE = getItemType("smooth_sandstone");
    Typed<ItemMeta> SMOOTH_STONE = getItemType("smooth_stone");
    Typed<ItemMeta> BRICKS = getItemType("bricks");
    Typed<ItemMeta> ACACIA_SHELF = getItemType("acacia_shelf");
    Typed<ItemMeta> BAMBOO_SHELF = getItemType("bamboo_shelf");
    Typed<ItemMeta> BIRCH_SHELF = getItemType("birch_shelf");
    Typed<ItemMeta> CHERRY_SHELF = getItemType("cherry_shelf");
    Typed<ItemMeta> CRIMSON_SHELF = getItemType("crimson_shelf");
    Typed<ItemMeta> DARK_OAK_SHELF = getItemType("dark_oak_shelf");
    Typed<ItemMeta> JUNGLE_SHELF = getItemType("jungle_shelf");
    Typed<ItemMeta> MANGROVE_SHELF = getItemType("mangrove_shelf");
    Typed<ItemMeta> OAK_SHELF = getItemType("oak_shelf");
    Typed<ItemMeta> PALE_OAK_SHELF = getItemType("pale_oak_shelf");
    Typed<ItemMeta> SPRUCE_SHELF = getItemType("spruce_shelf");
    Typed<ItemMeta> WARPED_SHELF = getItemType("warped_shelf");
    Typed<ItemMeta> BOOKSHELF = getItemType("bookshelf");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CHISELED_BOOKSHELF = getItemType("chiseled_bookshelf");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> DECORATED_POT = getItemType("decorated_pot");
    Typed<ItemMeta> MOSSY_COBBLESTONE = getItemType("mossy_cobblestone");
    Typed<ItemMeta> OBSIDIAN = getItemType("obsidian");
    Typed<ItemMeta> TORCH = getItemType("torch");
    Typed<ItemMeta> COPPER_TORCH = getItemType("copper_torch");
    Typed<ItemMeta> END_ROD = getItemType("end_rod");
    Typed<ItemMeta> CHORUS_PLANT = getItemType("chorus_plant");
    Typed<ItemMeta> CHORUS_FLOWER = getItemType("chorus_flower");
    Typed<ItemMeta> PURPUR_BLOCK = getItemType("purpur_block");
    Typed<ItemMeta> PURPUR_PILLAR = getItemType("purpur_pillar");
    Typed<ItemMeta> PURPUR_STAIRS = getItemType("purpur_stairs");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SPAWNER = getItemType("spawner");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CREAKING_HEART = getItemType("creaking_heart");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CHEST = getItemType("chest");
    Typed<ItemMeta> CRAFTING_TABLE = getItemType("crafting_table");
    Typed<ItemMeta> FARMLAND = getItemType("farmland");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> FURNACE = getItemType("furnace");
    Typed<ItemMeta> LADDER = getItemType("ladder");
    Typed<ItemMeta> COBBLESTONE_STAIRS = getItemType("cobblestone_stairs");
    Typed<ItemMeta> SNOW = getItemType("snow");
    Typed<ItemMeta> ICE = getItemType("ice");
    Typed<ItemMeta> SNOW_BLOCK = getItemType("snow_block");
    Typed<ItemMeta> CACTUS = getItemType("cactus");
    Typed<ItemMeta> CACTUS_FLOWER = getItemType("cactus_flower");
    Typed<ItemMeta> CLAY = getItemType("clay");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> JUKEBOX = getItemType("jukebox");
    Typed<ItemMeta> OAK_FENCE = getItemType("oak_fence");
    Typed<ItemMeta> SPRUCE_FENCE = getItemType("spruce_fence");
    Typed<ItemMeta> BIRCH_FENCE = getItemType("birch_fence");
    Typed<ItemMeta> JUNGLE_FENCE = getItemType("jungle_fence");
    Typed<ItemMeta> ACACIA_FENCE = getItemType("acacia_fence");
    Typed<ItemMeta> CHERRY_FENCE = getItemType("cherry_fence");
    Typed<ItemMeta> DARK_OAK_FENCE = getItemType("dark_oak_fence");
    Typed<ItemMeta> PALE_OAK_FENCE = getItemType("pale_oak_fence");
    Typed<ItemMeta> MANGROVE_FENCE = getItemType("mangrove_fence");
    Typed<ItemMeta> BAMBOO_FENCE = getItemType("bamboo_fence");
    Typed<ItemMeta> CRIMSON_FENCE = getItemType("crimson_fence");
    Typed<ItemMeta> WARPED_FENCE = getItemType("warped_fence");
    Typed<ItemMeta> PUMPKIN = getItemType("pumpkin");
    Typed<ItemMeta> CARVED_PUMPKIN = getItemType("carved_pumpkin");
    Typed<ItemMeta> JACK_O_LANTERN = getItemType("jack_o_lantern");
    Typed<ItemMeta> NETHERRACK = getItemType("netherrack");
    Typed<ItemMeta> SOUL_SAND = getItemType("soul_sand");
    Typed<ItemMeta> SOUL_SOIL = getItemType("soul_soil");
    Typed<ItemMeta> BASALT = getItemType("basalt");
    Typed<ItemMeta> POLISHED_BASALT = getItemType("polished_basalt");
    Typed<ItemMeta> SMOOTH_BASALT = getItemType("smooth_basalt");
    Typed<ItemMeta> SOUL_TORCH = getItemType("soul_torch");
    Typed<ItemMeta> GLOWSTONE = getItemType("glowstone");
    Typed<ItemMeta> INFESTED_STONE = getItemType("infested_stone");
    Typed<ItemMeta> INFESTED_COBBLESTONE = getItemType("infested_cobblestone");
    Typed<ItemMeta> INFESTED_STONE_BRICKS = getItemType("infested_stone_bricks");
    Typed<ItemMeta> INFESTED_MOSSY_STONE_BRICKS = getItemType("infested_mossy_stone_bricks");
    Typed<ItemMeta> INFESTED_CRACKED_STONE_BRICKS = getItemType("infested_cracked_stone_bricks");
    Typed<ItemMeta> INFESTED_CHISELED_STONE_BRICKS = getItemType("infested_chiseled_stone_bricks");
    Typed<ItemMeta> INFESTED_DEEPSLATE = getItemType("infested_deepslate");
    Typed<ItemMeta> STONE_BRICKS = getItemType("stone_bricks");
    Typed<ItemMeta> MOSSY_STONE_BRICKS = getItemType("mossy_stone_bricks");
    Typed<ItemMeta> CRACKED_STONE_BRICKS = getItemType("cracked_stone_bricks");
    Typed<ItemMeta> CHISELED_STONE_BRICKS = getItemType("chiseled_stone_bricks");
    Typed<ItemMeta> PACKED_MUD = getItemType("packed_mud");
    Typed<ItemMeta> MUD_BRICKS = getItemType("mud_bricks");
    Typed<ItemMeta> DEEPSLATE_BRICKS = getItemType("deepslate_bricks");
    Typed<ItemMeta> CRACKED_DEEPSLATE_BRICKS = getItemType("cracked_deepslate_bricks");
    Typed<ItemMeta> DEEPSLATE_TILES = getItemType("deepslate_tiles");
    Typed<ItemMeta> CRACKED_DEEPSLATE_TILES = getItemType("cracked_deepslate_tiles");
    Typed<ItemMeta> CHISELED_DEEPSLATE = getItemType("chiseled_deepslate");
    Typed<ItemMeta> REINFORCED_DEEPSLATE = getItemType("reinforced_deepslate");
    Typed<ItemMeta> BROWN_MUSHROOM_BLOCK = getItemType("brown_mushroom_block");
    Typed<ItemMeta> RED_MUSHROOM_BLOCK = getItemType("red_mushroom_block");
    Typed<ItemMeta> MUSHROOM_STEM = getItemType("mushroom_stem");
    Typed<ItemMeta> IRON_BARS = getItemType("iron_bars");
    Typed<ItemMeta> COPPER_BARS = getItemType("copper_bars");
    Typed<ItemMeta> IRON_CHAIN = getItemType("iron_chain");
    Typed<ItemMeta> COPPER_CHAIN = getItemType("copper_chain");
    Typed<ItemMeta> GLASS_PANE = getItemType("glass_pane");
    Typed<ItemMeta> MELON = getItemType("melon");
    Typed<ItemMeta> VINE = getItemType("vine");
    Typed<ItemMeta> GLOW_LICHEN = getItemType("glow_lichen");
    Typed<ItemMeta> RESIN_CLUMP = getItemType("resin_clump");
    Typed<ItemMeta> RESIN_BLOCK = getItemType("resin_block");
    Typed<ItemMeta> RESIN_BRICKS = getItemType("resin_bricks");
    Typed<ItemMeta> RESIN_BRICK_STAIRS = getItemType("resin_brick_stairs");
    Typed<ItemMeta> RESIN_BRICK_SLAB = getItemType("resin_brick_slab");
    Typed<ItemMeta> RESIN_BRICK_WALL = getItemType("resin_brick_wall");
    Typed<ItemMeta> CHISELED_RESIN_BRICKS = getItemType("chiseled_resin_bricks");
    Typed<ItemMeta> BRICK_STAIRS = getItemType("brick_stairs");
    Typed<ItemMeta> STONE_BRICK_STAIRS = getItemType("stone_brick_stairs");
    Typed<ItemMeta> MUD_BRICK_STAIRS = getItemType("mud_brick_stairs");
    Typed<ItemMeta> MYCELIUM = getItemType("mycelium");
    Typed<ItemMeta> LILY_PAD = getItemType("lily_pad");
    Typed<ItemMeta> NETHER_BRICKS = getItemType("nether_bricks");
    Typed<ItemMeta> CRACKED_NETHER_BRICKS = getItemType("cracked_nether_bricks");
    Typed<ItemMeta> CHISELED_NETHER_BRICKS = getItemType("chiseled_nether_bricks");
    Typed<ItemMeta> NETHER_BRICK_FENCE = getItemType("nether_brick_fence");
    Typed<ItemMeta> NETHER_BRICK_STAIRS = getItemType("nether_brick_stairs");
    Typed<ItemMeta> SCULK = getItemType("sculk");
    Typed<ItemMeta> SCULK_VEIN = getItemType("sculk_vein");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SCULK_CATALYST = getItemType("sculk_catalyst");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SCULK_SHRIEKER = getItemType("sculk_shrieker");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> ENCHANTING_TABLE = getItemType("enchanting_table");
    Typed<ItemMeta> END_PORTAL_FRAME = getItemType("end_portal_frame");
    Typed<ItemMeta> END_STONE = getItemType("end_stone");
    Typed<ItemMeta> END_STONE_BRICKS = getItemType("end_stone_bricks");
    Typed<ItemMeta> DRAGON_EGG = getItemType("dragon_egg");
    Typed<ItemMeta> SANDSTONE_STAIRS = getItemType("sandstone_stairs");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> ENDER_CHEST = getItemType("ender_chest");
    Typed<ItemMeta> EMERALD_BLOCK = getItemType("emerald_block");
    Typed<ItemMeta> OAK_STAIRS = getItemType("oak_stairs");
    Typed<ItemMeta> SPRUCE_STAIRS = getItemType("spruce_stairs");
    Typed<ItemMeta> BIRCH_STAIRS = getItemType("birch_stairs");
    Typed<ItemMeta> JUNGLE_STAIRS = getItemType("jungle_stairs");
    Typed<ItemMeta> ACACIA_STAIRS = getItemType("acacia_stairs");
    Typed<ItemMeta> CHERRY_STAIRS = getItemType("cherry_stairs");
    Typed<ItemMeta> DARK_OAK_STAIRS = getItemType("dark_oak_stairs");
    Typed<ItemMeta> PALE_OAK_STAIRS = getItemType("pale_oak_stairs");
    Typed<ItemMeta> MANGROVE_STAIRS = getItemType("mangrove_stairs");
    Typed<ItemMeta> BAMBOO_STAIRS = getItemType("bamboo_stairs");
    Typed<ItemMeta> BAMBOO_MOSAIC_STAIRS = getItemType("bamboo_mosaic_stairs");
    Typed<ItemMeta> CRIMSON_STAIRS = getItemType("crimson_stairs");
    Typed<ItemMeta> WARPED_STAIRS = getItemType("warped_stairs");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> COMMAND_BLOCK = getItemType("command_block");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BEACON = getItemType("beacon");
    Typed<ItemMeta> COBBLESTONE_WALL = getItemType("cobblestone_wall");
    Typed<ItemMeta> MOSSY_COBBLESTONE_WALL = getItemType("mossy_cobblestone_wall");
    Typed<ItemMeta> BRICK_WALL = getItemType("brick_wall");
    Typed<ItemMeta> PRISMARINE_WALL = getItemType("prismarine_wall");
    Typed<ItemMeta> RED_SANDSTONE_WALL = getItemType("red_sandstone_wall");
    Typed<ItemMeta> MOSSY_STONE_BRICK_WALL = getItemType("mossy_stone_brick_wall");
    Typed<ItemMeta> GRANITE_WALL = getItemType("granite_wall");
    Typed<ItemMeta> STONE_BRICK_WALL = getItemType("stone_brick_wall");
    Typed<ItemMeta> MUD_BRICK_WALL = getItemType("mud_brick_wall");
    Typed<ItemMeta> NETHER_BRICK_WALL = getItemType("nether_brick_wall");
    Typed<ItemMeta> ANDESITE_WALL = getItemType("andesite_wall");
    Typed<ItemMeta> RED_NETHER_BRICK_WALL = getItemType("red_nether_brick_wall");
    Typed<ItemMeta> SANDSTONE_WALL = getItemType("sandstone_wall");
    Typed<ItemMeta> END_STONE_BRICK_WALL = getItemType("end_stone_brick_wall");
    Typed<ItemMeta> DIORITE_WALL = getItemType("diorite_wall");
    Typed<ItemMeta> BLACKSTONE_WALL = getItemType("blackstone_wall");
    Typed<ItemMeta> POLISHED_BLACKSTONE_WALL = getItemType("polished_blackstone_wall");
    Typed<ItemMeta> POLISHED_BLACKSTONE_BRICK_WALL = getItemType("polished_blackstone_brick_wall");
    Typed<ItemMeta> COBBLED_DEEPSLATE_WALL = getItemType("cobbled_deepslate_wall");
    Typed<ItemMeta> POLISHED_DEEPSLATE_WALL = getItemType("polished_deepslate_wall");
    Typed<ItemMeta> DEEPSLATE_BRICK_WALL = getItemType("deepslate_brick_wall");
    Typed<ItemMeta> DEEPSLATE_TILE_WALL = getItemType("deepslate_tile_wall");
    Typed<ItemMeta> ANVIL = getItemType("anvil");
    Typed<ItemMeta> CHIPPED_ANVIL = getItemType("chipped_anvil");
    Typed<ItemMeta> DAMAGED_ANVIL = getItemType("damaged_anvil");
    Typed<ItemMeta> CHISELED_QUARTZ_BLOCK = getItemType("chiseled_quartz_block");
    Typed<ItemMeta> QUARTZ_BLOCK = getItemType("quartz_block");
    Typed<ItemMeta> QUARTZ_BRICKS = getItemType("quartz_bricks");
    Typed<ItemMeta> QUARTZ_PILLAR = getItemType("quartz_pillar");
    Typed<ItemMeta> QUARTZ_STAIRS = getItemType("quartz_stairs");
    Typed<ItemMeta> WHITE_TERRACOTTA = getItemType("white_terracotta");
    Typed<ItemMeta> ORANGE_TERRACOTTA = getItemType("orange_terracotta");
    Typed<ItemMeta> MAGENTA_TERRACOTTA = getItemType("magenta_terracotta");
    Typed<ItemMeta> LIGHT_BLUE_TERRACOTTA = getItemType("light_blue_terracotta");
    Typed<ItemMeta> YELLOW_TERRACOTTA = getItemType("yellow_terracotta");
    Typed<ItemMeta> LIME_TERRACOTTA = getItemType("lime_terracotta");
    Typed<ItemMeta> PINK_TERRACOTTA = getItemType("pink_terracotta");
    Typed<ItemMeta> GRAY_TERRACOTTA = getItemType("gray_terracotta");
    Typed<ItemMeta> LIGHT_GRAY_TERRACOTTA = getItemType("light_gray_terracotta");
    Typed<ItemMeta> CYAN_TERRACOTTA = getItemType("cyan_terracotta");
    Typed<ItemMeta> PURPLE_TERRACOTTA = getItemType("purple_terracotta");
    Typed<ItemMeta> BLUE_TERRACOTTA = getItemType("blue_terracotta");
    Typed<ItemMeta> BROWN_TERRACOTTA = getItemType("brown_terracotta");
    Typed<ItemMeta> GREEN_TERRACOTTA = getItemType("green_terracotta");
    Typed<ItemMeta> RED_TERRACOTTA = getItemType("red_terracotta");
    Typed<ItemMeta> BLACK_TERRACOTTA = getItemType("black_terracotta");
    Typed<ItemMeta> BARRIER = getItemType("barrier");
    Typed<ItemMeta> LIGHT = getItemType("light");
    Typed<ItemMeta> HAY_BLOCK = getItemType("hay_block");
    Typed<ItemMeta> WHITE_CARPET = getItemType("white_carpet");
    Typed<ItemMeta> ORANGE_CARPET = getItemType("orange_carpet");
    Typed<ItemMeta> MAGENTA_CARPET = getItemType("magenta_carpet");
    Typed<ItemMeta> LIGHT_BLUE_CARPET = getItemType("light_blue_carpet");
    Typed<ItemMeta> YELLOW_CARPET = getItemType("yellow_carpet");
    Typed<ItemMeta> LIME_CARPET = getItemType("lime_carpet");
    Typed<ItemMeta> PINK_CARPET = getItemType("pink_carpet");
    Typed<ItemMeta> GRAY_CARPET = getItemType("gray_carpet");
    Typed<ItemMeta> LIGHT_GRAY_CARPET = getItemType("light_gray_carpet");
    Typed<ItemMeta> CYAN_CARPET = getItemType("cyan_carpet");
    Typed<ItemMeta> PURPLE_CARPET = getItemType("purple_carpet");
    Typed<ItemMeta> BLUE_CARPET = getItemType("blue_carpet");
    Typed<ItemMeta> BROWN_CARPET = getItemType("brown_carpet");
    Typed<ItemMeta> GREEN_CARPET = getItemType("green_carpet");
    Typed<ItemMeta> RED_CARPET = getItemType("red_carpet");
    Typed<ItemMeta> BLACK_CARPET = getItemType("black_carpet");
    Typed<ItemMeta> TERRACOTTA = getItemType("terracotta");
    Typed<ItemMeta> PACKED_ICE = getItemType("packed_ice");
    Typed<ItemMeta> DIRT_PATH = getItemType("dirt_path");
    Typed<ItemMeta> SUNFLOWER = getItemType("sunflower");
    Typed<ItemMeta> LILAC = getItemType("lilac");
    Typed<ItemMeta> ROSE_BUSH = getItemType("rose_bush");
    Typed<ItemMeta> PEONY = getItemType("peony");
    Typed<ItemMeta> TALL_GRASS = getItemType("tall_grass");
    Typed<ItemMeta> LARGE_FERN = getItemType("large_fern");
    Typed<ItemMeta> WHITE_STAINED_GLASS = getItemType("white_stained_glass");
    Typed<ItemMeta> ORANGE_STAINED_GLASS = getItemType("orange_stained_glass");
    Typed<ItemMeta> MAGENTA_STAINED_GLASS = getItemType("magenta_stained_glass");
    Typed<ItemMeta> LIGHT_BLUE_STAINED_GLASS = getItemType("light_blue_stained_glass");
    Typed<ItemMeta> YELLOW_STAINED_GLASS = getItemType("yellow_stained_glass");
    Typed<ItemMeta> LIME_STAINED_GLASS = getItemType("lime_stained_glass");
    Typed<ItemMeta> PINK_STAINED_GLASS = getItemType("pink_stained_glass");
    Typed<ItemMeta> GRAY_STAINED_GLASS = getItemType("gray_stained_glass");
    Typed<ItemMeta> LIGHT_GRAY_STAINED_GLASS = getItemType("light_gray_stained_glass");
    Typed<ItemMeta> CYAN_STAINED_GLASS = getItemType("cyan_stained_glass");
    Typed<ItemMeta> PURPLE_STAINED_GLASS = getItemType("purple_stained_glass");
    Typed<ItemMeta> BLUE_STAINED_GLASS = getItemType("blue_stained_glass");
    Typed<ItemMeta> BROWN_STAINED_GLASS = getItemType("brown_stained_glass");
    Typed<ItemMeta> GREEN_STAINED_GLASS = getItemType("green_stained_glass");
    Typed<ItemMeta> RED_STAINED_GLASS = getItemType("red_stained_glass");
    Typed<ItemMeta> BLACK_STAINED_GLASS = getItemType("black_stained_glass");
    Typed<ItemMeta> WHITE_STAINED_GLASS_PANE = getItemType("white_stained_glass_pane");
    Typed<ItemMeta> ORANGE_STAINED_GLASS_PANE = getItemType("orange_stained_glass_pane");
    Typed<ItemMeta> MAGENTA_STAINED_GLASS_PANE = getItemType("magenta_stained_glass_pane");
    Typed<ItemMeta> LIGHT_BLUE_STAINED_GLASS_PANE = getItemType("light_blue_stained_glass_pane");
    Typed<ItemMeta> YELLOW_STAINED_GLASS_PANE = getItemType("yellow_stained_glass_pane");
    Typed<ItemMeta> LIME_STAINED_GLASS_PANE = getItemType("lime_stained_glass_pane");
    Typed<ItemMeta> PINK_STAINED_GLASS_PANE = getItemType("pink_stained_glass_pane");
    Typed<ItemMeta> GRAY_STAINED_GLASS_PANE = getItemType("gray_stained_glass_pane");
    Typed<ItemMeta> LIGHT_GRAY_STAINED_GLASS_PANE = getItemType("light_gray_stained_glass_pane");
    Typed<ItemMeta> CYAN_STAINED_GLASS_PANE = getItemType("cyan_stained_glass_pane");
    Typed<ItemMeta> PURPLE_STAINED_GLASS_PANE = getItemType("purple_stained_glass_pane");
    Typed<ItemMeta> BLUE_STAINED_GLASS_PANE = getItemType("blue_stained_glass_pane");
    Typed<ItemMeta> BROWN_STAINED_GLASS_PANE = getItemType("brown_stained_glass_pane");
    Typed<ItemMeta> GREEN_STAINED_GLASS_PANE = getItemType("green_stained_glass_pane");
    Typed<ItemMeta> RED_STAINED_GLASS_PANE = getItemType("red_stained_glass_pane");
    Typed<ItemMeta> BLACK_STAINED_GLASS_PANE = getItemType("black_stained_glass_pane");
    Typed<ItemMeta> PRISMARINE = getItemType("prismarine");
    Typed<ItemMeta> PRISMARINE_BRICKS = getItemType("prismarine_bricks");
    Typed<ItemMeta> DARK_PRISMARINE = getItemType("dark_prismarine");
    Typed<ItemMeta> PRISMARINE_STAIRS = getItemType("prismarine_stairs");
    Typed<ItemMeta> PRISMARINE_BRICK_STAIRS = getItemType("prismarine_brick_stairs");
    Typed<ItemMeta> DARK_PRISMARINE_STAIRS = getItemType("dark_prismarine_stairs");
    Typed<ItemMeta> SEA_LANTERN = getItemType("sea_lantern");
    Typed<ItemMeta> RED_SANDSTONE = getItemType("red_sandstone");
    Typed<ItemMeta> CHISELED_RED_SANDSTONE = getItemType("chiseled_red_sandstone");
    Typed<ItemMeta> CUT_RED_SANDSTONE = getItemType("cut_red_sandstone");
    Typed<ItemMeta> RED_SANDSTONE_STAIRS = getItemType("red_sandstone_stairs");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> REPEATING_COMMAND_BLOCK = getItemType("repeating_command_block");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CHAIN_COMMAND_BLOCK = getItemType("chain_command_block");
    Typed<ItemMeta> MAGMA_BLOCK = getItemType("magma_block");
    Typed<ItemMeta> NETHER_WART_BLOCK = getItemType("nether_wart_block");
    Typed<ItemMeta> WARPED_WART_BLOCK = getItemType("warped_wart_block");
    Typed<ItemMeta> RED_NETHER_BRICKS = getItemType("red_nether_bricks");
    Typed<ItemMeta> BONE_BLOCK = getItemType("bone_block");
    Typed<ItemMeta> STRUCTURE_VOID = getItemType("structure_void");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SHULKER_BOX = getItemType("shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> WHITE_SHULKER_BOX = getItemType("white_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> ORANGE_SHULKER_BOX = getItemType("orange_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> MAGENTA_SHULKER_BOX = getItemType("magenta_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> LIGHT_BLUE_SHULKER_BOX = getItemType("light_blue_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> YELLOW_SHULKER_BOX = getItemType("yellow_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> LIME_SHULKER_BOX = getItemType("lime_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> PINK_SHULKER_BOX = getItemType("pink_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> GRAY_SHULKER_BOX = getItemType("gray_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> LIGHT_GRAY_SHULKER_BOX = getItemType("light_gray_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CYAN_SHULKER_BOX = getItemType("cyan_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> PURPLE_SHULKER_BOX = getItemType("purple_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BLUE_SHULKER_BOX = getItemType("blue_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BROWN_SHULKER_BOX = getItemType("brown_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> GREEN_SHULKER_BOX = getItemType("green_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> RED_SHULKER_BOX = getItemType("red_shulker_box");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BLACK_SHULKER_BOX = getItemType("black_shulker_box");
    Typed<ItemMeta> WHITE_GLAZED_TERRACOTTA = getItemType("white_glazed_terracotta");
    Typed<ItemMeta> ORANGE_GLAZED_TERRACOTTA = getItemType("orange_glazed_terracotta");
    Typed<ItemMeta> MAGENTA_GLAZED_TERRACOTTA = getItemType("magenta_glazed_terracotta");
    Typed<ItemMeta> LIGHT_BLUE_GLAZED_TERRACOTTA = getItemType("light_blue_glazed_terracotta");
    Typed<ItemMeta> YELLOW_GLAZED_TERRACOTTA = getItemType("yellow_glazed_terracotta");
    Typed<ItemMeta> LIME_GLAZED_TERRACOTTA = getItemType("lime_glazed_terracotta");
    Typed<ItemMeta> PINK_GLAZED_TERRACOTTA = getItemType("pink_glazed_terracotta");
    Typed<ItemMeta> GRAY_GLAZED_TERRACOTTA = getItemType("gray_glazed_terracotta");
    Typed<ItemMeta> LIGHT_GRAY_GLAZED_TERRACOTTA = getItemType("light_gray_glazed_terracotta");
    Typed<ItemMeta> CYAN_GLAZED_TERRACOTTA = getItemType("cyan_glazed_terracotta");
    Typed<ItemMeta> PURPLE_GLAZED_TERRACOTTA = getItemType("purple_glazed_terracotta");
    Typed<ItemMeta> BLUE_GLAZED_TERRACOTTA = getItemType("blue_glazed_terracotta");
    Typed<ItemMeta> BROWN_GLAZED_TERRACOTTA = getItemType("brown_glazed_terracotta");
    Typed<ItemMeta> GREEN_GLAZED_TERRACOTTA = getItemType("green_glazed_terracotta");
    Typed<ItemMeta> RED_GLAZED_TERRACOTTA = getItemType("red_glazed_terracotta");
    Typed<ItemMeta> BLACK_GLAZED_TERRACOTTA = getItemType("black_glazed_terracotta");
    Typed<ItemMeta> WHITE_CONCRETE = getItemType("white_concrete");
    Typed<ItemMeta> ORANGE_CONCRETE = getItemType("orange_concrete");
    Typed<ItemMeta> MAGENTA_CONCRETE = getItemType("magenta_concrete");
    Typed<ItemMeta> LIGHT_BLUE_CONCRETE = getItemType("light_blue_concrete");
    Typed<ItemMeta> YELLOW_CONCRETE = getItemType("yellow_concrete");
    Typed<ItemMeta> LIME_CONCRETE = getItemType("lime_concrete");
    Typed<ItemMeta> PINK_CONCRETE = getItemType("pink_concrete");
    Typed<ItemMeta> GRAY_CONCRETE = getItemType("gray_concrete");
    Typed<ItemMeta> LIGHT_GRAY_CONCRETE = getItemType("light_gray_concrete");
    Typed<ItemMeta> CYAN_CONCRETE = getItemType("cyan_concrete");
    Typed<ItemMeta> PURPLE_CONCRETE = getItemType("purple_concrete");
    Typed<ItemMeta> BLUE_CONCRETE = getItemType("blue_concrete");
    Typed<ItemMeta> BROWN_CONCRETE = getItemType("brown_concrete");
    Typed<ItemMeta> GREEN_CONCRETE = getItemType("green_concrete");
    Typed<ItemMeta> RED_CONCRETE = getItemType("red_concrete");
    Typed<ItemMeta> BLACK_CONCRETE = getItemType("black_concrete");
    Typed<ItemMeta> WHITE_CONCRETE_POWDER = getItemType("white_concrete_powder");
    Typed<ItemMeta> ORANGE_CONCRETE_POWDER = getItemType("orange_concrete_powder");
    Typed<ItemMeta> MAGENTA_CONCRETE_POWDER = getItemType("magenta_concrete_powder");
    Typed<ItemMeta> LIGHT_BLUE_CONCRETE_POWDER = getItemType("light_blue_concrete_powder");
    Typed<ItemMeta> YELLOW_CONCRETE_POWDER = getItemType("yellow_concrete_powder");
    Typed<ItemMeta> LIME_CONCRETE_POWDER = getItemType("lime_concrete_powder");
    Typed<ItemMeta> PINK_CONCRETE_POWDER = getItemType("pink_concrete_powder");
    Typed<ItemMeta> GRAY_CONCRETE_POWDER = getItemType("gray_concrete_powder");
    Typed<ItemMeta> LIGHT_GRAY_CONCRETE_POWDER = getItemType("light_gray_concrete_powder");
    Typed<ItemMeta> CYAN_CONCRETE_POWDER = getItemType("cyan_concrete_powder");
    Typed<ItemMeta> PURPLE_CONCRETE_POWDER = getItemType("purple_concrete_powder");
    Typed<ItemMeta> BLUE_CONCRETE_POWDER = getItemType("blue_concrete_powder");
    Typed<ItemMeta> BROWN_CONCRETE_POWDER = getItemType("brown_concrete_powder");
    Typed<ItemMeta> GREEN_CONCRETE_POWDER = getItemType("green_concrete_powder");
    Typed<ItemMeta> RED_CONCRETE_POWDER = getItemType("red_concrete_powder");
    Typed<ItemMeta> BLACK_CONCRETE_POWDER = getItemType("black_concrete_powder");
    Typed<ItemMeta> TURTLE_EGG = getItemType("turtle_egg");
    Typed<ItemMeta> SNIFFER_EGG = getItemType("sniffer_egg");
    Typed<ItemMeta> DRIED_GHAST = getItemType("dried_ghast");
    Typed<ItemMeta> DEAD_TUBE_CORAL_BLOCK = getItemType("dead_tube_coral_block");
    Typed<ItemMeta> DEAD_BRAIN_CORAL_BLOCK = getItemType("dead_brain_coral_block");
    Typed<ItemMeta> DEAD_BUBBLE_CORAL_BLOCK = getItemType("dead_bubble_coral_block");
    Typed<ItemMeta> DEAD_FIRE_CORAL_BLOCK = getItemType("dead_fire_coral_block");
    Typed<ItemMeta> DEAD_HORN_CORAL_BLOCK = getItemType("dead_horn_coral_block");
    Typed<ItemMeta> TUBE_CORAL_BLOCK = getItemType("tube_coral_block");
    Typed<ItemMeta> BRAIN_CORAL_BLOCK = getItemType("brain_coral_block");
    Typed<ItemMeta> BUBBLE_CORAL_BLOCK = getItemType("bubble_coral_block");
    Typed<ItemMeta> FIRE_CORAL_BLOCK = getItemType("fire_coral_block");
    Typed<ItemMeta> HORN_CORAL_BLOCK = getItemType("horn_coral_block");
    Typed<ItemMeta> TUBE_CORAL = getItemType("tube_coral");
    Typed<ItemMeta> BRAIN_CORAL = getItemType("brain_coral");
    Typed<ItemMeta> BUBBLE_CORAL = getItemType("bubble_coral");
    Typed<ItemMeta> FIRE_CORAL = getItemType("fire_coral");
    Typed<ItemMeta> HORN_CORAL = getItemType("horn_coral");
    Typed<ItemMeta> DEAD_BRAIN_CORAL = getItemType("dead_brain_coral");
    Typed<ItemMeta> DEAD_BUBBLE_CORAL = getItemType("dead_bubble_coral");
    Typed<ItemMeta> DEAD_FIRE_CORAL = getItemType("dead_fire_coral");
    Typed<ItemMeta> DEAD_HORN_CORAL = getItemType("dead_horn_coral");
    Typed<ItemMeta> DEAD_TUBE_CORAL = getItemType("dead_tube_coral");
    Typed<ItemMeta> TUBE_CORAL_FAN = getItemType("tube_coral_fan");
    Typed<ItemMeta> BRAIN_CORAL_FAN = getItemType("brain_coral_fan");
    Typed<ItemMeta> BUBBLE_CORAL_FAN = getItemType("bubble_coral_fan");
    Typed<ItemMeta> FIRE_CORAL_FAN = getItemType("fire_coral_fan");
    Typed<ItemMeta> HORN_CORAL_FAN = getItemType("horn_coral_fan");
    Typed<ItemMeta> DEAD_TUBE_CORAL_FAN = getItemType("dead_tube_coral_fan");
    Typed<ItemMeta> DEAD_BRAIN_CORAL_FAN = getItemType("dead_brain_coral_fan");
    Typed<ItemMeta> DEAD_BUBBLE_CORAL_FAN = getItemType("dead_bubble_coral_fan");
    Typed<ItemMeta> DEAD_FIRE_CORAL_FAN = getItemType("dead_fire_coral_fan");
    Typed<ItemMeta> DEAD_HORN_CORAL_FAN = getItemType("dead_horn_coral_fan");
    Typed<ItemMeta> BLUE_ICE = getItemType("blue_ice");
    Typed<ItemMeta> CONDUIT = getItemType("conduit");
    Typed<ItemMeta> POLISHED_GRANITE_STAIRS = getItemType("polished_granite_stairs");
    Typed<ItemMeta> SMOOTH_RED_SANDSTONE_STAIRS = getItemType("smooth_red_sandstone_stairs");
    Typed<ItemMeta> MOSSY_STONE_BRICK_STAIRS = getItemType("mossy_stone_brick_stairs");
    Typed<ItemMeta> POLISHED_DIORITE_STAIRS = getItemType("polished_diorite_stairs");
    Typed<ItemMeta> MOSSY_COBBLESTONE_STAIRS = getItemType("mossy_cobblestone_stairs");
    Typed<ItemMeta> END_STONE_BRICK_STAIRS = getItemType("end_stone_brick_stairs");
    Typed<ItemMeta> STONE_STAIRS = getItemType("stone_stairs");
    Typed<ItemMeta> SMOOTH_SANDSTONE_STAIRS = getItemType("smooth_sandstone_stairs");
    Typed<ItemMeta> SMOOTH_QUARTZ_STAIRS = getItemType("smooth_quartz_stairs");
    Typed<ItemMeta> GRANITE_STAIRS = getItemType("granite_stairs");
    Typed<ItemMeta> ANDESITE_STAIRS = getItemType("andesite_stairs");
    Typed<ItemMeta> RED_NETHER_BRICK_STAIRS = getItemType("red_nether_brick_stairs");
    Typed<ItemMeta> POLISHED_ANDESITE_STAIRS = getItemType("polished_andesite_stairs");
    Typed<ItemMeta> DIORITE_STAIRS = getItemType("diorite_stairs");
    Typed<ItemMeta> COBBLED_DEEPSLATE_STAIRS = getItemType("cobbled_deepslate_stairs");
    Typed<ItemMeta> POLISHED_DEEPSLATE_STAIRS = getItemType("polished_deepslate_stairs");
    Typed<ItemMeta> DEEPSLATE_BRICK_STAIRS = getItemType("deepslate_brick_stairs");
    Typed<ItemMeta> DEEPSLATE_TILE_STAIRS = getItemType("deepslate_tile_stairs");
    Typed<ItemMeta> POLISHED_GRANITE_SLAB = getItemType("polished_granite_slab");
    Typed<ItemMeta> SMOOTH_RED_SANDSTONE_SLAB = getItemType("smooth_red_sandstone_slab");
    Typed<ItemMeta> MOSSY_STONE_BRICK_SLAB = getItemType("mossy_stone_brick_slab");
    Typed<ItemMeta> POLISHED_DIORITE_SLAB = getItemType("polished_diorite_slab");
    Typed<ItemMeta> MOSSY_COBBLESTONE_SLAB = getItemType("mossy_cobblestone_slab");
    Typed<ItemMeta> END_STONE_BRICK_SLAB = getItemType("end_stone_brick_slab");
    Typed<ItemMeta> SMOOTH_SANDSTONE_SLAB = getItemType("smooth_sandstone_slab");
    Typed<ItemMeta> SMOOTH_QUARTZ_SLAB = getItemType("smooth_quartz_slab");
    Typed<ItemMeta> GRANITE_SLAB = getItemType("granite_slab");
    Typed<ItemMeta> ANDESITE_SLAB = getItemType("andesite_slab");
    Typed<ItemMeta> RED_NETHER_BRICK_SLAB = getItemType("red_nether_brick_slab");
    Typed<ItemMeta> POLISHED_ANDESITE_SLAB = getItemType("polished_andesite_slab");
    Typed<ItemMeta> DIORITE_SLAB = getItemType("diorite_slab");
    Typed<ItemMeta> COBBLED_DEEPSLATE_SLAB = getItemType("cobbled_deepslate_slab");
    Typed<ItemMeta> POLISHED_DEEPSLATE_SLAB = getItemType("polished_deepslate_slab");
    Typed<ItemMeta> DEEPSLATE_BRICK_SLAB = getItemType("deepslate_brick_slab");
    Typed<ItemMeta> DEEPSLATE_TILE_SLAB = getItemType("deepslate_tile_slab");
    Typed<ItemMeta> SCAFFOLDING = getItemType("scaffolding");
    Typed<ItemMeta> REDSTONE = getItemType("redstone");
    Typed<ItemMeta> REDSTONE_TORCH = getItemType("redstone_torch");
    Typed<ItemMeta> REDSTONE_BLOCK = getItemType("redstone_block");
    Typed<ItemMeta> REPEATER = getItemType("repeater");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> COMPARATOR = getItemType("comparator");
    Typed<ItemMeta> PISTON = getItemType("piston");
    Typed<ItemMeta> STICKY_PISTON = getItemType("sticky_piston");
    Typed<ItemMeta> SLIME_BLOCK = getItemType("slime_block");
    Typed<ItemMeta> HONEY_BLOCK = getItemType("honey_block");
    Typed<ItemMeta> OBSERVER = getItemType("observer");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> HOPPER = getItemType("hopper");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> DISPENSER = getItemType("dispenser");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> DROPPER = getItemType("dropper");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> LECTERN = getItemType("lectern");
    Typed<ItemMeta> TARGET = getItemType("target");
    Typed<ItemMeta> LEVER = getItemType("lever");
    Typed<ItemMeta> LIGHTNING_ROD = getItemType("lightning_rod");
    Typed<ItemMeta> EXPOSED_LIGHTNING_ROD = getItemType("exposed_lightning_rod");
    Typed<ItemMeta> WEATHERED_LIGHTNING_ROD = getItemType("weathered_lightning_rod");
    Typed<ItemMeta> OXIDIZED_LIGHTNING_ROD = getItemType("oxidized_lightning_rod");
    Typed<ItemMeta> WAXED_LIGHTNING_ROD = getItemType("waxed_lightning_rod");
    Typed<ItemMeta> WAXED_EXPOSED_LIGHTNING_ROD = getItemType("waxed_exposed_lightning_rod");
    Typed<ItemMeta> WAXED_WEATHERED_LIGHTNING_ROD = getItemType("waxed_weathered_lightning_rod");
    Typed<ItemMeta> WAXED_OXIDIZED_LIGHTNING_ROD = getItemType("waxed_oxidized_lightning_rod");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> DAYLIGHT_DETECTOR = getItemType("daylight_detector");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SCULK_SENSOR = getItemType("sculk_sensor");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CALIBRATED_SCULK_SENSOR = getItemType("calibrated_sculk_sensor");
    Typed<ItemMeta> TRIPWIRE_HOOK = getItemType("tripwire_hook");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> TRAPPED_CHEST = getItemType("trapped_chest");
    Typed<ItemMeta> TNT = getItemType("tnt");
    Typed<ItemMeta> REDSTONE_LAMP = getItemType("redstone_lamp");
    Typed<ItemMeta> NOTE_BLOCK = getItemType("note_block");
    Typed<ItemMeta> STONE_BUTTON = getItemType("stone_button");
    Typed<ItemMeta> POLISHED_BLACKSTONE_BUTTON = getItemType("polished_blackstone_button");
    Typed<ItemMeta> OAK_BUTTON = getItemType("oak_button");
    Typed<ItemMeta> SPRUCE_BUTTON = getItemType("spruce_button");
    Typed<ItemMeta> BIRCH_BUTTON = getItemType("birch_button");
    Typed<ItemMeta> JUNGLE_BUTTON = getItemType("jungle_button");
    Typed<ItemMeta> ACACIA_BUTTON = getItemType("acacia_button");
    Typed<ItemMeta> CHERRY_BUTTON = getItemType("cherry_button");
    Typed<ItemMeta> DARK_OAK_BUTTON = getItemType("dark_oak_button");
    Typed<ItemMeta> PALE_OAK_BUTTON = getItemType("pale_oak_button");
    Typed<ItemMeta> MANGROVE_BUTTON = getItemType("mangrove_button");
    Typed<ItemMeta> BAMBOO_BUTTON = getItemType("bamboo_button");
    Typed<ItemMeta> CRIMSON_BUTTON = getItemType("crimson_button");
    Typed<ItemMeta> WARPED_BUTTON = getItemType("warped_button");
    Typed<ItemMeta> STONE_PRESSURE_PLATE = getItemType("stone_pressure_plate");
    Typed<ItemMeta> POLISHED_BLACKSTONE_PRESSURE_PLATE = getItemType("polished_blackstone_pressure_plate");
    Typed<ItemMeta> LIGHT_WEIGHTED_PRESSURE_PLATE = getItemType("light_weighted_pressure_plate");
    Typed<ItemMeta> HEAVY_WEIGHTED_PRESSURE_PLATE = getItemType("heavy_weighted_pressure_plate");
    Typed<ItemMeta> OAK_PRESSURE_PLATE = getItemType("oak_pressure_plate");
    Typed<ItemMeta> SPRUCE_PRESSURE_PLATE = getItemType("spruce_pressure_plate");
    Typed<ItemMeta> BIRCH_PRESSURE_PLATE = getItemType("birch_pressure_plate");
    Typed<ItemMeta> JUNGLE_PRESSURE_PLATE = getItemType("jungle_pressure_plate");
    Typed<ItemMeta> ACACIA_PRESSURE_PLATE = getItemType("acacia_pressure_plate");
    Typed<ItemMeta> CHERRY_PRESSURE_PLATE = getItemType("cherry_pressure_plate");
    Typed<ItemMeta> DARK_OAK_PRESSURE_PLATE = getItemType("dark_oak_pressure_plate");
    Typed<ItemMeta> PALE_OAK_PRESSURE_PLATE = getItemType("pale_oak_pressure_plate");
    Typed<ItemMeta> MANGROVE_PRESSURE_PLATE = getItemType("mangrove_pressure_plate");
    Typed<ItemMeta> BAMBOO_PRESSURE_PLATE = getItemType("bamboo_pressure_plate");
    Typed<ItemMeta> CRIMSON_PRESSURE_PLATE = getItemType("crimson_pressure_plate");
    Typed<ItemMeta> WARPED_PRESSURE_PLATE = getItemType("warped_pressure_plate");
    Typed<ItemMeta> IRON_DOOR = getItemType("iron_door");
    Typed<ItemMeta> OAK_DOOR = getItemType("oak_door");
    Typed<ItemMeta> SPRUCE_DOOR = getItemType("spruce_door");
    Typed<ItemMeta> BIRCH_DOOR = getItemType("birch_door");
    Typed<ItemMeta> JUNGLE_DOOR = getItemType("jungle_door");
    Typed<ItemMeta> ACACIA_DOOR = getItemType("acacia_door");
    Typed<ItemMeta> CHERRY_DOOR = getItemType("cherry_door");
    Typed<ItemMeta> DARK_OAK_DOOR = getItemType("dark_oak_door");
    Typed<ItemMeta> PALE_OAK_DOOR = getItemType("pale_oak_door");
    Typed<ItemMeta> MANGROVE_DOOR = getItemType("mangrove_door");
    Typed<ItemMeta> BAMBOO_DOOR = getItemType("bamboo_door");
    Typed<ItemMeta> CRIMSON_DOOR = getItemType("crimson_door");
    Typed<ItemMeta> WARPED_DOOR = getItemType("warped_door");
    Typed<ItemMeta> COPPER_DOOR = getItemType("copper_door");
    Typed<ItemMeta> EXPOSED_COPPER_DOOR = getItemType("exposed_copper_door");
    Typed<ItemMeta> WEATHERED_COPPER_DOOR = getItemType("weathered_copper_door");
    Typed<ItemMeta> OXIDIZED_COPPER_DOOR = getItemType("oxidized_copper_door");
    Typed<ItemMeta> WAXED_COPPER_DOOR = getItemType("waxed_copper_door");
    Typed<ItemMeta> WAXED_EXPOSED_COPPER_DOOR = getItemType("waxed_exposed_copper_door");
    Typed<ItemMeta> WAXED_WEATHERED_COPPER_DOOR = getItemType("waxed_weathered_copper_door");
    Typed<ItemMeta> WAXED_OXIDIZED_COPPER_DOOR = getItemType("waxed_oxidized_copper_door");
    Typed<ItemMeta> IRON_TRAPDOOR = getItemType("iron_trapdoor");
    Typed<ItemMeta> OAK_TRAPDOOR = getItemType("oak_trapdoor");
    Typed<ItemMeta> SPRUCE_TRAPDOOR = getItemType("spruce_trapdoor");
    Typed<ItemMeta> BIRCH_TRAPDOOR = getItemType("birch_trapdoor");
    Typed<ItemMeta> JUNGLE_TRAPDOOR = getItemType("jungle_trapdoor");
    Typed<ItemMeta> ACACIA_TRAPDOOR = getItemType("acacia_trapdoor");
    Typed<ItemMeta> CHERRY_TRAPDOOR = getItemType("cherry_trapdoor");
    Typed<ItemMeta> DARK_OAK_TRAPDOOR = getItemType("dark_oak_trapdoor");
    Typed<ItemMeta> PALE_OAK_TRAPDOOR = getItemType("pale_oak_trapdoor");
    Typed<ItemMeta> MANGROVE_TRAPDOOR = getItemType("mangrove_trapdoor");
    Typed<ItemMeta> BAMBOO_TRAPDOOR = getItemType("bamboo_trapdoor");
    Typed<ItemMeta> CRIMSON_TRAPDOOR = getItemType("crimson_trapdoor");
    Typed<ItemMeta> WARPED_TRAPDOOR = getItemType("warped_trapdoor");
    Typed<ItemMeta> COPPER_TRAPDOOR = getItemType("copper_trapdoor");
    Typed<ItemMeta> EXPOSED_COPPER_TRAPDOOR = getItemType("exposed_copper_trapdoor");
    Typed<ItemMeta> WEATHERED_COPPER_TRAPDOOR = getItemType("weathered_copper_trapdoor");
    Typed<ItemMeta> OXIDIZED_COPPER_TRAPDOOR = getItemType("oxidized_copper_trapdoor");
    Typed<ItemMeta> WAXED_COPPER_TRAPDOOR = getItemType("waxed_copper_trapdoor");
    Typed<ItemMeta> WAXED_EXPOSED_COPPER_TRAPDOOR = getItemType("waxed_exposed_copper_trapdoor");
    Typed<ItemMeta> WAXED_WEATHERED_COPPER_TRAPDOOR = getItemType("waxed_weathered_copper_trapdoor");
    Typed<ItemMeta> WAXED_OXIDIZED_COPPER_TRAPDOOR = getItemType("waxed_oxidized_copper_trapdoor");
    Typed<ItemMeta> OAK_FENCE_GATE = getItemType("oak_fence_gate");
    Typed<ItemMeta> SPRUCE_FENCE_GATE = getItemType("spruce_fence_gate");
    Typed<ItemMeta> BIRCH_FENCE_GATE = getItemType("birch_fence_gate");
    Typed<ItemMeta> JUNGLE_FENCE_GATE = getItemType("jungle_fence_gate");
    Typed<ItemMeta> ACACIA_FENCE_GATE = getItemType("acacia_fence_gate");
    Typed<ItemMeta> CHERRY_FENCE_GATE = getItemType("cherry_fence_gate");
    Typed<ItemMeta> DARK_OAK_FENCE_GATE = getItemType("dark_oak_fence_gate");
    Typed<ItemMeta> PALE_OAK_FENCE_GATE = getItemType("pale_oak_fence_gate");
    Typed<ItemMeta> MANGROVE_FENCE_GATE = getItemType("mangrove_fence_gate");
    Typed<ItemMeta> BAMBOO_FENCE_GATE = getItemType("bamboo_fence_gate");
    Typed<ItemMeta> CRIMSON_FENCE_GATE = getItemType("crimson_fence_gate");
    Typed<ItemMeta> WARPED_FENCE_GATE = getItemType("warped_fence_gate");
    Typed<ItemMeta> POWERED_RAIL = getItemType("powered_rail");
    Typed<ItemMeta> DETECTOR_RAIL = getItemType("detector_rail");
    Typed<ItemMeta> RAIL = getItemType("rail");
    Typed<ItemMeta> ACTIVATOR_RAIL = getItemType("activator_rail");
    Typed<ItemMeta> SADDLE = getItemType("saddle");
    Typed<ItemMeta> WHITE_HARNESS = getItemType("white_harness");
    Typed<ItemMeta> ORANGE_HARNESS = getItemType("orange_harness");
    Typed<ItemMeta> MAGENTA_HARNESS = getItemType("magenta_harness");
    Typed<ItemMeta> LIGHT_BLUE_HARNESS = getItemType("light_blue_harness");
    Typed<ItemMeta> YELLOW_HARNESS = getItemType("yellow_harness");
    Typed<ItemMeta> LIME_HARNESS = getItemType("lime_harness");
    Typed<ItemMeta> PINK_HARNESS = getItemType("pink_harness");
    Typed<ItemMeta> GRAY_HARNESS = getItemType("gray_harness");
    Typed<ItemMeta> LIGHT_GRAY_HARNESS = getItemType("light_gray_harness");
    Typed<ItemMeta> CYAN_HARNESS = getItemType("cyan_harness");
    Typed<ItemMeta> PURPLE_HARNESS = getItemType("purple_harness");
    Typed<ItemMeta> BLUE_HARNESS = getItemType("blue_harness");
    Typed<ItemMeta> BROWN_HARNESS = getItemType("brown_harness");
    Typed<ItemMeta> GREEN_HARNESS = getItemType("green_harness");
    Typed<ItemMeta> RED_HARNESS = getItemType("red_harness");
    Typed<ItemMeta> BLACK_HARNESS = getItemType("black_harness");
    Typed<ItemMeta> MINECART = getItemType("minecart");
    Typed<ItemMeta> CHEST_MINECART = getItemType("chest_minecart");
    Typed<ItemMeta> FURNACE_MINECART = getItemType("furnace_minecart");
    Typed<ItemMeta> TNT_MINECART = getItemType("tnt_minecart");
    Typed<ItemMeta> HOPPER_MINECART = getItemType("hopper_minecart");
    Typed<ItemMeta> CARROT_ON_A_STICK = getItemType("carrot_on_a_stick");
    Typed<ItemMeta> WARPED_FUNGUS_ON_A_STICK = getItemType("warped_fungus_on_a_stick");
    Typed<ItemMeta> PHANTOM_MEMBRANE = getItemType("phantom_membrane");
    Typed<ItemMeta> ELYTRA = getItemType("elytra");
    Typed<ItemMeta> OAK_BOAT = getItemType("oak_boat");
    Typed<ItemMeta> OAK_CHEST_BOAT = getItemType("oak_chest_boat");
    Typed<ItemMeta> SPRUCE_BOAT = getItemType("spruce_boat");
    Typed<ItemMeta> SPRUCE_CHEST_BOAT = getItemType("spruce_chest_boat");
    Typed<ItemMeta> BIRCH_BOAT = getItemType("birch_boat");
    Typed<ItemMeta> BIRCH_CHEST_BOAT = getItemType("birch_chest_boat");
    Typed<ItemMeta> JUNGLE_BOAT = getItemType("jungle_boat");
    Typed<ItemMeta> JUNGLE_CHEST_BOAT = getItemType("jungle_chest_boat");
    Typed<ItemMeta> ACACIA_BOAT = getItemType("acacia_boat");
    Typed<ItemMeta> ACACIA_CHEST_BOAT = getItemType("acacia_chest_boat");
    Typed<ItemMeta> CHERRY_BOAT = getItemType("cherry_boat");
    Typed<ItemMeta> CHERRY_CHEST_BOAT = getItemType("cherry_chest_boat");
    Typed<ItemMeta> DARK_OAK_BOAT = getItemType("dark_oak_boat");
    Typed<ItemMeta> DARK_OAK_CHEST_BOAT = getItemType("dark_oak_chest_boat");
    Typed<ItemMeta> PALE_OAK_BOAT = getItemType("pale_oak_boat");
    Typed<ItemMeta> PALE_OAK_CHEST_BOAT = getItemType("pale_oak_chest_boat");
    Typed<ItemMeta> MANGROVE_BOAT = getItemType("mangrove_boat");
    Typed<ItemMeta> MANGROVE_CHEST_BOAT = getItemType("mangrove_chest_boat");
    Typed<ItemMeta> BAMBOO_RAFT = getItemType("bamboo_raft");
    Typed<ItemMeta> BAMBOO_CHEST_RAFT = getItemType("bamboo_chest_raft");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> STRUCTURE_BLOCK = getItemType("structure_block");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> JIGSAW = getItemType("jigsaw");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> TEST_BLOCK = getItemType("test_block");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> TEST_INSTANCE_BLOCK = getItemType("test_instance_block");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> TURTLE_HELMET = getItemType("turtle_helmet");
    Typed<ItemMeta> TURTLE_SCUTE = getItemType("turtle_scute");
    Typed<ItemMeta> ARMADILLO_SCUTE = getItemType("armadillo_scute");
    /**
     * ItemMeta: {@link ColorableArmorMeta}
     */
    Typed<ColorableArmorMeta> WOLF_ARMOR = getItemType("wolf_armor");
    Typed<ItemMeta> FLINT_AND_STEEL = getItemType("flint_and_steel");
    Typed<ItemMeta> BOWL = getItemType("bowl");
    Typed<ItemMeta> APPLE = getItemType("apple");
    Typed<ItemMeta> BOW = getItemType("bow");
    Typed<ItemMeta> ARROW = getItemType("arrow");
    Typed<ItemMeta> COAL = getItemType("coal");
    Typed<ItemMeta> CHARCOAL = getItemType("charcoal");
    Typed<ItemMeta> DIAMOND = getItemType("diamond");
    Typed<ItemMeta> EMERALD = getItemType("emerald");
    Typed<ItemMeta> LAPIS_LAZULI = getItemType("lapis_lazuli");
    Typed<ItemMeta> QUARTZ = getItemType("quartz");
    Typed<ItemMeta> AMETHYST_SHARD = getItemType("amethyst_shard");
    Typed<ItemMeta> RAW_IRON = getItemType("raw_iron");
    Typed<ItemMeta> IRON_INGOT = getItemType("iron_ingot");
    Typed<ItemMeta> RAW_COPPER = getItemType("raw_copper");
    Typed<ItemMeta> COPPER_INGOT = getItemType("copper_ingot");
    Typed<ItemMeta> RAW_GOLD = getItemType("raw_gold");
    Typed<ItemMeta> GOLD_INGOT = getItemType("gold_ingot");
    Typed<ItemMeta> NETHERITE_INGOT = getItemType("netherite_ingot");
    Typed<ItemMeta> NETHERITE_SCRAP = getItemType("netherite_scrap");
    Typed<ItemMeta> WOODEN_SWORD = getItemType("wooden_sword");
    Typed<ItemMeta> WOODEN_SHOVEL = getItemType("wooden_shovel");
    Typed<ItemMeta> WOODEN_PICKAXE = getItemType("wooden_pickaxe");
    Typed<ItemMeta> WOODEN_AXE = getItemType("wooden_axe");
    Typed<ItemMeta> WOODEN_HOE = getItemType("wooden_hoe");
    Typed<ItemMeta> COPPER_SWORD = getItemType("copper_sword");
    Typed<ItemMeta> COPPER_SHOVEL = getItemType("copper_shovel");
    Typed<ItemMeta> COPPER_PICKAXE = getItemType("copper_pickaxe");
    Typed<ItemMeta> COPPER_AXE = getItemType("copper_axe");
    Typed<ItemMeta> COPPER_HOE = getItemType("copper_hoe");
    Typed<ItemMeta> STONE_SWORD = getItemType("stone_sword");
    Typed<ItemMeta> STONE_SHOVEL = getItemType("stone_shovel");
    Typed<ItemMeta> STONE_PICKAXE = getItemType("stone_pickaxe");
    Typed<ItemMeta> STONE_AXE = getItemType("stone_axe");
    Typed<ItemMeta> STONE_HOE = getItemType("stone_hoe");
    Typed<ItemMeta> GOLDEN_SWORD = getItemType("golden_sword");
    Typed<ItemMeta> GOLDEN_SHOVEL = getItemType("golden_shovel");
    Typed<ItemMeta> GOLDEN_PICKAXE = getItemType("golden_pickaxe");
    Typed<ItemMeta> GOLDEN_AXE = getItemType("golden_axe");
    Typed<ItemMeta> GOLDEN_HOE = getItemType("golden_hoe");
    Typed<ItemMeta> IRON_SWORD = getItemType("iron_sword");
    Typed<ItemMeta> IRON_SHOVEL = getItemType("iron_shovel");
    Typed<ItemMeta> IRON_PICKAXE = getItemType("iron_pickaxe");
    Typed<ItemMeta> IRON_AXE = getItemType("iron_axe");
    Typed<ItemMeta> IRON_HOE = getItemType("iron_hoe");
    Typed<ItemMeta> DIAMOND_SWORD = getItemType("diamond_sword");
    Typed<ItemMeta> DIAMOND_SHOVEL = getItemType("diamond_shovel");
    Typed<ItemMeta> DIAMOND_PICKAXE = getItemType("diamond_pickaxe");
    Typed<ItemMeta> DIAMOND_AXE = getItemType("diamond_axe");
    Typed<ItemMeta> DIAMOND_HOE = getItemType("diamond_hoe");
    Typed<ItemMeta> NETHERITE_SWORD = getItemType("netherite_sword");
    Typed<ItemMeta> NETHERITE_SHOVEL = getItemType("netherite_shovel");
    Typed<ItemMeta> NETHERITE_PICKAXE = getItemType("netherite_pickaxe");
    Typed<ItemMeta> NETHERITE_AXE = getItemType("netherite_axe");
    Typed<ItemMeta> NETHERITE_HOE = getItemType("netherite_hoe");
    Typed<ItemMeta> STICK = getItemType("stick");
    Typed<ItemMeta> MUSHROOM_STEW = getItemType("mushroom_stew");
    Typed<ItemMeta> STRING = getItemType("string");
    Typed<ItemMeta> FEATHER = getItemType("feather");
    Typed<ItemMeta> GUNPOWDER = getItemType("gunpowder");
    Typed<ItemMeta> WHEAT_SEEDS = getItemType("wheat_seeds");
    Typed<ItemMeta> WHEAT = getItemType("wheat");
    Typed<ItemMeta> BREAD = getItemType("bread");
    /**
     * ItemMeta: {@link ColorableArmorMeta}
     */
    Typed<ColorableArmorMeta> LEATHER_HELMET = getItemType("leather_helmet");
    /**
     * ItemMeta: {@link ColorableArmorMeta}
     */
    Typed<ColorableArmorMeta> LEATHER_CHESTPLATE = getItemType("leather_chestplate");
    /**
     * ItemMeta: {@link ColorableArmorMeta}
     */
    Typed<ColorableArmorMeta> LEATHER_LEGGINGS = getItemType("leather_leggings");
    /**
     * ItemMeta: {@link ColorableArmorMeta}
     */
    Typed<ColorableArmorMeta> LEATHER_BOOTS = getItemType("leather_boots");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> COPPER_HELMET = getItemType("copper_helmet");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> COPPER_CHESTPLATE = getItemType("copper_chestplate");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> COPPER_LEGGINGS = getItemType("copper_leggings");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> COPPER_BOOTS = getItemType("copper_boots");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> CHAINMAIL_HELMET = getItemType("chainmail_helmet");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> CHAINMAIL_CHESTPLATE = getItemType("chainmail_chestplate");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> CHAINMAIL_LEGGINGS = getItemType("chainmail_leggings");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> CHAINMAIL_BOOTS = getItemType("chainmail_boots");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> IRON_HELMET = getItemType("iron_helmet");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> IRON_CHESTPLATE = getItemType("iron_chestplate");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> IRON_LEGGINGS = getItemType("iron_leggings");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> IRON_BOOTS = getItemType("iron_boots");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> DIAMOND_HELMET = getItemType("diamond_helmet");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> DIAMOND_CHESTPLATE = getItemType("diamond_chestplate");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> DIAMOND_LEGGINGS = getItemType("diamond_leggings");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> DIAMOND_BOOTS = getItemType("diamond_boots");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> GOLDEN_HELMET = getItemType("golden_helmet");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> GOLDEN_CHESTPLATE = getItemType("golden_chestplate");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> GOLDEN_LEGGINGS = getItemType("golden_leggings");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> GOLDEN_BOOTS = getItemType("golden_boots");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> NETHERITE_HELMET = getItemType("netherite_helmet");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> NETHERITE_CHESTPLATE = getItemType("netherite_chestplate");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> NETHERITE_LEGGINGS = getItemType("netherite_leggings");
    /**
     * ItemMeta: {@link ArmorMeta}
     */
    Typed<ArmorMeta> NETHERITE_BOOTS = getItemType("netherite_boots");
    Typed<ItemMeta> FLINT = getItemType("flint");
    Typed<ItemMeta> PORKCHOP = getItemType("porkchop");
    Typed<ItemMeta> COOKED_PORKCHOP = getItemType("cooked_porkchop");
    Typed<ItemMeta> PAINTING = getItemType("painting");
    Typed<ItemMeta> GOLDEN_APPLE = getItemType("golden_apple");
    Typed<ItemMeta> ENCHANTED_GOLDEN_APPLE = getItemType("enchanted_golden_apple");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> OAK_SIGN = getItemType("oak_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SPRUCE_SIGN = getItemType("spruce_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BIRCH_SIGN = getItemType("birch_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> JUNGLE_SIGN = getItemType("jungle_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> ACACIA_SIGN = getItemType("acacia_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CHERRY_SIGN = getItemType("cherry_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> DARK_OAK_SIGN = getItemType("dark_oak_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> PALE_OAK_SIGN = getItemType("pale_oak_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> MANGROVE_SIGN = getItemType("mangrove_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BAMBOO_SIGN = getItemType("bamboo_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CRIMSON_SIGN = getItemType("crimson_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> WARPED_SIGN = getItemType("warped_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> OAK_HANGING_SIGN = getItemType("oak_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SPRUCE_HANGING_SIGN = getItemType("spruce_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BIRCH_HANGING_SIGN = getItemType("birch_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> JUNGLE_HANGING_SIGN = getItemType("jungle_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> ACACIA_HANGING_SIGN = getItemType("acacia_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CHERRY_HANGING_SIGN = getItemType("cherry_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> DARK_OAK_HANGING_SIGN = getItemType("dark_oak_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> PALE_OAK_HANGING_SIGN = getItemType("pale_oak_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> MANGROVE_HANGING_SIGN = getItemType("mangrove_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BAMBOO_HANGING_SIGN = getItemType("bamboo_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CRIMSON_HANGING_SIGN = getItemType("crimson_hanging_sign");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> WARPED_HANGING_SIGN = getItemType("warped_hanging_sign");
    Typed<ItemMeta> BUCKET = getItemType("bucket");
    Typed<ItemMeta> WATER_BUCKET = getItemType("water_bucket");
    Typed<ItemMeta> LAVA_BUCKET = getItemType("lava_bucket");
    Typed<ItemMeta> POWDER_SNOW_BUCKET = getItemType("powder_snow_bucket");
    Typed<ItemMeta> SNOWBALL = getItemType("snowball");
    Typed<ItemMeta> LEATHER = getItemType("leather");
    Typed<ItemMeta> MILK_BUCKET = getItemType("milk_bucket");
    Typed<ItemMeta> PUFFERFISH_BUCKET = getItemType("pufferfish_bucket");
    Typed<ItemMeta> SALMON_BUCKET = getItemType("salmon_bucket");
    Typed<ItemMeta> COD_BUCKET = getItemType("cod_bucket");
    /**
     * ItemMeta: {@link TropicalFishBucketMeta}
     */
    Typed<TropicalFishBucketMeta> TROPICAL_FISH_BUCKET = getItemType("tropical_fish_bucket");
    /**
     * ItemMeta: {@link AxolotlBucketMeta}
     */
    Typed<AxolotlBucketMeta> AXOLOTL_BUCKET = getItemType("axolotl_bucket");
    Typed<ItemMeta> SULFUR_CUBE_BUCKET = getItemType("sulfur_cube_bucket");
    Typed<ItemMeta> TADPOLE_BUCKET = getItemType("tadpole_bucket");
    Typed<ItemMeta> BRICK = getItemType("brick");
    Typed<ItemMeta> CLAY_BALL = getItemType("clay_ball");
    Typed<ItemMeta> DRIED_KELP_BLOCK = getItemType("dried_kelp_block");
    Typed<ItemMeta> PAPER = getItemType("paper");
    Typed<ItemMeta> BOOK = getItemType("book");
    Typed<ItemMeta> SLIME_BALL = getItemType("slime_ball");
    Typed<ItemMeta> EGG = getItemType("egg");
    Typed<ItemMeta> BLUE_EGG = getItemType("blue_egg");
    Typed<ItemMeta> BROWN_EGG = getItemType("brown_egg");
    /**
     * ItemMeta: {@link CompassMeta}
     */
    Typed<CompassMeta> COMPASS = getItemType("compass");
    Typed<ItemMeta> RECOVERY_COMPASS = getItemType("recovery_compass");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> BUNDLE = getItemType("bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> WHITE_BUNDLE = getItemType("white_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> ORANGE_BUNDLE = getItemType("orange_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> MAGENTA_BUNDLE = getItemType("magenta_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> LIGHT_BLUE_BUNDLE = getItemType("light_blue_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> YELLOW_BUNDLE = getItemType("yellow_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> LIME_BUNDLE = getItemType("lime_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> PINK_BUNDLE = getItemType("pink_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> GRAY_BUNDLE = getItemType("gray_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> LIGHT_GRAY_BUNDLE = getItemType("light_gray_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> CYAN_BUNDLE = getItemType("cyan_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> PURPLE_BUNDLE = getItemType("purple_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> BLUE_BUNDLE = getItemType("blue_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> BROWN_BUNDLE = getItemType("brown_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> GREEN_BUNDLE = getItemType("green_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> RED_BUNDLE = getItemType("red_bundle");
    /**
     * ItemMeta: {@link BundleMeta}
     */
    Typed<BundleMeta> BLACK_BUNDLE = getItemType("black_bundle");
    Typed<ItemMeta> FISHING_ROD = getItemType("fishing_rod");
    Typed<ItemMeta> CLOCK = getItemType("clock");
    Typed<ItemMeta> SPYGLASS = getItemType("spyglass");
    Typed<ItemMeta> GLOWSTONE_DUST = getItemType("glowstone_dust");
    Typed<ItemMeta> COD = getItemType("cod");
    Typed<ItemMeta> SALMON = getItemType("salmon");
    Typed<ItemMeta> TROPICAL_FISH = getItemType("tropical_fish");
    Typed<ItemMeta> PUFFERFISH = getItemType("pufferfish");
    Typed<ItemMeta> COOKED_COD = getItemType("cooked_cod");
    Typed<ItemMeta> COOKED_SALMON = getItemType("cooked_salmon");
    Typed<ItemMeta> INK_SAC = getItemType("ink_sac");
    Typed<ItemMeta> GLOW_INK_SAC = getItemType("glow_ink_sac");
    Typed<ItemMeta> COCOA_BEANS = getItemType("cocoa_beans");
    Typed<ItemMeta> WHITE_DYE = getItemType("white_dye");
    Typed<ItemMeta> ORANGE_DYE = getItemType("orange_dye");
    Typed<ItemMeta> MAGENTA_DYE = getItemType("magenta_dye");
    Typed<ItemMeta> LIGHT_BLUE_DYE = getItemType("light_blue_dye");
    Typed<ItemMeta> YELLOW_DYE = getItemType("yellow_dye");
    Typed<ItemMeta> LIME_DYE = getItemType("lime_dye");
    Typed<ItemMeta> PINK_DYE = getItemType("pink_dye");
    Typed<ItemMeta> GRAY_DYE = getItemType("gray_dye");
    Typed<ItemMeta> LIGHT_GRAY_DYE = getItemType("light_gray_dye");
    Typed<ItemMeta> CYAN_DYE = getItemType("cyan_dye");
    Typed<ItemMeta> PURPLE_DYE = getItemType("purple_dye");
    Typed<ItemMeta> BLUE_DYE = getItemType("blue_dye");
    Typed<ItemMeta> BROWN_DYE = getItemType("brown_dye");
    Typed<ItemMeta> GREEN_DYE = getItemType("green_dye");
    Typed<ItemMeta> RED_DYE = getItemType("red_dye");
    Typed<ItemMeta> BLACK_DYE = getItemType("black_dye");
    Typed<ItemMeta> BONE_MEAL = getItemType("bone_meal");
    Typed<ItemMeta> BONE = getItemType("bone");
    Typed<ItemMeta> SUGAR = getItemType("sugar");
    Typed<ItemMeta> CAKE = getItemType("cake");
    Typed<ItemMeta> WHITE_BED = getItemType("white_bed");
    Typed<ItemMeta> ORANGE_BED = getItemType("orange_bed");
    Typed<ItemMeta> MAGENTA_BED = getItemType("magenta_bed");
    Typed<ItemMeta> LIGHT_BLUE_BED = getItemType("light_blue_bed");
    Typed<ItemMeta> YELLOW_BED = getItemType("yellow_bed");
    Typed<ItemMeta> LIME_BED = getItemType("lime_bed");
    Typed<ItemMeta> PINK_BED = getItemType("pink_bed");
    Typed<ItemMeta> GRAY_BED = getItemType("gray_bed");
    Typed<ItemMeta> LIGHT_GRAY_BED = getItemType("light_gray_bed");
    Typed<ItemMeta> CYAN_BED = getItemType("cyan_bed");
    Typed<ItemMeta> PURPLE_BED = getItemType("purple_bed");
    Typed<ItemMeta> BLUE_BED = getItemType("blue_bed");
    Typed<ItemMeta> BROWN_BED = getItemType("brown_bed");
    Typed<ItemMeta> GREEN_BED = getItemType("green_bed");
    Typed<ItemMeta> RED_BED = getItemType("red_bed");
    Typed<ItemMeta> BLACK_BED = getItemType("black_bed");
    Typed<ItemMeta> COOKIE = getItemType("cookie");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CRAFTER = getItemType("crafter");
    /**
     * ItemMeta: {@link MapMeta}
     */
    Typed<MapMeta> FILLED_MAP = getItemType("filled_map");
    Typed<ItemMeta> SHEARS = getItemType("shears");
    Typed<ItemMeta> MELON_SLICE = getItemType("melon_slice");
    Typed<ItemMeta> DRIED_KELP = getItemType("dried_kelp");
    Typed<ItemMeta> PUMPKIN_SEEDS = getItemType("pumpkin_seeds");
    Typed<ItemMeta> MELON_SEEDS = getItemType("melon_seeds");
    Typed<ItemMeta> BEEF = getItemType("beef");
    Typed<ItemMeta> COOKED_BEEF = getItemType("cooked_beef");
    Typed<ItemMeta> CHICKEN = getItemType("chicken");
    Typed<ItemMeta> COOKED_CHICKEN = getItemType("cooked_chicken");
    Typed<ItemMeta> ROTTEN_FLESH = getItemType("rotten_flesh");
    Typed<ItemMeta> ENDER_PEARL = getItemType("ender_pearl");
    Typed<ItemMeta> BLAZE_ROD = getItemType("blaze_rod");
    Typed<ItemMeta> GHAST_TEAR = getItemType("ghast_tear");
    Typed<ItemMeta> GOLD_NUGGET = getItemType("gold_nugget");
    Typed<ItemMeta> NETHER_WART = getItemType("nether_wart");
    Typed<ItemMeta> GLASS_BOTTLE = getItemType("glass_bottle");
    /**
     * ItemMeta: {@link PotionMeta}
     */
    Typed<PotionMeta> POTION = getItemType("potion");
    Typed<ItemMeta> SPIDER_EYE = getItemType("spider_eye");
    Typed<ItemMeta> FERMENTED_SPIDER_EYE = getItemType("fermented_spider_eye");
    Typed<ItemMeta> BLAZE_POWDER = getItemType("blaze_powder");
    Typed<ItemMeta> MAGMA_CREAM = getItemType("magma_cream");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BREWING_STAND = getItemType("brewing_stand");
    Typed<ItemMeta> CAULDRON = getItemType("cauldron");
    Typed<ItemMeta> ENDER_EYE = getItemType("ender_eye");
    Typed<ItemMeta> GLISTERING_MELON_SLICE = getItemType("glistering_melon_slice");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> CHICKEN_SPAWN_EGG = getItemType("chicken_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> COW_SPAWN_EGG = getItemType("cow_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> PIG_SPAWN_EGG = getItemType("pig_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SHEEP_SPAWN_EGG = getItemType("sheep_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> CAMEL_SPAWN_EGG = getItemType("camel_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> DONKEY_SPAWN_EGG = getItemType("donkey_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> HORSE_SPAWN_EGG = getItemType("horse_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> MULE_SPAWN_EGG = getItemType("mule_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> CAT_SPAWN_EGG = getItemType("cat_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> PARROT_SPAWN_EGG = getItemType("parrot_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> WOLF_SPAWN_EGG = getItemType("wolf_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ARMADILLO_SPAWN_EGG = getItemType("armadillo_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> BAT_SPAWN_EGG = getItemType("bat_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> BEE_SPAWN_EGG = getItemType("bee_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> FOX_SPAWN_EGG = getItemType("fox_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> GOAT_SPAWN_EGG = getItemType("goat_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> LLAMA_SPAWN_EGG = getItemType("llama_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> OCELOT_SPAWN_EGG = getItemType("ocelot_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> PANDA_SPAWN_EGG = getItemType("panda_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> POLAR_BEAR_SPAWN_EGG = getItemType("polar_bear_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> RABBIT_SPAWN_EGG = getItemType("rabbit_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> AXOLOTL_SPAWN_EGG = getItemType("axolotl_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> COD_SPAWN_EGG = getItemType("cod_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> DOLPHIN_SPAWN_EGG = getItemType("dolphin_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> FROG_SPAWN_EGG = getItemType("frog_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> GLOW_SQUID_SPAWN_EGG = getItemType("glow_squid_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> NAUTILUS_SPAWN_EGG = getItemType("nautilus_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> PUFFERFISH_SPAWN_EGG = getItemType("pufferfish_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SALMON_SPAWN_EGG = getItemType("salmon_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SQUID_SPAWN_EGG = getItemType("squid_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> TADPOLE_SPAWN_EGG = getItemType("tadpole_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> TROPICAL_FISH_SPAWN_EGG = getItemType("tropical_fish_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> TURTLE_SPAWN_EGG = getItemType("turtle_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ALLAY_SPAWN_EGG = getItemType("allay_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> MOOSHROOM_SPAWN_EGG = getItemType("mooshroom_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SNIFFER_SPAWN_EGG = getItemType("sniffer_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SULFUR_CUBE_SPAWN_EGG = getItemType("sulfur_cube_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> COPPER_GOLEM_SPAWN_EGG = getItemType("copper_golem_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> IRON_GOLEM_SPAWN_EGG = getItemType("iron_golem_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SNOW_GOLEM_SPAWN_EGG = getItemType("snow_golem_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> TRADER_LLAMA_SPAWN_EGG = getItemType("trader_llama_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> VILLAGER_SPAWN_EGG = getItemType("villager_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> WANDERING_TRADER_SPAWN_EGG = getItemType("wandering_trader_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> BOGGED_SPAWN_EGG = getItemType("bogged_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> CAMEL_HUSK_SPAWN_EGG = getItemType("camel_husk_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> DROWNED_SPAWN_EGG = getItemType("drowned_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> HUSK_SPAWN_EGG = getItemType("husk_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> PARCHED_SPAWN_EGG = getItemType("parched_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SKELETON_SPAWN_EGG = getItemType("skeleton_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SKELETON_HORSE_SPAWN_EGG = getItemType("skeleton_horse_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> STRAY_SPAWN_EGG = getItemType("stray_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> WITHER_SPAWN_EGG = getItemType("wither_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> WITHER_SKELETON_SPAWN_EGG = getItemType("wither_skeleton_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ZOMBIE_SPAWN_EGG = getItemType("zombie_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ZOMBIE_HORSE_SPAWN_EGG = getItemType("zombie_horse_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ZOMBIE_NAUTILUS_SPAWN_EGG = getItemType("zombie_nautilus_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ZOMBIE_VILLAGER_SPAWN_EGG = getItemType("zombie_villager_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> CAVE_SPIDER_SPAWN_EGG = getItemType("cave_spider_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SPIDER_SPAWN_EGG = getItemType("spider_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> BREEZE_SPAWN_EGG = getItemType("breeze_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> CREAKING_SPAWN_EGG = getItemType("creaking_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> CREEPER_SPAWN_EGG = getItemType("creeper_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ELDER_GUARDIAN_SPAWN_EGG = getItemType("elder_guardian_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> GUARDIAN_SPAWN_EGG = getItemType("guardian_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> PHANTOM_SPAWN_EGG = getItemType("phantom_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SILVERFISH_SPAWN_EGG = getItemType("silverfish_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SLIME_SPAWN_EGG = getItemType("slime_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> WARDEN_SPAWN_EGG = getItemType("warden_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> WITCH_SPAWN_EGG = getItemType("witch_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> EVOKER_SPAWN_EGG = getItemType("evoker_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> PILLAGER_SPAWN_EGG = getItemType("pillager_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> RAVAGER_SPAWN_EGG = getItemType("ravager_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> VINDICATOR_SPAWN_EGG = getItemType("vindicator_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> VEX_SPAWN_EGG = getItemType("vex_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> BLAZE_SPAWN_EGG = getItemType("blaze_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> GHAST_SPAWN_EGG = getItemType("ghast_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> HAPPY_GHAST_SPAWN_EGG = getItemType("happy_ghast_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> HOGLIN_SPAWN_EGG = getItemType("hoglin_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> MAGMA_CUBE_SPAWN_EGG = getItemType("magma_cube_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> PIGLIN_SPAWN_EGG = getItemType("piglin_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> PIGLIN_BRUTE_SPAWN_EGG = getItemType("piglin_brute_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> STRIDER_SPAWN_EGG = getItemType("strider_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ZOGLIN_SPAWN_EGG = getItemType("zoglin_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ZOMBIFIED_PIGLIN_SPAWN_EGG = getItemType("zombified_piglin_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ENDER_DRAGON_SPAWN_EGG = getItemType("ender_dragon_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ENDERMAN_SPAWN_EGG = getItemType("enderman_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> ENDERMITE_SPAWN_EGG = getItemType("endermite_spawn_egg");
    /**
     * ItemMeta: {@link SpawnEggMeta}
     */
    Typed<SpawnEggMeta> SHULKER_SPAWN_EGG = getItemType("shulker_spawn_egg");
    Typed<ItemMeta> EXPERIENCE_BOTTLE = getItemType("experience_bottle");
    Typed<ItemMeta> FIRE_CHARGE = getItemType("fire_charge");
    Typed<ItemMeta> WIND_CHARGE = getItemType("wind_charge");
    /**
     * ItemMeta: {@link BookMeta}
     */
    Typed<BookMeta> WRITABLE_BOOK = getItemType("writable_book");
    /**
     * ItemMeta: {@link BookMeta}
     */
    Typed<BookMeta> WRITTEN_BOOK = getItemType("written_book");
    Typed<ItemMeta> BREEZE_ROD = getItemType("breeze_rod");
    Typed<ItemMeta> MACE = getItemType("mace");
    Typed<ItemMeta> ITEM_FRAME = getItemType("item_frame");
    Typed<ItemMeta> GLOW_ITEM_FRAME = getItemType("glow_item_frame");
    Typed<ItemMeta> FLOWER_POT = getItemType("flower_pot");
    Typed<ItemMeta> CARROT = getItemType("carrot");
    Typed<ItemMeta> POTATO = getItemType("potato");
    Typed<ItemMeta> BAKED_POTATO = getItemType("baked_potato");
    Typed<ItemMeta> POISONOUS_POTATO = getItemType("poisonous_potato");
    Typed<ItemMeta> MAP = getItemType("map");
    Typed<ItemMeta> GOLDEN_CARROT = getItemType("golden_carrot");
    /**
     * ItemMeta: {@link SkullMeta}
     */
    Typed<SkullMeta> SKELETON_SKULL = getItemType("skeleton_skull");
    /**
     * ItemMeta: {@link SkullMeta}
     */
    Typed<SkullMeta> WITHER_SKELETON_SKULL = getItemType("wither_skeleton_skull");
    /**
     * ItemMeta: {@link SkullMeta}
     */
    Typed<SkullMeta> PLAYER_HEAD = getItemType("player_head");
    /**
     * ItemMeta: {@link SkullMeta}
     */
    Typed<SkullMeta> ZOMBIE_HEAD = getItemType("zombie_head");
    /**
     * ItemMeta: {@link SkullMeta}
     */
    Typed<SkullMeta> CREEPER_HEAD = getItemType("creeper_head");
    /**
     * ItemMeta: {@link SkullMeta}
     */
    Typed<SkullMeta> DRAGON_HEAD = getItemType("dragon_head");
    /**
     * ItemMeta: {@link SkullMeta}
     */
    Typed<SkullMeta> PIGLIN_HEAD = getItemType("piglin_head");
    Typed<ItemMeta> NETHER_STAR = getItemType("nether_star");
    Typed<ItemMeta> PUMPKIN_PIE = getItemType("pumpkin_pie");
    /**
     * ItemMeta: {@link FireworkMeta}
     */
    Typed<FireworkMeta> FIREWORK_ROCKET = getItemType("firework_rocket");
    /**
     * ItemMeta: {@link FireworkEffectMeta}
     */
    Typed<FireworkEffectMeta> FIREWORK_STAR = getItemType("firework_star");
    /**
     * ItemMeta: {@link EnchantmentStorageMeta}
     */
    Typed<EnchantmentStorageMeta> ENCHANTED_BOOK = getItemType("enchanted_book");
    Typed<ItemMeta> NETHER_BRICK = getItemType("nether_brick");
    Typed<ItemMeta> RESIN_BRICK = getItemType("resin_brick");
    Typed<ItemMeta> PRISMARINE_SHARD = getItemType("prismarine_shard");
    Typed<ItemMeta> PRISMARINE_CRYSTALS = getItemType("prismarine_crystals");
    Typed<ItemMeta> RABBIT = getItemType("rabbit");
    Typed<ItemMeta> COOKED_RABBIT = getItemType("cooked_rabbit");
    Typed<ItemMeta> RABBIT_STEW = getItemType("rabbit_stew");
    Typed<ItemMeta> RABBIT_FOOT = getItemType("rabbit_foot");
    Typed<ItemMeta> RABBIT_HIDE = getItemType("rabbit_hide");
    Typed<ItemMeta> ARMOR_STAND = getItemType("armor_stand");
    Typed<ItemMeta> COPPER_HORSE_ARMOR = getItemType("copper_horse_armor");
    Typed<ItemMeta> IRON_HORSE_ARMOR = getItemType("iron_horse_armor");
    Typed<ItemMeta> GOLDEN_HORSE_ARMOR = getItemType("golden_horse_armor");
    Typed<ItemMeta> DIAMOND_HORSE_ARMOR = getItemType("diamond_horse_armor");
    Typed<ItemMeta> NETHERITE_HORSE_ARMOR = getItemType("netherite_horse_armor");
    /**
     * ItemMeta: {@link LeatherArmorMeta}
     */
    Typed<LeatherArmorMeta> LEATHER_HORSE_ARMOR = getItemType("leather_horse_armor");
    Typed<ItemMeta> LEAD = getItemType("lead");
    Typed<ItemMeta> NAME_TAG = getItemType("name_tag");
    Typed<ItemMeta> COMMAND_BLOCK_MINECART = getItemType("command_block_minecart");
    Typed<ItemMeta> MUTTON = getItemType("mutton");
    Typed<ItemMeta> COOKED_MUTTON = getItemType("cooked_mutton");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> WHITE_BANNER = getItemType("white_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> ORANGE_BANNER = getItemType("orange_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> MAGENTA_BANNER = getItemType("magenta_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> LIGHT_BLUE_BANNER = getItemType("light_blue_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> YELLOW_BANNER = getItemType("yellow_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> LIME_BANNER = getItemType("lime_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> PINK_BANNER = getItemType("pink_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> GRAY_BANNER = getItemType("gray_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> LIGHT_GRAY_BANNER = getItemType("light_gray_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> CYAN_BANNER = getItemType("cyan_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> PURPLE_BANNER = getItemType("purple_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> BLUE_BANNER = getItemType("blue_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> BROWN_BANNER = getItemType("brown_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> GREEN_BANNER = getItemType("green_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> RED_BANNER = getItemType("red_banner");
    /**
     * ItemMeta: {@link BannerMeta}
     */
    Typed<BannerMeta> BLACK_BANNER = getItemType("black_banner");
    Typed<ItemMeta> END_CRYSTAL = getItemType("end_crystal");
    Typed<ItemMeta> CHORUS_FRUIT = getItemType("chorus_fruit");
    Typed<ItemMeta> POPPED_CHORUS_FRUIT = getItemType("popped_chorus_fruit");
    Typed<ItemMeta> TORCHFLOWER_SEEDS = getItemType("torchflower_seeds");
    Typed<ItemMeta> PITCHER_POD = getItemType("pitcher_pod");
    Typed<ItemMeta> BEETROOT = getItemType("beetroot");
    Typed<ItemMeta> BEETROOT_SEEDS = getItemType("beetroot_seeds");
    Typed<ItemMeta> BEETROOT_SOUP = getItemType("beetroot_soup");
    Typed<ItemMeta> DRAGON_BREATH = getItemType("dragon_breath");
    /**
     * ItemMeta: {@link PotionMeta}
     */
    Typed<PotionMeta> SPLASH_POTION = getItemType("splash_potion");
    Typed<ItemMeta> SPECTRAL_ARROW = getItemType("spectral_arrow");
    /**
     * ItemMeta: {@link PotionMeta}
     */
    Typed<PotionMeta> TIPPED_ARROW = getItemType("tipped_arrow");
    /**
     * ItemMeta: {@link PotionMeta}
     */
    Typed<PotionMeta> LINGERING_POTION = getItemType("lingering_potion");
    /**
     * ItemMeta: {@link ShieldMeta}
     */
    Typed<ShieldMeta> SHIELD = getItemType("shield");
    Typed<ItemMeta> WOODEN_SPEAR = getItemType("wooden_spear");
    Typed<ItemMeta> STONE_SPEAR = getItemType("stone_spear");
    Typed<ItemMeta> COPPER_SPEAR = getItemType("copper_spear");
    Typed<ItemMeta> IRON_SPEAR = getItemType("iron_spear");
    Typed<ItemMeta> GOLDEN_SPEAR = getItemType("golden_spear");
    Typed<ItemMeta> DIAMOND_SPEAR = getItemType("diamond_spear");
    Typed<ItemMeta> NETHERITE_SPEAR = getItemType("netherite_spear");
    Typed<ItemMeta> TOTEM_OF_UNDYING = getItemType("totem_of_undying");
    Typed<ItemMeta> SHULKER_SHELL = getItemType("shulker_shell");
    Typed<ItemMeta> IRON_NUGGET = getItemType("iron_nugget");
    Typed<ItemMeta> COPPER_NUGGET = getItemType("copper_nugget");
    /**
     * ItemMeta: {@link KnowledgeBookMeta}
     */
    Typed<KnowledgeBookMeta> KNOWLEDGE_BOOK = getItemType("knowledge_book");
    Typed<ItemMeta> DEBUG_STICK = getItemType("debug_stick");
    Typed<ItemMeta> MUSIC_DISC_13 = getItemType("music_disc_13");
    Typed<ItemMeta> MUSIC_DISC_CAT = getItemType("music_disc_cat");
    Typed<ItemMeta> MUSIC_DISC_BLOCKS = getItemType("music_disc_blocks");
    Typed<ItemMeta> MUSIC_DISC_BOUNCE = getItemType("music_disc_bounce");
    Typed<ItemMeta> MUSIC_DISC_CHIRP = getItemType("music_disc_chirp");
    Typed<ItemMeta> MUSIC_DISC_CREATOR = getItemType("music_disc_creator");
    Typed<ItemMeta> MUSIC_DISC_CREATOR_MUSIC_BOX = getItemType("music_disc_creator_music_box");
    Typed<ItemMeta> MUSIC_DISC_FAR = getItemType("music_disc_far");
    Typed<ItemMeta> MUSIC_DISC_LAVA_CHICKEN = getItemType("music_disc_lava_chicken");
    Typed<ItemMeta> MUSIC_DISC_MALL = getItemType("music_disc_mall");
    Typed<ItemMeta> MUSIC_DISC_MELLOHI = getItemType("music_disc_mellohi");
    Typed<ItemMeta> MUSIC_DISC_STAL = getItemType("music_disc_stal");
    Typed<ItemMeta> MUSIC_DISC_STRAD = getItemType("music_disc_strad");
    Typed<ItemMeta> MUSIC_DISC_WARD = getItemType("music_disc_ward");
    Typed<ItemMeta> MUSIC_DISC_11 = getItemType("music_disc_11");
    Typed<ItemMeta> MUSIC_DISC_WAIT = getItemType("music_disc_wait");
    Typed<ItemMeta> MUSIC_DISC_OTHERSIDE = getItemType("music_disc_otherside");
    Typed<ItemMeta> MUSIC_DISC_RELIC = getItemType("music_disc_relic");
    Typed<ItemMeta> MUSIC_DISC_5 = getItemType("music_disc_5");
    Typed<ItemMeta> MUSIC_DISC_PIGSTEP = getItemType("music_disc_pigstep");
    Typed<ItemMeta> MUSIC_DISC_PRECIPICE = getItemType("music_disc_precipice");
    Typed<ItemMeta> MUSIC_DISC_TEARS = getItemType("music_disc_tears");
    Typed<ItemMeta> DISC_FRAGMENT_5 = getItemType("disc_fragment_5");
    Typed<ItemMeta> TRIDENT = getItemType("trident");
    Typed<ItemMeta> NAUTILUS_SHELL = getItemType("nautilus_shell");
    Typed<ItemMeta> IRON_NAUTILUS_ARMOR = getItemType("iron_nautilus_armor");
    Typed<ItemMeta> GOLDEN_NAUTILUS_ARMOR = getItemType("golden_nautilus_armor");
    Typed<ItemMeta> DIAMOND_NAUTILUS_ARMOR = getItemType("diamond_nautilus_armor");
    Typed<ItemMeta> NETHERITE_NAUTILUS_ARMOR = getItemType("netherite_nautilus_armor");
    Typed<ItemMeta> COPPER_NAUTILUS_ARMOR = getItemType("copper_nautilus_armor");
    Typed<ItemMeta> HEART_OF_THE_SEA = getItemType("heart_of_the_sea");
    /**
     * ItemMeta: {@link CrossbowMeta}
     */
    Typed<CrossbowMeta> CROSSBOW = getItemType("crossbow");
    /**
     * ItemMeta: {@link SuspiciousStewMeta}
     */
    Typed<SuspiciousStewMeta> SUSPICIOUS_STEW = getItemType("suspicious_stew");
    Typed<ItemMeta> LOOM = getItemType("loom");
    Typed<ItemMeta> FLOWER_BANNER_PATTERN = getItemType("flower_banner_pattern");
    Typed<ItemMeta> CREEPER_BANNER_PATTERN = getItemType("creeper_banner_pattern");
    Typed<ItemMeta> SKULL_BANNER_PATTERN = getItemType("skull_banner_pattern");
    Typed<ItemMeta> MOJANG_BANNER_PATTERN = getItemType("mojang_banner_pattern");
    Typed<ItemMeta> GLOBE_BANNER_PATTERN = getItemType("globe_banner_pattern");
    Typed<ItemMeta> PIGLIN_BANNER_PATTERN = getItemType("piglin_banner_pattern");
    Typed<ItemMeta> FLOW_BANNER_PATTERN = getItemType("flow_banner_pattern");
    Typed<ItemMeta> GUSTER_BANNER_PATTERN = getItemType("guster_banner_pattern");
    Typed<ItemMeta> FIELD_MASONED_BANNER_PATTERN = getItemType("field_masoned_banner_pattern");
    Typed<ItemMeta> BORDURE_INDENTED_BANNER_PATTERN = getItemType("bordure_indented_banner_pattern");
    /**
     * ItemMeta: {@link MusicInstrumentMeta}
     */
    Typed<MusicInstrumentMeta> GOAT_HORN = getItemType("goat_horn");
    Typed<ItemMeta> COMPOSTER = getItemType("composter");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BARREL = getItemType("barrel");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SMOKER = getItemType("smoker");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BLAST_FURNACE = getItemType("blast_furnace");
    Typed<ItemMeta> CARTOGRAPHY_TABLE = getItemType("cartography_table");
    Typed<ItemMeta> FLETCHING_TABLE = getItemType("fletching_table");
    Typed<ItemMeta> GRINDSTONE = getItemType("grindstone");
    Typed<ItemMeta> SMITHING_TABLE = getItemType("smithing_table");
    Typed<ItemMeta> STONECUTTER = getItemType("stonecutter");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BELL = getItemType("bell");
    Typed<ItemMeta> LANTERN = getItemType("lantern");
    Typed<ItemMeta> COPPER_LANTERN = getItemType("copper_lantern");
    Typed<ItemMeta> SOUL_LANTERN = getItemType("soul_lantern");
    Typed<ItemMeta> SWEET_BERRIES = getItemType("sweet_berries");
    Typed<ItemMeta> GLOW_BERRIES = getItemType("glow_berries");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> CAMPFIRE = getItemType("campfire");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> SOUL_CAMPFIRE = getItemType("soul_campfire");
    Typed<ItemMeta> SHROOMLIGHT = getItemType("shroomlight");
    Typed<ItemMeta> HONEYCOMB = getItemType("honeycomb");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BEE_NEST = getItemType("bee_nest");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> BEEHIVE = getItemType("beehive");
    Typed<ItemMeta> HONEY_BOTTLE = getItemType("honey_bottle");
    Typed<ItemMeta> HONEYCOMB_BLOCK = getItemType("honeycomb_block");
    Typed<ItemMeta> LODESTONE = getItemType("lodestone");
    Typed<ItemMeta> CRYING_OBSIDIAN = getItemType("crying_obsidian");
    Typed<ItemMeta> BLACKSTONE = getItemType("blackstone");
    Typed<ItemMeta> BLACKSTONE_SLAB = getItemType("blackstone_slab");
    Typed<ItemMeta> BLACKSTONE_STAIRS = getItemType("blackstone_stairs");
    Typed<ItemMeta> GILDED_BLACKSTONE = getItemType("gilded_blackstone");
    Typed<ItemMeta> POLISHED_BLACKSTONE = getItemType("polished_blackstone");
    Typed<ItemMeta> POLISHED_BLACKSTONE_SLAB = getItemType("polished_blackstone_slab");
    Typed<ItemMeta> POLISHED_BLACKSTONE_STAIRS = getItemType("polished_blackstone_stairs");
    Typed<ItemMeta> CHISELED_POLISHED_BLACKSTONE = getItemType("chiseled_polished_blackstone");
    Typed<ItemMeta> POLISHED_BLACKSTONE_BRICKS = getItemType("polished_blackstone_bricks");
    Typed<ItemMeta> POLISHED_BLACKSTONE_BRICK_SLAB = getItemType("polished_blackstone_brick_slab");
    Typed<ItemMeta> POLISHED_BLACKSTONE_BRICK_STAIRS = getItemType("polished_blackstone_brick_stairs");
    Typed<ItemMeta> CRACKED_POLISHED_BLACKSTONE_BRICKS = getItemType("cracked_polished_blackstone_bricks");
    Typed<ItemMeta> RESPAWN_ANCHOR = getItemType("respawn_anchor");
    Typed<ItemMeta> CANDLE = getItemType("candle");
    Typed<ItemMeta> WHITE_CANDLE = getItemType("white_candle");
    Typed<ItemMeta> ORANGE_CANDLE = getItemType("orange_candle");
    Typed<ItemMeta> MAGENTA_CANDLE = getItemType("magenta_candle");
    Typed<ItemMeta> LIGHT_BLUE_CANDLE = getItemType("light_blue_candle");
    Typed<ItemMeta> YELLOW_CANDLE = getItemType("yellow_candle");
    Typed<ItemMeta> LIME_CANDLE = getItemType("lime_candle");
    Typed<ItemMeta> PINK_CANDLE = getItemType("pink_candle");
    Typed<ItemMeta> GRAY_CANDLE = getItemType("gray_candle");
    Typed<ItemMeta> LIGHT_GRAY_CANDLE = getItemType("light_gray_candle");
    Typed<ItemMeta> CYAN_CANDLE = getItemType("cyan_candle");
    Typed<ItemMeta> PURPLE_CANDLE = getItemType("purple_candle");
    Typed<ItemMeta> BLUE_CANDLE = getItemType("blue_candle");
    Typed<ItemMeta> BROWN_CANDLE = getItemType("brown_candle");
    Typed<ItemMeta> GREEN_CANDLE = getItemType("green_candle");
    Typed<ItemMeta> RED_CANDLE = getItemType("red_candle");
    Typed<ItemMeta> BLACK_CANDLE = getItemType("black_candle");
    Typed<ItemMeta> SMALL_AMETHYST_BUD = getItemType("small_amethyst_bud");
    Typed<ItemMeta> MEDIUM_AMETHYST_BUD = getItemType("medium_amethyst_bud");
    Typed<ItemMeta> LARGE_AMETHYST_BUD = getItemType("large_amethyst_bud");
    Typed<ItemMeta> AMETHYST_CLUSTER = getItemType("amethyst_cluster");
    Typed<ItemMeta> POINTED_DRIPSTONE = getItemType("pointed_dripstone");
    Typed<ItemMeta> SULFUR_SPIKE = getItemType("sulfur_spike");
    Typed<ItemMeta> OCHRE_FROGLIGHT = getItemType("ochre_froglight");
    Typed<ItemMeta> VERDANT_FROGLIGHT = getItemType("verdant_froglight");
    Typed<ItemMeta> PEARLESCENT_FROGLIGHT = getItemType("pearlescent_froglight");
    Typed<ItemMeta> FROGSPAWN = getItemType("frogspawn");
    Typed<ItemMeta> ECHO_SHARD = getItemType("echo_shard");
    Typed<ItemMeta> BRUSH = getItemType("brush");
    Typed<ItemMeta> NETHERITE_UPGRADE_SMITHING_TEMPLATE = getItemType("netherite_upgrade_smithing_template");
    Typed<ItemMeta> SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("sentry_armor_trim_smithing_template");
    Typed<ItemMeta> DUNE_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("dune_armor_trim_smithing_template");
    Typed<ItemMeta> COAST_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("coast_armor_trim_smithing_template");
    Typed<ItemMeta> WILD_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("wild_armor_trim_smithing_template");
    Typed<ItemMeta> WARD_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("ward_armor_trim_smithing_template");
    Typed<ItemMeta> EYE_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("eye_armor_trim_smithing_template");
    Typed<ItemMeta> VEX_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("vex_armor_trim_smithing_template");
    Typed<ItemMeta> TIDE_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("tide_armor_trim_smithing_template");
    Typed<ItemMeta> SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("snout_armor_trim_smithing_template");
    Typed<ItemMeta> RIB_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("rib_armor_trim_smithing_template");
    Typed<ItemMeta> SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("spire_armor_trim_smithing_template");
    Typed<ItemMeta> WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("wayfinder_armor_trim_smithing_template");
    Typed<ItemMeta> SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("shaper_armor_trim_smithing_template");
    Typed<ItemMeta> SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("silence_armor_trim_smithing_template");
    Typed<ItemMeta> RAISER_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("raiser_armor_trim_smithing_template");
    Typed<ItemMeta> HOST_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("host_armor_trim_smithing_template");
    Typed<ItemMeta> FLOW_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("flow_armor_trim_smithing_template");
    Typed<ItemMeta> BOLT_ARMOR_TRIM_SMITHING_TEMPLATE = getItemType("bolt_armor_trim_smithing_template");
    Typed<ItemMeta> ANGLER_POTTERY_SHERD = getItemType("angler_pottery_sherd");
    Typed<ItemMeta> ARCHER_POTTERY_SHERD = getItemType("archer_pottery_sherd");
    Typed<ItemMeta> ARMS_UP_POTTERY_SHERD = getItemType("arms_up_pottery_sherd");
    Typed<ItemMeta> BLADE_POTTERY_SHERD = getItemType("blade_pottery_sherd");
    Typed<ItemMeta> BREWER_POTTERY_SHERD = getItemType("brewer_pottery_sherd");
    Typed<ItemMeta> BURN_POTTERY_SHERD = getItemType("burn_pottery_sherd");
    Typed<ItemMeta> DANGER_POTTERY_SHERD = getItemType("danger_pottery_sherd");
    Typed<ItemMeta> EXPLORER_POTTERY_SHERD = getItemType("explorer_pottery_sherd");
    Typed<ItemMeta> FLOW_POTTERY_SHERD = getItemType("flow_pottery_sherd");
    Typed<ItemMeta> FRIEND_POTTERY_SHERD = getItemType("friend_pottery_sherd");
    Typed<ItemMeta> GUSTER_POTTERY_SHERD = getItemType("guster_pottery_sherd");
    Typed<ItemMeta> HEART_POTTERY_SHERD = getItemType("heart_pottery_sherd");
    Typed<ItemMeta> HEARTBREAK_POTTERY_SHERD = getItemType("heartbreak_pottery_sherd");
    Typed<ItemMeta> HOWL_POTTERY_SHERD = getItemType("howl_pottery_sherd");
    Typed<ItemMeta> MINER_POTTERY_SHERD = getItemType("miner_pottery_sherd");
    Typed<ItemMeta> MOURNER_POTTERY_SHERD = getItemType("mourner_pottery_sherd");
    Typed<ItemMeta> PLENTY_POTTERY_SHERD = getItemType("plenty_pottery_sherd");
    Typed<ItemMeta> PRIZE_POTTERY_SHERD = getItemType("prize_pottery_sherd");
    Typed<ItemMeta> SCRAPE_POTTERY_SHERD = getItemType("scrape_pottery_sherd");
    Typed<ItemMeta> SHEAF_POTTERY_SHERD = getItemType("sheaf_pottery_sherd");
    Typed<ItemMeta> SHELTER_POTTERY_SHERD = getItemType("shelter_pottery_sherd");
    Typed<ItemMeta> SKULL_POTTERY_SHERD = getItemType("skull_pottery_sherd");
    Typed<ItemMeta> SNORT_POTTERY_SHERD = getItemType("snort_pottery_sherd");
    Typed<ItemMeta> COPPER_GRATE = getItemType("copper_grate");
    Typed<ItemMeta> EXPOSED_COPPER_GRATE = getItemType("exposed_copper_grate");
    Typed<ItemMeta> WEATHERED_COPPER_GRATE = getItemType("weathered_copper_grate");
    Typed<ItemMeta> OXIDIZED_COPPER_GRATE = getItemType("oxidized_copper_grate");
    Typed<ItemMeta> WAXED_COPPER_GRATE = getItemType("waxed_copper_grate");
    Typed<ItemMeta> WAXED_EXPOSED_COPPER_GRATE = getItemType("waxed_exposed_copper_grate");
    Typed<ItemMeta> WAXED_WEATHERED_COPPER_GRATE = getItemType("waxed_weathered_copper_grate");
    Typed<ItemMeta> WAXED_OXIDIZED_COPPER_GRATE = getItemType("waxed_oxidized_copper_grate");
    Typed<ItemMeta> COPPER_BULB = getItemType("copper_bulb");
    Typed<ItemMeta> EXPOSED_COPPER_BULB = getItemType("exposed_copper_bulb");
    Typed<ItemMeta> WEATHERED_COPPER_BULB = getItemType("weathered_copper_bulb");
    Typed<ItemMeta> OXIDIZED_COPPER_BULB = getItemType("oxidized_copper_bulb");
    Typed<ItemMeta> WAXED_COPPER_BULB = getItemType("waxed_copper_bulb");
    Typed<ItemMeta> WAXED_EXPOSED_COPPER_BULB = getItemType("waxed_exposed_copper_bulb");
    Typed<ItemMeta> WAXED_WEATHERED_COPPER_BULB = getItemType("waxed_weathered_copper_bulb");
    Typed<ItemMeta> WAXED_OXIDIZED_COPPER_BULB = getItemType("waxed_oxidized_copper_bulb");
    Typed<ItemMeta> COPPER_CHEST = getItemType("copper_chest");
    Typed<ItemMeta> EXPOSED_COPPER_CHEST = getItemType("exposed_copper_chest");
    Typed<ItemMeta> WEATHERED_COPPER_CHEST = getItemType("weathered_copper_chest");
    Typed<ItemMeta> OXIDIZED_COPPER_CHEST = getItemType("oxidized_copper_chest");
    Typed<ItemMeta> WAXED_COPPER_CHEST = getItemType("waxed_copper_chest");
    Typed<ItemMeta> WAXED_EXPOSED_COPPER_CHEST = getItemType("waxed_exposed_copper_chest");
    Typed<ItemMeta> WAXED_WEATHERED_COPPER_CHEST = getItemType("waxed_weathered_copper_chest");
    Typed<ItemMeta> WAXED_OXIDIZED_COPPER_CHEST = getItemType("waxed_oxidized_copper_chest");
    Typed<ItemMeta> COPPER_GOLEM_STATUE = getItemType("copper_golem_statue");
    Typed<ItemMeta> EXPOSED_COPPER_GOLEM_STATUE = getItemType("exposed_copper_golem_statue");
    Typed<ItemMeta> WEATHERED_COPPER_GOLEM_STATUE = getItemType("weathered_copper_golem_statue");
    Typed<ItemMeta> OXIDIZED_COPPER_GOLEM_STATUE = getItemType("oxidized_copper_golem_statue");
    Typed<ItemMeta> WAXED_COPPER_GOLEM_STATUE = getItemType("waxed_copper_golem_statue");
    Typed<ItemMeta> WAXED_EXPOSED_COPPER_GOLEM_STATUE = getItemType("waxed_exposed_copper_golem_statue");
    Typed<ItemMeta> WAXED_WEATHERED_COPPER_GOLEM_STATUE = getItemType("waxed_weathered_copper_golem_statue");
    Typed<ItemMeta> WAXED_OXIDIZED_COPPER_GOLEM_STATUE = getItemType("waxed_oxidized_copper_golem_statue");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> TRIAL_SPAWNER = getItemType("trial_spawner");
    Typed<ItemMeta> TRIAL_KEY = getItemType("trial_key");
    Typed<ItemMeta> OMINOUS_TRIAL_KEY = getItemType("ominous_trial_key");
    /**
     * ItemMeta: {@link BlockStateMeta}
     */
    Typed<BlockStateMeta> VAULT = getItemType("vault");
    /**
     * ItemMeta: {@link OminousBottleMeta}
     */
    Typed<OminousBottleMeta> OMINOUS_BOTTLE = getItemType("ominous_bottle");
    //</editor-fold>

    @NotNull
    private static <M extends ItemType> M getItemType(@NotNull String key) {
        // Cast instead of using ItemType#typed, since item type can be a mock during testing and would return null
        return (M) Registry.ITEM.getOrThrow(NamespacedKey.minecraft(key));
    }

    /**
     * Yields this item type as a typed version of itself with a plain {@link ItemMeta} representing it.
     *
     * @return the typed item type.
     */
    @NotNull
    Typed<ItemMeta> typed();

    /**
     * Yields this item type as a typed version of itself with a plain {@link ItemMeta} representing it.
     *
     * @param itemMetaType the class type of the {@link ItemMeta} to type this {@link ItemType} with.
     * @param <M> the generic type of the item meta to type this item type with.
     * @return the typed item type.
     */
    @NotNull
    <M extends ItemMeta> Typed<M> typed(@NotNull Class<M> itemMetaType);

    /**
     * Constructs a new itemstack with this item type that has the amount 1.
     *
     * @return the constructed item stack.
     */
    @NotNull
    ItemStack createItemStack();

    /**
     * Constructs a new itemstack with this item type.
     *
     * @param amount the amount of the item stack.
     * @return the constructed item stack.
     */
    @NotNull
    ItemStack createItemStack(int amount);

    /**
     * Returns true if this ItemType has a corresponding {@link BlockType}.
     *
     * @return true if there is a corresponding BlockType, otherwise false
     * @see #getBlockType()
     */
    boolean hasBlockType();

    /**
     * Returns the corresponding {@link BlockType} for the given ItemType.
     * <p>
     * If there is no corresponding {@link BlockType} an error will be thrown.
     *
     * @return the corresponding BlockType
     * @see #hasBlockType()
     */
    @NotNull
    BlockType getBlockType();

    /**
     * Gets the ItemMeta class of this ItemType
     *
     * @return the ItemMeta class of this ItemType
     */
    @NotNull
    Class<? extends ItemMeta> getItemMetaClass();

    /**
     * Gets the maximum amount of this item type that can be held in a stack
     *
     * @return Maximum stack size for this item type
     */
    int getMaxStackSize();

    /**
     * Gets the maximum durability of this item type
     *
     * @return Maximum durability for this item type
     */
    short getMaxDurability();

    /**
     * Checks if this item type is edible.
     *
     * @return true if this item type is edible.
     */
    boolean isEdible();

    /**
     * @return True if this item type represents a playable music disk.
     */
    boolean isRecord();

    /**
     * Checks if this item type can be used as fuel in a Furnace
     *
     * @return true if this item type can be used as fuel.
     */
    boolean isFuel();

    /**
     * Checks whether this item type is compostable (can be inserted into a
     * composter).
     *
     * @return true if this item type is compostable
     * @see #getCompostChance()
     */
    boolean isCompostable();

    /**
     * Get the chance that this item type will successfully compost. The
     * returned value is between 0 and 1 (inclusive).
     *
     * Items with a compost chance of 1 will always raise the composter's level,
     * while items with a compost chance of 0 will never raise it.
     *
     * Plugins should check that {@link #isCompostable} returns true before
     * calling this method.
     *
     * @return the chance that this item type will successfully compost
     * @throws IllegalArgumentException if this item type is not compostable
     * @see #isCompostable()
     */
    float getCompostChance();

    /**
     * Determines the remaining item in a crafting grid after crafting with this
     * ingredient.
     *
     * @return the item left behind when crafting, or null if nothing is.
     */
    @Nullable
    ItemType getCraftingRemainingItem();

//    /**
//     * Get the best suitable slot for this item type.
//     *
//     * For most items this will be {@link EquipmentSlot#HAND}.
//     *
//     * @return the best EquipmentSlot for this item type
//     */
//    @NotNull
//    EquipmentSlot getEquipmentSlot();

    /**
     * Return an immutable copy of all default {@link Attribute}s and their
     * {@link AttributeModifier}s for a given {@link EquipmentSlot}.
     *
     * Default attributes are those that are always preset on some items, such
     * as the attack damage on weapons or the armor value on armor.
     *
     * @param slot the {@link EquipmentSlot} to check
     * @return the immutable {@link Multimap} with the respective default
     * Attributes and modifiers, or an empty map if no attributes are set.
     */
    @NotNull
    Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot);

    /**
     * Get the {@link CreativeCategory} to which this item type belongs.
     *
     * @return the creative category. null if does not belong to a category
     * @deprecated creative categories no longer exist on the server
     */
    @Nullable
    @Deprecated(since = "1.20.6")
    CreativeCategory getCreativeCategory();

    /**
     * Gets if the ItemType is enabled by the features in a world.
     *
     * @param world the world to check
     * @return true if this ItemType can be used in this World.
     */
    boolean isEnabledByFeature(@NotNull World world);

    /**
     * {@inheritDoc}
     *
     * @see #getKeyOrThrow()
     * @see #isRegistered()
     * @deprecated A key might not always be present, use {@link #getKeyOrThrow()} instead.
     */
    @NotNull
    @Override
    @Deprecated(since = "1.21.4")
    NamespacedKey getKey();

    /**
     * Tries to convert this ItemType into a Material
     *
     * @return the converted Material or null
     * @deprecated only for internal use
     */
    @Nullable
    @Deprecated(since = "1.20.6")
    Material asMaterial();
}
