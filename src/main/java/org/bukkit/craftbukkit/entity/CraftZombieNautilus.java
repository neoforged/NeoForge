package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.registry.CraftRegistryItem;
import org.bukkit.entity.ZombieNautilus;

public class CraftZombieNautilus extends CraftAbstractNautilus implements ZombieNautilus {

    public CraftZombieNautilus(CraftServer server, net.minecraft.world.entity.animal.nautilus.ZombieNautilus entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.nautilus.ZombieNautilus getHandle() {
        return (net.minecraft.world.entity.animal.nautilus.ZombieNautilus) entity;
    }

    @Override
    public String toString() {
        return "CraftZombieNautilus";
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

    public static class CraftVariant extends CraftRegistryItem<ZombieNautilusVariant> implements Variant {

        public static Variant minecraftToBukkit(ZombieNautilusVariant minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, Registries.ZOMBIE_NAUTILUS_VARIANT, Registry.ZOMBIE_NAUTILUS_VARIANT);
        }

        public static Variant minecraftHolderToBukkit(Holder<ZombieNautilusVariant> minecraft) {
            return minecraftToBukkit(minecraft.value());
        }

        public static ZombieNautilusVariant bukkitToMinecraft(Variant bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        public static Holder<ZombieNautilusVariant> bukkitToMinecraftHolder(Variant bukkit) {
            return CraftRegistry.bukkitToMinecraftHolder(bukkit, Registries.ZOMBIE_NAUTILUS_VARIANT);
        }

        public CraftVariant(NamespacedKey key, Holder<ZombieNautilusVariant> handle) {
            super(key, handle);
        }

        @Override
        public NamespacedKey getKey() {
            return getKeyOrThrow();
        }
    }
}
