package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import org.bukkit.Art;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.registry.CraftOldEnumRegistryItem;
import org.jetbrains.annotations.NotNull;

public class CraftArt extends CraftOldEnumRegistryItem<Art, PaintingVariant> implements Art {

    private static int count = 0;

    public static Art minecraftToBukkit(PaintingVariant minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.PAINTING_VARIANT, Registry.ART);
    }

    public static Art minecraftHolderToBukkit(Holder<PaintingVariant> minecraft) {
        return minecraftToBukkit(minecraft.value());
    }

    public static PaintingVariant bukkitToMinecraft(Art bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static Holder<PaintingVariant> bukkitToMinecraftHolder(Art bukkit) {
        Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<PaintingVariant> registry = CraftRegistry.getMinecraftRegistry(Registries.PAINTING_VARIANT);

        if (registry.wrapAsHolder(bukkitToMinecraft(bukkit)) instanceof Holder.Reference<PaintingVariant> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own painting variant with out properly registering it.");
    }

    public CraftArt(NamespacedKey key, Holder<PaintingVariant> handle) {
        super(key, handle, count++);
    }

    @Override
    public int getBlockWidth() {
        return getHandle().width();
    }

    @Override
    public int getBlockHeight() {
        return getHandle().height();
    }

    @Override
    public int getId() {
        return CraftRegistry.getMinecraftRegistry(Registries.PAINTING_VARIANT).getId(getHandle());
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return getKeyOrThrow();
    }
}
