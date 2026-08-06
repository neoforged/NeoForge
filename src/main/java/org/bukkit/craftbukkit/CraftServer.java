package org.bukkit.craftbukkit;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.worldupdate.UpgradeProgress;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.phys.Vec3;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.ServerLinks;
import org.bukkit.ServerTickManager;
import org.bukkit.StructureType;
import org.bukkit.UnsafeValues;
import org.bukkit.Warning.WarningState;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.conversations.Conversable;
import org.bukkit.craftbukkit.ban.CraftIpBanList;
import org.bukkit.craftbukkit.ban.CraftProfileBanList;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.boss.CraftBossBar;
import org.bukkit.craftbukkit.boss.CraftKeyedBossbar;
import org.bukkit.craftbukkit.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.command.CraftCommandMap;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.entity.CraftEntityFactory;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.generator.OldCraftChunkData;
import org.bukkit.craftbukkit.help.SimpleHelpMap;
import org.bukkit.craftbukkit.inventory.CraftBlastingRecipe;
import org.bukkit.craftbukkit.inventory.CraftCampfireRecipe;
import org.bukkit.craftbukkit.inventory.CraftDyeRecipe;
import org.bukkit.craftbukkit.inventory.CraftFurnaceRecipe;
import org.bukkit.craftbukkit.inventory.CraftImbueRecipe;
import org.bukkit.craftbukkit.inventory.CraftItemCraftResult;
import org.bukkit.craftbukkit.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftMerchantCustom;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapedRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapelessRecipe;
import org.bukkit.craftbukkit.inventory.CraftSmithingTransformRecipe;
import org.bukkit.craftbukkit.inventory.CraftSmithingTrimRecipe;
import org.bukkit.craftbukkit.inventory.CraftSmokingRecipe;
import org.bukkit.craftbukkit.inventory.CraftStonecuttingRecipe;
import org.bukkit.craftbukkit.inventory.CraftTransmuteRecipe;
import org.bukkit.craftbukkit.inventory.RecipeIterator;
import org.bukkit.craftbukkit.inventory.util.CraftInventoryCreator;
import org.bukkit.craftbukkit.map.CraftMapColorCache;
import org.bukkit.craftbukkit.map.CraftMapView;
import org.bukkit.craftbukkit.metadata.EntityMetadataStore;
import org.bukkit.craftbukkit.metadata.PlayerMetadataStore;
import org.bukkit.craftbukkit.metadata.WorldMetadataStore;
import org.bukkit.craftbukkit.packs.CraftDataPackManager;
import org.bukkit.craftbukkit.packs.CraftResourcePack;
import org.bukkit.craftbukkit.profile.CraftPlayerProfile;
import org.bukkit.craftbukkit.profile.CraftPlayerSkinPatch;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.scoreboard.CraftCriteria;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager;
import org.bukkit.craftbukkit.structure.CraftStructureManager;
import org.bukkit.craftbukkit.tag.CraftBlockTag;
import org.bukkit.craftbukkit.tag.CraftDamageTag;
import org.bukkit.craftbukkit.tag.CraftEntityTag;
import org.bukkit.craftbukkit.tag.CraftFluidTag;
import org.bukkit.craftbukkit.tag.CraftItemTag;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftIconCache;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.CraftSpawnCategory;
import org.bukkit.craftbukkit.util.DatFileFilter;
import org.bukkit.craftbukkit.util.Versioning;
import org.bukkit.craftbukkit.util.permissions.CraftDefaultPermissions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.DyeRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ImbueRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemCraftResult;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.SmithingTrimRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.inventory.TransmuteRecipe;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapPalette;
import org.bukkit.packs.DataPackManager;
import org.bukkit.packs.ResourcePack;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.StringUtil;
import org.bukkit.util.permissions.DefaultPermissions;
import org.jline.terminal.Terminal;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;

public final class CraftServer implements Server {
    private final String serverVersion;
    private final String bukkitVersion = Versioning.getBukkitVersion();
    private final Logger logger = Logger.getLogger("Minecraft");
    private final ServicesManager servicesManager = new SimpleServicesManager();
    private final CraftScheduler scheduler = new CraftScheduler();
    private final CraftCommandMap commandMap = new CraftCommandMap(this);
    private final SimpleHelpMap helpMap = new SimpleHelpMap(this);
    private final StandardMessenger messenger = new StandardMessenger();
    private final SimplePluginManager pluginManager = new SimplePluginManager(this, commandMap);
    private final StructureManager structureManager;
    protected final DedicatedServer console;
    protected final DedicatedPlayerList playerList;
    private final Map<String, World> worlds = new LinkedHashMap<String, World>();
    private final Map<Class<?>, Registry<?>> registries = new HashMap<>();
    private YamlConfiguration configuration;
    private YamlConfiguration commandsConfiguration;
    private final Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
    private final Map<UUID, OfflinePlayer> offlinePlayers = new MapMaker().weakValues().makeMap();
    private final EntityMetadataStore entityMetadata = new EntityMetadataStore();
    private final PlayerMetadataStore playerMetadata = new PlayerMetadataStore();
    private final WorldMetadataStore worldMetadata = new WorldMetadataStore();
    private final Object2IntOpenHashMap<SpawnCategory> spawnCategoryLimit = new Object2IntOpenHashMap<>();
    private File container;
    private WarningState warningState = WarningState.DEFAULT;
    public ApiVersion minimumAPI;
    public CraftScoreboardManager scoreboardManager;
    public CraftDataPackManager dataPackManager;
    private CraftServerTickManager serverTickManager;
    private CraftServerLinks serverLinks;
    private final CraftEntityFactory entityFactory;
    public boolean playerCommandState;
    private boolean printSaveWarning;
    private CraftIconCache icon;
    private boolean overrideAllCommandBlockCommands = false;
    public boolean ignoreVanillaPermissions = false;
    private final List<CraftPlayer> playerView;
    public int reloadCount;
    public Set<String> activeCompatibilities = Collections.emptySet();

    static {
        ConfigurationSerialization.registerClass(CraftOfflinePlayer.class);
        ConfigurationSerialization.registerClass(CraftPlayerProfile.class);
        ConfigurationSerialization.registerClass(CraftPlayerSkinPatch.class);
        CraftItemFactory.instance();
    }

