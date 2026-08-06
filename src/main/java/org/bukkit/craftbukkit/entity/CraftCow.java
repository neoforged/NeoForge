package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.animal.cow.CowVariant;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.registry.CraftRegistryItem;
import org.bukkit.entity.Cow;

public class CraftCow extends CraftAbstractCow implements Cow {

    public CraftCow(CraftServer server, net.minecraft.world.entity.animal.cow.Cow entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.cow.Cow getHandle() {
        return (net.minecraft.world.entity.animal.cow.Cow) entity;
    }

    @Override
    public String toString() {
        return "CraftCow";
    }

    @Override
    public Variant getVariant() {
        return CraftVariant.minecraftHolderToBukkit(getHandle().getVariant());
    }

    @Override
    public void setVariant(Variant variant) {
        Preconditions.checkArgument(variant != null, "variant");

        getHandle().setVariant(CraftVariant.bukkitToMinecraftHolder(variant));
    }

    public static class CraftVariant extends CraftRegistryItem<CowVariant> implements Variant {

        public static Variant minecraftToBukkit(CowVariant minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, Registries.COW_VARIANT, Registry.COW_VARIANT);
        }

        public static Variant minecraftHolderToBukkit(Holder<CowVariant> minecraft) {
            return minecraftToBukkit(minecraft.value());
        }

        public static CowVariant bukkitToMinecraft(Variant bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        public static Holder<CowVariant> bukkitToMinecraftHolder(Variant bukkit) {
            return CraftRegistry.bukkitToMinecraftHolder(bukkit, Registries.COW_VARIANT);
        }

        public CraftVariant(NamespacedKey key, Holder<CowVariant> handle) {
            super(key, handle);
        }

        @Override
        public NamespacedKey getKey() {
            return getKeyOrThrow();
        }
    }
}
