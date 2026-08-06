package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.craftbukkit.registry.CraftRegistryItem;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

public class CraftBlockType<B extends BlockData> extends CraftRegistryItem<Block> implements BlockType.Typed<B> {

    private final Class<B> blockDataClass;
    private final boolean interactable;

    public static Material minecraftToBukkit(Block block) {
        return CraftMagicNumbers.getMaterial(block);
    }

    public static Block bukkitToMinecraft(Material material) {
        return CraftMagicNumbers.getBlock(material);
    }

    public static BlockType minecraftToBukkitNew(Block minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.BLOCK, Registry.BLOCK);
    }

    public static Block bukkitToMinecraftNew(BlockType bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    private static boolean hasMethod(Class<?> clazz, Class<?>... params) {
        boolean hasMethod = false;
        for (Method method : clazz.getDeclaredMethods()) {
            if (Arrays.equals(method.getParameterTypes(), params)) {
                Preconditions.checkArgument(!hasMethod, "More than one matching method for %s, args %s", clazz, Arrays.toString(params));

                hasMethod = true;
            }
        }

        return hasMethod;
    }

    private static final Class<?>[] USE_WITHOUT_ITEM_ARGS = new Class[]{
        BlockState.class, net.minecraft.world.level.Level.class, BlockPos.class, Player.class, BlockHitResult.class
    };
    private static final Class<?>[] USE_ITEM_ON_ARGS = new Class[]{
        net.minecraft.world.item.ItemStack.class, BlockState.class, net.minecraft.world.level.Level.class, BlockPos.class, Player.class, InteractionHand.class, BlockHitResult.class
    };

    private static boolean isInteractable(Block block) {
        Class<?> clazz = block.getClass();

        boolean hasMethod = hasMethod(clazz, USE_WITHOUT_ITEM_ARGS) || hasMethod(clazz, USE_ITEM_ON_ARGS);

        if (!hasMethod && clazz.getSuperclass() != BlockBehaviour.class) {
            clazz = clazz.getSuperclass();

            hasMethod = hasMethod(clazz, USE_WITHOUT_ITEM_ARGS) || hasMethod(clazz, USE_ITEM_ON_ARGS);
        }

        return hasMethod;
    }

    public CraftBlockType(NamespacedKey key, Holder<Block> handle) {
        super(key, handle);
        this.blockDataClass = (Class<B>) CraftBlockData.fromData(getHandle().defaultBlockState()).getClass().getInterfaces()[0];
        this.interactable = isInteractable(getHandle());
    }

    @NotNull
    @Override
    public Typed<BlockData> typed() {
        return this.typed(BlockData.class);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <Other extends BlockData> Typed<Other> typed(@NotNull Class<Other> blockDataType) {
        if (blockDataType.isAssignableFrom(this.blockDataClass)) return (Typed<Other>) this;
        throw new IllegalArgumentException("Cannot type block type " + (isRegistered() ? getKeyOrThrow() : toString()) + " to blockdata type " + blockDataType.getSimpleName());
    }

    @Override
    public boolean hasItemType() {
        if (this == AIR) {
            return true;
        }

        return getHandle().asItem() != Items.AIR;
    }

    @NotNull
    @Override
    public ItemType getItemType() {
        if (this == AIR) {
            return ItemType.AIR;
        }

        Item item = getHandle().asItem();
        Preconditions.checkArgument(item != Items.AIR, "The block type %s has no corresponding item type", (isRegistered() ? getKeyOrThrow() : toString()));
        return CraftItemType.minecraftToBukkitNew(item);
    }

    @Override
    public Class<B> getBlockDataClass() {
        return blockDataClass;
    }

    @Override
    public B createBlockData() {
        return createBlockData((String) null);
    }

    @Override
    public B createBlockData(Consumer<? super B> consumer) {
        B data = createBlockData();

        if (consumer != null) {
            consumer.accept(data);
        }

        return data;
    }

    @Override
    public B createBlockData(String data) {
        return (B) CraftBlockData.newData(this, data);
    }

    @Override
    public boolean isSolid() {
        return getHandle().defaultBlockState().blocksMotion();
    }

    @Override
    public boolean isAir() {
        return getHandle().defaultBlockState().isAir();
    }

    @Override
    public boolean isEnabledByFeature(@NotNull World world) {
        Preconditions.checkNotNull(world, "World cannot be null");
        return getHandle().isEnabled(((CraftWorld) world).getHandle().enabledFeatures());
    }

    @Override
    public boolean isFlammable() {
        return getHandle().defaultBlockState().ignitedByLava();
    }

    @Override
    public boolean isBurnable() {
        return ((FireBlock) Blocks.FIRE).igniteOdds.getOrDefault(getHandle(), 0) > 0;
    }

    @Override
    public boolean isOccluding() {
        return getHandle().defaultBlockState().isRedstoneConductor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    @Override
    public boolean hasGravity() {
        return getHandle() instanceof Fallable;
    }

    @Override
    public boolean isInteractable() {
        return interactable;
    }

    @Override
    public float getHardness() {
        return getHandle().defaultBlockState().destroySpeed;
    }

    @Override
    public float getBlastResistance() {
        return getHandle().getExplosionResistance();
    }

    @Override
    public float getSlipperiness() {
        return getHandle().getFriction();
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return getHandle().getDescriptionId();
    }

    @Override
    public NamespacedKey getKey() {
        return getKeyOrThrow();
    }

    @Override
    public Material asMaterial() {
        return Registry.MATERIAL.get(getKeyOrThrow());
    }
}
