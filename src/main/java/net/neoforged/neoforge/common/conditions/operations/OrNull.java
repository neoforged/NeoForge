package net.neoforged.neoforge.common.conditions.operations;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.neoforged.neoforge.common.conditions.ConditionalOperationCodecCache;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * A basic version of {@link SimpleAlternative} that returns the ops 'empty' as the alternative.
 * Primarily used for a root level condition so that the entire result is empty (and thus ignored) if conditions are not met
 */
public class OrNull<T> extends SimpleAlternative<T> {
    public static final ConditionalOperationCodecCache CODECS = new ConditionalOperationCodecCache(OrNull::makeCodec);

    private static <T> MapCodec<OrNull<T>> makeCodec(DynamicOps<T> dops) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                conditionList(),
                typedPassThrough(dops).fieldOf(VALUE_KEY).forGetter(OrNull::getSuccess)).apply(instance, (conditions, value) -> new OrNull<>(dops, conditions, value)));
    }

    public OrNull(DynamicOps<T> ops, List<ICondition> conditions, T value) {
        super(ops, conditions, value, ops.empty());
    }

    @Override
    public ConditionalOperationCodecCache getCodecCache() {
        return CODECS;
    }
}
