package net.neoforged.neoforge.registries.attachment;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An interface used to merge two conflicting registry attachments attached to the same object. <br>
 * Using a merger you can, for example, merge list attachments that come from different sources, when
 * otherwise the newest entry would win and override the older one.
 *
 * @param <T> the attachment type
 * @param <R> the type of the registry this merger is for
 */
@FunctionalInterface
public interface RegistryAttachmentValueMerger<T, R> {
    /**
     * Merge two conflicting attachment values.
     *
     * @param registry    the registry the attachment is for
     * @param first       the source of the first (older) attachment
     * @param firstValue  the first (older) attachment
     * @param second      the source of the second (newer) attachment
     * @param secondValue the second (newer) attachment
     * @return the merged attachment value
     */
    T merge(Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> first, T firstValue, Either<TagKey<R>, ResourceKey<R>> second, T secondValue);

    /**
     * {@return a default merge that overrides the old value with the new one}
     */
    static <T, R> RegistryAttachmentValueMerger<T, R> defaultMerger() {
        return (registry, first, firstValue, second, secondValue) -> secondValue;
    }

    /**
     * {@return a default merge that merges list attachments}
     */
    static <T, R> RegistryAttachmentValueMerger<List<T>, R> listMerger() {
        return (registry, first, firstValue, second, secondValue) -> {
            final List<T> list = new ArrayList<>(firstValue);
            list.addAll(secondValue);
            return list;
        };
    }

    /**
     * {@return a default merge that merges set attachments}
     */
    static <T, R> RegistryAttachmentValueMerger<Set<T>, R> setMerger() {
        return (registry, first, firstValue, second, secondValue) -> {
            final Set<T> set = new HashSet<>(firstValue);
            set.addAll(secondValue);
            return set;
        };
    }

    /**
     * {@return a default merge that merges map attachments}
     */
    static <K, V, R> RegistryAttachmentValueMerger<Map<K, V>, R> mapMerger() {
        return (registry, first, firstValue, second, secondValue) -> {
            final Map<K, V> map = new HashMap<>(firstValue);
            map.putAll(secondValue);
            return map;
        };
    }
}
