package org.bukkit.craftbukkit.map;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.registry.CraftOldEnumRegistryItem;
import org.bukkit.map.MapCursor;

public final class CraftMapCursor {

    public static final class CraftType extends CraftOldEnumRegistryItem<MapCursor.Type, MapDecorationType> implements MapCursor.Type {

        private static int count = 0;

        public static MapCursor.Type minecraftToBukkit(MapDecorationType minecraft) {
            return CraftRegistry.minecraftToBukkit(minecraft, Registries.MAP_DECORATION_TYPE, Registry.MAP_DECORATION_TYPE);
        }

        public static MapCursor.Type minecraftHolderToBukkit(Holder<MapDecorationType> minecraft) {
            return minecraftToBukkit(minecraft.value());
        }

        public static MapDecorationType bukkitToMinecraft(MapCursor.Type bukkit) {
            return CraftRegistry.bukkitToMinecraft(bukkit);
        }

        public static Holder<MapDecorationType> bukkitToMinecraftHolder(MapCursor.Type bukkit) {
            return CraftRegistry.bukkitToMinecraftHolder(bukkit, Registries.MAP_DECORATION_TYPE);
        }

        public CraftType(NamespacedKey key, Holder<MapDecorationType> handle) {
            super(key, handle, count++);
        }

        @Override
        public NamespacedKey getKey() {
            return getKeyOrThrow();
        }

        @Override
        public byte getValue() {
            return (byte) CraftRegistry.getMinecraftRegistry(Registries.MAP_DECORATION_TYPE).getId(getHandle());
        }
    }
}
