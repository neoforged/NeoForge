package net.neoforged.neoforge.common.tagdefaults;

import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents a tag default value from a tag_defaults datapack file.
 * Basically just a wrapper around a map, to avoid generics headaches in {@link TagDefaultsManager}.
 * @param <T> The type of the tag key and the associated value.
 */
public class TagDefaults<T> {
    protected final Map<TagKey<T>, T> values;

    public TagDefaults(Map<TagKey<T>, T> values) {
        this.values = values;
    }

    /**
     * @param tagKey The tag key to query for.
     * @return The contents associated with the given tag key, or null if no value was found.
     */
    @Nullable
    public T resolve(TagKey<T> tagKey) {
        return values.get(tagKey);
    }
}
