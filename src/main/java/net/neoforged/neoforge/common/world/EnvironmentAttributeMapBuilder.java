package net.neoforged.neoforge.common.world;

import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import org.jspecify.annotations.Nullable;

public final class EnvironmentAttributeMapBuilder extends EnvironmentAttributeMap.Builder {
    public static EnvironmentAttributeMapBuilder copyOf(EnvironmentAttributeMap map) {
        EnvironmentAttributeMapBuilder builder = new EnvironmentAttributeMapBuilder();
        builder.putAll(map);
        return builder;
    }

    private EnvironmentAttributeMapBuilder() { }

    @SuppressWarnings("unchecked")
    public <V> EnvironmentAttributeMap.@Nullable Entry<V, ?> get(EnvironmentAttribute<V> attribute) {
        return (EnvironmentAttributeMap.Entry<V, ?>) entries.get(attribute);
    }
}
