package org.bukkit.block;

import java.util.function.Consumer;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Translatable;
import org.bukkit.World;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Brushable;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Hatchable;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Snowable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.AmethystCluster;
import org.bukkit.block.data.type.Bamboo;
import org.bukkit.block.data.type.Barrel;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.block.data.type.Bell;
import org.bukkit.block.data.type.BigDripleaf;
import org.bukkit.block.data.type.BrewingStand;
import org.bukkit.block.data.type.BubbleColumn;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.CalibratedSculkSensor;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.block.data.type.Candle;
import org.bukkit.block.data.type.CaveVines;
import org.bukkit.block.data.type.CaveVinesPlant;
import org.bukkit.block.data.type.Chain;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.block.data.type.CopperBulb;
import org.bukkit.block.data.type.CopperGolemStatue;
import org.bukkit.block.data.type.CoralWallFan;
import org.bukkit.block.data.type.Crafter;
import org.bukkit.block.data.type.CreakingHeart;
import org.bukkit.block.data.type.DaylightDetector;
import org.bukkit.block.data.type.DecoratedPot;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.DriedGhast;
import org.bukkit.block.data.type.Dripleaf;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.block.data.type.EnderChest;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Fire;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.block.data.type.GlowLichen;
import org.bukkit.block.data.type.Grindstone;
import org.bukkit.block.data.type.HangingMoss;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.block.data.type.Jukebox;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.LeafLitter;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.block.data.type.Light;
import org.bukkit.block.data.type.LightningRod;
import org.bukkit.block.data.type.MangrovePropagule;
import org.bukkit.block.data.type.MossyCarpet;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Observer;
import org.bukkit.block.data.type.PinkPetals;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.block.data.type.PitcherCrop;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.block.data.type.PotentSulfur;
import org.bukkit.block.data.type.RedstoneRail;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Repeater;
import org.bukkit.block.data.type.ResinClump;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.block.data.type.Scaffolding;
import org.bukkit.block.data.type.SculkCatalyst;
import org.bukkit.block.data.type.SculkSensor;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.block.data.type.SculkVein;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.block.data.type.Shelf;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.Skull;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.SmallDripleaf;
import org.bukkit.block.data.type.Snow;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.StructureBlock;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.TNT;
import org.bukkit.block.data.type.TechnicalPiston;
import org.bukkit.block.data.type.TestBlock;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.block.data.type.TripwireHook;
import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.block.data.type.Vault;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.WallHangingSign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.data.type.WallSkull;
import org.bukkit.inventory.ItemType;
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
public interface BlockType extends Keyed, Translatable, RegistryAware {

    /**
     * Typed represents a subtype of {@link BlockType}s that have a known block
     * data type at compile time.
     *
     * @param <B> the generic type of the block data that represents the block
     * type.
     */
    interface Typed<B extends BlockData> extends BlockType {

        /**
         * Gets the BlockData class of this BlockType
         *
         * @return the BlockData class of this BlockType
         */
        @NotNull
        @Override
        Class<B> getBlockDataClass();

        /**
         * Creates a new {@link BlockData} instance for this block type, with
         * all properties initialized to unspecified defaults.
         *
         * @param consumer consumer to run on new instance before returning
         * @return new data instance
         */
        @NotNull
        B createBlockData(@Nullable Consumer<? super B> consumer);

        /**
         * Creates a new {@link BlockData} instance for this block type, with all
         * properties initialized to unspecified defaults.
         *
         * @return new data instance
         */
        @NotNull
        @Override
        B createBlockData();

        /**
         * Creates a new {@link BlockData} instance for this block type, with all
         * properties initialized to unspecified defaults, except for those provided
         * in data.
         *
         * @param data data string
         * @return new data instance
         * @throws IllegalArgumentException if the specified data is not valid
         */
        @NotNull
        B createBlockData(@Nullable String data);
    }

