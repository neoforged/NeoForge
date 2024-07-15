package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.WeakHashMap;
import java.util.function.Function;

public class ConditionalOperationCodecCache {
    public interface ConditionCodecConstructor extends Function<DynamicOps<?>, MapCodec<? extends ConditionalOperation<?>>> {
        @Override
        default MapCodec<? extends ConditionalOperation<?>> apply(DynamicOps<?> dynamicOps) {
            return make(dynamicOps);
        }

        <T> MapCodec<? extends ConditionalOperation<T>> make(DynamicOps<T> ops);
    }

    private final ConditionCodecConstructor constructor;
    private final WeakHashMap<DynamicOps<?>, MapCodec<? extends ConditionalOperation<?>>> map = new WeakHashMap<>();

    public ConditionalOperationCodecCache(ConditionCodecConstructor constructor) {
        this.constructor = constructor;
    }

    public <T> void putCodec(DynamicOps<T> key, MapCodec<? extends ConditionalOperation<T>> value) {
        map.put(key, value);
    }

    @SuppressWarnings("unchecked") // the cast is safe because we control all puts with the same generic contract enforced
    public <T> MapCodec<? extends ConditionalOperation<T>> getFromCache(DynamicOps<T> key) {
        return (MapCodec<? extends ConditionalOperation<T>>) map.computeIfAbsent(key, constructor);
    }
}
