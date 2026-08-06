package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.memory.CraftMemoryMapper;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftContainer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryAbstractHorse;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.inventory.CraftInventoryLectern;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftMerchantCustom;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.util.CraftMenus;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.model.PlayerModelPart;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class CraftHumanEntity extends CraftLivingEntity implements HumanEntity {
    private CraftInventoryPlayer inventory;
    private final CraftInventory enderChest;
    protected final PermissibleBase perm = new PermissibleBase(this);
    private boolean op;
    private GameMode mode;

    public CraftHumanEntity(final CraftServer server, final Player entity) {
        super(server, entity);
        mode = server.getDefaultGameMode();
        this.inventory = new CraftInventoryPlayer(entity.getInventory());
        enderChest = new CraftInventory(entity.getEnderChestInventory());
    }

    @Override
    public PlayerInventory getInventory() {
        return inventory;
    }

    @Override
    public EntityEquipment getEquipment() {
        return inventory;
    }

    @Override
    public Inventory getEnderChest() {
        return enderChest;
    }

    @Override
    public MainHand getMainHand() {
        return getHandle().getMainArm() == HumanoidArm.LEFT ? MainHand.LEFT : MainHand.RIGHT;
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        Preconditions.checkArgument(part != null, "part cannot be null");
        net.minecraft.world.entity.player.PlayerModelPart nms = net.minecraft.world.entity.player.PlayerModelPart.valueOf(part.name());

        return getHandle().isModelPartShown(nms);
    }

    @Override
    public ItemStack getItemInHand() {
        return getInventory().getItemInHand();
    }

    @Override
    public void setItemInHand(ItemStack item) {
        getInventory().setItemInHand(item);
    }

    @Override
    public ItemStack getItemOnCursor() {
        return CraftItemStack.asCraftMirror(getHandle().containerMenu.getCarried());
    }

    @Override
    public void setItemOnCursor(ItemStack item) {
        net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
        getHandle().containerMenu.setCarried(stack);
        if (this instanceof CraftPlayer) {
            getHandle().containerMenu.broadcastCarriedItem(); // Send set slot for cursor
        }
    }

    @Override
    public int getSleepTicks() {
        return getHandle().sleepCounter;
    }

    @Override
    public boolean sleep(Location location, boolean force) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "Location needs to be in a world");
        Preconditions.checkArgument(location.getWorld().equals(getWorld()), "Cannot sleep across worlds");

        BlockPos blockpos = CraftLocation.toBlockPosition(location);
        BlockState blockstate = getHandle().level().getBlockState(blockpos);
        if (!(blockstate.getBlock() instanceof BedBlock)) {
            return false;
        }

        if (getHandle().startSleepInBed(blockpos, force).left().isPresent()) {
            return false;
        }

        // From BlockBed
        blockstate = blockstate.setValue(BedBlock.OCCUPIED, true);
        getHandle().level().setBlock(blockpos, blockstate, 4);

        return true;
    }

    @Override
    public void wakeup(boolean setSpawnLocation) {
        Preconditions.checkState(isSleeping(), "Cannot wakeup if not sleeping");

        getHandle().stopSleepInBed(true, setSpawnLocation);
    }

    @Override
    public void startRiptideAttack(int duration, float damage, ItemStack attackItem) {
        Preconditions.checkArgument(duration > 0, "Duration must be greater than 0");
        Preconditions.checkArgument(damage >= 0, "Damage must not be negative");

        getHandle().startAutoSpinAttack(duration, damage, CraftItemStack.asNMSCopy(attackItem));
    }

    @Override
    public Location getBedLocation() {
        Preconditions.checkState(isSleeping(), "Not sleeping");

        BlockPos bed = getHandle().getSleepingPos().get();
        return CraftLocation.toBukkit(bed, getWorld());
    }

    @Override
    public String getName() {
        return getHandle().getScoreboardName();
    }

    @Override
    public boolean isOp() {
        return op;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return this.perm.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return perm.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return this.perm.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return perm.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return perm.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return perm.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return perm.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        perm.recalculatePermissions();
    }

    @Override
    public void setOp(boolean value) {
        this.op = value;
        perm.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return perm.getEffectivePermissions();
    }

    @Override
    public GameMode getGameMode() {
        return mode;
    }

    @Override
    public void setGameMode(GameMode mode) {
        Preconditions.checkArgument(mode != null, "GameMode cannot be null");

        this.mode = mode;
    }

    @Override
    public Player getHandle() {
        return (Player) entity;
    }

    public void setHandle(final Player entity) {
        super.setHandle(entity);
        this.inventory = new CraftInventoryPlayer(entity.getInventory());
    }

    @Override
    public String toString() {
        return "CraftHumanEntity{" + "id=" + getEntityId() + "name=" + getName() + '}';
    }

    @Override
    public InventoryView getOpenInventory() {
        return getHandle().containerMenu.getBukkitView();
    }

    @Override
    public InventoryView openInventory(Inventory inventory) {
        if (!(getHandle() instanceof ServerPlayer)) return null;
        ServerPlayer player = (ServerPlayer) getHandle();
        AbstractContainerMenu formerContainer = getHandle().containerMenu;

        MenuProvider tileInventory = null;
        if (inventory instanceof CraftInventoryDoubleChest) {
            tileInventory = ((CraftInventoryDoubleChest) inventory).tile;
        } else if (inventory instanceof CraftInventoryLectern) {
            tileInventory = ((CraftInventoryLectern) inventory).tile;
        } else if (inventory instanceof CraftInventory) {
            CraftInventory craft = (CraftInventory) inventory;
            if (craft.getInventory() instanceof MenuProvider) {
                tileInventory = (MenuProvider) craft.getInventory();
            }
        }

        if (tileInventory instanceof MenuProvider) {
            if (tileInventory instanceof BlockEntity) {
                BlockEntity te = (BlockEntity) tileInventory;
                if (!te.hasLevel()) {
                    te.setLevel(getHandle().level());
                }
            }
        }

        if (tileInventory instanceof MenuProvider) {
            getHandle().openMenu(tileInventory);
        } else if (inventory instanceof CraftInventoryAbstractHorse craft && craft.getInventory().getOwner() instanceof CraftAbstractHorse horse) {
            getHandle().openHorseInventory(horse.getHandle(), craft.getInventory());
        } else {
            MenuType<?> container = CraftContainer.getNotchInventoryType(inventory);
            openCustomInventory(inventory, player, container);
        }

        if (getHandle().containerMenu == formerContainer) {
            return null;
        }
        getHandle().containerMenu.checkReachable = false;
        return getHandle().containerMenu.getBukkitView();
    }

    private static void openCustomInventory(Inventory inventory, ServerPlayer player, MenuType<?> windowType) {
        if (player.connection == null) return;
        Preconditions.checkArgument(windowType != null, "Unknown windowType");
        AbstractContainerMenu abstractcontainermenu = new CraftContainer(inventory, player, player.nextContainerCounter());

        abstractcontainermenu = CraftEventFactory.callInventoryOpenEvent(player, abstractcontainermenu);
        if (abstractcontainermenu == null) return;

        String title = abstractcontainermenu.getBukkitView().getTitle();

        player.connection.send(new ClientboundOpenScreenPacket(abstractcontainermenu.containerId, windowType, CraftChatMessage.fromString(title)[0]));
        player.containerMenu = abstractcontainermenu;
        player.initMenu(abstractcontainermenu);
    }

    @Override
    public InventoryView openWorkbench(Location location, boolean force) {
        if (location == null) {
            location = getLocation();
        }
        if (!force) {
            Block block = location.getBlock();
            if (block.getType() != Material.CRAFTING_TABLE) {
                return null;
            }
        }
        getHandle().openMenu(Blocks.CRAFTING_TABLE.defaultBlockState().getMenuProvider(getHandle().level(), CraftLocation.toBlockPosition(location)));
        if (force) {
            getHandle().containerMenu.checkReachable = false;
        }
        return getHandle().containerMenu.getBukkitView();
    }

    @Override
    public InventoryView openEnchanting(Location location, boolean force) {
        if (location == null) {
            location = getLocation();
        }
        if (!force) {
            Block block = location.getBlock();
            if (block.getType() != Material.ENCHANTING_TABLE) {
                return null;
            }
        }

        // If there isn't an enchant table we can force create one, won't be very useful though.
        BlockPos pos = CraftLocation.toBlockPosition(location);
        getHandle().openMenu(Blocks.ENCHANTING_TABLE.defaultBlockState().getMenuProvider(getHandle().level(), pos));

        if (force) {
            getHandle().containerMenu.checkReachable = false;
        }
        return getHandle().containerMenu.getBukkitView();
    }

    @Override
    public void openInventory(InventoryView inventory) {
        Preconditions.checkArgument(this.equals(inventory.getPlayer()), "InventoryView must belong to the opening player");
        if (!(getHandle() instanceof ServerPlayer)) return; // TODO: NPC support?
        if (((ServerPlayer) getHandle()).connection == null) return;
        if (getHandle().containerMenu != getHandle().inventoryMenu) {
            // fire INVENTORY_CLOSE if one already open
            ((ServerPlayer) getHandle()).connection.handleContainerClose(new ServerboundContainerClosePacket(getHandle().containerMenu.containerId));
        }
        ServerPlayer player = (ServerPlayer) getHandle();
        AbstractContainerMenu abstractcontainermenu;
        if (inventory instanceof CraftInventoryView) {
            abstractcontainermenu = ((CraftInventoryView) inventory).getHandle();
        } else {
            abstractcontainermenu = new CraftContainer(inventory, this.getHandle(), player.nextContainerCounter());
        }

        // Trigger an INVENTORY_OPEN event
        abstractcontainermenu = CraftEventFactory.callInventoryOpenEvent(player, abstractcontainermenu);
        if (abstractcontainermenu == null) {
            return;
        }

        // Now open the window
        MenuType<?> windowType = CraftContainer.getNotchInventoryType(inventory.getTopInventory());
        // we can open these now delegeate for now
        if (windowType == MenuType.MERCHANT) {
            CraftMenus.openMerchantMenu(player, (MerchantMenu) abstractcontainermenu);
            return;
        }
        String title = inventory.getTitle();
        player.connection.send(new ClientboundOpenScreenPacket(abstractcontainermenu.containerId, windowType, CraftChatMessage.fromString(title)[0]));
        player.containerMenu = abstractcontainermenu;
        player.initMenu(abstractcontainermenu);
    }

    @Override
    public InventoryView openMerchant(Villager villager, boolean force) {
        Preconditions.checkNotNull(villager, "villager cannot be null");

        return this.openMerchant((Merchant) villager, force);
    }

    @Override
    public InventoryView openMerchant(Merchant merchant, boolean force) {
        Preconditions.checkNotNull(merchant, "merchant cannot be null");

        if (!force && merchant.isTrading()) {
            return null;
        } else if (merchant.isTrading()) {
            // we're not supposed to have multiple people using the same merchant, so we have to close it.
            merchant.getTrader().closeInventory();
        }

        net.minecraft.world.item.trading.Merchant mcMerchant;
        Component name;
        int level = 1; // note: using level 0 with active 'is-regular-villager'-flag allows hiding the name suffix
        if (merchant instanceof CraftAbstractVillager) {
            mcMerchant = ((CraftAbstractVillager) merchant).getHandle();
            name = ((CraftAbstractVillager) merchant).getHandle().getDisplayName();
            if (merchant instanceof CraftVillager) {
                level = ((CraftVillager) merchant).getHandle().getVillagerData().level();
            }
        } else if (merchant instanceof CraftMerchantCustom) {
            mcMerchant = ((CraftMerchantCustom) merchant).getMerchant();
            name = ((CraftMerchantCustom) merchant).getMerchant().getScoreboardDisplayName();
        } else {
            throw new IllegalArgumentException("Can't open merchant " + merchant.toString());
        }

        mcMerchant.setTradingPlayer(this.getHandle());
        mcMerchant.openTradingScreen(this.getHandle(), name, level);

        return this.getHandle().containerMenu.getBukkitView();
    }

    @Override
    public void closeInventory() {
        getHandle().closeContainer();
    }

    @Override
    public boolean isBlocking() {
        return getHandle().isBlocking();
    }

    @Override
    public boolean isHandRaised() {
        return getHandle().isUsingItem();
    }

    @Override
    public boolean setWindowProperty(InventoryView.Property prop, int value) {
        return false;
    }

    @Override
    public int getEnchantmentSeed() {
        return getHandle().enchantmentSeed;
    }

    @Override
    public void setEnchantmentSeed(int i) {
        getHandle().enchantmentSeed = i;
    }

    @Override
    public int getExpToLevel() {
        return getHandle().getXpNeededForNextLevel();
    }

    @Override
    public float getAttackCooldown() {
        return getHandle().getAttackStrengthScale(0.5f);
    }

    @Override
    public boolean hasCooldown(Material material) {
        Preconditions.checkArgument(material != null, "Material cannot be null");
        Preconditions.checkArgument(material.isItem(), "Material %s is not an item", material);

        return hasCooldown(new ItemStack(material));
    }

    @Override
    public int getCooldown(Material material) {
        Preconditions.checkArgument(material != null, "Material cannot be null");
        Preconditions.checkArgument(material.isItem(), "Material %s is not an item", material);

        return getCooldown(new ItemStack(material));
    }

    @Override
    public void setCooldown(Material material, int ticks) {
        setCooldown(new ItemStack(material), ticks);
    }

    @Override
    public boolean hasCooldown(ItemStack item) {
        Preconditions.checkArgument(item != null, "Material cannot be null");

        return getHandle().getCooldowns().isOnCooldown(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public int getCooldown(ItemStack item) {
        Preconditions.checkArgument(item != null, "Material cannot be null");

        Identifier group = getHandle().getCooldowns().getCooldownGroup(CraftItemStack.asNMSCopy(item));
        if (group == null) {
            return 0;
        }

        ItemCooldowns.CooldownInstance cooldown = getHandle().getCooldowns().cooldowns.get(group);
        return (cooldown == null) ? 0 : Math.max(0, cooldown.endTime() - getHandle().getCooldowns().tickCount);
    }

    @Override
    public void setCooldown(ItemStack item, int ticks) {
        Preconditions.checkArgument(item != null, "Material cannot be null");
        Preconditions.checkArgument(ticks >= 0, "Cannot have negative cooldown");

        getHandle().getCooldowns().addCooldown(CraftItemStack.asNMSCopy(item), ticks);
    }

    @Override
    public boolean discoverRecipe(NamespacedKey recipe) {
        return discoverRecipes(Arrays.asList(recipe)) != 0;
    }

    @Override
    public int discoverRecipes(Collection<NamespacedKey> recipes) {
        return getHandle().awardRecipes(bukkitKeysToMinecraftRecipes(recipes));
    }

    @Override
    public boolean undiscoverRecipe(NamespacedKey recipe) {
        return undiscoverRecipes(Arrays.asList(recipe)) != 0;
    }

    @Override
    public int undiscoverRecipes(Collection<NamespacedKey> recipes) {
        return getHandle().resetRecipes(bukkitKeysToMinecraftRecipes(recipes));
    }

    @Override
    public boolean hasDiscoveredRecipe(NamespacedKey recipe) {
        return false;
    }

    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        return ImmutableSet.of();
    }

    private Collection<RecipeHolder<?>> bukkitKeysToMinecraftRecipes(Collection<NamespacedKey> recipeKeys) {
        Collection<RecipeHolder<?>> recipes = new ArrayList<>();
        RecipeManager manager = getHandle().level().getServer().getRecipeManager();

        for (NamespacedKey recipeKey : recipeKeys) {
            Optional<? extends RecipeHolder<?>> recipe = manager.byKey(CraftRecipe.toMinecraft(recipeKey));
            if (!recipe.isPresent()) {
                continue;
            }

            recipes.add(recipe.get());
        }

        return recipes;
    }

    @Override
    public org.bukkit.entity.Entity getShoulderEntityLeft() {
        if (!(getHandle() instanceof ServerPlayer handle)) {
            return null;
        }
        if (!handle.getShoulderEntityLeft().isEmpty()) {
            Entity shoulder = handle.getEntityOnShoulder(handle.getShoulderEntityLeft());

            return (shoulder == null) ? null : shoulder.getBukkitEntity();
        }

        return null;
    }

    @Override
    public void setShoulderEntityLeft(org.bukkit.entity.Entity entity) {
        if (!(getHandle() instanceof ServerPlayer handle)) {
            return;
        }
        handle.setShoulderEntityLeft(entity == null ? new CompoundTag() : ((CraftEntity) entity).save());
        if (entity != null) {
            entity.remove();
        }
    }

    @Override
    public org.bukkit.entity.Entity getShoulderEntityRight() {
        if (!(getHandle() instanceof ServerPlayer handle)) {
            return null;
        }
        if (!handle.getShoulderEntityRight().isEmpty()) {
            Entity shoulder = handle.getEntityOnShoulder(handle.getShoulderEntityRight());

            return (shoulder == null) ? null : shoulder.getBukkitEntity();
        }

        return null;
    }

    @Override
    public void setShoulderEntityRight(org.bukkit.entity.Entity entity) {
        if (!(getHandle() instanceof ServerPlayer handle)) {
            return;
        }
        handle.setShoulderEntityRight(entity == null ? new CompoundTag() : ((CraftEntity) entity).save());
        if (entity != null) {
            entity.remove();
        }
    }

    @Override
    public boolean dropItem(boolean dropAll) {
        if (!(getHandle() instanceof ServerPlayer)) return false;
        return ((ServerPlayer) getHandle()).dropItem(dropAll) != null;
    }

    @Override
    public float getExhaustion() {
        return getHandle().getFoodData().exhaustionLevel;
    }

    @Override
    public void setExhaustion(float value) {
        getHandle().getFoodData().exhaustionLevel = value;
    }

    @Override
    public float getSaturation() {
        return getHandle().getFoodData().saturationLevel;
    }

    @Override
    public void setSaturation(float value) {
        getHandle().getFoodData().saturationLevel = value;
    }

    @Override
    public int getFoodLevel() {
        return getHandle().getFoodData().foodLevel;
    }

    @Override
    public void setFoodLevel(int value) {
        getHandle().getFoodData().foodLevel = value;
    }

    @Override
    public int getSaturatedRegenRate() {
        return getHandle().getFoodData().saturatedRegenRate;
    }

    @Override
    public void setSaturatedRegenRate(int i) {
        getHandle().getFoodData().saturatedRegenRate = i;
    }

    @Override
    public int getUnsaturatedRegenRate() {
        return getHandle().getFoodData().unsaturatedRegenRate;
    }

    @Override
    public void setUnsaturatedRegenRate(int i) {
        getHandle().getFoodData().unsaturatedRegenRate = i;
    }

    @Override
    public int getStarvationRate() {
        return getHandle().getFoodData().starvationRate;
    }

    @Override
    public void setStarvationRate(int i) {
        getHandle().getFoodData().starvationRate = i;
    }

    @Override
    public Location getLastDeathLocation() {
        return getHandle().getLastDeathLocation().map(CraftMemoryMapper::fromNms).orElse(null);
    }

    @Override
    public void setLastDeathLocation(Location location) {
        if (location == null) {
            getHandle().setLastDeathLocation(Optional.empty());
        } else {
            getHandle().setLastDeathLocation(Optional.of(CraftMemoryMapper.toNms(location)));
        }
    }

    @Override
    public Firework fireworkBoost(ItemStack fireworkItemStack) {
        Preconditions.checkArgument(fireworkItemStack != null, "fireworkItemStack must not be null");
        Preconditions.checkArgument(fireworkItemStack.getType() == Material.FIREWORK_ROCKET, "fireworkItemStack must be of type %s", Material.FIREWORK_ROCKET);

        FireworkRocketEntity fireworks = new FireworkRocketEntity(getHandle().level(), CraftItemStack.asNMSCopy(fireworkItemStack), getHandle());
        boolean success = getHandle().level().addFreshEntity(fireworks, SpawnReason.CUSTOM);
        return success ? (Firework) fireworks.getBukkitEntity() : null;
    }

    @Override
    public org.bukkit.entity.Entity copy() {
        throw new UnsupportedOperationException("Cannot copy human entities");
    }

    @Override
    public org.bukkit.entity.Entity copy(Location location) {
        throw new UnsupportedOperationException("Cannot copy human entities");
    }
}
