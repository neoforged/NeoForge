package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.animal.feline.CatVariant;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.registry.CraftOldEnumRegistryItem;
import org.bukkit.entity.Cat;

public class CraftCat extends CraftTameableAnimal implements Cat {

    public CraftCat(CraftServer server, net.minecraft.world.entity.animal.feline.Cat entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.feline.Cat getHandle() {
        return (net.minecraft.world.entity.animal.feline.Cat) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftCat";
    }

    @Override
    public Type getCatType() {
        return CraftType.minecraftHolderToBukkit(getHandle().getVariant());
    }

    @Override
    public void setCatType(Type type) {
        Preconditions.checkArgument(type != null, "Cannot have null Type");

        getHandle().setVariant(CraftType.bukkitToMinecraftHolder(type));
    }

    @Override
    public DyeColor getCollarColor() {
        return DyeColor.getByWoolData((byte) getHandle().getCollarColor().getId());
    }

    @Override
    public void setCollarColor(DyeColor color) {
        getHandle().setCollarColor(net.minecraft.world.item.DyeColor.byId(color.getWoolData()));
    }

    public static class CraftType extends CraftOldEnumRegistryItem<Type, CatVariant> implements Type {
        private static int count = 0;

        public static Type minecraftToBukkit(CatVariant minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, Registries.CAT_VARIANT, Registry.CAT_VARIANT);
        }

        public static Type minecraftHolderToBukkit(Holder<CatVariant> minecraft) {
            return minecraftToBukkit(minecraft.value());
        }

        public static CatVariant bukkitToMinecraft(Type bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        public static Holder<CatVariant> bukkitToMinecraftHolder(Type bukkit) {
            return CraftRegistry.bukkitToMinecraftHolder(bukkit, Registries.CAT_VARIANT);
        }

        public CraftType(NamespacedKey key, Holder<CatVariant> handle) {
            super(key, handle, count++);
        }

        @Override
        public NamespacedKey getKey() {
            return getKeyOrThrow();
        }
    }
}
