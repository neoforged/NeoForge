package net.neoforged.neoforge.common.conditions.operations;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.neoforged.neoforge.common.conditions.ConditionalOperation;
import net.neoforged.neoforge.common.conditions.ConditionalOperationCodecCache;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Returns one value if all conditions pass, or another if they do not.
 */
public class SimpleAlternative<T> extends ConditionalOperation<T> {
    public static final ConditionalOperationCodecCache CODECS = new ConditionalOperationCodecCache(SimpleAlternative::makeCodec);
    protected final T value;
    protected final T orElse;

    public SimpleAlternative(DynamicOps<T> ops, List<ICondition> conditions, T value, T orElse) {
        super(ops, conditions);
        this.value = value;
        this.orElse = orElse;
    }

    private static <T> MapCodec<SimpleAlternative<T>> makeCodec(DynamicOps<T> ops) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                conditionList(),
                typedPassThrough(ops).fieldOf(VALUE_KEY).forGetter(SimpleAlternative::getSuccess),
                typedPassThrough(ops).fieldOf("or_else").forGetter(SimpleAlternative::getFail)).apply(instance, (c, v, oe) -> new SimpleAlternative<>(ops, c, v, oe)));
    }

    @Override
    public ConditionalOperationCodecCache getCodecCache() {
        return CODECS;
    }

    @Override
    protected T getSuccess() {
        return value;
    }

    @Override
    protected T getFail() {
        return orElse;
    }
}
