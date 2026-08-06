package org.bukkit.craftbukkit.inventory.components;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public final class CraftHolderUtil {

    private CraftHolderUtil() {
    }

    public static void serialize(ImmutableMap.Builder<String, Object> result, String key, HolderSet<?> handle) {
        handle.unwrap()
                .ifLeft(tag -> result.put(key, "#" + tag.location().toString())) // Tag
                .ifRight(list -> result.put(key, list.stream().map((entry) -> entry.unwrapKey().orElseThrow().identifier().toString()).toList())); // List
    }

    public static void serialize(Map<String, Object> result, String key, HolderSet<?> handle) {
        handle.unwrap()
                .ifLeft(tag -> result.put(key, "#" + tag.location().toString())) // Tag
                .ifRight(list -> result.put(key, list.stream().map((entry) -> entry.unwrapKey().orElseThrow().identifier().toString()).toList())); // List
    }

    public static <T> HolderSet<T> parse(Object parseObject, ResourceKey<Registry<T>> registryKey, Registry<T> registry) {
        HolderSet<T> holderSet = null;

        if (parseObject instanceof String parseString) {
            if (parseString.startsWith("#")) { // Tag
                parseString = parseString.substring(1);
                Identifier key = Identifier.tryParse(parseString);
                if (key != null) {
                    holderSet = registry.get(TagKey.create(registryKey, key)).orElse(null);
                }
            } else { // Singleton
                Identifier key = Identifier.tryParse(parseString);
                if (key != null) {
                    Holder<T> holder = registry.get(key).orElse(null);

                    if (holder != null) {
                        return HolderSet.direct(holder);
                    }
                }
            }
        } else if (parseObject instanceof List parseList) { // List
            List<Holder.Reference<T>> holderList = new ArrayList<>(parseList.size());

            for (Object entry : parseList) {
                Identifier key = Identifier.tryParse(entry.toString());
                if (key == null) {
                    continue;
                }

                registry.get(key).ifPresent(holderList::add);
            }

            holderSet = HolderSet.direct(holderList);
        } else {
            throw new IllegalArgumentException("(" + parseObject + ") is not a valid String or List");
        }

        if (holderSet == null) {
            holderSet = HolderSet.empty();
        }

        return holderSet;
    }
}
