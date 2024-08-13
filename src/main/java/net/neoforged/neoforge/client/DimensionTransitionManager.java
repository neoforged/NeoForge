package net.neoforged.neoforge.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class DimensionTransitionManager {
    private static Map<ResourceKey<Level>, DimensionTransitionScreen> toDimensionTransitions = Map.of();
    private static Map<ResourceKey<Level>, DimensionTransitionScreen> fromDimensionTransitions = Map.of();

    @ApiStatus.Internal
    static void init() {
        Map<ResourceKey<Level>, DimensionTransitionScreen> populatedToEffects = new HashMap<>();
        Map<ResourceKey<Level>, DimensionTransitionScreen> populatedFromEffects = new HashMap<>();
        RegisterDimensionTransitionEvent event = new RegisterDimensionTransitionEvent(populatedToEffects, populatedFromEffects);
        ModLoader.postEventWrapContainerInModOrder(event);
        toDimensionTransitions = populatedToEffects;
        fromDimensionTransitions = populatedFromEffects;
    }

    @Nullable
    public static DimensionTransitionScreen get(@Nullable ResourceKey<Level> toDimension, @Nullable ResourceKey<Level> fromDimension) {
        if (toDimension != null && toDimensionTransitions.containsKey(toDimension)) {
            return toDimensionTransitions.get(toDimension);
        } else if (fromDimension != null && fromDimensionTransitions.containsKey(fromDimension)) {
            return fromDimensionTransitions.get(fromDimension);
        }
        return null;
    }
}
