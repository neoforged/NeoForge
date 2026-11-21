package net.neoforged.neoforge.client.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class NeoForgeClientRegistries {
    public static final class Keys {

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, name));
        }

        private Keys() {}
    }

    private NeoForgeClientRegistries() { }
}
