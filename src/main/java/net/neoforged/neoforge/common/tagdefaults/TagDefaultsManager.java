package net.neoforged.neoforge.common.tagdefaults;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is the central manager class for {@link TagDefaults}. Access an instance via {@link NeoForgeEventHandler#getTagDefaultsManager()}.
 */
public class TagDefaultsManager extends SimpleJsonResourceReloadListener<JsonElement> {
    public static final String FOLDER = "tag_defaults";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<ResourceKey<? extends Registry<?>>, TagDefaults<?>> entries = new HashMap<>();

    public TagDefaultsManager() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(FOLDER));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        entries.clear();
        resourceList.forEach(this::put);
    }

    /**
     * Resolves a {@link TagDefaults} into an optional result.
     *
     * @param registryKey The key of the associated {@link Registry}.
     * @param tagKey      The {@link TagKey} to resolve.
     * @return An {@link Optional} of a {@link TagDefaults}'s resolved value, or an empty {@link Optional} if the {@link TagDefaults} could not be resolved.
     * @param <T> The type of the {@link Registry} and the result.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolve(ResourceKey<? extends Registry<T>> registryKey, TagKey<T> tagKey) {
        return Optional.ofNullable(((TagDefaults<T>) entries.get(registryKey)).resolve(tagKey));
    }

    /**
     * Called for every element in {@link TagDefaultsManager#apply(Map, ResourceManager, ProfilerFiller)}.
     * Moved into a separate method due to generics (unlike above, we can force the registry and the value to have the same generic type here).
     *
     * @param key   The registry's {@link ResourceLocation}.
     * @param value The {@link JsonElement} to parse.
     * @param <T> The type of the registry and the value.
     */
    @SuppressWarnings("unchecked")
    private <T> void put(ResourceLocation key, JsonElement value) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(key);
        if (registry == null) {
            LOGGER.warn("Skipping loading data file '{}' as no corresponding registry was found", key);
            return;
        }
        Codec.unboundedMap(TagKey.codec(registry.key()), registry.byNameCodec()).parse(JsonOps.INSTANCE, value)
                .ifError(error -> LOGGER.error("Couldn't parse data file '{}': {}", key, error))
                .ifSuccess(map -> entries.put(registry.key(), new TagDefaults<>(map)));
    }
}