    //<editor-fold desc="BlockTypes" defaultstate="collapsed">
    Typed<BlockData> AIR = getBlockType("air");
    Typed<BlockData> STONE = getBlockType("stone");
    Typed<BlockData> GRANITE = getBlockType("granite");
    Typed<BlockData> POLISHED_GRANITE = getBlockType("polished_granite");
    Typed<BlockData> DIORITE = getBlockType("diorite");
    Typed<BlockData> POLISHED_DIORITE = getBlockType("polished_diorite");
    Typed<BlockData> ANDESITE = getBlockType("andesite");
    Typed<BlockData> POLISHED_ANDESITE = getBlockType("polished_andesite");
    /**
     * BlockData: {@link Snowable}
     */
    Typed<Snowable> GRASS_BLOCK = getBlockType("grass_block");
    Typed<BlockData> DIRT = getBlockType("dirt");
    Typed<BlockData> COARSE_DIRT = getBlockType("coarse_dirt");
    /**
     * BlockData: {@link Snowable}
     */
    Typed<Snowable> PODZOL = getBlockType("podzol");
    Typed<BlockData> COBBLESTONE = getBlockType("cobblestone");
    Typed<BlockData> OAK_PLANKS = getBlockType("oak_planks");
    Typed<BlockData> SPRUCE_PLANKS = getBlockType("spruce_planks");
    Typed<BlockData> BIRCH_PLANKS = getBlockType("birch_planks");
    Typed<BlockData> JUNGLE_PLANKS = getBlockType("jungle_planks");
    Typed<BlockData> ACACIA_PLANKS = getBlockType("acacia_planks");
    Typed<BlockData> CHERRY_PLANKS = getBlockType("cherry_planks");
    Typed<BlockData> DARK_OAK_PLANKS = getBlockType("dark_oak_planks");
    Typed<BlockData> PALE_OAK_PLANKS = getBlockType("pale_oak_planks");
    Typed<BlockData> MANGROVE_PLANKS = getBlockType("mangrove_planks");
    Typed<BlockData> BAMBOO_PLANKS = getBlockType("bamboo_planks");
    Typed<BlockData> BAMBOO_MOSAIC = getBlockType("bamboo_mosaic");
    /**
     * BlockData: {@link Sapling}
     */
    Typed<Sapling> OAK_SAPLING = getBlockType("oak_sapling");
    /**
     * BlockData: {@link Sapling}
     */
    Typed<Sapling> SPRUCE_SAPLING = getBlockType("spruce_sapling");
    /**
     * BlockData: {@link Sapling}
     */
    Typed<Sapling> BIRCH_SAPLING = getBlockType("birch_sapling");
    /**
     * BlockData: {@link Sapling}
     */
    Typed<Sapling> JUNGLE_SAPLING = getBlockType("jungle_sapling");
    /**
     * BlockData: {@link Sapling}
     */
    Typed<Sapling> ACACIA_SAPLING = getBlockType("acacia_sapling");
    /**
     * BlockData: {@link Sapling}
     */
    Typed<Sapling> CHERRY_SAPLING = getBlockType("cherry_sapling");
    /**
     * BlockData: {@link Sapling}
     */
    Typed<Sapling> DARK_OAK_SAPLING = getBlockType("dark_oak_sapling");
    /**
     * BlockData: {@link Sapling}
     */
    Typed<Sapling> PALE_OAK_SAPLING = getBlockType("pale_oak_sapling");
    /**
     * BlockData: {@link MangrovePropagule}
     */
    Typed<MangrovePropagule> MANGROVE_PROPAGULE = getBlockType("mangrove_propagule");
    Typed<BlockData> BEDROCK = getBlockType("bedrock");
    /**
     * BlockData: {@link Levelled}
     */
    Typed<Levelled> WATER = getBlockType("water");
    /**
     * BlockData: {@link Levelled}
     */
    Typed<Levelled> LAVA = getBlockType("lava");
    Typed<BlockData> SAND = getBlockType("sand");
    /**
     * BlockData: {@link Brushable}
     */
    Typed<Brushable> SUSPICIOUS_SAND = getBlockType("suspicious_sand");
    Typed<BlockData> RED_SAND = getBlockType("red_sand");
    Typed<BlockData> GRAVEL = getBlockType("gravel");
    /**
     * BlockData: {@link Brushable}
     */
    Typed<Brushable> SUSPICIOUS_GRAVEL = getBlockType("suspicious_gravel");
    Typed<BlockData> GOLD_ORE = getBlockType("gold_ore");
    Typed<BlockData> DEEPSLATE_GOLD_ORE = getBlockType("deepslate_gold_ore");
    Typed<BlockData> IRON_ORE = getBlockType("iron_ore");
    Typed<BlockData> DEEPSLATE_IRON_ORE = getBlockType("deepslate_iron_ore");
    Typed<BlockData> COAL_ORE = getBlockType("coal_ore");
    Typed<BlockData> DEEPSLATE_COAL_ORE = getBlockType("deepslate_coal_ore");
    Typed<BlockData> NETHER_GOLD_ORE = getBlockType("nether_gold_ore");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> OAK_LOG = getBlockType("oak_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> SPRUCE_LOG = getBlockType("spruce_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> BIRCH_LOG = getBlockType("birch_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> JUNGLE_LOG = getBlockType("jungle_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> ACACIA_LOG = getBlockType("acacia_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> CHERRY_LOG = getBlockType("cherry_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> DARK_OAK_LOG = getBlockType("dark_oak_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> PALE_OAK_LOG = getBlockType("pale_oak_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> MANGROVE_LOG = getBlockType("mangrove_log");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> MANGROVE_ROOTS = getBlockType("mangrove_roots");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> MUDDY_MANGROVE_ROOTS = getBlockType("muddy_mangrove_roots");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> BAMBOO_BLOCK = getBlockType("bamboo_block");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_SPRUCE_LOG = getBlockType("stripped_spruce_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_BIRCH_LOG = getBlockType("stripped_birch_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_JUNGLE_LOG = getBlockType("stripped_jungle_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_ACACIA_LOG = getBlockType("stripped_acacia_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_CHERRY_LOG = getBlockType("stripped_cherry_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_DARK_OAK_LOG = getBlockType("stripped_dark_oak_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_PALE_OAK_LOG = getBlockType("stripped_pale_oak_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_OAK_LOG = getBlockType("stripped_oak_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_MANGROVE_LOG = getBlockType("stripped_mangrove_log");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_BAMBOO_BLOCK = getBlockType("stripped_bamboo_block");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> OAK_WOOD = getBlockType("oak_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> SPRUCE_WOOD = getBlockType("spruce_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> BIRCH_WOOD = getBlockType("birch_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> JUNGLE_WOOD = getBlockType("jungle_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> ACACIA_WOOD = getBlockType("acacia_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> CHERRY_WOOD = getBlockType("cherry_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> DARK_OAK_WOOD = getBlockType("dark_oak_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> PALE_OAK_WOOD = getBlockType("pale_oak_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> MANGROVE_WOOD = getBlockType("mangrove_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_OAK_WOOD = getBlockType("stripped_oak_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_SPRUCE_WOOD = getBlockType("stripped_spruce_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_BIRCH_WOOD = getBlockType("stripped_birch_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_JUNGLE_WOOD = getBlockType("stripped_jungle_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_ACACIA_WOOD = getBlockType("stripped_acacia_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_CHERRY_WOOD = getBlockType("stripped_cherry_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_DARK_OAK_WOOD = getBlockType("stripped_dark_oak_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_PALE_OAK_WOOD = getBlockType("stripped_pale_oak_wood");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_MANGROVE_WOOD = getBlockType("stripped_mangrove_wood");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> OAK_LEAVES = getBlockType("oak_leaves");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> SPRUCE_LEAVES = getBlockType("spruce_leaves");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> BIRCH_LEAVES = getBlockType("birch_leaves");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> JUNGLE_LEAVES = getBlockType("jungle_leaves");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> ACACIA_LEAVES = getBlockType("acacia_leaves");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> CHERRY_LEAVES = getBlockType("cherry_leaves");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> DARK_OAK_LEAVES = getBlockType("dark_oak_leaves");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> PALE_OAK_LEAVES = getBlockType("pale_oak_leaves");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> MANGROVE_LEAVES = getBlockType("mangrove_leaves");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> AZALEA_LEAVES = getBlockType("azalea_leaves");
    /**
     * BlockData: {@link Leaves}
     */
    Typed<Leaves> FLOWERING_AZALEA_LEAVES = getBlockType("flowering_azalea_leaves");
    Typed<BlockData> SPONGE = getBlockType("sponge");
    Typed<BlockData> WET_SPONGE = getBlockType("wet_sponge");
    Typed<BlockData> GLASS = getBlockType("glass");
    Typed<BlockData> LAPIS_ORE = getBlockType("lapis_ore");
    Typed<BlockData> DEEPSLATE_LAPIS_ORE = getBlockType("deepslate_lapis_ore");
    Typed<BlockData> LAPIS_BLOCK = getBlockType("lapis_block");
    /**
     * BlockData: {@link Dispenser}
     */
    Typed<Dispenser> DISPENSER = getBlockType("dispenser");
    Typed<BlockData> SANDSTONE = getBlockType("sandstone");
    Typed<BlockData> CHISELED_SANDSTONE = getBlockType("chiseled_sandstone");
    Typed<BlockData> CUT_SANDSTONE = getBlockType("cut_sandstone");
    /**
     * BlockData: {@link NoteBlock}
     */
    Typed<NoteBlock> NOTE_BLOCK = getBlockType("note_block");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> WHITE_BED = getBlockType("white_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> ORANGE_BED = getBlockType("orange_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> MAGENTA_BED = getBlockType("magenta_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> LIGHT_BLUE_BED = getBlockType("light_blue_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> YELLOW_BED = getBlockType("yellow_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> LIME_BED = getBlockType("lime_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> PINK_BED = getBlockType("pink_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> GRAY_BED = getBlockType("gray_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> LIGHT_GRAY_BED = getBlockType("light_gray_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> CYAN_BED = getBlockType("cyan_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> PURPLE_BED = getBlockType("purple_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> BLUE_BED = getBlockType("blue_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> BROWN_BED = getBlockType("brown_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> GREEN_BED = getBlockType("green_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> RED_BED = getBlockType("red_bed");
    /**
     * BlockData: {@link Bed}
     */
    Typed<Bed> BLACK_BED = getBlockType("black_bed");
    /**
     * BlockData: {@link RedstoneRail}
     */
    Typed<RedstoneRail> POWERED_RAIL = getBlockType("powered_rail");
    /**
     * BlockData: {@link RedstoneRail}
     */
    Typed<RedstoneRail> DETECTOR_RAIL = getBlockType("detector_rail");
    /**
     * BlockData: {@link Piston}
     */
    Typed<Piston> STICKY_PISTON = getBlockType("sticky_piston");
    Typed<BlockData> COBWEB = getBlockType("cobweb");
    Typed<BlockData> SHORT_GRASS = getBlockType("short_grass");
    Typed<BlockData> FERN = getBlockType("fern");
    Typed<BlockData> DEAD_BUSH = getBlockType("dead_bush");
    Typed<BlockData> BUSH = getBlockType("bush");
    Typed<BlockData> SHORT_DRY_GRASS = getBlockType("short_dry_grass");
    Typed<BlockData> TALL_DRY_GRASS = getBlockType("tall_dry_grass");
    Typed<BlockData> SEAGRASS = getBlockType("seagrass");
    /**
     * BlockData: {@link Bisected}
     */
    Typed<Bisected> TALL_SEAGRASS = getBlockType("tall_seagrass");
    /**
     * BlockData: {@link Piston}
     */
    Typed<Piston> PISTON = getBlockType("piston");
    /**
     * BlockData: {@link PistonHead}
     */
    Typed<PistonHead> PISTON_HEAD = getBlockType("piston_head");
    Typed<BlockData> WHITE_WOOL = getBlockType("white_wool");
    Typed<BlockData> ORANGE_WOOL = getBlockType("orange_wool");
    Typed<BlockData> MAGENTA_WOOL = getBlockType("magenta_wool");
    Typed<BlockData> LIGHT_BLUE_WOOL = getBlockType("light_blue_wool");
    Typed<BlockData> YELLOW_WOOL = getBlockType("yellow_wool");
    Typed<BlockData> LIME_WOOL = getBlockType("lime_wool");
    Typed<BlockData> PINK_WOOL = getBlockType("pink_wool");
    Typed<BlockData> GRAY_WOOL = getBlockType("gray_wool");
    Typed<BlockData> LIGHT_GRAY_WOOL = getBlockType("light_gray_wool");
    Typed<BlockData> CYAN_WOOL = getBlockType("cyan_wool");
    Typed<BlockData> PURPLE_WOOL = getBlockType("purple_wool");
    Typed<BlockData> BLUE_WOOL = getBlockType("blue_wool");
    Typed<BlockData> BROWN_WOOL = getBlockType("brown_wool");
    Typed<BlockData> GREEN_WOOL = getBlockType("green_wool");
    Typed<BlockData> RED_WOOL = getBlockType("red_wool");
    Typed<BlockData> BLACK_WOOL = getBlockType("black_wool");
    /**
     * BlockData: {@link TechnicalPiston}
     */
    Typed<TechnicalPiston> MOVING_PISTON = getBlockType("moving_piston");
    Typed<BlockData> DANDELION = getBlockType("dandelion");
    Typed<BlockData> GOLDEN_DANDELION = getBlockType("golden_dandelion");
    Typed<BlockData> TORCHFLOWER = getBlockType("torchflower");
    Typed<BlockData> POPPY = getBlockType("poppy");
    Typed<BlockData> BLUE_ORCHID = getBlockType("blue_orchid");
    Typed<BlockData> ALLIUM = getBlockType("allium");
    Typed<BlockData> AZURE_BLUET = getBlockType("azure_bluet");
    Typed<BlockData> RED_TULIP = getBlockType("red_tulip");
    Typed<BlockData> ORANGE_TULIP = getBlockType("orange_tulip");
    Typed<BlockData> WHITE_TULIP = getBlockType("white_tulip");
    Typed<BlockData> PINK_TULIP = getBlockType("pink_tulip");
    Typed<BlockData> OXEYE_DAISY = getBlockType("oxeye_daisy");
    Typed<BlockData> CORNFLOWER = getBlockType("cornflower");
    Typed<BlockData> WITHER_ROSE = getBlockType("wither_rose");
    Typed<BlockData> LILY_OF_THE_VALLEY = getBlockType("lily_of_the_valley");
    Typed<BlockData> BROWN_MUSHROOM = getBlockType("brown_mushroom");
    Typed<BlockData> RED_MUSHROOM = getBlockType("red_mushroom");
    Typed<BlockData> GOLD_BLOCK = getBlockType("gold_block");
    Typed<BlockData> IRON_BLOCK = getBlockType("iron_block");
    Typed<BlockData> BRICKS = getBlockType("bricks");
    /**
     * BlockData: {@link TNT}
     */
    Typed<TNT> TNT = getBlockType("tnt");
    Typed<BlockData> BOOKSHELF = getBlockType("bookshelf");
    /**
     * BlockData: {@link ChiseledBookshelf}
     */
    Typed<ChiseledBookshelf> CHISELED_BOOKSHELF = getBlockType("chiseled_bookshelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> ACACIA_SHELF = getBlockType("acacia_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> BAMBOO_SHELF = getBlockType("bamboo_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> BIRCH_SHELF = getBlockType("birch_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> CHERRY_SHELF = getBlockType("cherry_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> CRIMSON_SHELF = getBlockType("crimson_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> DARK_OAK_SHELF = getBlockType("dark_oak_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> JUNGLE_SHELF = getBlockType("jungle_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> MANGROVE_SHELF = getBlockType("mangrove_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> OAK_SHELF = getBlockType("oak_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> PALE_OAK_SHELF = getBlockType("pale_oak_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> SPRUCE_SHELF = getBlockType("spruce_shelf");
    /**
     * BlockData: {@link Shelf}
     */
    Typed<Shelf> WARPED_SHELF = getBlockType("warped_shelf");
    Typed<BlockData> MOSSY_COBBLESTONE = getBlockType("mossy_cobblestone");
    Typed<BlockData> OBSIDIAN = getBlockType("obsidian");
    Typed<BlockData> TORCH = getBlockType("torch");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> WALL_TORCH = getBlockType("wall_torch");
    /**
     * BlockData: {@link Fire}
     */
    Typed<Fire> FIRE = getBlockType("fire");
    Typed<BlockData> SOUL_FIRE = getBlockType("soul_fire");
    Typed<BlockData> SPAWNER = getBlockType("spawner");
    /**
     * BlockData: {@link Fire}
     */
    Typed<CreakingHeart> CREAKING_HEART = getBlockType("creaking_heart");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> OAK_STAIRS = getBlockType("oak_stairs");
    /**
     * BlockData: {@link Chest}
     */
    Typed<Chest> CHEST = getBlockType("chest");
    /**
     * BlockData: {@link RedstoneWire}
     */
    Typed<RedstoneWire> REDSTONE_WIRE = getBlockType("redstone_wire");
    Typed<BlockData> DIAMOND_ORE = getBlockType("diamond_ore");
    Typed<BlockData> DEEPSLATE_DIAMOND_ORE = getBlockType("deepslate_diamond_ore");
    Typed<BlockData> DIAMOND_BLOCK = getBlockType("diamond_block");
    Typed<BlockData> CRAFTING_TABLE = getBlockType("crafting_table");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> WHEAT = getBlockType("wheat");
    /**
     * BlockData: {@link Farmland}
     */
    Typed<Farmland> FARMLAND = getBlockType("farmland");
    /**
     * BlockData: {@link Furnace}
     */
    Typed<Furnace> FURNACE = getBlockType("furnace");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> OAK_SIGN = getBlockType("oak_sign");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> SPRUCE_SIGN = getBlockType("spruce_sign");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> BIRCH_SIGN = getBlockType("birch_sign");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> ACACIA_SIGN = getBlockType("acacia_sign");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> CHERRY_SIGN = getBlockType("cherry_sign");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> JUNGLE_SIGN = getBlockType("jungle_sign");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> DARK_OAK_SIGN = getBlockType("dark_oak_sign");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> PALE_OAK_SIGN = getBlockType("pale_oak_sign");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> MANGROVE_SIGN = getBlockType("mangrove_sign");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> BAMBOO_SIGN = getBlockType("bamboo_sign");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> OAK_DOOR = getBlockType("oak_door");
    /**
     * BlockData: {@link Ladder}
     */
    Typed<Ladder> LADDER = getBlockType("ladder");
    /**
     * BlockData: {@link Rail}
     */
    Typed<Rail> RAIL = getBlockType("rail");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> COBBLESTONE_STAIRS = getBlockType("cobblestone_stairs");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> OAK_WALL_SIGN = getBlockType("oak_wall_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> SPRUCE_WALL_SIGN = getBlockType("spruce_wall_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> BIRCH_WALL_SIGN = getBlockType("birch_wall_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> ACACIA_WALL_SIGN = getBlockType("acacia_wall_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> CHERRY_WALL_SIGN = getBlockType("cherry_wall_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> JUNGLE_WALL_SIGN = getBlockType("jungle_wall_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> DARK_OAK_WALL_SIGN = getBlockType("dark_oak_wall_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> PALE_OAK_WALL_SIGN = getBlockType("pale_oak_wall_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> MANGROVE_WALL_SIGN = getBlockType("mangrove_wall_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> BAMBOO_WALL_SIGN = getBlockType("bamboo_wall_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> OAK_HANGING_SIGN = getBlockType("oak_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> SPRUCE_HANGING_SIGN = getBlockType("spruce_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> BIRCH_HANGING_SIGN = getBlockType("birch_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> ACACIA_HANGING_SIGN = getBlockType("acacia_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> CHERRY_HANGING_SIGN = getBlockType("cherry_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> JUNGLE_HANGING_SIGN = getBlockType("jungle_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> DARK_OAK_HANGING_SIGN = getBlockType("dark_oak_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> PALE_OAK_HANGING_SIGN = getBlockType("pale_oak_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> CRIMSON_HANGING_SIGN = getBlockType("crimson_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> WARPED_HANGING_SIGN = getBlockType("warped_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> MANGROVE_HANGING_SIGN = getBlockType("mangrove_hanging_sign");
    /**
     * BlockData: {@link HangingSign}
     */
    Typed<HangingSign> BAMBOO_HANGING_SIGN = getBlockType("bamboo_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> OAK_WALL_HANGING_SIGN = getBlockType("oak_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> SPRUCE_WALL_HANGING_SIGN = getBlockType("spruce_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> BIRCH_WALL_HANGING_SIGN = getBlockType("birch_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> ACACIA_WALL_HANGING_SIGN = getBlockType("acacia_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> CHERRY_WALL_HANGING_SIGN = getBlockType("cherry_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> JUNGLE_WALL_HANGING_SIGN = getBlockType("jungle_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> DARK_OAK_WALL_HANGING_SIGN = getBlockType("dark_oak_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> PALE_OAK_WALL_HANGING_SIGN = getBlockType("pale_oak_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> MANGROVE_WALL_HANGING_SIGN = getBlockType("mangrove_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> CRIMSON_WALL_HANGING_SIGN = getBlockType("crimson_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> WARPED_WALL_HANGING_SIGN = getBlockType("warped_wall_hanging_sign");
    /**
     * BlockData: {@link WallHangingSign}
     */
    Typed<WallHangingSign> BAMBOO_WALL_HANGING_SIGN = getBlockType("bamboo_wall_hanging_sign");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> LEVER = getBlockType("lever");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> STONE_PRESSURE_PLATE = getBlockType("stone_pressure_plate");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> IRON_DOOR = getBlockType("iron_door");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> OAK_PRESSURE_PLATE = getBlockType("oak_pressure_plate");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> SPRUCE_PRESSURE_PLATE = getBlockType("spruce_pressure_plate");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> BIRCH_PRESSURE_PLATE = getBlockType("birch_pressure_plate");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> JUNGLE_PRESSURE_PLATE = getBlockType("jungle_pressure_plate");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> ACACIA_PRESSURE_PLATE = getBlockType("acacia_pressure_plate");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> CHERRY_PRESSURE_PLATE = getBlockType("cherry_pressure_plate");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> DARK_OAK_PRESSURE_PLATE = getBlockType("dark_oak_pressure_plate");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> PALE_OAK_PRESSURE_PLATE = getBlockType("pale_oak_pressure_plate");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> MANGROVE_PRESSURE_PLATE = getBlockType("mangrove_pressure_plate");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> BAMBOO_PRESSURE_PLATE = getBlockType("bamboo_pressure_plate");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> REDSTONE_ORE = getBlockType("redstone_ore");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> DEEPSLATE_REDSTONE_ORE = getBlockType("deepslate_redstone_ore");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> REDSTONE_TORCH = getBlockType("redstone_torch");
    /**
     * BlockData: {@link RedstoneWallTorch}
     */
    Typed<RedstoneWallTorch> REDSTONE_WALL_TORCH = getBlockType("redstone_wall_torch");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> STONE_BUTTON = getBlockType("stone_button");
    /**
     * BlockData: {@link Snow}
     */
    Typed<Snow> SNOW = getBlockType("snow");
    Typed<BlockData> ICE = getBlockType("ice");
    Typed<BlockData> SNOW_BLOCK = getBlockType("snow_block");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> CACTUS = getBlockType("cactus");
    Typed<BlockData> CACTUS_FLOWER = getBlockType("cactus_flower");
    Typed<BlockData> CLAY = getBlockType("clay");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> SUGAR_CANE = getBlockType("sugar_cane");
    /**
     * BlockData: {@link Jukebox}
     */
    Typed<Jukebox> JUKEBOX = getBlockType("jukebox");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> OAK_FENCE = getBlockType("oak_fence");
    Typed<BlockData> NETHERRACK = getBlockType("netherrack");
    Typed<BlockData> SOUL_SAND = getBlockType("soul_sand");
    Typed<BlockData> SOUL_SOIL = getBlockType("soul_soil");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> BASALT = getBlockType("basalt");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> POLISHED_BASALT = getBlockType("polished_basalt");
    Typed<BlockData> SOUL_TORCH = getBlockType("soul_torch");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> SOUL_WALL_TORCH = getBlockType("soul_wall_torch");
    Typed<BlockData> COPPER_TORCH = getBlockType("copper_torch");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> COPPER_WALL_TORCH = getBlockType("copper_wall_torch");
    Typed<BlockData> GLOWSTONE = getBlockType("glowstone");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> NETHER_PORTAL = getBlockType("nether_portal");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> CARVED_PUMPKIN = getBlockType("carved_pumpkin");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> JACK_O_LANTERN = getBlockType("jack_o_lantern");
    /**
     * BlockData: {@link Cake}
     */
    Typed<Cake> CAKE = getBlockType("cake");
    /**
     * BlockData: {@link Repeater}
     */
    Typed<Repeater> REPEATER = getBlockType("repeater");
    Typed<BlockData> WHITE_STAINED_GLASS = getBlockType("white_stained_glass");
    Typed<BlockData> ORANGE_STAINED_GLASS = getBlockType("orange_stained_glass");
    Typed<BlockData> MAGENTA_STAINED_GLASS = getBlockType("magenta_stained_glass");
    Typed<BlockData> LIGHT_BLUE_STAINED_GLASS = getBlockType("light_blue_stained_glass");
    Typed<BlockData> YELLOW_STAINED_GLASS = getBlockType("yellow_stained_glass");
    Typed<BlockData> LIME_STAINED_GLASS = getBlockType("lime_stained_glass");
    Typed<BlockData> PINK_STAINED_GLASS = getBlockType("pink_stained_glass");
    Typed<BlockData> GRAY_STAINED_GLASS = getBlockType("gray_stained_glass");
    Typed<BlockData> LIGHT_GRAY_STAINED_GLASS = getBlockType("light_gray_stained_glass");
    Typed<BlockData> CYAN_STAINED_GLASS = getBlockType("cyan_stained_glass");
    Typed<BlockData> PURPLE_STAINED_GLASS = getBlockType("purple_stained_glass");
    Typed<BlockData> BLUE_STAINED_GLASS = getBlockType("blue_stained_glass");
    Typed<BlockData> BROWN_STAINED_GLASS = getBlockType("brown_stained_glass");
    Typed<BlockData> GREEN_STAINED_GLASS = getBlockType("green_stained_glass");
    Typed<BlockData> RED_STAINED_GLASS = getBlockType("red_stained_glass");
    Typed<BlockData> BLACK_STAINED_GLASS = getBlockType("black_stained_glass");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> OAK_TRAPDOOR = getBlockType("oak_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> SPRUCE_TRAPDOOR = getBlockType("spruce_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> BIRCH_TRAPDOOR = getBlockType("birch_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> JUNGLE_TRAPDOOR = getBlockType("jungle_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> ACACIA_TRAPDOOR = getBlockType("acacia_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> CHERRY_TRAPDOOR = getBlockType("cherry_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> DARK_OAK_TRAPDOOR = getBlockType("dark_oak_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> PALE_OAK_TRAPDOOR = getBlockType("pale_oak_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> MANGROVE_TRAPDOOR = getBlockType("mangrove_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> BAMBOO_TRAPDOOR = getBlockType("bamboo_trapdoor");
    Typed<BlockData> STONE_BRICKS = getBlockType("stone_bricks");
    Typed<BlockData> MOSSY_STONE_BRICKS = getBlockType("mossy_stone_bricks");
    Typed<BlockData> CRACKED_STONE_BRICKS = getBlockType("cracked_stone_bricks");
    Typed<BlockData> CHISELED_STONE_BRICKS = getBlockType("chiseled_stone_bricks");
    Typed<BlockData> PACKED_MUD = getBlockType("packed_mud");
    Typed<BlockData> MUD_BRICKS = getBlockType("mud_bricks");
    Typed<BlockData> INFESTED_STONE = getBlockType("infested_stone");
    Typed<BlockData> INFESTED_COBBLESTONE = getBlockType("infested_cobblestone");
    Typed<BlockData> INFESTED_STONE_BRICKS = getBlockType("infested_stone_bricks");
    Typed<BlockData> INFESTED_MOSSY_STONE_BRICKS = getBlockType("infested_mossy_stone_bricks");
    Typed<BlockData> INFESTED_CRACKED_STONE_BRICKS = getBlockType("infested_cracked_stone_bricks");
    Typed<BlockData> INFESTED_CHISELED_STONE_BRICKS = getBlockType("infested_chiseled_stone_bricks");
    /**
     * BlockData: {@link MultipleFacing}
     */
    Typed<MultipleFacing> BROWN_MUSHROOM_BLOCK = getBlockType("brown_mushroom_block");
    /**
     * BlockData: {@link MultipleFacing}
     */
    Typed<MultipleFacing> RED_MUSHROOM_BLOCK = getBlockType("red_mushroom_block");
    /**
     * BlockData: {@link MultipleFacing}
     */
    Typed<MultipleFacing> MUSHROOM_STEM = getBlockType("mushroom_stem");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> IRON_BARS = getBlockType("iron_bars");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> COPPER_BARS = getBlockType("copper_bars");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> EXPOSED_COPPER_BARS = getBlockType("exposed_copper_bars");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> WEATHERED_COPPER_BARS = getBlockType("weathered_copper_bars");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> OXIDIZED_COPPER_BARS = getBlockType("oxidized_copper_bars");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> WAXED_COPPER_BARS = getBlockType("waxed_copper_bars");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> WAXED_EXPOSED_COPPER_BARS = getBlockType("waxed_exposed_copper_bars");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> WAXED_WEATHERED_COPPER_BARS = getBlockType("waxed_weathered_copper_bars");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> WAXED_OXIDIZED_COPPER_BARS = getBlockType("waxed_oxidized_copper_bars");
    /**
     * BlockData: {@link Chain}
     */
    Typed<Chain> IRON_CHAIN = getBlockType("iron_chain");
    /**
     * BlockData: {@link Chain}
     */
    Typed<Chain> COPPER_CHAIN = getBlockType("copper_chain");
    /**
     * BlockData: {@link Chain}
     */
    Typed<Chain> EXPOSED_COPPER_CHAIN = getBlockType("exposed_copper_chain");
    /**
     * BlockData: {@link Chain}
     */
    Typed<Chain> WEATHERED_COPPER_CHAIN = getBlockType("weathered_copper_chain");
    /**
     * BlockData: {@link Chain}
     */
    Typed<Chain> OXIDIZED_COPPER_CHAIN = getBlockType("oxidized_copper_chain");
    /**
     * BlockData: {@link Chain}
     */
    Typed<Chain> WAXED_COPPER_CHAIN = getBlockType("waxed_copper_chain");
    /**
     * BlockData: {@link Chain}
     */
    Typed<Chain> WAXED_EXPOSED_COPPER_CHAIN = getBlockType("waxed_exposed_copper_chain");
    /**
     * BlockData: {@link Chain}
     */
    Typed<Chain> WAXED_WEATHERED_COPPER_CHAIN = getBlockType("waxed_weathered_copper_chain");
    /**
     * BlockData: {@link Chain}
     */
    Typed<Chain> WAXED_OXIDIZED_COPPER_CHAIN = getBlockType("waxed_oxidized_copper_chain");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> GLASS_PANE = getBlockType("glass_pane");
    Typed<BlockData> PUMPKIN = getBlockType("pumpkin");
    Typed<BlockData> MELON = getBlockType("melon");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> ATTACHED_PUMPKIN_STEM = getBlockType("attached_pumpkin_stem");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> ATTACHED_MELON_STEM = getBlockType("attached_melon_stem");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> PUMPKIN_STEM = getBlockType("pumpkin_stem");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> MELON_STEM = getBlockType("melon_stem");
    /**
     * BlockData: {@link MultipleFacing}
     */
    Typed<MultipleFacing> VINE = getBlockType("vine");
    /**
     * BlockData: {@link GlowLichen}
     */
    Typed<GlowLichen> GLOW_LICHEN = getBlockType("glow_lichen");
    /**
     * BlockData: {@link ResinClump}
     */
    Typed<ResinClump> RESIN_CLUMP = getBlockType("resin_clump");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> OAK_FENCE_GATE = getBlockType("oak_fence_gate");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> BRICK_STAIRS = getBlockType("brick_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> STONE_BRICK_STAIRS = getBlockType("stone_brick_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> MUD_BRICK_STAIRS = getBlockType("mud_brick_stairs");
    /**
     * BlockData: {@link Snowable}
     */
    Typed<Snowable> MYCELIUM = getBlockType("mycelium");
    Typed<BlockData> LILY_PAD = getBlockType("lily_pad");
    Typed<BlockData> RESIN_BLOCK = getBlockType("resin_block");
    Typed<BlockData> RESIN_BRICKS = getBlockType("resin_bricks");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> RESIN_BRICK_STAIRS = getBlockType("resin_brick_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> RESIN_BRICK_SLAB = getBlockType("resin_brick_slab");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> RESIN_BRICK_WALL = getBlockType("resin_brick_wall");
    Typed<BlockData> CHISELED_RESIN_BRICKS = getBlockType("chiseled_resin_bricks");
    Typed<BlockData> NETHER_BRICKS = getBlockType("nether_bricks");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> NETHER_BRICK_FENCE = getBlockType("nether_brick_fence");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> NETHER_BRICK_STAIRS = getBlockType("nether_brick_stairs");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> NETHER_WART = getBlockType("nether_wart");
    Typed<BlockData> ENCHANTING_TABLE = getBlockType("enchanting_table");
    /**
     * BlockData: {@link BrewingStand}
     */
    Typed<BrewingStand> BREWING_STAND = getBlockType("brewing_stand");
    Typed<BlockData> CAULDRON = getBlockType("cauldron");
    /**
     * BlockData: {@link Levelled}
     */
    Typed<Levelled> WATER_CAULDRON = getBlockType("water_cauldron");
    Typed<BlockData> LAVA_CAULDRON = getBlockType("lava_cauldron");
    /**
     * BlockData: {@link Levelled}
     */
    Typed<Levelled> POWDER_SNOW_CAULDRON = getBlockType("powder_snow_cauldron");
    Typed<BlockData> END_PORTAL = getBlockType("end_portal");
    /**
     * BlockData: {@link EndPortalFrame}
     */
    Typed<EndPortalFrame> END_PORTAL_FRAME = getBlockType("end_portal_frame");
    Typed<BlockData> END_STONE = getBlockType("end_stone");
    Typed<BlockData> DRAGON_EGG = getBlockType("dragon_egg");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> REDSTONE_LAMP = getBlockType("redstone_lamp");
    /**
     * BlockData: {@link Cocoa}
     */
    Typed<Cocoa> COCOA = getBlockType("cocoa");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> SANDSTONE_STAIRS = getBlockType("sandstone_stairs");
    Typed<BlockData> EMERALD_ORE = getBlockType("emerald_ore");
    Typed<BlockData> DEEPSLATE_EMERALD_ORE = getBlockType("deepslate_emerald_ore");
    /**
     * BlockData: {@link EnderChest}
     */
    Typed<EnderChest> ENDER_CHEST = getBlockType("ender_chest");
    /**
     * BlockData: {@link TripwireHook}
     */
    Typed<TripwireHook> TRIPWIRE_HOOK = getBlockType("tripwire_hook");
    /**
     * BlockData: {@link Tripwire}
     */
    Typed<Tripwire> TRIPWIRE = getBlockType("tripwire");
    Typed<BlockData> EMERALD_BLOCK = getBlockType("emerald_block");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> SPRUCE_STAIRS = getBlockType("spruce_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> BIRCH_STAIRS = getBlockType("birch_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> JUNGLE_STAIRS = getBlockType("jungle_stairs");
    /**
     * BlockData: {@link CommandBlock}
     */
    Typed<CommandBlock> COMMAND_BLOCK = getBlockType("command_block");
    Typed<BlockData> BEACON = getBlockType("beacon");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> COBBLESTONE_WALL = getBlockType("cobblestone_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> MOSSY_COBBLESTONE_WALL = getBlockType("mossy_cobblestone_wall");
    Typed<BlockData> FLOWER_POT = getBlockType("flower_pot");
    Typed<BlockData> POTTED_TORCHFLOWER = getBlockType("potted_torchflower");
    Typed<BlockData> POTTED_OAK_SAPLING = getBlockType("potted_oak_sapling");
    Typed<BlockData> POTTED_SPRUCE_SAPLING = getBlockType("potted_spruce_sapling");
    Typed<BlockData> POTTED_BIRCH_SAPLING = getBlockType("potted_birch_sapling");
    Typed<BlockData> POTTED_JUNGLE_SAPLING = getBlockType("potted_jungle_sapling");
    Typed<BlockData> POTTED_ACACIA_SAPLING = getBlockType("potted_acacia_sapling");
    Typed<BlockData> POTTED_CHERRY_SAPLING = getBlockType("potted_cherry_sapling");
    Typed<BlockData> POTTED_DARK_OAK_SAPLING = getBlockType("potted_dark_oak_sapling");
    Typed<BlockData> POTTED_PALE_OAK_SAPLING = getBlockType("potted_pale_oak_sapling");
    Typed<BlockData> POTTED_MANGROVE_PROPAGULE = getBlockType("potted_mangrove_propagule");
    Typed<BlockData> POTTED_FERN = getBlockType("potted_fern");
    Typed<BlockData> POTTED_DANDELION = getBlockType("potted_dandelion");
    Typed<BlockData> POTTED_GOLDEN_DANDELION = getBlockType("potted_golden_dandelion");
    Typed<BlockData> POTTED_POPPY = getBlockType("potted_poppy");
    Typed<BlockData> POTTED_BLUE_ORCHID = getBlockType("potted_blue_orchid");
    Typed<BlockData> POTTED_ALLIUM = getBlockType("potted_allium");
    Typed<BlockData> POTTED_AZURE_BLUET = getBlockType("potted_azure_bluet");
    Typed<BlockData> POTTED_RED_TULIP = getBlockType("potted_red_tulip");
    Typed<BlockData> POTTED_ORANGE_TULIP = getBlockType("potted_orange_tulip");
    Typed<BlockData> POTTED_WHITE_TULIP = getBlockType("potted_white_tulip");
    Typed<BlockData> POTTED_PINK_TULIP = getBlockType("potted_pink_tulip");
    Typed<BlockData> POTTED_OXEYE_DAISY = getBlockType("potted_oxeye_daisy");
    Typed<BlockData> POTTED_CORNFLOWER = getBlockType("potted_cornflower");
    Typed<BlockData> POTTED_LILY_OF_THE_VALLEY = getBlockType("potted_lily_of_the_valley");
    Typed<BlockData> POTTED_WITHER_ROSE = getBlockType("potted_wither_rose");
    Typed<BlockData> POTTED_RED_MUSHROOM = getBlockType("potted_red_mushroom");
    Typed<BlockData> POTTED_BROWN_MUSHROOM = getBlockType("potted_brown_mushroom");
    Typed<BlockData> POTTED_DEAD_BUSH = getBlockType("potted_dead_bush");
    Typed<BlockData> POTTED_CACTUS = getBlockType("potted_cactus");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> CARROTS = getBlockType("carrots");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> POTATOES = getBlockType("potatoes");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> OAK_BUTTON = getBlockType("oak_button");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> SPRUCE_BUTTON = getBlockType("spruce_button");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> BIRCH_BUTTON = getBlockType("birch_button");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> JUNGLE_BUTTON = getBlockType("jungle_button");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> ACACIA_BUTTON = getBlockType("acacia_button");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> CHERRY_BUTTON = getBlockType("cherry_button");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> DARK_OAK_BUTTON = getBlockType("dark_oak_button");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> PALE_OAK_BUTTON = getBlockType("pale_oak_button");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> MANGROVE_BUTTON = getBlockType("mangrove_button");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> BAMBOO_BUTTON = getBlockType("bamboo_button");
    /**
     * BlockData: {@link Skull}
     */
    Typed<Skull> SKELETON_SKULL = getBlockType("skeleton_skull");
    /**
     * BlockData: {@link WallSkull}
     */
    Typed<WallSkull> SKELETON_WALL_SKULL = getBlockType("skeleton_wall_skull");
    /**
     * BlockData: {@link Skull}
     */
    Typed<Skull> WITHER_SKELETON_SKULL = getBlockType("wither_skeleton_skull");
    /**
     * BlockData: {@link WallSkull}
     */
    Typed<WallSkull> WITHER_SKELETON_WALL_SKULL = getBlockType("wither_skeleton_wall_skull");
    /**
     * BlockData: {@link Skull}
     */
    Typed<Skull> ZOMBIE_HEAD = getBlockType("zombie_head");
    /**
     * BlockData: {@link WallSkull}
     */
    Typed<WallSkull> ZOMBIE_WALL_HEAD = getBlockType("zombie_wall_head");
    /**
     * BlockData: {@link Skull}
     */
    Typed<Skull> PLAYER_HEAD = getBlockType("player_head");
    /**
     * BlockData: {@link WallSkull}
     */
    Typed<WallSkull> PLAYER_WALL_HEAD = getBlockType("player_wall_head");
    /**
     * BlockData: {@link Skull}
     */
    Typed<Skull> CREEPER_HEAD = getBlockType("creeper_head");
    /**
     * BlockData: {@link WallSkull}
     */
    Typed<WallSkull> CREEPER_WALL_HEAD = getBlockType("creeper_wall_head");
    /**
     * BlockData: {@link Skull}
     */
    Typed<Skull> DRAGON_HEAD = getBlockType("dragon_head");
    /**
     * BlockData: {@link WallSkull}
     */
    Typed<WallSkull> DRAGON_WALL_HEAD = getBlockType("dragon_wall_head");
    /**
     * BlockData: {@link Skull}
     */
    Typed<Skull> PIGLIN_HEAD = getBlockType("piglin_head");
    /**
     * BlockData: {@link WallSkull}
     */
    Typed<WallSkull> PIGLIN_WALL_HEAD = getBlockType("piglin_wall_head");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> ANVIL = getBlockType("anvil");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> CHIPPED_ANVIL = getBlockType("chipped_anvil");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> DAMAGED_ANVIL = getBlockType("damaged_anvil");
    /**
     * BlockData: {@link Chest}
     */
    Typed<Chest> TRAPPED_CHEST = getBlockType("trapped_chest");
    /**
     * BlockData: {@link AnaloguePowerable}
     */
    Typed<AnaloguePowerable> LIGHT_WEIGHTED_PRESSURE_PLATE = getBlockType("light_weighted_pressure_plate");
    /**
     * BlockData: {@link AnaloguePowerable}
     */
    Typed<AnaloguePowerable> HEAVY_WEIGHTED_PRESSURE_PLATE = getBlockType("heavy_weighted_pressure_plate");
    /**
     * BlockData: {@link Comparator}
     */
    Typed<Comparator> COMPARATOR = getBlockType("comparator");
    /**
     * BlockData: {@link DaylightDetector}
     */
    Typed<DaylightDetector> DAYLIGHT_DETECTOR = getBlockType("daylight_detector");
    Typed<BlockData> REDSTONE_BLOCK = getBlockType("redstone_block");
    Typed<BlockData> NETHER_QUARTZ_ORE = getBlockType("nether_quartz_ore");
    /**
     * BlockData: {@link Hopper}
     */
    Typed<Hopper> HOPPER = getBlockType("hopper");
    Typed<BlockData> QUARTZ_BLOCK = getBlockType("quartz_block");
    Typed<BlockData> CHISELED_QUARTZ_BLOCK = getBlockType("chiseled_quartz_block");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> QUARTZ_PILLAR = getBlockType("quartz_pillar");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> QUARTZ_STAIRS = getBlockType("quartz_stairs");
    /**
     * BlockData: {@link RedstoneRail}
     */
    Typed<RedstoneRail> ACTIVATOR_RAIL = getBlockType("activator_rail");
    /**
     * BlockData: {@link Dispenser}
     */
    Typed<Dispenser> DROPPER = getBlockType("dropper");
    Typed<BlockData> WHITE_TERRACOTTA = getBlockType("white_terracotta");
    Typed<BlockData> ORANGE_TERRACOTTA = getBlockType("orange_terracotta");
    Typed<BlockData> MAGENTA_TERRACOTTA = getBlockType("magenta_terracotta");
    Typed<BlockData> LIGHT_BLUE_TERRACOTTA = getBlockType("light_blue_terracotta");
    Typed<BlockData> YELLOW_TERRACOTTA = getBlockType("yellow_terracotta");
    Typed<BlockData> LIME_TERRACOTTA = getBlockType("lime_terracotta");
    Typed<BlockData> PINK_TERRACOTTA = getBlockType("pink_terracotta");
    Typed<BlockData> GRAY_TERRACOTTA = getBlockType("gray_terracotta");
    Typed<BlockData> LIGHT_GRAY_TERRACOTTA = getBlockType("light_gray_terracotta");
    Typed<BlockData> CYAN_TERRACOTTA = getBlockType("cyan_terracotta");
    Typed<BlockData> PURPLE_TERRACOTTA = getBlockType("purple_terracotta");
    Typed<BlockData> BLUE_TERRACOTTA = getBlockType("blue_terracotta");
    Typed<BlockData> BROWN_TERRACOTTA = getBlockType("brown_terracotta");
    Typed<BlockData> GREEN_TERRACOTTA = getBlockType("green_terracotta");
    Typed<BlockData> RED_TERRACOTTA = getBlockType("red_terracotta");
    Typed<BlockData> BLACK_TERRACOTTA = getBlockType("black_terracotta");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> WHITE_STAINED_GLASS_PANE = getBlockType("white_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> ORANGE_STAINED_GLASS_PANE = getBlockType("orange_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> MAGENTA_STAINED_GLASS_PANE = getBlockType("magenta_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> LIGHT_BLUE_STAINED_GLASS_PANE = getBlockType("light_blue_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> YELLOW_STAINED_GLASS_PANE = getBlockType("yellow_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> LIME_STAINED_GLASS_PANE = getBlockType("lime_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> PINK_STAINED_GLASS_PANE = getBlockType("pink_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> GRAY_STAINED_GLASS_PANE = getBlockType("gray_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> LIGHT_GRAY_STAINED_GLASS_PANE = getBlockType("light_gray_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> CYAN_STAINED_GLASS_PANE = getBlockType("cyan_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> PURPLE_STAINED_GLASS_PANE = getBlockType("purple_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> BLUE_STAINED_GLASS_PANE = getBlockType("blue_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> BROWN_STAINED_GLASS_PANE = getBlockType("brown_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> GREEN_STAINED_GLASS_PANE = getBlockType("green_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> RED_STAINED_GLASS_PANE = getBlockType("red_stained_glass_pane");
    /**
     * BlockData: {@link GlassPane}
     */
    Typed<GlassPane> BLACK_STAINED_GLASS_PANE = getBlockType("black_stained_glass_pane");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> ACACIA_STAIRS = getBlockType("acacia_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> CHERRY_STAIRS = getBlockType("cherry_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> DARK_OAK_STAIRS = getBlockType("dark_oak_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> PALE_OAK_STAIRS = getBlockType("pale_oak_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> MANGROVE_STAIRS = getBlockType("mangrove_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> BAMBOO_STAIRS = getBlockType("bamboo_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> BAMBOO_MOSAIC_STAIRS = getBlockType("bamboo_mosaic_stairs");
    Typed<BlockData> SLIME_BLOCK = getBlockType("slime_block");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> BARRIER = getBlockType("barrier");
    /**
     * BlockData: {@link Light}
     */
    Typed<Light> LIGHT = getBlockType("light");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> IRON_TRAPDOOR = getBlockType("iron_trapdoor");
    Typed<BlockData> PRISMARINE = getBlockType("prismarine");
    Typed<BlockData> PRISMARINE_BRICKS = getBlockType("prismarine_bricks");
    Typed<BlockData> DARK_PRISMARINE = getBlockType("dark_prismarine");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> PRISMARINE_STAIRS = getBlockType("prismarine_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> PRISMARINE_BRICK_STAIRS = getBlockType("prismarine_brick_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> DARK_PRISMARINE_STAIRS = getBlockType("dark_prismarine_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> PRISMARINE_SLAB = getBlockType("prismarine_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> PRISMARINE_BRICK_SLAB = getBlockType("prismarine_brick_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> DARK_PRISMARINE_SLAB = getBlockType("dark_prismarine_slab");
    Typed<BlockData> SEA_LANTERN = getBlockType("sea_lantern");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> HAY_BLOCK = getBlockType("hay_block");
    Typed<BlockData> WHITE_CARPET = getBlockType("white_carpet");
    Typed<BlockData> ORANGE_CARPET = getBlockType("orange_carpet");
    Typed<BlockData> MAGENTA_CARPET = getBlockType("magenta_carpet");
    Typed<BlockData> LIGHT_BLUE_CARPET = getBlockType("light_blue_carpet");
    Typed<BlockData> YELLOW_CARPET = getBlockType("yellow_carpet");
    Typed<BlockData> LIME_CARPET = getBlockType("lime_carpet");
    Typed<BlockData> PINK_CARPET = getBlockType("pink_carpet");
    Typed<BlockData> GRAY_CARPET = getBlockType("gray_carpet");
    Typed<BlockData> LIGHT_GRAY_CARPET = getBlockType("light_gray_carpet");
    Typed<BlockData> CYAN_CARPET = getBlockType("cyan_carpet");
    Typed<BlockData> PURPLE_CARPET = getBlockType("purple_carpet");
    Typed<BlockData> BLUE_CARPET = getBlockType("blue_carpet");
    Typed<BlockData> BROWN_CARPET = getBlockType("brown_carpet");
    Typed<BlockData> GREEN_CARPET = getBlockType("green_carpet");
    Typed<BlockData> RED_CARPET = getBlockType("red_carpet");
    Typed<BlockData> BLACK_CARPET = getBlockType("black_carpet");
    Typed<BlockData> TERRACOTTA = getBlockType("terracotta");
    Typed<BlockData> COAL_BLOCK = getBlockType("coal_block");
    Typed<BlockData> PACKED_ICE = getBlockType("packed_ice");
    /**
     * BlockData: {@link Bisected}
     */
    Typed<Bisected> SUNFLOWER = getBlockType("sunflower");
    /**
     * BlockData: {@link Bisected}
     */
    Typed<Bisected> LILAC = getBlockType("lilac");
    /**
     * BlockData: {@link Bisected}
     */
    Typed<Bisected> ROSE_BUSH = getBlockType("rose_bush");
    /**
     * BlockData: {@link Bisected}
     */
    Typed<Bisected> PEONY = getBlockType("peony");
    /**
     * BlockData: {@link Bisected}
     */
    Typed<Bisected> TALL_GRASS = getBlockType("tall_grass");
    /**
     * BlockData: {@link Bisected}
     */
    Typed<Bisected> LARGE_FERN = getBlockType("large_fern");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> WHITE_BANNER = getBlockType("white_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> ORANGE_BANNER = getBlockType("orange_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> MAGENTA_BANNER = getBlockType("magenta_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> LIGHT_BLUE_BANNER = getBlockType("light_blue_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> YELLOW_BANNER = getBlockType("yellow_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> LIME_BANNER = getBlockType("lime_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> PINK_BANNER = getBlockType("pink_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> GRAY_BANNER = getBlockType("gray_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> LIGHT_GRAY_BANNER = getBlockType("light_gray_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> CYAN_BANNER = getBlockType("cyan_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> PURPLE_BANNER = getBlockType("purple_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> BLUE_BANNER = getBlockType("blue_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> BROWN_BANNER = getBlockType("brown_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> GREEN_BANNER = getBlockType("green_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> RED_BANNER = getBlockType("red_banner");
    /**
     * BlockData: {@link Rotatable}
     */
    Typed<Rotatable> BLACK_BANNER = getBlockType("black_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> WHITE_WALL_BANNER = getBlockType("white_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> ORANGE_WALL_BANNER = getBlockType("orange_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> MAGENTA_WALL_BANNER = getBlockType("magenta_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> LIGHT_BLUE_WALL_BANNER = getBlockType("light_blue_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> YELLOW_WALL_BANNER = getBlockType("yellow_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> LIME_WALL_BANNER = getBlockType("lime_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> PINK_WALL_BANNER = getBlockType("pink_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> GRAY_WALL_BANNER = getBlockType("gray_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> LIGHT_GRAY_WALL_BANNER = getBlockType("light_gray_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> CYAN_WALL_BANNER = getBlockType("cyan_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> PURPLE_WALL_BANNER = getBlockType("purple_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> BLUE_WALL_BANNER = getBlockType("blue_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> BROWN_WALL_BANNER = getBlockType("brown_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> GREEN_WALL_BANNER = getBlockType("green_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> RED_WALL_BANNER = getBlockType("red_wall_banner");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> BLACK_WALL_BANNER = getBlockType("black_wall_banner");
    Typed<BlockData> RED_SANDSTONE = getBlockType("red_sandstone");
    Typed<BlockData> CHISELED_RED_SANDSTONE = getBlockType("chiseled_red_sandstone");
    Typed<BlockData> CUT_RED_SANDSTONE = getBlockType("cut_red_sandstone");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> RED_SANDSTONE_STAIRS = getBlockType("red_sandstone_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> OAK_SLAB = getBlockType("oak_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> SPRUCE_SLAB = getBlockType("spruce_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> BIRCH_SLAB = getBlockType("birch_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> JUNGLE_SLAB = getBlockType("jungle_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> ACACIA_SLAB = getBlockType("acacia_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> CHERRY_SLAB = getBlockType("cherry_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> DARK_OAK_SLAB = getBlockType("dark_oak_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> PALE_OAK_SLAB = getBlockType("pale_oak_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> MANGROVE_SLAB = getBlockType("mangrove_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> BAMBOO_SLAB = getBlockType("bamboo_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> BAMBOO_MOSAIC_SLAB = getBlockType("bamboo_mosaic_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> STONE_SLAB = getBlockType("stone_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> SMOOTH_STONE_SLAB = getBlockType("smooth_stone_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> SANDSTONE_SLAB = getBlockType("sandstone_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> CUT_SANDSTONE_SLAB = getBlockType("cut_sandstone_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> PETRIFIED_OAK_SLAB = getBlockType("petrified_oak_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> COBBLESTONE_SLAB = getBlockType("cobblestone_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> BRICK_SLAB = getBlockType("brick_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> STONE_BRICK_SLAB = getBlockType("stone_brick_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> MUD_BRICK_SLAB = getBlockType("mud_brick_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> NETHER_BRICK_SLAB = getBlockType("nether_brick_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> QUARTZ_SLAB = getBlockType("quartz_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> RED_SANDSTONE_SLAB = getBlockType("red_sandstone_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> CUT_RED_SANDSTONE_SLAB = getBlockType("cut_red_sandstone_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> PURPUR_SLAB = getBlockType("purpur_slab");
    Typed<BlockData> SMOOTH_STONE = getBlockType("smooth_stone");
    Typed<BlockData> SMOOTH_SANDSTONE = getBlockType("smooth_sandstone");
    Typed<BlockData> SMOOTH_QUARTZ = getBlockType("smooth_quartz");
    Typed<BlockData> SMOOTH_RED_SANDSTONE = getBlockType("smooth_red_sandstone");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> SPRUCE_FENCE_GATE = getBlockType("spruce_fence_gate");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> BIRCH_FENCE_GATE = getBlockType("birch_fence_gate");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> JUNGLE_FENCE_GATE = getBlockType("jungle_fence_gate");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> ACACIA_FENCE_GATE = getBlockType("acacia_fence_gate");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> CHERRY_FENCE_GATE = getBlockType("cherry_fence_gate");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> DARK_OAK_FENCE_GATE = getBlockType("dark_oak_fence_gate");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> PALE_OAK_FENCE_GATE = getBlockType("pale_oak_fence_gate");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> MANGROVE_FENCE_GATE = getBlockType("mangrove_fence_gate");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> BAMBOO_FENCE_GATE = getBlockType("bamboo_fence_gate");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> SPRUCE_FENCE = getBlockType("spruce_fence");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> BIRCH_FENCE = getBlockType("birch_fence");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> JUNGLE_FENCE = getBlockType("jungle_fence");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> ACACIA_FENCE = getBlockType("acacia_fence");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> CHERRY_FENCE = getBlockType("cherry_fence");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> DARK_OAK_FENCE = getBlockType("dark_oak_fence");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> PALE_OAK_FENCE = getBlockType("pale_oak_fence");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> MANGROVE_FENCE = getBlockType("mangrove_fence");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> BAMBOO_FENCE = getBlockType("bamboo_fence");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> SPRUCE_DOOR = getBlockType("spruce_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> BIRCH_DOOR = getBlockType("birch_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> JUNGLE_DOOR = getBlockType("jungle_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> ACACIA_DOOR = getBlockType("acacia_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> CHERRY_DOOR = getBlockType("cherry_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> DARK_OAK_DOOR = getBlockType("dark_oak_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> PALE_OAK_DOOR = getBlockType("pale_oak_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> MANGROVE_DOOR = getBlockType("mangrove_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> BAMBOO_DOOR = getBlockType("bamboo_door");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> END_ROD = getBlockType("end_rod");
    /**
     * BlockData: {@link MultipleFacing}
     */
    Typed<MultipleFacing> CHORUS_PLANT = getBlockType("chorus_plant");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> CHORUS_FLOWER = getBlockType("chorus_flower");
    Typed<BlockData> PURPUR_BLOCK = getBlockType("purpur_block");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> PURPUR_PILLAR = getBlockType("purpur_pillar");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> PURPUR_STAIRS = getBlockType("purpur_stairs");
    Typed<BlockData> END_STONE_BRICKS = getBlockType("end_stone_bricks");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> TORCHFLOWER_CROP = getBlockType("torchflower_crop");
    /**
     * BlockData: {@link PitcherCrop}
     */
    Typed<PitcherCrop> PITCHER_CROP = getBlockType("pitcher_crop");
    /**
     * BlockData: {@link Bisected}
     */
    Typed<Bisected> PITCHER_PLANT = getBlockType("pitcher_plant");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> BEETROOTS = getBlockType("beetroots");
    Typed<BlockData> DIRT_PATH = getBlockType("dirt_path");
    Typed<BlockData> END_GATEWAY = getBlockType("end_gateway");
    /**
     * BlockData: {@link CommandBlock}
     */
    Typed<CommandBlock> REPEATING_COMMAND_BLOCK = getBlockType("repeating_command_block");
    /**
     * BlockData: {@link CommandBlock}
     */
    Typed<CommandBlock> CHAIN_COMMAND_BLOCK = getBlockType("chain_command_block");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> FROSTED_ICE = getBlockType("frosted_ice");
    Typed<BlockData> MAGMA_BLOCK = getBlockType("magma_block");
    Typed<BlockData> NETHER_WART_BLOCK = getBlockType("nether_wart_block");
    Typed<BlockData> RED_NETHER_BRICKS = getBlockType("red_nether_bricks");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> BONE_BLOCK = getBlockType("bone_block");
    Typed<BlockData> STRUCTURE_VOID = getBlockType("structure_void");
    /**
     * BlockData: {@link Observer}
     */
    Typed<Observer> OBSERVER = getBlockType("observer");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> SHULKER_BOX = getBlockType("shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> WHITE_SHULKER_BOX = getBlockType("white_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> ORANGE_SHULKER_BOX = getBlockType("orange_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> MAGENTA_SHULKER_BOX = getBlockType("magenta_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> LIGHT_BLUE_SHULKER_BOX = getBlockType("light_blue_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> YELLOW_SHULKER_BOX = getBlockType("yellow_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> LIME_SHULKER_BOX = getBlockType("lime_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> PINK_SHULKER_BOX = getBlockType("pink_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> GRAY_SHULKER_BOX = getBlockType("gray_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> LIGHT_GRAY_SHULKER_BOX = getBlockType("light_gray_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> CYAN_SHULKER_BOX = getBlockType("cyan_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> PURPLE_SHULKER_BOX = getBlockType("purple_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> BLUE_SHULKER_BOX = getBlockType("blue_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> BROWN_SHULKER_BOX = getBlockType("brown_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> GREEN_SHULKER_BOX = getBlockType("green_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> RED_SHULKER_BOX = getBlockType("red_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> BLACK_SHULKER_BOX = getBlockType("black_shulker_box");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> WHITE_GLAZED_TERRACOTTA = getBlockType("white_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> ORANGE_GLAZED_TERRACOTTA = getBlockType("orange_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> MAGENTA_GLAZED_TERRACOTTA = getBlockType("magenta_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> LIGHT_BLUE_GLAZED_TERRACOTTA = getBlockType("light_blue_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> YELLOW_GLAZED_TERRACOTTA = getBlockType("yellow_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> LIME_GLAZED_TERRACOTTA = getBlockType("lime_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> PINK_GLAZED_TERRACOTTA = getBlockType("pink_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> GRAY_GLAZED_TERRACOTTA = getBlockType("gray_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> LIGHT_GRAY_GLAZED_TERRACOTTA = getBlockType("light_gray_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> CYAN_GLAZED_TERRACOTTA = getBlockType("cyan_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> PURPLE_GLAZED_TERRACOTTA = getBlockType("purple_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> BLUE_GLAZED_TERRACOTTA = getBlockType("blue_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> BROWN_GLAZED_TERRACOTTA = getBlockType("brown_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> GREEN_GLAZED_TERRACOTTA = getBlockType("green_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> RED_GLAZED_TERRACOTTA = getBlockType("red_glazed_terracotta");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> BLACK_GLAZED_TERRACOTTA = getBlockType("black_glazed_terracotta");
    Typed<BlockData> WHITE_CONCRETE = getBlockType("white_concrete");
    Typed<BlockData> ORANGE_CONCRETE = getBlockType("orange_concrete");
    Typed<BlockData> MAGENTA_CONCRETE = getBlockType("magenta_concrete");
    Typed<BlockData> LIGHT_BLUE_CONCRETE = getBlockType("light_blue_concrete");
    Typed<BlockData> YELLOW_CONCRETE = getBlockType("yellow_concrete");
    Typed<BlockData> LIME_CONCRETE = getBlockType("lime_concrete");
    Typed<BlockData> PINK_CONCRETE = getBlockType("pink_concrete");
    Typed<BlockData> GRAY_CONCRETE = getBlockType("gray_concrete");
    Typed<BlockData> LIGHT_GRAY_CONCRETE = getBlockType("light_gray_concrete");
    Typed<BlockData> CYAN_CONCRETE = getBlockType("cyan_concrete");
    Typed<BlockData> PURPLE_CONCRETE = getBlockType("purple_concrete");
    Typed<BlockData> BLUE_CONCRETE = getBlockType("blue_concrete");
    Typed<BlockData> BROWN_CONCRETE = getBlockType("brown_concrete");
    Typed<BlockData> GREEN_CONCRETE = getBlockType("green_concrete");
    Typed<BlockData> RED_CONCRETE = getBlockType("red_concrete");
    Typed<BlockData> BLACK_CONCRETE = getBlockType("black_concrete");
    Typed<BlockData> WHITE_CONCRETE_POWDER = getBlockType("white_concrete_powder");
    Typed<BlockData> ORANGE_CONCRETE_POWDER = getBlockType("orange_concrete_powder");
    Typed<BlockData> MAGENTA_CONCRETE_POWDER = getBlockType("magenta_concrete_powder");
    Typed<BlockData> LIGHT_BLUE_CONCRETE_POWDER = getBlockType("light_blue_concrete_powder");
    Typed<BlockData> YELLOW_CONCRETE_POWDER = getBlockType("yellow_concrete_powder");
    Typed<BlockData> LIME_CONCRETE_POWDER = getBlockType("lime_concrete_powder");
    Typed<BlockData> PINK_CONCRETE_POWDER = getBlockType("pink_concrete_powder");
    Typed<BlockData> GRAY_CONCRETE_POWDER = getBlockType("gray_concrete_powder");
    Typed<BlockData> LIGHT_GRAY_CONCRETE_POWDER = getBlockType("light_gray_concrete_powder");
    Typed<BlockData> CYAN_CONCRETE_POWDER = getBlockType("cyan_concrete_powder");
    Typed<BlockData> PURPLE_CONCRETE_POWDER = getBlockType("purple_concrete_powder");
    Typed<BlockData> BLUE_CONCRETE_POWDER = getBlockType("blue_concrete_powder");
    Typed<BlockData> BROWN_CONCRETE_POWDER = getBlockType("brown_concrete_powder");
    Typed<BlockData> GREEN_CONCRETE_POWDER = getBlockType("green_concrete_powder");
    Typed<BlockData> RED_CONCRETE_POWDER = getBlockType("red_concrete_powder");
    Typed<BlockData> BLACK_CONCRETE_POWDER = getBlockType("black_concrete_powder");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> KELP = getBlockType("kelp");
    Typed<BlockData> KELP_PLANT = getBlockType("kelp_plant");
    Typed<BlockData> DRIED_KELP_BLOCK = getBlockType("dried_kelp_block");
    /**
     * BlockData: {@link TurtleEgg}
     */
    Typed<TurtleEgg> TURTLE_EGG = getBlockType("turtle_egg");
    /**
     * BlockData: {@link Hatchable}
     */
    Typed<Hatchable> SNIFFER_EGG = getBlockType("sniffer_egg");
    /**
     * BlockData: {@link DriedGhast}
     */
    Typed<DriedGhast> DRIED_GHAST = getBlockType("dried_ghast");
    Typed<BlockData> DEAD_TUBE_CORAL_BLOCK = getBlockType("dead_tube_coral_block");
    Typed<BlockData> DEAD_BRAIN_CORAL_BLOCK = getBlockType("dead_brain_coral_block");
    Typed<BlockData> DEAD_BUBBLE_CORAL_BLOCK = getBlockType("dead_bubble_coral_block");
    Typed<BlockData> DEAD_FIRE_CORAL_BLOCK = getBlockType("dead_fire_coral_block");
    Typed<BlockData> DEAD_HORN_CORAL_BLOCK = getBlockType("dead_horn_coral_block");
    Typed<BlockData> TUBE_CORAL_BLOCK = getBlockType("tube_coral_block");
    Typed<BlockData> BRAIN_CORAL_BLOCK = getBlockType("brain_coral_block");
    Typed<BlockData> BUBBLE_CORAL_BLOCK = getBlockType("bubble_coral_block");
    Typed<BlockData> FIRE_CORAL_BLOCK = getBlockType("fire_coral_block");
    Typed<BlockData> HORN_CORAL_BLOCK = getBlockType("horn_coral_block");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> DEAD_TUBE_CORAL = getBlockType("dead_tube_coral");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> DEAD_BRAIN_CORAL = getBlockType("dead_brain_coral");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> DEAD_BUBBLE_CORAL = getBlockType("dead_bubble_coral");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> DEAD_FIRE_CORAL = getBlockType("dead_fire_coral");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> DEAD_HORN_CORAL = getBlockType("dead_horn_coral");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> TUBE_CORAL = getBlockType("tube_coral");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> BRAIN_CORAL = getBlockType("brain_coral");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> BUBBLE_CORAL = getBlockType("bubble_coral");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> FIRE_CORAL = getBlockType("fire_coral");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> HORN_CORAL = getBlockType("horn_coral");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> DEAD_TUBE_CORAL_FAN = getBlockType("dead_tube_coral_fan");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> DEAD_BRAIN_CORAL_FAN = getBlockType("dead_brain_coral_fan");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> DEAD_BUBBLE_CORAL_FAN = getBlockType("dead_bubble_coral_fan");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> DEAD_FIRE_CORAL_FAN = getBlockType("dead_fire_coral_fan");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> DEAD_HORN_CORAL_FAN = getBlockType("dead_horn_coral_fan");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> TUBE_CORAL_FAN = getBlockType("tube_coral_fan");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> BRAIN_CORAL_FAN = getBlockType("brain_coral_fan");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> BUBBLE_CORAL_FAN = getBlockType("bubble_coral_fan");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> FIRE_CORAL_FAN = getBlockType("fire_coral_fan");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> HORN_CORAL_FAN = getBlockType("horn_coral_fan");
    /**
     * BlockData: {@link CoralWallFan}
     */
    Typed<CoralWallFan> DEAD_TUBE_CORAL_WALL_FAN = getBlockType("dead_tube_coral_wall_fan");
    /**
     * BlockData: {@link CoralWallFan}
     */
    Typed<CoralWallFan> DEAD_BRAIN_CORAL_WALL_FAN = getBlockType("dead_brain_coral_wall_fan");
    /**
     * BlockData: {@link CoralWallFan}
     */
    Typed<CoralWallFan> DEAD_BUBBLE_CORAL_WALL_FAN = getBlockType("dead_bubble_coral_wall_fan");
    /**
     * BlockData: {@link CoralWallFan}
     */
    Typed<CoralWallFan> DEAD_FIRE_CORAL_WALL_FAN = getBlockType("dead_fire_coral_wall_fan");
    /**
     * BlockData: {@link CoralWallFan}
     */
    Typed<CoralWallFan> DEAD_HORN_CORAL_WALL_FAN = getBlockType("dead_horn_coral_wall_fan");
    /**
     * BlockData: {@link CoralWallFan}
     */
    Typed<CoralWallFan> TUBE_CORAL_WALL_FAN = getBlockType("tube_coral_wall_fan");
    /**
     * BlockData: {@link CoralWallFan}
     */
    Typed<CoralWallFan> BRAIN_CORAL_WALL_FAN = getBlockType("brain_coral_wall_fan");
    /**
     * BlockData: {@link CoralWallFan}
     */
    Typed<CoralWallFan> BUBBLE_CORAL_WALL_FAN = getBlockType("bubble_coral_wall_fan");
    /**
     * BlockData: {@link CoralWallFan}
     */
    Typed<CoralWallFan> FIRE_CORAL_WALL_FAN = getBlockType("fire_coral_wall_fan");
    /**
     * BlockData: {@link CoralWallFan}
     */
    Typed<CoralWallFan> HORN_CORAL_WALL_FAN = getBlockType("horn_coral_wall_fan");
    /**
     * BlockData: {@link SeaPickle}
     */
    Typed<SeaPickle> SEA_PICKLE = getBlockType("sea_pickle");
    Typed<BlockData> BLUE_ICE = getBlockType("blue_ice");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> CONDUIT = getBlockType("conduit");
    Typed<BlockData> BAMBOO_SAPLING = getBlockType("bamboo_sapling");
    /**
     * BlockData: {@link Bamboo}
     */
    Typed<Bamboo> BAMBOO = getBlockType("bamboo");
    Typed<BlockData> POTTED_BAMBOO = getBlockType("potted_bamboo");
    Typed<BlockData> VOID_AIR = getBlockType("void_air");
    Typed<BlockData> CAVE_AIR = getBlockType("cave_air");
    /**
     * BlockData: {@link BubbleColumn}
     */
    Typed<BubbleColumn> BUBBLE_COLUMN = getBlockType("bubble_column");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> POLISHED_GRANITE_STAIRS = getBlockType("polished_granite_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> SMOOTH_RED_SANDSTONE_STAIRS = getBlockType("smooth_red_sandstone_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> MOSSY_STONE_BRICK_STAIRS = getBlockType("mossy_stone_brick_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> POLISHED_DIORITE_STAIRS = getBlockType("polished_diorite_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> MOSSY_COBBLESTONE_STAIRS = getBlockType("mossy_cobblestone_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> END_STONE_BRICK_STAIRS = getBlockType("end_stone_brick_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> STONE_STAIRS = getBlockType("stone_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> SMOOTH_SANDSTONE_STAIRS = getBlockType("smooth_sandstone_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> SMOOTH_QUARTZ_STAIRS = getBlockType("smooth_quartz_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> GRANITE_STAIRS = getBlockType("granite_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> ANDESITE_STAIRS = getBlockType("andesite_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> RED_NETHER_BRICK_STAIRS = getBlockType("red_nether_brick_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> POLISHED_ANDESITE_STAIRS = getBlockType("polished_andesite_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> DIORITE_STAIRS = getBlockType("diorite_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> POLISHED_GRANITE_SLAB = getBlockType("polished_granite_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> SMOOTH_RED_SANDSTONE_SLAB = getBlockType("smooth_red_sandstone_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> MOSSY_STONE_BRICK_SLAB = getBlockType("mossy_stone_brick_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> POLISHED_DIORITE_SLAB = getBlockType("polished_diorite_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> MOSSY_COBBLESTONE_SLAB = getBlockType("mossy_cobblestone_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> END_STONE_BRICK_SLAB = getBlockType("end_stone_brick_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> SMOOTH_SANDSTONE_SLAB = getBlockType("smooth_sandstone_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> SMOOTH_QUARTZ_SLAB = getBlockType("smooth_quartz_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> GRANITE_SLAB = getBlockType("granite_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> ANDESITE_SLAB = getBlockType("andesite_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> RED_NETHER_BRICK_SLAB = getBlockType("red_nether_brick_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> POLISHED_ANDESITE_SLAB = getBlockType("polished_andesite_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> DIORITE_SLAB = getBlockType("diorite_slab");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> BRICK_WALL = getBlockType("brick_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> PRISMARINE_WALL = getBlockType("prismarine_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> RED_SANDSTONE_WALL = getBlockType("red_sandstone_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> MOSSY_STONE_BRICK_WALL = getBlockType("mossy_stone_brick_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> GRANITE_WALL = getBlockType("granite_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> STONE_BRICK_WALL = getBlockType("stone_brick_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> MUD_BRICK_WALL = getBlockType("mud_brick_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> NETHER_BRICK_WALL = getBlockType("nether_brick_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> ANDESITE_WALL = getBlockType("andesite_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> RED_NETHER_BRICK_WALL = getBlockType("red_nether_brick_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> SANDSTONE_WALL = getBlockType("sandstone_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> END_STONE_BRICK_WALL = getBlockType("end_stone_brick_wall");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> DIORITE_WALL = getBlockType("diorite_wall");
    /**
     * BlockData: {@link Scaffolding}
     */
    Typed<Scaffolding> SCAFFOLDING = getBlockType("scaffolding");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> LOOM = getBlockType("loom");
    /**
     * BlockData: {@link Barrel}
     */
    Typed<Barrel> BARREL = getBlockType("barrel");
    /**
     * BlockData: {@link Furnace}
     */
    Typed<Furnace> SMOKER = getBlockType("smoker");
    /**
     * BlockData: {@link Furnace}
     */
    Typed<Furnace> BLAST_FURNACE = getBlockType("blast_furnace");
    Typed<BlockData> CARTOGRAPHY_TABLE = getBlockType("cartography_table");
    Typed<BlockData> FLETCHING_TABLE = getBlockType("fletching_table");
    /**
     * BlockData: {@link Grindstone}
     */
    Typed<Grindstone> GRINDSTONE = getBlockType("grindstone");
    /**
     * BlockData: {@link Lectern}
     */
    Typed<Lectern> LECTERN = getBlockType("lectern");
    Typed<BlockData> SMITHING_TABLE = getBlockType("smithing_table");
    /**
     * BlockData: {@link Directional}
     */
    Typed<Directional> STONECUTTER = getBlockType("stonecutter");
    /**
     * BlockData: {@link Bell}
     */
    Typed<Bell> BELL = getBlockType("bell");
    /**
     * BlockData: {@link Lantern}
     */
    Typed<Lantern> LANTERN = getBlockType("lantern");
    /**
     * BlockData: {@link Lantern}
     */
    Typed<Lantern> SOUL_LANTERN = getBlockType("soul_lantern");
    /**
     * BlockData: {@link Lantern}
     */
    Typed<Lantern> COPPER_LANTERN = getBlockType("copper_lantern");
    /**
     * BlockData: {@link Lantern}
     */
    Typed<Lantern> EXPOSED_COPPER_LANTERN = getBlockType("exposed_copper_lantern");
    /**
     * BlockData: {@link Lantern}
     */
    Typed<Lantern> WEATHERED_COPPER_LANTERN = getBlockType("weathered_copper_lantern");
    /**
     * BlockData: {@link Lantern}
     */
    Typed<Lantern> OXIDIZED_COPPER_LANTERN = getBlockType("oxidized_copper_lantern");
    /**
     * BlockData: {@link Lantern}
     */
    Typed<Lantern> WAXED_COPPER_LANTERN = getBlockType("waxed_copper_lantern");
    /**
     * BlockData: {@link Lantern}
     */
    Typed<Lantern> WAXED_EXPOSED_COPPER_LANTERN = getBlockType("waxed_exposed_copper_lantern");
    /**
     * BlockData: {@link Lantern}
     */
    Typed<Lantern> WAXED_WEATHERED_COPPER_LANTERN = getBlockType("waxed_weathered_copper_lantern");
    /**
     * BlockData: {@link Lantern}
     */
    Typed<Lantern> WAXED_OXIDIZED_COPPER_LANTERN = getBlockType("waxed_oxidized_copper_lantern");
    /**
     * BlockData: {@link Campfire}
     */
    Typed<Campfire> CAMPFIRE = getBlockType("campfire");
    /**
     * BlockData: {@link Campfire}
     */
    Typed<Campfire> SOUL_CAMPFIRE = getBlockType("soul_campfire");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> SWEET_BERRY_BUSH = getBlockType("sweet_berry_bush");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> WARPED_STEM = getBlockType("warped_stem");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_WARPED_STEM = getBlockType("stripped_warped_stem");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> WARPED_HYPHAE = getBlockType("warped_hyphae");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_WARPED_HYPHAE = getBlockType("stripped_warped_hyphae");
    Typed<BlockData> WARPED_NYLIUM = getBlockType("warped_nylium");
    Typed<BlockData> WARPED_FUNGUS = getBlockType("warped_fungus");
    Typed<BlockData> WARPED_WART_BLOCK = getBlockType("warped_wart_block");
    Typed<BlockData> WARPED_ROOTS = getBlockType("warped_roots");
    Typed<BlockData> NETHER_SPROUTS = getBlockType("nether_sprouts");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> CRIMSON_STEM = getBlockType("crimson_stem");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_CRIMSON_STEM = getBlockType("stripped_crimson_stem");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> CRIMSON_HYPHAE = getBlockType("crimson_hyphae");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> STRIPPED_CRIMSON_HYPHAE = getBlockType("stripped_crimson_hyphae");
    Typed<BlockData> CRIMSON_NYLIUM = getBlockType("crimson_nylium");
    Typed<BlockData> CRIMSON_FUNGUS = getBlockType("crimson_fungus");
    Typed<BlockData> SHROOMLIGHT = getBlockType("shroomlight");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> WEEPING_VINES = getBlockType("weeping_vines");
    Typed<BlockData> WEEPING_VINES_PLANT = getBlockType("weeping_vines_plant");
    /**
     * BlockData: {@link Ageable}
     */
    Typed<Ageable> TWISTING_VINES = getBlockType("twisting_vines");
    Typed<BlockData> TWISTING_VINES_PLANT = getBlockType("twisting_vines_plant");
    Typed<BlockData> CRIMSON_ROOTS = getBlockType("crimson_roots");
    Typed<BlockData> CRIMSON_PLANKS = getBlockType("crimson_planks");
    Typed<BlockData> WARPED_PLANKS = getBlockType("warped_planks");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> CRIMSON_SLAB = getBlockType("crimson_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> WARPED_SLAB = getBlockType("warped_slab");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> CRIMSON_PRESSURE_PLATE = getBlockType("crimson_pressure_plate");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> WARPED_PRESSURE_PLATE = getBlockType("warped_pressure_plate");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> CRIMSON_FENCE = getBlockType("crimson_fence");
    /**
     * BlockData: {@link Fence}
     */
    Typed<Fence> WARPED_FENCE = getBlockType("warped_fence");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> CRIMSON_TRAPDOOR = getBlockType("crimson_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> WARPED_TRAPDOOR = getBlockType("warped_trapdoor");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> CRIMSON_FENCE_GATE = getBlockType("crimson_fence_gate");
    /**
     * BlockData: {@link Gate}
     */
    Typed<Gate> WARPED_FENCE_GATE = getBlockType("warped_fence_gate");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> CRIMSON_STAIRS = getBlockType("crimson_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> WARPED_STAIRS = getBlockType("warped_stairs");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> CRIMSON_BUTTON = getBlockType("crimson_button");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> WARPED_BUTTON = getBlockType("warped_button");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> CRIMSON_DOOR = getBlockType("crimson_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> WARPED_DOOR = getBlockType("warped_door");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> CRIMSON_SIGN = getBlockType("crimson_sign");
    /**
     * BlockData: {@link Sign}
     */
    Typed<Sign> WARPED_SIGN = getBlockType("warped_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> CRIMSON_WALL_SIGN = getBlockType("crimson_wall_sign");
    /**
     * BlockData: {@link WallSign}
     */
    Typed<WallSign> WARPED_WALL_SIGN = getBlockType("warped_wall_sign");
    /**
     * BlockData: {@link StructureBlock}
     */
    Typed<StructureBlock> STRUCTURE_BLOCK = getBlockType("structure_block");
    /**
     * BlockData: {@link Jigsaw}
     */
    Typed<Jigsaw> JIGSAW = getBlockType("jigsaw");
    /**
     * BlockData: {@link TestBlock}
     */
    Typed<TestBlock> TEST_BLOCK = getBlockType("test_block");
    Typed<BlockData> TEST_INSTANCE_BLOCK = getBlockType("test_instance_block");
    /**
     * BlockData: {@link Levelled}
     */
    Typed<Levelled> COMPOSTER = getBlockType("composter");
    /**
     * BlockData: {@link AnaloguePowerable}
     */
    Typed<AnaloguePowerable> TARGET = getBlockType("target");
    /**
     * BlockData: {@link Beehive}
     */
    Typed<Beehive> BEE_NEST = getBlockType("bee_nest");
    /**
     * BlockData: {@link Beehive}
     */
    Typed<Beehive> BEEHIVE = getBlockType("beehive");
    Typed<BlockData> HONEY_BLOCK = getBlockType("honey_block");
    Typed<BlockData> HONEYCOMB_BLOCK = getBlockType("honeycomb_block");
    Typed<BlockData> NETHERITE_BLOCK = getBlockType("netherite_block");
    Typed<BlockData> ANCIENT_DEBRIS = getBlockType("ancient_debris");
    Typed<BlockData> CRYING_OBSIDIAN = getBlockType("crying_obsidian");
    /**
     * BlockData: {@link RespawnAnchor}
     */
    Typed<RespawnAnchor> RESPAWN_ANCHOR = getBlockType("respawn_anchor");
    Typed<BlockData> POTTED_CRIMSON_FUNGUS = getBlockType("potted_crimson_fungus");
    Typed<BlockData> POTTED_WARPED_FUNGUS = getBlockType("potted_warped_fungus");
    Typed<BlockData> POTTED_CRIMSON_ROOTS = getBlockType("potted_crimson_roots");
    Typed<BlockData> POTTED_WARPED_ROOTS = getBlockType("potted_warped_roots");
    Typed<BlockData> LODESTONE = getBlockType("lodestone");
    Typed<BlockData> BLACKSTONE = getBlockType("blackstone");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> BLACKSTONE_STAIRS = getBlockType("blackstone_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> BLACKSTONE_WALL = getBlockType("blackstone_wall");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> BLACKSTONE_SLAB = getBlockType("blackstone_slab");
    Typed<BlockData> POLISHED_BLACKSTONE = getBlockType("polished_blackstone");
    Typed<BlockData> POLISHED_BLACKSTONE_BRICKS = getBlockType("polished_blackstone_bricks");
    Typed<BlockData> CRACKED_POLISHED_BLACKSTONE_BRICKS = getBlockType("cracked_polished_blackstone_bricks");
    Typed<BlockData> CHISELED_POLISHED_BLACKSTONE = getBlockType("chiseled_polished_blackstone");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> POLISHED_BLACKSTONE_BRICK_SLAB = getBlockType("polished_blackstone_brick_slab");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> POLISHED_BLACKSTONE_BRICK_STAIRS = getBlockType("polished_blackstone_brick_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> POLISHED_BLACKSTONE_BRICK_WALL = getBlockType("polished_blackstone_brick_wall");
    Typed<BlockData> GILDED_BLACKSTONE = getBlockType("gilded_blackstone");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> POLISHED_BLACKSTONE_STAIRS = getBlockType("polished_blackstone_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> POLISHED_BLACKSTONE_SLAB = getBlockType("polished_blackstone_slab");
    /**
     * BlockData: {@link Powerable}
     */
    Typed<Powerable> POLISHED_BLACKSTONE_PRESSURE_PLATE = getBlockType("polished_blackstone_pressure_plate");
    /**
     * BlockData: {@link Switch}
     */
    Typed<Switch> POLISHED_BLACKSTONE_BUTTON = getBlockType("polished_blackstone_button");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> POLISHED_BLACKSTONE_WALL = getBlockType("polished_blackstone_wall");
    Typed<BlockData> CHISELED_NETHER_BRICKS = getBlockType("chiseled_nether_bricks");
    Typed<BlockData> CRACKED_NETHER_BRICKS = getBlockType("cracked_nether_bricks");
    Typed<BlockData> QUARTZ_BRICKS = getBlockType("quartz_bricks");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> CANDLE = getBlockType("candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> WHITE_CANDLE = getBlockType("white_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> ORANGE_CANDLE = getBlockType("orange_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> MAGENTA_CANDLE = getBlockType("magenta_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> LIGHT_BLUE_CANDLE = getBlockType("light_blue_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> YELLOW_CANDLE = getBlockType("yellow_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> LIME_CANDLE = getBlockType("lime_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> PINK_CANDLE = getBlockType("pink_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> GRAY_CANDLE = getBlockType("gray_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> LIGHT_GRAY_CANDLE = getBlockType("light_gray_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> CYAN_CANDLE = getBlockType("cyan_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> PURPLE_CANDLE = getBlockType("purple_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> BLUE_CANDLE = getBlockType("blue_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> BROWN_CANDLE = getBlockType("brown_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> GREEN_CANDLE = getBlockType("green_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> RED_CANDLE = getBlockType("red_candle");
    /**
     * BlockData: {@link Candle}
     */
    Typed<Candle> BLACK_CANDLE = getBlockType("black_candle");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> CANDLE_CAKE = getBlockType("candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> WHITE_CANDLE_CAKE = getBlockType("white_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> ORANGE_CANDLE_CAKE = getBlockType("orange_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> MAGENTA_CANDLE_CAKE = getBlockType("magenta_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> LIGHT_BLUE_CANDLE_CAKE = getBlockType("light_blue_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> YELLOW_CANDLE_CAKE = getBlockType("yellow_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> LIME_CANDLE_CAKE = getBlockType("lime_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> PINK_CANDLE_CAKE = getBlockType("pink_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> GRAY_CANDLE_CAKE = getBlockType("gray_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> LIGHT_GRAY_CANDLE_CAKE = getBlockType("light_gray_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> CYAN_CANDLE_CAKE = getBlockType("cyan_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> PURPLE_CANDLE_CAKE = getBlockType("purple_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> BLUE_CANDLE_CAKE = getBlockType("blue_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> BROWN_CANDLE_CAKE = getBlockType("brown_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> GREEN_CANDLE_CAKE = getBlockType("green_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> RED_CANDLE_CAKE = getBlockType("red_candle_cake");
    /**
     * BlockData: {@link Lightable}
     */
    Typed<Lightable> BLACK_CANDLE_CAKE = getBlockType("black_candle_cake");
    Typed<BlockData> AMETHYST_BLOCK = getBlockType("amethyst_block");
    Typed<BlockData> BUDDING_AMETHYST = getBlockType("budding_amethyst");
    /**
     * BlockData: {@link AmethystCluster}
     */
    Typed<AmethystCluster> AMETHYST_CLUSTER = getBlockType("amethyst_cluster");
    /**
     * BlockData: {@link AmethystCluster}
     */
    Typed<AmethystCluster> LARGE_AMETHYST_BUD = getBlockType("large_amethyst_bud");
    /**
     * BlockData: {@link AmethystCluster}
     */
    Typed<AmethystCluster> MEDIUM_AMETHYST_BUD = getBlockType("medium_amethyst_bud");
    /**
     * BlockData: {@link AmethystCluster}
     */
    Typed<AmethystCluster> SMALL_AMETHYST_BUD = getBlockType("small_amethyst_bud");
    Typed<BlockData> TUFF = getBlockType("tuff");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> TUFF_SLAB = getBlockType("tuff_slab");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> TUFF_STAIRS = getBlockType("tuff_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> TUFF_WALL = getBlockType("tuff_wall");
    Typed<BlockData> POLISHED_TUFF = getBlockType("polished_tuff");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> POLISHED_TUFF_SLAB = getBlockType("polished_tuff_slab");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> POLISHED_TUFF_STAIRS = getBlockType("polished_tuff_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> POLISHED_TUFF_WALL = getBlockType("polished_tuff_wall");
    Typed<BlockData> CHISELED_TUFF = getBlockType("chiseled_tuff");
    Typed<BlockData> TUFF_BRICKS = getBlockType("tuff_bricks");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> TUFF_BRICK_SLAB = getBlockType("tuff_brick_slab");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> TUFF_BRICK_STAIRS = getBlockType("tuff_brick_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> TUFF_BRICK_WALL = getBlockType("tuff_brick_wall");
    Typed<BlockData> CHISELED_TUFF_BRICKS = getBlockType("chiseled_tuff_bricks");
    Typed<BlockData> SULFUR = getBlockType("sulfur");
    /**
     * BlockData: {@link PotentSulfur}
     */
    Typed<PotentSulfur> POTENT_SULFUR = getBlockType("potent_sulfur");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> SULFUR_SLAB = getBlockType("sulfur_slab");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> SULFUR_STAIRS = getBlockType("sulfur_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> SULFUR_WALL = getBlockType("sulfur_wall");
    Typed<BlockData> POLISHED_SULFUR = getBlockType("polished_sulfur");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> POLISHED_SULFUR_SLAB = getBlockType("polished_sulfur_slab");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> POLISHED_SULFUR_STAIRS = getBlockType("polished_sulfur_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> POLISHED_SULFUR_WALL = getBlockType("polished_sulfur_wall");
    Typed<BlockData> SULFUR_BRICKS = getBlockType("sulfur_bricks");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> SULFUR_BRICK_SLAB = getBlockType("sulfur_brick_slab");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> SULFUR_BRICK_STAIRS = getBlockType("sulfur_brick_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> SULFUR_BRICK_WALL = getBlockType("sulfur_brick_wall");
    Typed<BlockData> CHISELED_SULFUR = getBlockType("chiseled_sulfur");
    Typed<BlockData> CINNABAR = getBlockType("cinnabar");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> CINNABAR_SLAB = getBlockType("cinnabar_slab");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> CINNABAR_STAIRS = getBlockType("cinnabar_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> CINNABAR_WALL = getBlockType("cinnabar_wall");
    Typed<BlockData> POLISHED_CINNABAR = getBlockType("polished_cinnabar");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> POLISHED_CINNABAR_SLAB = getBlockType("polished_cinnabar_slab");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> POLISHED_CINNABAR_STAIRS = getBlockType("polished_cinnabar_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> POLISHED_CINNABAR_WALL = getBlockType("polished_cinnabar_wall");
    Typed<BlockData> CINNABAR_BRICKS = getBlockType("cinnabar_bricks");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> CINNABAR_BRICK_SLAB = getBlockType("cinnabar_brick_slab");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> CINNABAR_BRICK_STAIRS = getBlockType("cinnabar_brick_stairs");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> CINNABAR_BRICK_WALL = getBlockType("cinnabar_brick_wall");
    Typed<BlockData> CHISELED_CINNABAR = getBlockType("chiseled_cinnabar");
    Typed<BlockData> CALCITE = getBlockType("calcite");
    Typed<BlockData> TINTED_GLASS = getBlockType("tinted_glass");
    Typed<BlockData> POWDER_SNOW = getBlockType("powder_snow");
    /**
     * BlockData: {@link SculkSensor}
     */
    Typed<SculkSensor> SCULK_SENSOR = getBlockType("sculk_sensor");
    /**
     * BlockData: {@link CalibratedSculkSensor}
     */
    Typed<CalibratedSculkSensor> CALIBRATED_SCULK_SENSOR = getBlockType("calibrated_sculk_sensor");
    Typed<BlockData> SCULK = getBlockType("sculk");
    /**
     * BlockData: {@link SculkVein}
     */
    Typed<SculkVein> SCULK_VEIN = getBlockType("sculk_vein");
    /**
     * BlockData: {@link SculkCatalyst}
     */
    Typed<SculkCatalyst> SCULK_CATALYST = getBlockType("sculk_catalyst");
    /**
     * BlockData: {@link SculkShrieker}
     */
    Typed<SculkShrieker> SCULK_SHRIEKER = getBlockType("sculk_shrieker");
    Typed<BlockData> COPPER_BLOCK = getBlockType("copper_block");
    Typed<BlockData> EXPOSED_COPPER = getBlockType("exposed_copper");
    Typed<BlockData> WEATHERED_COPPER = getBlockType("weathered_copper");
    Typed<BlockData> OXIDIZED_COPPER = getBlockType("oxidized_copper");
    Typed<BlockData> COPPER_ORE = getBlockType("copper_ore");
    Typed<BlockData> DEEPSLATE_COPPER_ORE = getBlockType("deepslate_copper_ore");
    Typed<BlockData> OXIDIZED_CUT_COPPER = getBlockType("oxidized_cut_copper");
    Typed<BlockData> WEATHERED_CUT_COPPER = getBlockType("weathered_cut_copper");
    Typed<BlockData> EXPOSED_CUT_COPPER = getBlockType("exposed_cut_copper");
    Typed<BlockData> CUT_COPPER = getBlockType("cut_copper");
    Typed<BlockData> OXIDIZED_CHISELED_COPPER = getBlockType("oxidized_chiseled_copper");
    Typed<BlockData> WEATHERED_CHISELED_COPPER = getBlockType("weathered_chiseled_copper");
    Typed<BlockData> EXPOSED_CHISELED_COPPER = getBlockType("exposed_chiseled_copper");
    Typed<BlockData> CHISELED_COPPER = getBlockType("chiseled_copper");
    Typed<BlockData> WAXED_OXIDIZED_CHISELED_COPPER = getBlockType("waxed_oxidized_chiseled_copper");
    Typed<BlockData> WAXED_WEATHERED_CHISELED_COPPER = getBlockType("waxed_weathered_chiseled_copper");
    Typed<BlockData> WAXED_EXPOSED_CHISELED_COPPER = getBlockType("waxed_exposed_chiseled_copper");
    Typed<BlockData> WAXED_CHISELED_COPPER = getBlockType("waxed_chiseled_copper");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> OXIDIZED_CUT_COPPER_STAIRS = getBlockType("oxidized_cut_copper_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> WEATHERED_CUT_COPPER_STAIRS = getBlockType("weathered_cut_copper_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> EXPOSED_CUT_COPPER_STAIRS = getBlockType("exposed_cut_copper_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> CUT_COPPER_STAIRS = getBlockType("cut_copper_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> OXIDIZED_CUT_COPPER_SLAB = getBlockType("oxidized_cut_copper_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> WEATHERED_CUT_COPPER_SLAB = getBlockType("weathered_cut_copper_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> EXPOSED_CUT_COPPER_SLAB = getBlockType("exposed_cut_copper_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> CUT_COPPER_SLAB = getBlockType("cut_copper_slab");
    Typed<BlockData> WAXED_COPPER_BLOCK = getBlockType("waxed_copper_block");
    Typed<BlockData> WAXED_WEATHERED_COPPER = getBlockType("waxed_weathered_copper");
    Typed<BlockData> WAXED_EXPOSED_COPPER = getBlockType("waxed_exposed_copper");
    Typed<BlockData> WAXED_OXIDIZED_COPPER = getBlockType("waxed_oxidized_copper");
    Typed<BlockData> WAXED_OXIDIZED_CUT_COPPER = getBlockType("waxed_oxidized_cut_copper");
    Typed<BlockData> WAXED_WEATHERED_CUT_COPPER = getBlockType("waxed_weathered_cut_copper");
    Typed<BlockData> WAXED_EXPOSED_CUT_COPPER = getBlockType("waxed_exposed_cut_copper");
    Typed<BlockData> WAXED_CUT_COPPER = getBlockType("waxed_cut_copper");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> WAXED_OXIDIZED_CUT_COPPER_STAIRS = getBlockType("waxed_oxidized_cut_copper_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> WAXED_WEATHERED_CUT_COPPER_STAIRS = getBlockType("waxed_weathered_cut_copper_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> WAXED_EXPOSED_CUT_COPPER_STAIRS = getBlockType("waxed_exposed_cut_copper_stairs");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> WAXED_CUT_COPPER_STAIRS = getBlockType("waxed_cut_copper_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> WAXED_OXIDIZED_CUT_COPPER_SLAB = getBlockType("waxed_oxidized_cut_copper_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> WAXED_WEATHERED_CUT_COPPER_SLAB = getBlockType("waxed_weathered_cut_copper_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> WAXED_EXPOSED_CUT_COPPER_SLAB = getBlockType("waxed_exposed_cut_copper_slab");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> WAXED_CUT_COPPER_SLAB = getBlockType("waxed_cut_copper_slab");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> COPPER_DOOR = getBlockType("copper_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> EXPOSED_COPPER_DOOR = getBlockType("exposed_copper_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> OXIDIZED_COPPER_DOOR = getBlockType("oxidized_copper_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> WEATHERED_COPPER_DOOR = getBlockType("weathered_copper_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> WAXED_COPPER_DOOR = getBlockType("waxed_copper_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> WAXED_EXPOSED_COPPER_DOOR = getBlockType("waxed_exposed_copper_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> WAXED_OXIDIZED_COPPER_DOOR = getBlockType("waxed_oxidized_copper_door");
    /**
     * BlockData: {@link Door}
     */
    Typed<Door> WAXED_WEATHERED_COPPER_DOOR = getBlockType("waxed_weathered_copper_door");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> COPPER_TRAPDOOR = getBlockType("copper_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> EXPOSED_COPPER_TRAPDOOR = getBlockType("exposed_copper_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> OXIDIZED_COPPER_TRAPDOOR = getBlockType("oxidized_copper_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> WEATHERED_COPPER_TRAPDOOR = getBlockType("weathered_copper_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> WAXED_COPPER_TRAPDOOR = getBlockType("waxed_copper_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> WAXED_EXPOSED_COPPER_TRAPDOOR = getBlockType("waxed_exposed_copper_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> WAXED_OXIDIZED_COPPER_TRAPDOOR = getBlockType("waxed_oxidized_copper_trapdoor");
    /**
     * BlockData: {@link TrapDoor}
     */
    Typed<TrapDoor> WAXED_WEATHERED_COPPER_TRAPDOOR = getBlockType("waxed_weathered_copper_trapdoor");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> COPPER_GRATE = getBlockType("copper_grate");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> EXPOSED_COPPER_GRATE = getBlockType("exposed_copper_grate");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> WEATHERED_COPPER_GRATE = getBlockType("weathered_copper_grate");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> OXIDIZED_COPPER_GRATE = getBlockType("oxidized_copper_grate");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> WAXED_COPPER_GRATE = getBlockType("waxed_copper_grate");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> WAXED_EXPOSED_COPPER_GRATE = getBlockType("waxed_exposed_copper_grate");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> WAXED_WEATHERED_COPPER_GRATE = getBlockType("waxed_weathered_copper_grate");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> WAXED_OXIDIZED_COPPER_GRATE = getBlockType("waxed_oxidized_copper_grate");
    /**
     * BlockData: {@link CopperBulb}
     */
    Typed<CopperBulb> COPPER_BULB = getBlockType("copper_bulb");
    /**
     * BlockData: {@link CopperBulb}
     */
    Typed<CopperBulb> EXPOSED_COPPER_BULB = getBlockType("exposed_copper_bulb");
    /**
     * BlockData: {@link CopperBulb}
     */
    Typed<CopperBulb> WEATHERED_COPPER_BULB = getBlockType("weathered_copper_bulb");
    /**
     * BlockData: {@link CopperBulb}
     */
    Typed<CopperBulb> OXIDIZED_COPPER_BULB = getBlockType("oxidized_copper_bulb");
    /**
     * BlockData: {@link CopperBulb}
     */
    Typed<CopperBulb> WAXED_COPPER_BULB = getBlockType("waxed_copper_bulb");
    /**
     * BlockData: {@link CopperBulb}
     */
    Typed<CopperBulb> WAXED_EXPOSED_COPPER_BULB = getBlockType("waxed_exposed_copper_bulb");
    /**
     * BlockData: {@link CopperBulb}
     */
    Typed<CopperBulb> WAXED_WEATHERED_COPPER_BULB = getBlockType("waxed_weathered_copper_bulb");
    /**
     * BlockData: {@link CopperBulb}
     */
    Typed<CopperBulb> WAXED_OXIDIZED_COPPER_BULB = getBlockType("waxed_oxidized_copper_bulb");
    /**
     * BlockData: {@link Chest}
     */
    Typed<Chest> COPPER_CHEST = getBlockType("copper_chest");
    /**
     * BlockData: {@link Chest}
     */
    Typed<Chest> EXPOSED_COPPER_CHEST = getBlockType("exposed_copper_chest");
    /**
     * BlockData: {@link Chest}
     */
    Typed<Chest> WEATHERED_COPPER_CHEST = getBlockType("weathered_copper_chest");
    /**
     * BlockData: {@link Chest}
     */
    Typed<Chest> OXIDIZED_COPPER_CHEST = getBlockType("oxidized_copper_chest");
    /**
     * BlockData: {@link Chest}
     */
    Typed<Chest> WAXED_COPPER_CHEST = getBlockType("waxed_copper_chest");
    /**
     * BlockData: {@link Chest}
     */
    Typed<Chest> WAXED_EXPOSED_COPPER_CHEST = getBlockType("waxed_exposed_copper_chest");
    /**
     * BlockData: {@link Chest}
     */
    Typed<Chest> WAXED_WEATHERED_COPPER_CHEST = getBlockType("waxed_weathered_copper_chest");
    /**
     * BlockData: {@link Chest}
     */
    Typed<Chest> WAXED_OXIDIZED_COPPER_CHEST = getBlockType("waxed_oxidized_copper_chest");
    /**
     * BlockData: {@link CopperGolemStatue}
     */
    Typed<CopperGolemStatue> COPPER_GOLEM_STATUE = getBlockType("copper_golem_statue");
    /**
     * BlockData: {@link CopperGolemStatue}
     */
    Typed<CopperGolemStatue> EXPOSED_COPPER_GOLEM_STATUE = getBlockType("exposed_copper_golem_statue");
    /**
     * BlockData: {@link CopperGolemStatue}
     */
    Typed<CopperGolemStatue> WEATHERED_COPPER_GOLEM_STATUE = getBlockType("weathered_copper_golem_statue");
    /**
     * BlockData: {@link CopperGolemStatue}
     */
    Typed<CopperGolemStatue> OXIDIZED_COPPER_GOLEM_STATUE = getBlockType("oxidized_copper_golem_statue");
    /**
     * BlockData: {@link CopperGolemStatue}
     */
    Typed<CopperGolemStatue> WAXED_COPPER_GOLEM_STATUE = getBlockType("waxed_copper_golem_statue");
    /**
     * BlockData: {@link CopperGolemStatue}
     */
    Typed<CopperGolemStatue> WAXED_EXPOSED_COPPER_GOLEM_STATUE = getBlockType("waxed_exposed_copper_golem_statue");
    /**
     * BlockData: {@link CopperGolemStatue}
     */
    Typed<CopperGolemStatue> WAXED_WEATHERED_COPPER_GOLEM_STATUE = getBlockType("waxed_weathered_copper_golem_statue");
    /**
     * BlockData: {@link CopperGolemStatue}
     */
    Typed<CopperGolemStatue> WAXED_OXIDIZED_COPPER_GOLEM_STATUE = getBlockType("waxed_oxidized_copper_golem_statue");
    /**
     * BlockData: {@link LightningRod}
     */
    Typed<LightningRod> LIGHTNING_ROD = getBlockType("lightning_rod");
    /**
     * BlockData: {@link LightningRod}
     */
    Typed<LightningRod> EXPOSED_LIGHTNING_ROD = getBlockType("exposed_lightning_rod");
    /**
     * BlockData: {@link LightningRod}
     */
    Typed<LightningRod> WEATHERED_LIGHTNING_ROD = getBlockType("weathered_lightning_rod");
    /**
     * BlockData: {@link LightningRod}
     */
    Typed<LightningRod> OXIDIZED_LIGHTNING_ROD = getBlockType("oxidized_lightning_rod");
    /**
     * BlockData: {@link LightningRod}
     */
    Typed<LightningRod> WAXED_LIGHTNING_ROD = getBlockType("waxed_lightning_rod");
    /**
     * BlockData: {@link LightningRod}
     */
    Typed<LightningRod> WAXED_EXPOSED_LIGHTNING_ROD = getBlockType("waxed_exposed_lightning_rod");
    /**
     * BlockData: {@link LightningRod}
     */
    Typed<LightningRod> WAXED_WEATHERED_LIGHTNING_ROD = getBlockType("waxed_weathered_lightning_rod");
    /**
     * BlockData: {@link LightningRod}
     */
    Typed<LightningRod> WAXED_OXIDIZED_LIGHTNING_ROD = getBlockType("waxed_oxidized_lightning_rod");
    /**
     * BlockData: {@link PointedDripstone}
     */
    Typed<PointedDripstone> POINTED_DRIPSTONE = getBlockType("pointed_dripstone");
    /**
     * BlockData: {@link PointedDripstone}
     */
    Typed<PointedDripstone> SULFUR_SPIKE = getBlockType("sulfur_spike");
    Typed<BlockData> DRIPSTONE_BLOCK = getBlockType("dripstone_block");
    /**
     * BlockData: {@link CaveVines}
     */
    Typed<CaveVines> CAVE_VINES = getBlockType("cave_vines");
    /**
     * BlockData: {@link CaveVinesPlant}
     */
    Typed<CaveVinesPlant> CAVE_VINES_PLANT = getBlockType("cave_vines_plant");
    Typed<BlockData> SPORE_BLOSSOM = getBlockType("spore_blossom");
    Typed<BlockData> AZALEA = getBlockType("azalea");
    Typed<BlockData> FLOWERING_AZALEA = getBlockType("flowering_azalea");
    /**
     * BlockData: {@link PinkPetals}
     */
    Typed<PinkPetals> PINK_PETALS = getBlockType("pink_petals");
    /**
     * BlockData: {@link PinkPetals}
     */
    Typed<PinkPetals> WILDFLOWERS = getBlockType("wildflowers");
    /**
     * BlockData: {@link LeafLitter}
     */
    Typed<LeafLitter> LEAF_LITTER = getBlockType("leaf_litter");
    Typed<BlockData> MOSS_CARPET = getBlockType("moss_carpet");
    Typed<BlockData> MOSS_BLOCK = getBlockType("moss_block");
    /**
     * BlockData: {@link MossyCarpet}
     */
    Typed<MossyCarpet> PALE_MOSS_CARPET = getBlockType("pale_moss_carpet");
    /**
     * BlockData: {@link HangingMoss}
     */
    Typed<HangingMoss> PALE_HANGING_MOSS = getBlockType("pale_hanging_moss");
    Typed<BlockData> PALE_MOSS_BLOCK = getBlockType("pale_moss_block");
    /**
     * BlockData: {@link BigDripleaf}
     */
    Typed<BigDripleaf> BIG_DRIPLEAF = getBlockType("big_dripleaf");
    /**
     * BlockData: {@link Dripleaf}
     */
    Typed<Dripleaf> BIG_DRIPLEAF_STEM = getBlockType("big_dripleaf_stem");
    /**
     * BlockData: {@link SmallDripleaf}
     */
    Typed<SmallDripleaf> SMALL_DRIPLEAF = getBlockType("small_dripleaf");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> HANGING_ROOTS = getBlockType("hanging_roots");
    Typed<BlockData> ROOTED_DIRT = getBlockType("rooted_dirt");
    Typed<BlockData> MUD = getBlockType("mud");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> DEEPSLATE = getBlockType("deepslate");
    Typed<BlockData> COBBLED_DEEPSLATE = getBlockType("cobbled_deepslate");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> COBBLED_DEEPSLATE_STAIRS = getBlockType("cobbled_deepslate_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> COBBLED_DEEPSLATE_SLAB = getBlockType("cobbled_deepslate_slab");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> COBBLED_DEEPSLATE_WALL = getBlockType("cobbled_deepslate_wall");
    Typed<BlockData> POLISHED_DEEPSLATE = getBlockType("polished_deepslate");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> POLISHED_DEEPSLATE_STAIRS = getBlockType("polished_deepslate_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> POLISHED_DEEPSLATE_SLAB = getBlockType("polished_deepslate_slab");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> POLISHED_DEEPSLATE_WALL = getBlockType("polished_deepslate_wall");
    Typed<BlockData> DEEPSLATE_TILES = getBlockType("deepslate_tiles");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> DEEPSLATE_TILE_STAIRS = getBlockType("deepslate_tile_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> DEEPSLATE_TILE_SLAB = getBlockType("deepslate_tile_slab");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> DEEPSLATE_TILE_WALL = getBlockType("deepslate_tile_wall");
    Typed<BlockData> DEEPSLATE_BRICKS = getBlockType("deepslate_bricks");
    /**
     * BlockData: {@link Stairs}
     */
    Typed<Stairs> DEEPSLATE_BRICK_STAIRS = getBlockType("deepslate_brick_stairs");
    /**
     * BlockData: {@link Slab}
     */
    Typed<Slab> DEEPSLATE_BRICK_SLAB = getBlockType("deepslate_brick_slab");
    /**
     * BlockData: {@link Wall}
     */
    Typed<Wall> DEEPSLATE_BRICK_WALL = getBlockType("deepslate_brick_wall");
    Typed<BlockData> CHISELED_DEEPSLATE = getBlockType("chiseled_deepslate");
    Typed<BlockData> CRACKED_DEEPSLATE_BRICKS = getBlockType("cracked_deepslate_bricks");
    Typed<BlockData> CRACKED_DEEPSLATE_TILES = getBlockType("cracked_deepslate_tiles");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> INFESTED_DEEPSLATE = getBlockType("infested_deepslate");
    Typed<BlockData> SMOOTH_BASALT = getBlockType("smooth_basalt");
    Typed<BlockData> RAW_IRON_BLOCK = getBlockType("raw_iron_block");
    Typed<BlockData> RAW_COPPER_BLOCK = getBlockType("raw_copper_block");
    Typed<BlockData> RAW_GOLD_BLOCK = getBlockType("raw_gold_block");
    Typed<BlockData> POTTED_AZALEA_BUSH = getBlockType("potted_azalea_bush");
    Typed<BlockData> POTTED_FLOWERING_AZALEA_BUSH = getBlockType("potted_flowering_azalea_bush");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> OCHRE_FROGLIGHT = getBlockType("ochre_froglight");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> VERDANT_FROGLIGHT = getBlockType("verdant_froglight");
    /**
     * BlockData: {@link Orientable}
     */
    Typed<Orientable> PEARLESCENT_FROGLIGHT = getBlockType("pearlescent_froglight");
    Typed<BlockData> FROGSPAWN = getBlockType("frogspawn");
    Typed<BlockData> REINFORCED_DEEPSLATE = getBlockType("reinforced_deepslate");
    /**
     * BlockData: {@link DecoratedPot}
     */
    Typed<DecoratedPot> DECORATED_POT = getBlockType("decorated_pot");
    /**
     * BlockData: {@link Crafter}
     */
    Typed<Crafter> CRAFTER = getBlockType("crafter");
    /**
     * BlockData: {@link TrialSpawner}
     */
    Typed<TrialSpawner> TRIAL_SPAWNER = getBlockType("trial_spawner");
    /**
     * BlockData: {@link Vault}
     */
    Typed<Vault> VAULT = getBlockType("vault");
    /**
     * BlockData: {@link Waterlogged}
     */
    Typed<Waterlogged> HEAVY_CORE = getBlockType("heavy_core");
    Typed<BlockData> OPEN_EYEBLOSSOM = getBlockType("open_eyeblossom");
    Typed<BlockData> CLOSED_EYEBLOSSOM = getBlockType("closed_eyeblossom");
    Typed<BlockData> POTTED_OPEN_EYEBLOSSOM = getBlockType("potted_open_eyeblossom");
    Typed<BlockData> POTTED_CLOSED_EYEBLOSSOM = getBlockType("potted_closed_eyeblossom");
    Typed<BlockData> FIREFLY_BUSH = getBlockType("firefly_bush");
    //</editor-fold>

    @NotNull
    private static <B extends BlockType> B getBlockType(@NotNull String key) {
        // Cast instead of using BlockType#typed, since block type can be a mock during testing and would return null
        return (B) Registry.BLOCK.getOrThrow(NamespacedKey.minecraft(key));
    }

    /**
     * Yields this block type as a typed version of itself with a plain {@link BlockData} representing it.
     *
     * @return the typed block type.
     */
    @NotNull
    BlockType.Typed<BlockData> typed();

    /**
     * Yields this block type as a typed version of itself with a specific {@link BlockData} representing it.
     *
     * @param blockDataType the class type of the {@link BlockData} to type this {@link BlockType} with.
     * @param <B>          the generic type of the block data to type this block type with.
     * @return the typed block type.
     */
    @NotNull
    <B extends BlockData> BlockType.Typed<B> typed(@NotNull Class<B> blockDataType);

    /**
     * Returns true if this BlockType has a corresponding {@link ItemType}.
     *
     * @return true if there is a corresponding ItemType, otherwise false
     * @see #getItemType()
     */
    boolean hasItemType();

    /**
     * Returns the corresponding {@link ItemType} for the given BlockType.
     * <p>
     * If there is no corresponding {@link ItemType} an error will be thrown.
     *
     * @return the corresponding ItemType
     * @see #hasItemType()
     * @see BlockData#getPlacementMaterial()
     */
    @NotNull
    ItemType getItemType();

    /**
     * Gets the BlockData class of this BlockType
     *
     * @return the BlockData class of this BlockType
     */
    @NotNull
    Class<? extends BlockData> getBlockDataClass();

    /**
     * Creates a new {@link BlockData} instance for this block type, with all
     * properties initialized to unspecified defaults.
     *
     * @return new data instance
     */
    @NotNull
    BlockData createBlockData();

    /**
     * Creates a new {@link BlockData} instance for this block type, with all
     * properties initialized to unspecified defaults, except for those provided
     * in data.
     *
     * @param data data string
     * @return new data instance
     * @throws IllegalArgumentException if the specified data is not valid
     */
    @NotNull
    BlockData createBlockData(@Nullable String data);

    /**
     * Check if the blockt type is solid (can be built upon)
     *
     * @return True if this block type is solid
     */
    boolean isSolid();

    /**
     * Check if the block type can catch fire
     *
     * @return True if this block type can catch fire
     */
    boolean isFlammable();

    /**
     * Check if the block type can burn away
     *
     * @return True if this block type can burn away
     */
    boolean isBurnable();

    /**
     * Check if the block type is occludes light in the lighting engine.
     * <p>
     * Generally speaking, most full blocks will occlude light. Non-full blocks are
     * not occluding (e.g. anvils, chests, tall grass, stairs, etc.), nor are specific
     * full blocks such as barriers or spawners which block light despite their texture.
     * <p>
     * An occluding block will have the following effects:
     * <ul>
     *   <li>Chests cannot be opened if an occluding block is above it.
     *   <li>Mobs cannot spawn inside of occluding blocks.
     *   <li>Only occluding blocks can be "powered" ({@link Block#isBlockPowered()}).
     * </ul>
     * This list may be inconclusive. For a full list of the side effects of an occluding
     * block, see the <a href="https://minecraft.fandom.com/wiki/Opacity">Minecraft Wiki</a>.
     *
     * @return True if this block type occludes light
     */
    boolean isOccluding();

    /**
     * @return True if this block type is affected by gravity.
     */
    boolean hasGravity();

    /**
     * Checks if this block type can be interacted with.
     * <p>
     * Interactable block types include those with functionality when they are
     * interacted with by a player such as chests, furnaces, etc.
     * <p>
     * Some blocks such as piston heads and stairs are considered interactable
     * though may not perform any additional functionality.
     * <p>
     * Note that the interactability of some block types may be dependant on their
     * state as well. This method will return true if there is at least one
     * state in which additional interact handling is performed for the
     * block type.
     *
     * @return true if this block type can be interacted with.
     */
    boolean isInteractable();

    /**
     * Obtains the block's hardness level (also known as "strength").
     * <br>
     * This number is used to calculate the time required to break each block.
     *
     * @return the hardness of that block type.
     */
    float getHardness();

    /**
     * Obtains the blast resistance value (also known as block "durability").
     * <br>
     * This value is used in explosions to calculate whether a block should be
     * broken or not.
     *
     * @return the blast resistance of that block type.
     */
    float getBlastResistance();

    /**
     * Returns a value that represents how 'slippery' the block is.
     * <p>
     * Blocks with higher slipperiness, like {@link BlockType#ICE} can be slid on
     * further by the player and other entities.
     * <p>
     * Most blocks have a default slipperiness of {@code 0.6f}.
     *
     * @return the slipperiness of this block
     */
    float getSlipperiness();

    /**
     * Check if the block type is an air block.
     *
     * @return True if this block type is an air block.
     */
    boolean isAir();

    /**
     * Gets if the BlockType is enabled by the features in a world.
     *
     * @param world the world to check
     * @return true if this BlockType can be used in this World.
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
     * Tries to convert this BlockType into a Material
     *
     * @return the converted Material or null
     * @deprecated only for internal use
     */
    @Nullable
    @Deprecated(since = "1.20.6")
    Material asMaterial();
}
