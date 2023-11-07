package net.neoforged.neoforge.registries;

import com.mojang.logging.LogUtils;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@ApiStatus.Internal
class RegistryRemapHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Marker REGISTRIES = MarkerFactory.getMarker("REGISTRIES");

    /**
     * Handles remapping of missing registry entries.
     *
     * @param missing      a set of all missing entries based on their resource keys
     * @param isLocalWorld whether the remaps are being handled for a local world or a remote connection
     * @return an immutable set of unhandled missing registry entries after firing remapping events for mods
     */
    static Set<ResourceKey<?>> handleRemaps(Set<ResourceKey<?>> missing, boolean isLocalWorld) {
        if (missing.isEmpty())
            return Set.of();

        LOGGER.debug(REGISTRIES, "There are {} mappings missing", missing.size());

        // Only log if the world save is something we control
        if (isLocalWorld && LOGGER.isWarnEnabled(REGISTRIES)) {
            StringBuilder builder = new StringBuilder("NeoForge detected missing registry entries.\n\n")
                    .append("There are ").append(missing.size()).append(" missing entries in this save.\n")
                    .append("These missing entries will be deleted from the save file on next save.");

            missing.forEach(key -> builder.append("Missing ").append(key).append('\n'));

            LOGGER.warn(REGISTRIES, builder.toString());
        }

        return Set.copyOf(missing);
    }
}
