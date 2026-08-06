package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.registry.CraftRegistryItem;
import org.bukkit.entity.Chicken;

public class CraftChicken extends CraftAnimals implements Chicken {

    public CraftChicken(CraftServer server, net.minecraft.world.entity.animal.chicken.Chicken entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.chicken.Chicken getHandle() {
        return (net.minecraft.world.entity.animal.chicken.Chicken) entity;
    }

    @Override
    public String toString() {
        return "CraftChicken";
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

    public static class CraftVariant extends CraftRegistryItem<ChickenVariant> implements Variant {

        public static Variant minecraftToBukkit(ChickenVariant minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, Registries.CHICKEN_VARIANT, Registry.CHICKEN_VARIANT);
        }

        public static Variant minecraftHolderToBukkit(Holder<ChickenVariant> minecraft) {
            return minecraftToBukkit(minecraft.value());
        }

        public static ChickenVariant bukkitToMinecraft(Variant bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        public static Holder<ChickenVariant> bukkitToMinecraftHolder(Variant bukkit) {
            return CraftRegistry.bukkitToMinecraftHolder(bukkit, Registries.CHICKEN_VARIANT);
        }

        public CraftVariant(NamespacedKey key, Holder<ChickenVariant> handle) {
            super(key, handle);
        }

        @Override
        public NamespacedKey getKey() {
            return getKeyOrThrow();
        }
    }
}
