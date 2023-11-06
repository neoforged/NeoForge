package net.neoforged.neoforge.registries;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Internal registry for tracking object holder references
 */
public class ObjectHolderRegistry {
    /**
     * Exposed to allow modders to register their own notification handlers.
     * This runnable will be called after a registry snapshot has been injected and finalized.
     * The internal list is backed by a HashSet so it is HIGHLY recommended you implement a proper equals
     * and hashCode function to de-duplicate callers here.
     * The default @ObjectHolder implementation uses the hashCode/equals for the field the annotation is on.
     */
    public static synchronized void addHandler(Consumer<Predicate<ResourceLocation>> ref) {
        objectHolders.add(ref);
    }

    /**
     * Removed the specified handler from the notification list.
     * <p>
     * The internal list is backed by a hash set, and so proper hashCode and equals operations are required for success.
     * <p>
     * The default @ObjectHolder implementation uses the hashCode/equals for the field the annotation is on.
     *
     * @return true if handler was matched and removed.
     */
    public static synchronized boolean removeHandler(Consumer<Predicate<ResourceLocation>> ref) {
        return objectHolders.remove(ref);
    }

    //==============================================================
    // Everything below is internal, do not use.
    //==============================================================

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Marker REGISTRIES = MarkerFactory.getMarker("REGISTRIES");
    private static final Set<Consumer<Predicate<ResourceLocation>>> objectHolders = new HashSet<>();

    public static void applyObjectHolders() {
        try {
            LOGGER.debug(REGISTRIES, "Applying holder lookups");
            applyObjectHolders(key -> true);
            LOGGER.debug(REGISTRIES, "Holder lookups applied");
        } catch (RuntimeException e) {
            // It is more important that the calling contexts continue without exception to prevent further cascading errors
            LOGGER.error("", e);
        }
    }

    public static void applyObjectHolders(Predicate<ResourceLocation> filter) {
        RuntimeException aggregate = new RuntimeException("Failed to apply some object holders, see suppressed exceptions for details");
        objectHolders.forEach(objectHolder -> {
            try {
                objectHolder.accept(filter);
            } catch (Exception e) {
                aggregate.addSuppressed(e);
            }
        });

        if (aggregate.getSuppressed().length > 0) {
            throw aggregate;
        }
    }
}