    public CraftServer(DedicatedServer console, PlayerList playerList) {
        this.console = console;
        this.playerList = (DedicatedPlayerList) playerList;
        this.playerView = Collections.unmodifiableList(Lists.transform(playerList.players, new Function<ServerPlayer, CraftPlayer>() {
            @Override
            public CraftPlayer apply(ServerPlayer player) {
                return player.getBukkitEntity();
            }
        }));
        this.serverVersion = CraftServer.class.getPackage().getImplementationVersion();
        this.structureManager = new CraftStructureManager(console.getStructureManager(), console.registryAccess());
        this.dataPackManager = new CraftDataPackManager(this.getServer().getPackRepository());
        this.serverTickManager = new CraftServerTickManager(console.tickRateManager());
        this.serverLinks = new CraftServerLinks(console);
        this.entityFactory = new CraftEntityFactory(console.registryAccess());

        Bukkit.setServer(this);

        CraftRegistry.setMinecraftRegistry(console.registryAccess());

        if (!Main.useConsole) {
            getLogger().info("Console input is disabled due to --noconsole command argument");
        }

        configuration = YamlConfiguration.loadConfiguration(getConfigFile());
        configuration.options().copyDefaults(true);
        configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("configurations/bukkit.yml"), Charsets.UTF_8)));
        ConfigurationSection legacyAlias = null;
        if (!configuration.isString("aliases")) {
            legacyAlias = configuration.getConfigurationSection("aliases");
            configuration.set("aliases", "now-in-commands.yml");
        }
        saveConfig();
        if (getCommandsConfigFile().isFile()) {
            legacyAlias = null;
        }
        commandsConfiguration = YamlConfiguration.loadConfiguration(getCommandsConfigFile());
        commandsConfiguration.options().copyDefaults(true);
        commandsConfiguration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("configurations/commands.yml"), Charsets.UTF_8)));
        saveCommandsConfig();

        // Migrate aliases from old file and add previously implicit $1- to pass all arguments
        if (legacyAlias != null) {
            ConfigurationSection aliases = commandsConfiguration.createSection("aliases");
            for (String key : legacyAlias.getKeys(false)) {
                ArrayList<String> commands = new ArrayList<String>();

                if (legacyAlias.isList(key)) {
                    for (String command : legacyAlias.getStringList(key)) {
                        commands.add(command + " $1-");
                    }
                } else {
                    commands.add(legacyAlias.getString(key) + " $1-");
                }

                aliases.set(key, commands);
            }
        }

        saveCommandsConfig();
        overrideAllCommandBlockCommands = commandsConfiguration.getStringList("command-block-overrides").contains("*");
        ignoreVanillaPermissions = commandsConfiguration.getBoolean("ignore-vanilla-permissions");
        pluginManager.useTimings(configuration.getBoolean("settings.plugin-profiling"));
        overrideSpawnLimits();
        console.autosavePeriod = configuration.getInt("ticks-per.autosave");
        warningState = WarningState.value(configuration.getString("settings.deprecated-verbose"));
        TicketType.pluginTimeout = configuration.getInt("chunk-gc.period-in-ticks");
        minimumAPI = ApiVersion.getOrCreateVersion(configuration.getString("settings.minimum-api"));
        loadIcon();
        loadCompatibilities();
        CraftMagicNumbers.INSTANCE.getCommodore().updateReroute(activeCompatibilities::contains);

        // Set map color cache
        if (configuration.getBoolean("settings.use-map-color-cache")) {
            MapPalette.setMapColorCache(new CraftMapColorCache(logger));
        }
    }

    public boolean getCommandBlockOverride(String command) {
        return overrideAllCommandBlockCommands || commandsConfiguration.getStringList("command-block-overrides").contains(command);
    }

    private File getConfigFile() {
        return (File) console.options.valueOf("bukkit-settings");
    }

    private File getCommandsConfigFile() {
        return (File) console.options.valueOf("commands-settings");
    }

    private void overrideSpawnLimits() {
        for (SpawnCategory spawnCategory : SpawnCategory.values()) {
            if (CraftSpawnCategory.isValidForLimits(spawnCategory)) {
                spawnCategoryLimit.put(spawnCategory, configuration.getInt(CraftSpawnCategory.getConfigNameSpawnLimit(spawnCategory)));
            }
        }
    }

    private void saveConfig() {
        try {
            configuration.save(getConfigFile());
        } catch (IOException ex) {
            Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, "Could not save " + getConfigFile(), ex);
        }
    }

    private void saveCommandsConfig() {
        try {
            commandsConfiguration.save(getCommandsConfigFile());
        } catch (IOException ex) {
            Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, "Could not save " + getCommandsConfigFile(), ex);
        }
    }

    private void loadCompatibilities() {
        ConfigurationSection compatibilities = configuration.getConfigurationSection("settings.compatibility");
        if (compatibilities == null) {
            activeCompatibilities = Collections.emptySet();
            return;
        }

        activeCompatibilities = compatibilities
                .getKeys(false)
                .stream()
                .filter(compatibilities::getBoolean)
                .collect(Collectors.toSet());

        if (!activeCompatibilities.isEmpty()) {
            logger.info("Using following compatibilities: `" + Joiner.on("`, `").join(activeCompatibilities) + "`, this will affect performance and other plugins behavior.");
            logger.info("Only use when necessary and prefer updating plugins if possible.");
        }

        if (activeCompatibilities.contains("enum-compatibility-mode")) {
            getLogger().warning("Loading plugins in enum compatibility mode. This will affect plugin performance. Use only as a transition period or when absolutely necessary.");
        } else if (System.getProperty("RemoveEnumBanner") == null) {
            // TODO 2024-06-16: Remove in newer version
            getLogger().info("*** This version of Spigot contains changes to some enums. If you notice that plugins no longer work after updating, please report this to the developers of those plugins first. ***");
            getLogger().info("*** If you cannot update those plugins, you can try setting `settings.compatibility.enum-compatibility-mode` to `true` in `bukkit.yml`. ***");
        }
    }

    public void loadPlugins() {
        pluginManager.registerInterface(JavaPluginLoader.class);

        File pluginFolder = (File) console.options.valueOf("plugins");

        if (pluginFolder.exists()) {
            Plugin[] plugins = pluginManager.loadPlugins(pluginFolder);
            for (Plugin plugin : plugins) {
                try {
                    String message = String.format("Loading %s", plugin.getDescription().getFullName());
                    plugin.getLogger().info(message);
                    plugin.onLoad();
                } catch (Throwable ex) {
                    Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " initializing " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
                }
            }
        } else {
            pluginFolder.mkdir();
        }
    }

    public void enablePlugins(PluginLoadOrder type) {
        if (type == PluginLoadOrder.STARTUP) {
            helpMap.clear();
            helpMap.initializeGeneralTopics();
        }

        Plugin[] plugins = pluginManager.getPlugins();

        for (Plugin plugin : plugins) {
            if ((!plugin.isEnabled()) && (plugin.getDescription().getLoad() == type)) {
                enablePlugin(plugin);
            }
        }

        if (type == PluginLoadOrder.POSTWORLD) {
            commandMap.setFallbackCommands();
            setVanillaCommands();
            commandMap.registerServerAliases();
            DefaultPermissions.registerCorePermissions();
            CraftDefaultPermissions.registerCorePermissions();
            loadCustomPermissions();
            helpMap.initializeCommands();
            syncCommands();
        }
    }

    public void disablePlugins() {
        pluginManager.disablePlugins();
    }

    private void setVanillaCommands() {
        Commands dispatcher = console.vanillaCommandDispatcher;

        // Build a list of all Vanilla commands and create wrappers
        for (CommandNode<CommandSourceStack> cmd : dispatcher.getDispatcher().getRoot().getChildren()) {
            commandMap.register("minecraft", new VanillaCommandWrapper(dispatcher, cmd));
        }
    }

    public void syncCommands() {
        // Clear existing commands
        Commands dispatcher = console.resources.managers().commands = new Commands();

        // Register all commands, vanilla ones will be using the old dispatcher references
        for (Map.Entry<String, Command> entry : commandMap.getKnownCommands().entrySet()) {
            String label = entry.getKey();
            Command command = entry.getValue();

            if (command instanceof VanillaCommandWrapper) {
                LiteralCommandNode<CommandSourceStack> node = (LiteralCommandNode<CommandSourceStack>) ((VanillaCommandWrapper) command).vanillaCommand;
                if (!node.getLiteral().equals(label)) {
                    LiteralCommandNode<CommandSourceStack> clone = new LiteralCommandNode(label, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());

                    for (CommandNode<CommandSourceStack> child : node.getChildren()) {
                        clone.addChild(child);
                    }
                    node = clone;
                }

                dispatcher.getDispatcher().getRoot().addChild(node);
            } else {
                new BukkitCommandWrapper(this, entry.getValue()).register(dispatcher.getDispatcher(), label);
            }
        }

        // Refresh commands
        for (ServerPlayer player : getHandle().players) {
            dispatcher.sendCommands(player);
        }
    }

    private void enablePlugin(Plugin plugin) {
        try {
            List<Permission> perms = plugin.getDescription().getPermissions();

            for (Permission perm : perms) {
                try {
                    pluginManager.addPermission(perm, false);
                } catch (IllegalArgumentException ex) {
                    getLogger().log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName() + " tried to register permission '" + perm.getName() + "' but it's already registered", ex);
                }
            }
            pluginManager.dirtyPermissibles();

            pluginManager.enablePlugin(plugin);
        } catch (Throwable ex) {
            Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " loading " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

    @Override
    public String getName() {
        return console.getServerModName();
    }

    @Override
    public String getVersion() {
        return serverVersion + " (MC: " + console.getServerVersion() + ")";
    }

    @Override
    public String getBukkitVersion() {
        return bukkitVersion;
    }

    @Override
    public List<CraftPlayer> getOnlinePlayers() {
        return this.playerView;
    }

    @Override
    @Deprecated
    public Player getPlayer(final String name) {
        Preconditions.checkArgument(name != null, "name cannot be null");

        Player found = getPlayerExact(name);
        // Try for an exact match first.
        if (found != null) {
            return found;
        }

        String lowerName = name.toLowerCase(Locale.ROOT);
        int delta = Integer.MAX_VALUE;
        for (Player player : getOnlinePlayers()) {
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(lowerName)) {
                int curDelta = Math.abs(player.getName().length() - lowerName.length());
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }
                if (curDelta == 0) break;
            }
        }
        return found;
    }

    @Override
    @Deprecated
    public Player getPlayerExact(String name) {
        Preconditions.checkArgument(name != null, "name cannot be null");

        ServerPlayer player = playerList.getPlayerByName(name);
        return (player != null) ? player.getBukkitEntity() : null;
    }

    @Override
    public Player getPlayer(UUID id) {
        Preconditions.checkArgument(id != null, "UUID id cannot be null");

        ServerPlayer player = playerList.getPlayer(id);
        if (player != null) {
            return player.getBukkitEntity();
        }

        return null;
    }

    @Override
    public int broadcastMessage(String message) {
        return broadcast(message, BROADCAST_CHANNEL_USERS);
    }

    @Override
    @Deprecated
    public List<Player> matchPlayer(String partialName) {
        Preconditions.checkArgument(partialName != null, "partialName cannot be null");

        List<Player> matchedPlayers = new ArrayList<>();

        for (Player iterPlayer : this.getOnlinePlayers()) {
            String iterPlayerName = iterPlayer.getName();

            if (partialName.equalsIgnoreCase(iterPlayerName)) {
                // Exact match
                matchedPlayers.clear();
                matchedPlayers.add(iterPlayer);
                break;
            }
            if (iterPlayerName.toLowerCase(Locale.ROOT).contains(partialName.toLowerCase(Locale.ROOT))) {
                // Partial match
                matchedPlayers.add(iterPlayer);
            }
        }

        return matchedPlayers;
    }

    @Override
    public int getMaxPlayers() {
        return playerList.getMaxPlayers();
    }

    @Override
    public void setMaxPlayers(int maxPlayers) {
        Preconditions.checkArgument(maxPlayers >= 0, "maxPlayers must be >= 0");

        console.setMaxPlayers(maxPlayers);
    }

    // NOTE: These are dependent on the corresponding call in MinecraftServer
    // so if that changes this will need to as well
    @Override
    public int getPort() {
        return this.getServer().getPort();
    }

    @Override
    public int getViewDistance() {
        return console.viewDistance();
    }

    @Override
    public int getSimulationDistance() {
        return console.simulationDistance();
    }

    @Override
    public String getIp() {
        return this.getServer().getLocalIp();
    }

    @Override
    public String getWorldType() {
        return this.getProperties().properties.getProperty("level-type");
    }

    @Override
    public boolean getGenerateStructures() {
        return this.getServer().overworld().getWorldGenSettings().options().generateStructures();
    }

    @Override
    public int getMaxWorldSize() {
        return this.getProperties().maxWorldSize;
    }

    @Override
    public boolean getAllowEnd() {
        return this.configuration.getBoolean("settings.allow-end");
    }

    @Override
    public boolean getAllowNether() {
        return this.configuration.getBoolean("settings.allow-nether");
    }

    @Override
    public boolean isLoggingIPs() {
        return this.getServer().logIPs();
    }

    public boolean getWarnOnOverload() {
        return this.configuration.getBoolean("settings.warn-on-overload");
    }

    public boolean getQueryPlugins() {
        return this.configuration.getBoolean("settings.query-plugins");
    }

    @Override
    public List<String> getInitialEnabledPacks() {
        return Collections.unmodifiableList(this.getProperties().initialDataPackConfiguration.getEnabled());
    }

    @Override
    public List<String> getInitialDisabledPacks() {
        return Collections.unmodifiableList(this.getProperties().initialDataPackConfiguration.getDisabled());
    }

    @Override
    public DataPackManager getDataPackManager() {
        return this.dataPackManager;
    }

    @Override
    public ServerTickManager getServerTickManager() {
        return this.serverTickManager;
    }

    @Override
    public Map<String, String> getCodeOfConducts() {
        return this.getServer().getCodeOfConducts();
    }

    @Override
    public ResourcePack getServerResourcePack() {
        return this.getServer().getServerResourcePack().map(CraftResourcePack::new).orElse(null);
    }

    @Override
    public String getResourcePack() {
        return this.getServer().getServerResourcePack().map(MinecraftServer.ServerResourcePackInfo::url).orElse("");
    }

    @Override
    public String getResourcePackHash() {
        return this.getServer().getServerResourcePack().map(MinecraftServer.ServerResourcePackInfo::hash).orElse("").toUpperCase(Locale.ROOT);
    }

    @Override
    public String getResourcePackPrompt() {
        return this.getServer().getServerResourcePack().map(MinecraftServer.ServerResourcePackInfo::prompt).map(CraftChatMessage::fromComponent).orElse("");
    }

    @Override
    public boolean isResourcePackRequired() {
        return this.getServer().isResourcePackRequired();
    }

    @Override
    public boolean hasWhitelist() {
        return this.getProperties().whiteList.get();
    }

    // NOTE: Temporary calls through to server.properies until its replaced
    private DedicatedServerProperties getProperties() {
        return this.console.getProperties();
    }
    // End Temporary calls

    @Override
    public String getUpdateFolder() {
        return this.configuration.getString("settings.update-folder", "update");
    }

    @Override
    public File getUpdateFolderFile() {
        return new File((File) console.options.valueOf("plugins"), this.configuration.getString("settings.update-folder", "update"));
    }

    @Override
    public long getConnectionThrottle() {
        return this.configuration.getInt("settings.connection-throttle");
    }

    @Override
    @Deprecated
    public int getTicksPerAnimalSpawns() {
        return getTicksPerSpawns(SpawnCategory.ANIMAL);
    }

    @Override
    @Deprecated
    public int getTicksPerMonsterSpawns() {
        return getTicksPerSpawns(SpawnCategory.MONSTER);
    }

    @Override
    @Deprecated
    public int getTicksPerWaterSpawns() {
        return getTicksPerSpawns(SpawnCategory.WATER_ANIMAL);
    }

    @Override
    @Deprecated
    public int getTicksPerWaterAmbientSpawns() {
        return getTicksPerSpawns(SpawnCategory.WATER_AMBIENT);
    }

    @Override
    @Deprecated
    public int getTicksPerWaterUndergroundCreatureSpawns() {
        return getTicksPerSpawns(SpawnCategory.WATER_UNDERGROUND_CREATURE);
    }

    @Override
    @Deprecated
    public int getTicksPerAmbientSpawns() {
        return getTicksPerSpawns(SpawnCategory.AMBIENT);
    }

    @Override
    public int getTicksPerSpawns(SpawnCategory spawnCategory) {
        Preconditions.checkArgument(spawnCategory != null, "SpawnCategory cannot be null");
        Preconditions.checkArgument(CraftSpawnCategory.isValidForLimits(spawnCategory), "SpawnCategory.%s are not supported", spawnCategory);
        return this.configuration.getInt(CraftSpawnCategory.getConfigNameTicksPerSpawn(spawnCategory));
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public CraftScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    @Override
    public List<World> getWorlds() {
        return new ArrayList<World>(worlds.values());
    }

    public DedicatedPlayerList getHandle() {
        return playerList;
    }

    // NOTE: Should only be called from DedicatedServer.ah()
    public boolean dispatchServerCommand(CommandSender sender, ConsoleInput serverCommand) {
        if (sender instanceof Conversable) {
            Conversable conversable = (Conversable) sender;

            if (conversable.isConversing()) {
                conversable.acceptConversationInput(serverCommand.msg);
                return true;
            }
        }
        try {
            this.playerCommandState = true;
            return dispatchCommand(sender, serverCommand.msg);
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Unexpected exception while parsing console command \"" + serverCommand.msg + '"', ex);
            return false;
        } finally {
            this.playerCommandState = false;
        }
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) {
        Preconditions.checkArgument(sender != null, "sender cannot be null");
        Preconditions.checkArgument(commandLine != null, "commandLine cannot be null");

        if (commandMap.dispatch(sender, commandLine)) {
            return true;
        }

        if (sender instanceof Player) {
            sender.sendMessage("Unknown command. Type \"/help\" for help.");
        } else {
            sender.sendMessage("Unknown command. Type \"help\" for help.");
        }

        return false;
    }

    @Override
    public void reload() {
        reloadCount++;
        configuration = YamlConfiguration.loadConfiguration(getConfigFile());
        commandsConfiguration = YamlConfiguration.loadConfiguration(getCommandsConfigFile());

        console.settings = new DedicatedServerSettings(console.options);
        DedicatedServerProperties config = console.settings.getProperties();

        overrideSpawnLimits();
        warningState = WarningState.value(configuration.getString("settings.deprecated-verbose"));
        TicketType.pluginTimeout = configuration.getInt("chunk-gc.period-in-ticks");
        minimumAPI = ApiVersion.getOrCreateVersion(configuration.getString("settings.minimum-api"));
        printSaveWarning = false;
        console.autosavePeriod = configuration.getInt("ticks-per.autosave");
        loadIcon();
        loadCompatibilities();
        CraftMagicNumbers.INSTANCE.getCommodore().updateReroute(activeCompatibilities::contains);

        try {
            playerList.getIpBans().load();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to load banned-ips.json, " + ex.getMessage());
        }
        try {
            playerList.getBans().load();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to load banned-players.json, " + ex.getMessage());
        }

        for (ServerLevel world : console.getAllLevels()) {
            world.serverLevelData.setDifficulty(config.difficulty.get());

            for (SpawnCategory spawnCategory : SpawnCategory.values()) {
                if (CraftSpawnCategory.isValidForLimits(spawnCategory)) {
                    long ticksPerCategorySpawn = this.getTicksPerSpawns(spawnCategory);
                    if (ticksPerCategorySpawn < 0) {
                        world.ticksPerSpawnCategory.put(spawnCategory, CraftSpawnCategory.getDefaultTicksPerSpawn(spawnCategory));
                    } else {
                        world.ticksPerSpawnCategory.put(spawnCategory, ticksPerCategorySpawn);
                    }
                }
            }
        }

        pluginManager.clearPlugins();
        commandMap.clearCommands();
        reloadData();
        overrideAllCommandBlockCommands = commandsConfiguration.getStringList("command-block-overrides").contains("*");
        ignoreVanillaPermissions = commandsConfiguration.getBoolean("ignore-vanilla-permissions");

        int pollCount = 0;

        // Wait for at most 2.5 seconds for plugins to close their threads
        while (pollCount < 50 && getScheduler().getActiveWorkers().size() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
            pollCount++;
        }

        List<BukkitWorker> overdueWorkers = getScheduler().getActiveWorkers();
        for (BukkitWorker worker : overdueWorkers) {
            Plugin plugin = worker.getOwner();
            getLogger().log(Level.SEVERE, String.format(
                "Nag author(s): '%s' of '%s' about the following: %s",
                plugin.getDescription().getAuthors(),
                plugin.getDescription().getFullName(),
                "This plugin is not properly shutting down its async tasks when it is being reloaded.  This may cause conflicts with the newly loaded version of the plugin"
            ));
        }
        loadPlugins();
        enablePlugins(PluginLoadOrder.STARTUP);
        enablePlugins(PluginLoadOrder.POSTWORLD);
        getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.RELOAD));
    }

    @Override
    public void reloadData() {
        ReloadCommand.reload(console);
    }

    private void loadIcon() {
        icon = new CraftIconCache(null);
        try {
            final File file = new File(new File("."), "server-icon.png");
            if (file.isFile()) {
                icon = loadServerIcon0(file);
            }
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Couldn't load server icon", ex);
        }
    }

    @SuppressWarnings({ "unchecked", "finally" })
    private void loadCustomPermissions() {
        File file = new File(configuration.getString("settings.permissions-file"));
        FileInputStream stream;

        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            try {
                file.createNewFile();
            } finally {
                return;
            }
        }

        Map<String, Map<String, Object>> perms;

        try {
            perms = (Map<String, Map<String, Object>>) yaml.load(stream);
        } catch (MarkedYAMLException ex) {
            getLogger().log(Level.WARNING, "Server permissions file " + file + " is not valid YAML: " + ex.toString());
            return;
        } catch (Throwable ex) {
            getLogger().log(Level.WARNING, "Server permissions file " + file + " is not valid YAML.", ex);
            return;
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {}
        }

        if (perms == null) {
            getLogger().log(Level.INFO, "Server permissions file " + file + " is empty, ignoring it");
            return;
        }

        List<Permission> permsList = Permission.loadPermissions(perms, "Permission node '%s' in " + file + " is invalid", Permission.DEFAULT_PERMISSION);

        for (Permission perm : permsList) {
            try {
                pluginManager.addPermission(perm);
            } catch (IllegalArgumentException ex) {
                getLogger().log(Level.SEVERE, "Permission in " + file + " was already defined", ex);
            }
        }
    }

    @Override
    public String toString() {
        return "CraftServer{" + "serverName=" + getName() + ",serverVersion=" + serverVersion + ",minecraftVersion=" + console.getServerVersion() + '}';
    }

    public World createWorld(String name, Environment environment) {
        return WorldCreator.name(name).environment(environment).createWorld();
    }

    public World createWorld(String name, Environment environment, long seed) {
        return WorldCreator.name(name).environment(environment).seed(seed).createWorld();
    }

    public World createWorld(String name, Environment environment, ChunkGenerator generator) {
        return WorldCreator.name(name).environment(environment).generator(generator).createWorld();
    }

    public World createWorld(String name, Environment environment, long seed, ChunkGenerator generator) {
        return WorldCreator.name(name).environment(environment).seed(seed).generator(generator).createWorld();
    }

    @Override
    public World createWorld(WorldCreator creator) {
        Preconditions.checkState(console.getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");
        Preconditions.checkArgument(creator != null, "WorldCreator cannot be null");

        String name = creator.name();
        ChunkGenerator generator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        File folder = new File(getWorldContainer(), name);
        World world = getWorld(name);

        if (world != null) {
            return world;
        }

        if (folder.exists()) {
            Preconditions.checkArgument(folder.isDirectory(), "File (%s) exists and isn't a folder", name);
        }

        if (generator == null) {
            generator = getGenerator(name);
        }

        if (biomeProvider == null) {
            biomeProvider = getBiomeProvider(name);
        }

        ResourceKey<LevelStem> actualDimension;
        switch (creator.environment()) {
            case NORMAL:
                actualDimension = LevelStem.OVERWORLD;
                break;
            case NETHER:
                actualDimension = LevelStem.NETHER;
                break;
            case THE_END:
                actualDimension = LevelStem.END;
                break;
            default:
                throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
        }

        LevelStorageSource.LevelStorageAccess worldSession;
        try {
            worldSession = LevelStorageSource.createDefault(getWorldContainer().toPath()).validateAndCreateAccess(name, actualDimension);
        } catch (IOException | ContentValidationException ex) {
            throw new RuntimeException(ex);
        }

        Dynamic<?> dynamic;
        if (worldSession.hasWorldData()) {
            Dynamic<?> dynamic1;

            try {
                dynamic1 = worldSession.getUnfixedDataTagWithFallback();
            } catch (NbtException | ReportedNbtException | IOException ioexception) {
                MinecraftServer.LOGGER.error("Failed to load world data. World files may be corrupted. Shutting down.", ioexception);
                return null;
            }

            LevelSummary levelsummary = worldSession.fixAndGetSummaryFromTag(dynamic1);

            if (levelsummary.requiresManualConversion()) {
                MinecraftServer.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                return null;
            }

            if (!levelsummary.isCompatible()) {
                MinecraftServer.LOGGER.info("This world was created by an incompatible version.");
                return null;
            }

            dynamic = DataFixers.getFileFixer().fix(worldSession, dynamic1, new UpgradeProgress());
        } else {
            dynamic = null;
        }

        boolean hardcore = creator.hardcore();

        LevelDataAndDimensions.WorldDataAndGenSettings dataAndSettings;
        WorldLoader.DataLoadContext worldloader_dataloadcontext = console.worldLoader;
        RegistryAccess.Frozen registryaccess_frozen = worldloader_dataloadcontext.datapackDimensions();
        net.minecraft.core.Registry<LevelStem> registry = registryaccess_frozen.lookupOrThrow(Registries.LEVEL_STEM);
        if (dynamic != null) {
            LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(worldSession, dynamic, worldloader_dataloadcontext.dataConfiguration(), registry, worldloader_dataloadcontext.datapackWorldgen());

            dataAndSettings = leveldataanddimensions.worldDataAndGenSettings();
            registryaccess_frozen = leveldataanddimensions.dimensions().dimensionsRegistryAccess();
        } else {
            LevelSettings levelsettings;
            WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
            WorldDimensions worlddimensions;

            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()), creator.type().name().toLowerCase(Locale.ROOT));

            levelsettings = new LevelSettings(name, GameType.byId(getDefaultGameMode().getValue()), new LevelSettings.DifficultySettings(Difficulty.EASY, hardcore, false), false, worldloader_dataloadcontext.dataConfiguration());
            worlddimensions = properties.create(worldloader_dataloadcontext.datapackWorldgen());

            WorldDimensions.Complete worlddimensions_complete = worlddimensions.bake(registry);
            Lifecycle lifecycle = worlddimensions_complete.lifecycle().add(worldloader_dataloadcontext.datapackWorldgen().allRegistriesLifecycle());

            PrimaryLevelData primaryleveldata = new PrimaryLevelData(levelsettings, worlddimensions_complete.specialWorldProperty(), lifecycle);
            dataAndSettings = new LevelDataAndDimensions.WorldDataAndGenSettings(primaryleveldata, new WorldGenSettings(worldoptions, worlddimensions));

            registryaccess_frozen = worlddimensions_complete.dimensionsRegistryAccess();
        }
        registry = registryaccess_frozen.lookupOrThrow(Registries.LEVEL_STEM);
        PrimaryLevelData serverleveldata = (PrimaryLevelData) dataAndSettings.data();
        serverleveldata.customDimensions = registry;
        serverleveldata.checkName(name);
        serverleveldata.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());

        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.forceUpgrade(worldSession, DataFixers.getDataFixer(), console.options.has("eraseCache"), () -> true, registryaccess_frozen, console.options.has("recreateRegionFiles"));
        }
        SavedDataStorage dimensiondatastorage = new SavedDataStorage(worldSession.getLevelPath(LevelResource.DATA), console.getFixerUpper(), console.registryAccess());

        WorldOptions worldoptions = dataAndSettings.genSettings().options();
        long j = BiomeManager.obfuscateSeed(creator.seed());
        List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(dimensiondatastorage));
        LevelStem levelstem = registry.getValue(actualDimension);

        WorldInfo worldInfo = new CraftWorldInfo(serverleveldata, worldoptions, worldSession, creator.environment(), levelstem.type().value());
        if (biomeProvider == null && generator != null) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
        }

        ResourceKey<net.minecraft.world.level.Level> worldKey;
        String levelName = this.getServer().getProperties().levelName;
        if (name.equals(levelName + "_nether")) {
            worldKey = net.minecraft.world.level.Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            worldKey = net.minecraft.world.level.Level.END;
        } else {
            worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.withDefaultNamespace(name.toLowerCase(Locale.ROOT)));
        }

        ServerLevel internal = (ServerLevel) new ServerLevel(console, console.executor, worldSession, serverleveldata, worldKey, levelstem,
                serverleveldata.isDebugWorld(), j, creator.environment() == Environment.NORMAL ? list : ImmutableList.of(), true, dimensiondatastorage, dataAndSettings.genSettings(), creator.environment(), generator, biomeProvider);

        if (!(worlds.containsKey(name.toLowerCase(Locale.ROOT)))) {
            return null;
        }

        console.initWorld(internal, serverleveldata, serverleveldata, worldoptions);

        internal.setSpawnSettings(true);
        console.addLevel(internal);

        getServer().prepareLevels(internal);
        internal.entityManager.tick(); // SPIGOT-6526: Load pending entities so they are available to the API

        pluginManager.callEvent(new WorldLoadEvent(internal.getWorld()));
        return internal.getWorld();
    }

    @Override
    public boolean unloadWorld(String name, boolean save) {
        return unloadWorld(getWorld(name), save);
    }

    @Override
    public boolean unloadWorld(World world, boolean save) {
        if (world == null) {
            return false;
        }

        ServerLevel handle = ((CraftWorld) world).getHandle();

        if (console.getLevel(handle.dimension()) == null) {
            return false;
        }

        if (handle.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
            return false;
        }

        if (handle.players().size() > 0) {
            return false;
        }

        WorldUnloadEvent e = new WorldUnloadEvent(handle.getWorld());
        pluginManager.callEvent(e);

        if (e.isCancelled()) {
            return false;
        }

        try {
            if (save) {
                handle.save(null, true, false);
            }

            handle.getChunkSource().close(save);
            handle.entityManager.close(save); // SPIGOT-6722: close entityManager
            handle.storageSource.close();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        worlds.remove(world.getName().toLowerCase(Locale.ROOT));
        console.removeLevel(handle);
        return true;
    }

    public DedicatedServer getServer() {
        return console;
    }

    @Override
    public World getWorld(String name) {
        Preconditions.checkArgument(name != null, "name cannot be null");

        return worlds.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public World getWorld(UUID uid) {
        for (World world : worlds.values()) {
            if (world.getUID().equals(uid)) {
                return world;
            }
        }
        return null;
    }

    public void addWorld(World world) {
        // Check if a World already exists with the UID.
        if (getWorld(world.getUID()) != null) {
            System.out.println("World " + world.getName() + " is a duplicate of another world and has been prevented from loading. Please delete the uid.dat file from " + world.getName() + "'s world directory if you want to be able to load the duplicate world.");
            return;
        }
        worlds.put(world.getName().toLowerCase(Locale.ROOT), world);
    }

    @Override
    public WorldBorder createWorldBorder() {
        return new CraftWorldBorder(new net.minecraft.world.level.border.WorldBorder());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public Terminal getTerminal() {
        return console.terminal;
    }

    @Override
    public PluginCommand getPluginCommand(String name) {
        Command command = commandMap.getCommand(name);

        if (command instanceof PluginCommand) {
            return (PluginCommand) command;
        } else {
            return null;
        }
    }

    @Override
    public void savePlayers() {
        checkSaveState();
        playerList.saveAll();
    }

    @Override
    public boolean addRecipe(Recipe recipe) {
        CraftRecipe toAdd;
        if (recipe instanceof CraftRecipe) {
            toAdd = (CraftRecipe) recipe;
        } else {
            if (recipe instanceof ShapedRecipe) {
                toAdd = CraftShapedRecipe.fromBukkitRecipe((ShapedRecipe) recipe);
            } else if (recipe instanceof ShapelessRecipe) {
                toAdd = CraftShapelessRecipe.fromBukkitRecipe((ShapelessRecipe) recipe);
            } else if (recipe instanceof FurnaceRecipe) {
                toAdd = CraftFurnaceRecipe.fromBukkitRecipe((FurnaceRecipe) recipe);
            } else if (recipe instanceof BlastingRecipe) {
                toAdd = CraftBlastingRecipe.fromBukkitRecipe((BlastingRecipe) recipe);
            } else if (recipe instanceof CampfireRecipe) {
                toAdd = CraftCampfireRecipe.fromBukkitRecipe((CampfireRecipe) recipe);
            } else if (recipe instanceof SmokingRecipe) {
                toAdd = CraftSmokingRecipe.fromBukkitRecipe((SmokingRecipe) recipe);
            } else if (recipe instanceof StonecuttingRecipe) {
                toAdd = CraftStonecuttingRecipe.fromBukkitRecipe((StonecuttingRecipe) recipe);
            } else if (recipe instanceof SmithingTransformRecipe) {
                toAdd = CraftSmithingTransformRecipe.fromBukkitRecipe((SmithingTransformRecipe) recipe);
            } else if (recipe instanceof SmithingTrimRecipe) {
                toAdd = CraftSmithingTrimRecipe.fromBukkitRecipe((SmithingTrimRecipe) recipe);
            } else if (recipe instanceof TransmuteRecipe) {
                toAdd = CraftTransmuteRecipe.fromBukkitRecipe((TransmuteRecipe) recipe);
            } else if (recipe instanceof DyeRecipe) {
                toAdd = CraftDyeRecipe.fromBukkitRecipe((DyeRecipe) recipe);
            } else if (recipe instanceof ImbueRecipe) {
                toAdd = CraftImbueRecipe.fromBukkitRecipe((ImbueRecipe) recipe);
            } else if (recipe instanceof ComplexRecipe) {
                throw new UnsupportedOperationException("Cannot add custom complex recipe");
            } else {
                return false;
            }
        }
        toAdd.addToCraftingManager();
        return true;
    }

    @Override
    public List<Recipe> getRecipesFor(ItemStack result) {
        Preconditions.checkArgument(result != null, "ItemStack cannot be null");

        List<Recipe> results = new ArrayList<Recipe>();
        Iterator<Recipe> iter = recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            ItemStack stack = recipe.getResult();
            if (stack.getType() != result.getType()) {
                continue;
            }
            if (result.getDurability() == -1 || result.getDurability() == stack.getDurability()) {
                results.add(recipe);
            }
        }
        return results;
    }

    @Override
    public Recipe getRecipe(NamespacedKey recipeKey) {
        Preconditions.checkArgument(recipeKey != null, "NamespacedKey recipeKey cannot be null");

        return getServer().getRecipeManager().byKey(CraftRecipe.toMinecraft(recipeKey)).map(RecipeHolder::toBukkitRecipe).orElse(null);
    }

    private CraftingContainer createInventoryCrafting() {
        // Create a players Crafting Inventory
        AbstractContainerMenu abstractcontainermenu = new AbstractContainerMenu(null, -1) {
            @Override
            public InventoryView getBukkitView() {
                return null;
            }

            @Override
            public boolean stillValid(net.minecraft.world.entity.player.Player player) {
                return false;
            }

            @Override
            public net.minecraft.world.item.ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int i) {
                return net.minecraft.world.item.ItemStack.EMPTY;
            }
        };
        CraftingContainer inventoryCrafting = new TransientCraftingContainer(abstractcontainermenu, 3, 3);
        return inventoryCrafting;
    }

    @Override
    public Recipe getCraftingRecipe(ItemStack[] craftingMatrix, World world) {
        return getNMSRecipe(craftingMatrix, createInventoryCrafting(), (CraftWorld) world).map(RecipeHolder::toBukkitRecipe).orElse(null);
    }

    @Override
    public ItemStack craftItem(ItemStack[] craftingMatrix, World world, Player player) {
        return craftItemResult(craftingMatrix, world, player).getResult();
    }

    @Override
    public ItemCraftResult craftItemResult(ItemStack[] craftingMatrix, World world, Player player) {
        Preconditions.checkArgument(world != null, "world cannot be null");
        Preconditions.checkArgument(player != null, "player cannot be null");

        CraftWorld craftWorld = (CraftWorld) world;
        CraftPlayer craftPlayer = (CraftPlayer) player;

        // Create a players Crafting Inventory and get the recipe
        CraftingMenu container = new CraftingMenu(-1, craftPlayer.getHandle().getInventory());
        CraftingContainer inventoryCrafting = container.craftSlots;
        ResultContainer craftResult = container.resultSlots;

        Optional<RecipeHolder<CraftingRecipe>> recipe = getNMSRecipe(craftingMatrix, inventoryCrafting, craftWorld);

        // Generate the resulting ItemStack from the Crafting Matrix
        net.minecraft.world.item.ItemStack itemstack = net.minecraft.world.item.ItemStack.EMPTY;

        if (recipe.isPresent()) {
            RecipeHolder<CraftingRecipe> recipeCrafting = recipe.get();
            if (craftResult.setRecipeUsed(craftPlayer.getHandle(), recipeCrafting)) {
                itemstack = recipeCrafting.value().assemble(inventoryCrafting.asCraftInput());
            }
        }

        // Call Bukkit event to check for matrix/result changes.
        net.minecraft.world.item.ItemStack result = CraftEventFactory.callPreCraftEvent(inventoryCrafting, craftResult, itemstack, container.getBukkitView(), recipe);

        return createItemCraftResult(recipe, CraftItemStack.asBukkitCopy(result), inventoryCrafting, craftWorld.getHandle());
    }

    @Override
    public ItemStack craftItem(ItemStack[] craftingMatrix, World world) {
        return craftItemResult(craftingMatrix, world).getResult();
    }

    @Override
    public ItemCraftResult craftItemResult(ItemStack[] craftingMatrix, World world) {
        Preconditions.checkArgument(world != null, "world must not be null");

        CraftWorld craftWorld = (CraftWorld) world;

        // Create a players Crafting Inventory and get the recipe
        CraftingContainer inventoryCrafting = createInventoryCrafting();

        Optional<RecipeHolder<CraftingRecipe>> recipe = getNMSRecipe(craftingMatrix, inventoryCrafting, craftWorld);

        // Generate the resulting ItemStack from the Crafting Matrix
        net.minecraft.world.item.ItemStack itemStack = net.minecraft.world.item.ItemStack.EMPTY;

        if (recipe.isPresent()) {
            itemStack = recipe.get().value().assemble(inventoryCrafting.asCraftInput());
        }

        return createItemCraftResult(recipe, CraftItemStack.asBukkitCopy(itemStack), inventoryCrafting, craftWorld.getHandle());
    }

    private CraftItemCraftResult createItemCraftResult(Optional<RecipeHolder<CraftingRecipe>> recipe, ItemStack itemStack, CraftingContainer inventoryCrafting, ServerLevel worldServer) {
        CraftItemCraftResult craftItemResult = new CraftItemCraftResult(itemStack);
        recipe.map((holder) -> holder.value().getRemainingItems(inventoryCrafting.asCraftInput())).ifPresent((remainingItems) -> {
            // Set the resulting matrix items and overflow items
            for (int i = 0; i < remainingItems.size(); ++i) {
                net.minecraft.world.item.ItemStack itemstack1 = inventoryCrafting.getItem(i);
                net.minecraft.world.item.ItemStack itemstack2 = (net.minecraft.world.item.ItemStack) remainingItems.get(i);

                if (!itemstack1.isEmpty()) {
                    inventoryCrafting.removeItem(i, 1);
                    itemstack1 = inventoryCrafting.getItem(i);
                }

                if (!itemstack2.isEmpty()) {
                    if (itemstack1.isEmpty()) {
                        inventoryCrafting.setItem(i, itemstack2);
                    } else if (net.minecraft.world.item.ItemStack.isSameItemSameComponents(itemstack1, itemstack2)) {
                        itemstack2.grow(itemstack1.getCount());
                        inventoryCrafting.setItem(i, itemstack2);
                    } else {
                        craftItemResult.getOverflowItems().add(CraftItemStack.asBukkitCopy(itemstack2));
                    }
                }
            }
        });

        for (int i = 0; i < inventoryCrafting.getContents().size(); i++) {
            craftItemResult.setResultMatrix(i, CraftItemStack.asBukkitCopy(inventoryCrafting.getItem(i)));
        }

        return craftItemResult;
    }

    private Optional<RecipeHolder<CraftingRecipe>> getNMSRecipe(ItemStack[] craftingMatrix, CraftingContainer inventoryCrafting, CraftWorld world) {
        Preconditions.checkArgument(craftingMatrix != null, "craftingMatrix must not be null");
        Preconditions.checkArgument(craftingMatrix.length == 9, "craftingMatrix must be an array of length 9");
        Preconditions.checkArgument(world != null, "world must not be null");

        for (int i = 0; i < craftingMatrix.length; i++) {
            inventoryCrafting.setItem(i, CraftItemStack.asNMSCopy(craftingMatrix[i]));
        }

        return getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inventoryCrafting.asCraftInput(), world.getHandle());
    }

    @Override
    public Iterator<Recipe> recipeIterator() {
        return new RecipeIterator();
    }

    @Override
    public void clearRecipes() {
        console.getRecipeManager().clearRecipes();
    }

    @Override
    public void resetRecipes() {
        reloadData(); // Not ideal but hard to reload a subset of a resource pack
    }

    @Override
    public boolean removeRecipe(NamespacedKey recipeKey) {
        Preconditions.checkArgument(recipeKey != null, "recipeKey == null");

        return getServer().getRecipeManager().removeRecipe(CraftRecipe.toMinecraft(recipeKey));
    }

    @Override
    public Map<String, String[]> getCommandAliases() {
        ConfigurationSection section = commandsConfiguration.getConfigurationSection("aliases");
        Map<String, String[]> result = new LinkedHashMap<String, String[]>();

        if (section != null) {
            for (String key : section.getKeys(false)) {
                List<String> commands;

                if (section.isList(key)) {
                    commands = section.getStringList(key);
                } else {
                    commands = ImmutableList.of(section.getString(key));
                }

                result.put(key, commands.toArray(new String[commands.size()]));
            }
        }

        return result;
    }

    public void removeBukkitSpawnRadius() {
        configuration.set("settings.spawn-radius", null);
        saveConfig();
    }

    public int getBukkitSpawnRadius() {
        return configuration.getInt("settings.spawn-radius", -1);
    }

    @Override
    public String getShutdownMessage() {
        return configuration.getString("settings.shutdown-message");
    }

    @Override
    public int getSpawnRadius() {
        return this.getServer().spawnProtectionRadius();
    }

    @Override
    public void setSpawnRadius(int value) {
        configuration.set("settings.spawn-radius", value);
        saveConfig();
    }

    @Override
    public boolean shouldSendChatPreviews() {
        return false;
    }

    @Override
    public boolean isEnforcingSecureProfiles() {
        return this.getServer().enforceSecureProfile();
    }

    @Override
    public boolean isAcceptingTransfers() {
        return this.getServer().acceptsTransfers();
    }

    @Override
    public boolean getHideOnlinePlayers() {
        return console.hidesOnlinePlayers();
    }

    @Override
    public boolean getOnlineMode() {
        return console.usesAuthentication();
    }

    @Override
    public boolean getAllowFlight() {
        return console.allowFlight();
    }

    @Override
    public boolean isHardcore() {
        return console.isHardcore();
    }

    public ChunkGenerator getGenerator(String world) {
        ConfigurationSection section = configuration.getConfigurationSection("worlds");
        ChunkGenerator result = null;

        if (section != null) {
            section = section.getConfigurationSection(world);

            if (section != null) {
                String name = section.getString("generator");

                if ((name != null) && (!name.equals(""))) {
                    String[] split = name.split(":", 2);
                    String id = (split.length > 1) ? split[1] : null;
                    Plugin plugin = pluginManager.getPlugin(split[0]);

                    if (plugin == null) {
                        getLogger().severe("Could not set generator for default world '" + world + "': Plugin '" + split[0] + "' does not exist");
                    } else if (!plugin.isEnabled()) {
                        getLogger().severe("Could not set generator for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' is not enabled yet (is it load:STARTUP?)");
                    } else {
                        try {
                            result = plugin.getDefaultWorldGenerator(world, id);
                            if (result == null) {
                                getLogger().severe("Could not set generator for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' lacks a default world generator");
                            }
                        } catch (Throwable t) {
                            plugin.getLogger().log(Level.SEVERE, "Could not set generator for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName(), t);
                        }
                    }
                }
            }
        }

        return result;
    }

    public BiomeProvider getBiomeProvider(String world) {
        ConfigurationSection section = configuration.getConfigurationSection("worlds");
        BiomeProvider result = null;

        if (section != null) {
            section = section.getConfigurationSection(world);

            if (section != null) {
                String name = section.getString("biome-provider");

                if ((name != null) && (!name.equals(""))) {
                    String[] split = name.split(":", 2);
                    String id = (split.length > 1) ? split[1] : null;
                    Plugin plugin = pluginManager.getPlugin(split[0]);

                    if (plugin == null) {
                        getLogger().severe("Could not set biome provider for default world '" + world + "': Plugin '" + split[0] + "' does not exist");
                    } else if (!plugin.isEnabled()) {
                        getLogger().severe("Could not set biome provider for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' is not enabled yet (is it load:STARTUP?)");
                    } else {
                        try {
                            result = plugin.getDefaultBiomeProvider(world, id);
                            if (result == null) {
                                getLogger().severe("Could not set biome provider for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName() + "' lacks a default world biome provider");
                            }
                        } catch (Throwable t) {
                            plugin.getLogger().log(Level.SEVERE, "Could not set biome provider for default world '" + world + "': Plugin '" + plugin.getDescription().getFullName(), t);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    @Deprecated
    public CraftMapView getMap(int id) {
        MapItemSavedData mapitemsaveddata = console.getLevel(net.minecraft.world.level.Level.OVERWORLD).getMapData(new MapId(id));
        if (mapitemsaveddata == null) {
            return null;
        }
        return mapitemsaveddata.mapView;
    }

    @Override
    public CraftMapView createMap(World world) {
        Preconditions.checkArgument(world != null, "World cannot be null");

        ServerLevel minecraftWorld = ((CraftWorld) world).getHandle();
        // creates a new map at world spawn with the scale of 3, with out tracking position and unlimited tracking
        BlockPos spawn = minecraftWorld.getRespawnData().pos();
        MapId newId = MapItem.createNewSavedData(minecraftWorld, spawn.getX(), spawn.getZ(), 3, false, false, minecraftWorld.dimension());
        return minecraftWorld.getMapData(newId).mapView;
    }

    @Override
    public ItemStack createExplorerMap(World world, Location location, StructureType structureType) {
        return this.createExplorerMap(world, location, structureType, 100, true);
    }

    @Override
    public ItemStack createExplorerMap(World world, Location location, StructureType structureType, int radius, boolean findUnexplored) {
        Preconditions.checkArgument(world != null, "World cannot be null");
        Preconditions.checkArgument(structureType != null, "StructureType cannot be null");
        Preconditions.checkArgument(structureType.getMapIcon() != null, "Cannot create explorer maps for StructureType %s", structureType.getName());

        Location structureLocation = world.locateNearestStructure(location, structureType, radius, findUnexplored);
        if (structureLocation == null) {
            throw new IllegalStateException("Could not locate a " + structureType + " within " + radius + " blocks of " + location);
        }

        return getItemFactory().createExplorerMap(world, structureLocation, structureType.getMapIcon());
    }

    @Override
    public void shutdown() {
        console.halt(false);
    }

    @Override
    public int broadcast(String message, String permission) {
        Set<CommandSender> recipients = new HashSet<>();
        for (Permissible permissible : getPluginManager().getPermissionSubscriptions(permission)) {
            if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
                recipients.add((CommandSender) permissible);
            }
        }

        BroadcastMessageEvent broadcastMessageEvent = new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), message, recipients);
        getPluginManager().callEvent(broadcastMessageEvent);

        if (broadcastMessageEvent.isCancelled()) {
            return 0;
        }

        message = broadcastMessageEvent.getMessage();

        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.size();
    }

    @Override
    @Deprecated
    public OfflinePlayer getOfflinePlayer(String name) {
        Preconditions.checkArgument(name != null, "name cannot be null");
        Preconditions.checkArgument(!name.isBlank(), "name cannot be empty");

        OfflinePlayer result = getPlayerExact(name);
        if (result == null) {
            NameAndId profile = null;
            // Only fetch an online UUID in online mode
            if (getOnlineMode()) {
                // This is potentially blocking :(
                profile = console.services().nameToIdCache().get(name).orElse(null);
            }

            if (profile == null) {
                // Make an OfflinePlayer using an offline mode UUID since the name has no profile
                result = getOfflinePlayer(NameAndId.createOffline(name));
            } else {
                // Use the GameProfile even when we get a UUID so we ensure we still have a name
                result = getOfflinePlayer(profile);
            }
        } else {
            offlinePlayers.remove(result.getUniqueId());
        }

        return result;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID id) {
        Preconditions.checkArgument(id != null, "UUID id cannot be null");

        OfflinePlayer result = getPlayer(id);
        if (result == null) {
            result = offlinePlayers.get(id);
            if (result == null) {
                result = new CraftOfflinePlayer(this, new NameAndId(id, ""));
                offlinePlayers.put(id, result);
            }
        } else {
            offlinePlayers.remove(id);
        }

        return result;
    }

    @Override
    public PlayerProfile createPlayerProfile(UUID uniqueId, String name) {
        return new CraftPlayerProfile(uniqueId, name);
    }

    @Override
    public PlayerProfile createPlayerProfile(UUID uniqueId) {
        return new CraftPlayerProfile(uniqueId, null);
    }

    @Override
    public PlayerProfile createPlayerProfile(String name) {
        return new CraftPlayerProfile(null, name);
    }

    public OfflinePlayer getOfflinePlayer(NameAndId profile) {
        OfflinePlayer player = new CraftOfflinePlayer(this, profile);
        offlinePlayers.put(profile.id(), player);
        return player;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getIPBans() {
        return playerList.getIpBans().getEntries().stream().map(IpBanListEntry::getUser).collect(Collectors.toSet());
    }

    @Override
    public void banIP(String address) {
        Preconditions.checkArgument(address != null && !address.isBlank(), "Address cannot be null or blank.");

        this.getBanList(BanList.Type.IP).addBan(address, null, null, null);
    }

    @Override
    public void unbanIP(String address) {
        Preconditions.checkArgument(address != null && !address.isBlank(), "Address cannot be null or blank.");

        this.getBanList(BanList.Type.IP).pardon(address);
    }

    @Override
    public void banIP(InetAddress address) {
        Preconditions.checkArgument(address != null, "Address cannot be null.");

        ((CraftIpBanList) this.getBanList(BanList.Type.IP)).addBan(address, null, (Date) null, null);
    }

    @Override
    public void unbanIP(InetAddress address) {
        Preconditions.checkArgument(address != null, "Address cannot be null.");

        ((CraftIpBanList) this.getBanList(BanList.Type.IP)).pardon(address);
    }

    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        Set<OfflinePlayer> result = new HashSet<OfflinePlayer>();

        for (UserBanListEntry entry : playerList.getBans().getEntries()) {
            result.add(getOfflinePlayer(entry.getUser()));
        }

        return result;
    }

    @Override
    public <T extends BanList<?>> T getBanList(BanList.Type type) {
        Preconditions.checkArgument(type != null, "BanList.Type cannot be null");

        return switch (type) {
            case IP -> (T) new CraftIpBanList(this.playerList.getIpBans());
            case PROFILE, NAME -> (T) new CraftProfileBanList(this.playerList.getBans());
        };
    }

    @Override
    public void setWhitelist(boolean value) {
        console.setUsingWhitelist(value);
    }

    @Override
    public boolean isWhitelistEnforced() {
        return console.isEnforceWhitelist();
    }

    @Override
    public void setWhitelistEnforced(boolean value) {
        console.setEnforceWhitelist(value);
    }

    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        Set<OfflinePlayer> result = new LinkedHashSet<OfflinePlayer>();

        for (UserWhiteListEntry entry : playerList.getWhiteList().getEntries()) {
            result.add(getOfflinePlayer(entry.getUser()));
        }

        return result;
    }

    @Override
    public Set<OfflinePlayer> getOperators() {
        Set<OfflinePlayer> result = new HashSet<OfflinePlayer>();

        for (ServerOpListEntry entry : playerList.getOps().getEntries()) {
            result.add(getOfflinePlayer(entry.getUser()));
        }

        return result;
    }

    @Override
    public void reloadWhitelist() {
        playerList.reloadWhiteList();
    }

    @Override
    public GameMode getDefaultGameMode() {
        return GameMode.getByValue(console.getLevel(net.minecraft.world.level.Level.OVERWORLD).serverLevelData.getGameType().getId());
    }

    @Override
    public void setDefaultGameMode(GameMode mode) {
        Preconditions.checkArgument(mode != null, "GameMode cannot be null");

        for (World world : getWorlds()) {
            ((CraftWorld) world).getHandle().serverLevelData.setGameType(GameType.byId(mode.getValue()));
        }
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return console.console;
    }

    public EntityMetadataStore getEntityMetadata() {
        return entityMetadata;
    }

    public PlayerMetadataStore getPlayerMetadata() {
        return playerMetadata;
    }

    public WorldMetadataStore getWorldMetadata() {
        return worldMetadata;
    }

    @Override
    public File getWorldContainer() {
        return this.getServer().storageSource.getLevelDirectory().path().getParent().toFile();
    }

    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        PlayerDataStorage storage = console.playerDataStorage;
        String[] files = storage.getPlayerDir().list(new DatFileFilter());
        Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();

        for (String file : files) {
            try {
                players.add(getOfflinePlayer(UUID.fromString(file.substring(0, file.length() - 4))));
            } catch (IllegalArgumentException ex) {
                // Who knows what is in this directory, just ignore invalid files
            }
        }

        players.addAll(getOnlinePlayers());

        return players.toArray(new OfflinePlayer[players.size()]);
    }

    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(getMessenger(), source, channel, message);

        for (Player player : getOnlinePlayers()) {
            player.sendPluginMessage(source, channel, message);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        Set<String> result = new HashSet<String>();

        for (Player player : getOnlinePlayers()) {
            result.addAll(player.getListeningPluginChannels());
        }

        return result;
    }

    @Override
    public Inventory createInventory(InventoryHolder owner, InventoryType type) {
        Preconditions.checkArgument(type != null, "InventoryType cannot be null");
        Preconditions.checkArgument(type.isCreatable(), "InventoryType.%s cannot be used to create a inventory", type);
        return CraftInventoryCreator.INSTANCE.createInventory(owner, type);
    }

    @Override
    public Inventory createInventory(InventoryHolder owner, InventoryType type, String title) {
        Preconditions.checkArgument(type != null, "InventoryType cannot be null");
        Preconditions.checkArgument(type.isCreatable(), "InventoryType.%s cannot be used to create a inventory", type);
        Preconditions.checkArgument(title != null, "title cannot be null");
        return CraftInventoryCreator.INSTANCE.createInventory(owner, type, title);
    }

    @Override
    public Inventory createInventory(InventoryHolder owner, int size) throws IllegalArgumentException {
        Preconditions.checkArgument(9 <= size && size <= 54 && size % 9 == 0, "Size for custom inventory must be a multiple of 9 between 9 and 54 slots (got %s)", size);
        return CraftInventoryCreator.INSTANCE.createInventory(owner, size);
    }

    @Override
    public Inventory createInventory(InventoryHolder owner, int size, String title) throws IllegalArgumentException {
        Preconditions.checkArgument(9 <= size && size <= 54 && size % 9 == 0, "Size for custom inventory must be a multiple of 9 between 9 and 54 slots (got %s)", size);
        return CraftInventoryCreator.INSTANCE.createInventory(owner, size, title);
    }

    @Override
    public Merchant createMerchant(String title) {
        return new CraftMerchantCustom(title == null ? InventoryType.MERCHANT.getDefaultTitle() : title);
    }

    @Override
    public Merchant createMerchant() {
        return new CraftMerchantCustom(""); // if opened with the old methods title won't render, but will be empty.
    }

    @Override
    public int getMaxChainedNeighborUpdates() {
        return this.getServer().getMaxChainedNeighborUpdates();
    }

    @Override
    public HelpMap getHelpMap() {
        return helpMap;
    }

    public SimpleCommandMap getCommandMap() {
        return commandMap;
    }

    @Override
    @Deprecated
    public int getMonsterSpawnLimit() {
        return getSpawnLimit(SpawnCategory.MONSTER);
    }

    @Override
    @Deprecated
    public int getAnimalSpawnLimit() {
        return getSpawnLimit(SpawnCategory.ANIMAL);
    }

    @Override
    @Deprecated
    public int getWaterAnimalSpawnLimit() {
        return getSpawnLimit(SpawnCategory.WATER_ANIMAL);
    }

    @Override
    @Deprecated
    public int getWaterAmbientSpawnLimit() {
        return getSpawnLimit(SpawnCategory.WATER_AMBIENT);
    }

    @Override
    @Deprecated
    public int getWaterUndergroundCreatureSpawnLimit() {
        return getSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE);
    }

    @Override
    @Deprecated
    public int getAmbientSpawnLimit() {
        return getSpawnLimit(SpawnCategory.AMBIENT);
    }

    @Override
    public int getSpawnLimit(SpawnCategory spawnCategory) {
        return spawnCategoryLimit.getOrDefault(spawnCategory, -1);
    }

    @Override
    public boolean isPrimaryThread() {
        return Thread.currentThread().equals(console.serverThread) || console.hasStopped(); // All bets are off if we have shut down (e.g. due to watchdog)
    }

    @Override
    public String getMotd() {
        return console.getMotd();
    }

    @Override
    public void setMotd(String motd) {
        console.setMotd(motd);
    }

    @Override
    public ServerLinks getServerLinks() {
        return this.serverLinks;
    }

    @Override
    public WarningState getWarningState() {
        return warningState;
    }

    public List<String> tabComplete(CommandSender sender, String message, ServerLevel world, Vec3 pos, boolean forceCommand) {
        if (!(sender instanceof Player)) {
            return ImmutableList.of();
        }

        List<String> offers;
        Player player = (Player) sender;
        if (message.startsWith("/") || forceCommand) {
            offers = tabCompleteCommand(player, message, world, pos);
        } else {
            offers = tabCompleteChat(player, message);
        }

        TabCompleteEvent tabEvent = new TabCompleteEvent(player, message, offers);
        getPluginManager().callEvent(tabEvent);

        return tabEvent.isCancelled() ? Collections.EMPTY_LIST : tabEvent.getCompletions();
    }

    public List<String> tabCompleteCommand(Player player, String message, ServerLevel world, Vec3 pos) {
        List<String> completions = null;
        try {
            if (message.startsWith("/")) {
                // Trim leading '/' if present (won't always be present in command blocks)
                message = message.substring(1);
            }
            if (pos == null) {
                completions = getCommandMap().tabComplete(player, message);
            } else {
                completions = getCommandMap().tabComplete(player, message, CraftLocation.toBukkit(pos, world.getWorld()));
            }
        } catch (CommandException ex) {
            player.sendMessage(ChatColor.RED + "An internal error occurred while attempting to tab-complete this command");
            getLogger().log(Level.SEVERE, "Exception when " + player.getName() + " attempted to tab complete " + message, ex);
        }

        return completions == null ? ImmutableList.<String>of() : completions;
    }

    public List<String> tabCompleteChat(Player player, String message) {
        List<String> completions = new ArrayList<String>();
        PlayerChatTabCompleteEvent event = new PlayerChatTabCompleteEvent(player, message, completions);
        String token = event.getLastToken();
        for (Player p : getOnlinePlayers()) {
            if (player.canSee(p) && StringUtil.startsWithIgnoreCase(p.getName(), token)) {
                completions.add(p.getName());
            }
        }
        pluginManager.callEvent(event);

        Iterator<?> it = completions.iterator();
        while (it.hasNext()) {
            Object current = it.next();
            if (!(current instanceof String)) {
                // Sanity
                it.remove();
            }
        }
        Collections.sort(completions, String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

    @Override
    public CraftItemFactory getItemFactory() {
        return CraftItemFactory.instance();
    }

    @Override
    public CraftEntityFactory getEntityFactory() {
        return entityFactory;
    }

    @Override
    public CraftScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    @Override
    public Criteria getScoreboardCriteria(String name) {
        return CraftCriteria.getFromBukkit(name);
    }

    public void checkSaveState() {
        if (this.playerCommandState || this.printSaveWarning || this.console.autosavePeriod <= 0) {
            return;
        }
        this.printSaveWarning = true;
        getLogger().log(Level.WARNING, "A manual (plugin-induced) save has been detected while server is configured to auto-save. This may affect performance.", warningState == WarningState.ON ? new Throwable() : null);
    }

    @Override
    public CraftIconCache getServerIcon() {
        return icon;
    }

    @Override
    public CraftIconCache loadServerIcon(File file) throws Exception {
        Preconditions.checkArgument(file != null, "File cannot be null");
        Preconditions.checkArgument(file.isFile(), "File (%s) is not a valid file", file);
        return loadServerIcon0(file);
    }

    static CraftIconCache loadServerIcon0(File file) throws Exception {
        return loadServerIcon0(ImageIO.read(file));
    }

    @Override
    public CraftIconCache loadServerIcon(BufferedImage image) throws Exception {
        Preconditions.checkArgument(image != null, "BufferedImage image cannot be null");
        return loadServerIcon0(image);
    }

    static CraftIconCache loadServerIcon0(BufferedImage image) throws Exception {
        Preconditions.checkArgument(image.getWidth() == 64, "BufferedImage must be 64 pixels wide (%s)", image.getWidth());
        Preconditions.checkArgument(image.getHeight() == 64, "BufferedImage must be 64 pixels high (%s)", image.getHeight());

        ByteArrayOutputStream bytebuf = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", bytebuf);

        return new CraftIconCache(bytebuf.toByteArray());
    }

    @Override
    public void setIdleTimeout(int threshold) {
        console.setPlayerIdleTimeout(threshold);
    }

    @Override
    public int getIdleTimeout() {
        return console.playerIdleTimeout();
    }

    @Override
    public int getPauseWhenEmptyTime() {
        return console.pauseWhenEmptySeconds();
    }

    @Override
    public void setPauseWhenEmptyTime(int seconds) {
        console.setPauseWhenEmptySeconds(seconds);
    }

    @Override
    public ChunkGenerator.ChunkData createChunkData(World world) {
        Preconditions.checkArgument(world != null, "World cannot be null");
        ServerLevel handle = ((CraftWorld) world).getHandle();
        return new OldCraftChunkData(world.getMinHeight(), world.getMaxHeight(), handle.palettedContainerFactory());
    }

    @Override
    public BossBar createBossBar(String title, BarColor color, BarStyle style, BarFlag... flags) {
        return new CraftBossBar(title, color, style, flags);
    }

    @Override
    public KeyedBossBar createBossBar(NamespacedKey key, String title, BarColor barColor, BarStyle barStyle, BarFlag... barFlags) {
        Preconditions.checkArgument(key != null, "NamespacedKey key cannot be null");
        Preconditions.checkArgument(barColor != null, "BarColor key cannot be null");
        Preconditions.checkArgument(barStyle != null, "BarStyle key cannot be null");

        CustomBossEvent bossBattleCustom = getServer().getCustomBossEvents().create(getServer().overworld().getRandom(), CraftNamespacedKey.toMinecraft(key), CraftChatMessage.fromString(title, true)[0]);
        CraftKeyedBossbar craftKeyedBossbar = new CraftKeyedBossbar(bossBattleCustom);
        craftKeyedBossbar.setColor(barColor);
        craftKeyedBossbar.setStyle(barStyle);
        for (BarFlag flag : barFlags) {
            if (flag == null) {
                continue;
            }
            craftKeyedBossbar.addFlag(flag);
        }

        return craftKeyedBossbar;
    }

    @Override
    public Iterator<KeyedBossBar> getBossBars() {
        return Iterators.unmodifiableIterator(Iterators.transform(getServer().getCustomBossEvents().getEvents().iterator(), new Function<CustomBossEvent, KeyedBossBar>() {
            @Override
            public KeyedBossBar apply(CustomBossEvent bossBattleCustom) {
                return bossBattleCustom.getBukkitEntity();
            }
        }));
    }

    @Override
    public KeyedBossBar getBossBar(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "key");
        CustomBossEvent bossBattleCustom = getServer().getCustomBossEvents().get(CraftNamespacedKey.toMinecraft(key));

        return (bossBattleCustom == null) ? null : bossBattleCustom.getBukkitEntity();
    }

    @Override
    public boolean removeBossBar(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "key");
        net.minecraft.server.bossevents.CustomBossEvents bossBattleCustomData = getServer().getCustomBossEvents();
        CustomBossEvent bossBattleCustom = bossBattleCustomData.get(CraftNamespacedKey.toMinecraft(key));

        if (bossBattleCustom != null) {
            bossBattleCustomData.remove(bossBattleCustom);
            return true;
        }

        return false;
    }

    @Override
    public Entity getEntity(UUID uuid) {
        Preconditions.checkArgument(uuid != null, "UUID id cannot be null");

        for (ServerLevel world : getServer().getAllLevels()) {
            net.minecraft.world.entity.Entity entity = world.getEntity(uuid);
            if (entity != null) {
                return entity.getBukkitEntity();
            }
        }

        return null;
    }

    @Override
    public org.bukkit.advancement.Advancement getAdvancement(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "NamespacedKey key cannot be null");

        AdvancementHolder advancement = console.getAdvancements().get(CraftNamespacedKey.toMinecraft(key));
        return (advancement == null) ? null : advancement.toBukkit();
    }

    @Override
    public Iterator<org.bukkit.advancement.Advancement> advancementIterator() {
        return Iterators.unmodifiableIterator(Iterators.transform(console.getAdvancements().getAllAdvancements().iterator(), new Function<AdvancementHolder, org.bukkit.advancement.Advancement>() {
            @Override
            public org.bukkit.advancement.Advancement apply(AdvancementHolder advancement) {
                return advancement.toBukkit();
            }
        }));
    }

    @Override
    public BlockData createBlockData(Material material) {
        Preconditions.checkArgument(material != null, "Material cannot be null");

        return createBlockData(material, (String) null);
    }

    @Override
    public BlockData createBlockData(Material material, Consumer<? super BlockData> consumer) {
        BlockData data = createBlockData(material);

        if (consumer != null) {
            consumer.accept(data);
        }

        return data;
    }

    @Override
    public BlockData createBlockData(String data) throws IllegalArgumentException {
        Preconditions.checkArgument(data != null, "data cannot be null");

        return createBlockData((Material) null, data);
    }

    @Override
    public BlockData createBlockData(Material material, String data) {
        Preconditions.checkArgument(material != null || data != null, "Must provide one of material or data");

        return CraftBlockData.newData((material != null) ? material.asBlockType() : null, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> org.bukkit.Tag<T> getTag(String registry, NamespacedKey tag, Class<T> clazz) {
        Preconditions.checkArgument(registry != null, "registry cannot be null");
        Preconditions.checkArgument(tag != null, "NamespacedKey tag cannot be null");
        Preconditions.checkArgument(clazz != null, "Class clazz cannot be null");
        Identifier key = CraftNamespacedKey.toMinecraft(tag);

        switch (registry) {
            case org.bukkit.Tag.REGISTRY_BLOCKS -> {
                Preconditions.checkArgument(clazz == Material.class, "Block namespace (%s) must have material type", clazz.getName());
                TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK, key);
                if (BuiltInRegistries.BLOCK.get(blockTagKey).isPresent()) {
                    return (org.bukkit.Tag<T>) new CraftBlockTag(BuiltInRegistries.BLOCK, blockTagKey);
                }
            }
            case org.bukkit.Tag.REGISTRY_ITEMS -> {
                Preconditions.checkArgument(clazz == Material.class, "Item namespace (%s) must have material type", clazz.getName());
                TagKey<Item> itemTagKey = TagKey.create(Registries.ITEM, key);
                if (BuiltInRegistries.ITEM.get(itemTagKey).isPresent()) {
                    return (org.bukkit.Tag<T>) new CraftItemTag(BuiltInRegistries.ITEM, itemTagKey);
                }
            }
            case org.bukkit.Tag.REGISTRY_FLUIDS -> {
                Preconditions.checkArgument(clazz == org.bukkit.Fluid.class, "Fluid namespace (%s) must have fluid type", clazz.getName());
                TagKey<Fluid> fluidTagKey = TagKey.create(Registries.FLUID, key);
                if (BuiltInRegistries.FLUID.get(fluidTagKey).isPresent()) {
                    return (org.bukkit.Tag<T>) new CraftFluidTag(BuiltInRegistries.FLUID, fluidTagKey);
                }
            }
            case org.bukkit.Tag.REGISTRY_ENTITY_TYPES -> {
                Preconditions.checkArgument(clazz == org.bukkit.entity.EntityType.class, "Entity type namespace (%s) must have entity type", clazz.getName());
                TagKey<EntityType<?>> entityTagKey = TagKey.create(Registries.ENTITY_TYPE, key);
                if (BuiltInRegistries.ENTITY_TYPE.get(entityTagKey).isPresent()) {
                    return (org.bukkit.Tag<T>) new CraftEntityTag(BuiltInRegistries.ENTITY_TYPE, entityTagKey);
                }
            }
            case org.bukkit.tag.DamageTypeTags.REGISTRY_DAMAGE_TYPES -> {
                Preconditions.checkArgument(clazz == org.bukkit.damage.DamageType.class, "Damage type namespace (%s) must have damage type", clazz.getName());
                TagKey<DamageType> damageTagKey = TagKey.create(Registries.DAMAGE_TYPE, key);
                net.minecraft.core.Registry<DamageType> damageRegistry = CraftRegistry.getMinecraftRegistry(Registries.DAMAGE_TYPE);
                if (damageRegistry.get(damageTagKey).isPresent()) {
                    return (org.bukkit.Tag<T>) new CraftDamageTag(damageRegistry, damageTagKey);
                }
            }
            default -> throw new IllegalArgumentException();
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> Iterable<org.bukkit.Tag<T>> getTags(String registry, Class<T> clazz) {
        Preconditions.checkArgument(registry != null, "registry cannot be null");
        Preconditions.checkArgument(clazz != null, "Class clazz cannot be null");
        switch (registry) {
            case org.bukkit.Tag.REGISTRY_BLOCKS -> {
                Preconditions.checkArgument(clazz == Material.class, "Block namespace (%s) must have material type", clazz.getName());
                net.minecraft.core.Registry<Block> blockTags = BuiltInRegistries.BLOCK;
                return blockTags.getTags().map(pair -> (org.bukkit.Tag<T>) new CraftBlockTag(blockTags, pair.key())).collect(ImmutableList.toImmutableList());
            }
            case org.bukkit.Tag.REGISTRY_ITEMS -> {
                Preconditions.checkArgument(clazz == Material.class, "Item namespace (%s) must have material type", clazz.getName());
                net.minecraft.core.Registry<Item> itemTags = BuiltInRegistries.ITEM;
                return itemTags.getTags().map(pair -> (org.bukkit.Tag<T>) new CraftItemTag(itemTags, pair.key())).collect(ImmutableList.toImmutableList());
            }
            case org.bukkit.Tag.REGISTRY_FLUIDS -> {
                Preconditions.checkArgument(clazz == org.bukkit.Fluid.class, "Fluid namespace (%s) must have fluid type", clazz.getName());
                net.minecraft.core.Registry<Fluid> fluidTags = BuiltInRegistries.FLUID;
                return fluidTags.getTags().map(pair -> (org.bukkit.Tag<T>) new CraftFluidTag(fluidTags, pair.key())).collect(ImmutableList.toImmutableList());
            }
            case org.bukkit.Tag.REGISTRY_ENTITY_TYPES -> {
                Preconditions.checkArgument(clazz == org.bukkit.entity.EntityType.class, "Entity type namespace (%s) must have entity type", clazz.getName());
                net.minecraft.core.Registry<EntityType<?>> entityTags = BuiltInRegistries.ENTITY_TYPE;
                return entityTags.getTags().map(pair -> (org.bukkit.Tag<T>) new CraftEntityTag(entityTags, pair.key())).collect(ImmutableList.toImmutableList());
            }
            case org.bukkit.tag.DamageTypeTags.REGISTRY_DAMAGE_TYPES -> {
                Preconditions.checkArgument(clazz == org.bukkit.damage.DamageType.class, "Damage type namespace (%s) must have damage type", clazz.getName());
                net.minecraft.core.Registry<DamageType> damageTags = CraftRegistry.getMinecraftRegistry(Registries.DAMAGE_TYPE);
                return damageTags.getTags().map(pair -> (org.bukkit.Tag<T>) new CraftDamageTag(damageTags, pair.key())).collect(ImmutableList.toImmutableList());
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public LootTable getLootTable(NamespacedKey key) {
        Preconditions.checkArgument(key != null, "NamespacedKey key cannot be null");

        ReloadableServerRegistries.Holder registry = getServer().reloadableRegistries();
        return registry.lookup().lookup(Registries.LOOT_TABLE)
                .flatMap((lookup) -> lookup.get(CraftLootTable.bukkitKeyToMinecraft(key)))
                .map((holder) -> new CraftLootTable(key, holder.value()))
                .orElse(null);
    }

    @Override
    public List<Entity> selectEntities(CommandSender sender, String selector) {
        Preconditions.checkArgument(selector != null, "selector cannot be null");
        Preconditions.checkArgument(sender != null, "CommandSender sender cannot be null");

        EntityArgument arg = EntityArgument.entities();
        List<? extends net.minecraft.world.entity.Entity> nms;

        try {
            StringReader reader = new StringReader(selector);
            nms = arg.parse(reader, true, true).findEntities(VanillaCommandWrapper.getListener(sender));
            Preconditions.checkArgument(!reader.canRead(), "Spurious trailing data in selector: %s", selector);
        } catch (CommandSyntaxException ex) {
            throw new IllegalArgumentException("Could not parse selector: " + selector, ex);
        }

        return new ArrayList<>(Lists.transform(nms, (entity) -> entity.getBukkitEntity()));
    }

    @Override
    public StructureManager getStructureManager() {
        return structureManager;
    }

    @Override
    public <T extends Keyed> Registry<T> getRegistry(Class<T> aClass) {
        return (Registry<T>) registries.computeIfAbsent(aClass, key -> CraftRegistry.createRegistry(aClass, console.registryAccess()));
    }

    @Deprecated
    @Override
    public UnsafeValues getUnsafe() {
        return CraftMagicNumbers.INSTANCE;
    }
}
