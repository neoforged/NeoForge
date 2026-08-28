package net.neoforged.neoforge.common.world;

import net.minecraft.world.attribute.EnvironmentAttributeMap;

public final class EnvironmentAttributeMapBuilder extends EnvironmentAttributeMap.Builder {
    public static EnvironmentAttributeMapBuilder copyOf(EnvironmentAttributeMap map) {
        EnvironmentAttributeMapBuilder builder = new EnvironmentAttributeMapBuilder();
        builder.putAll(map);
        return builder;
    }

    private EnvironmentAttributeMapBuilder() { }
}
