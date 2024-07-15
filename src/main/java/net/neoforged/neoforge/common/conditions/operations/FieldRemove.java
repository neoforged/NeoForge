package net.neoforged.neoforge.common.conditions.operations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.neoforged.neoforge.common.conditions.ConditionalOperation;
import net.neoforged.neoforge.common.conditions.ConditionalOperationCodecCache;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/**
 * Removes fields from a map-like if the conditions are true.
 */
public class FieldRemove<T> extends ConditionalOperation<T> {
    public static final Codec<Set<String>> SINGLE_OR_SET = Codec.either(Codec.STRING, NeoForgeExtraCodecs.setOf(Codec.STRING)).xmap(e -> Either.unwrap(e.mapLeft(Set::of)), Either::right);
    public static final ConditionalOperationCodecCache CODECS = new ConditionalOperationCodecCache(FieldRemove::makeCodec);
    static {
        // a special implementation for json ops as they use a really inefficient method of removing if you are removing multiple things in a row.
        CODECS.putCodec(JsonOps.INSTANCE, makeCodec(JsonOps.INSTANCE, (conditions, base, toRemove) -> new FieldRemove<>(JsonOps.INSTANCE, conditions, base, toRemove) {
            @Override
            protected JsonElement getSuccess() {
                if (base instanceof JsonObject) {
                    final JsonObject result = new JsonObject();
                    base.getAsJsonObject().entrySet().stream()
                            .filter(entry -> !fieldsToRemove.contains(entry.getKey()))
                            .forEach(entry -> result.add(entry.getKey(), entry.getValue()));
                    return result;
                }
                return base;
            }
        }));
    }

    protected final T base;
    protected final Set<String> fieldsToRemove;

    public FieldRemove(DynamicOps<T> ops, List<ICondition> conditions, T base, Set<String> fieldsToRemove) {
        super(ops, conditions);
        this.base = base;
        this.fieldsToRemove = fieldsToRemove;
    }

    private static <T> MapCodec<FieldRemove<T>> makeCodec(DynamicOps<T> ops, Function3<List<ICondition>, T, Set<String>, FieldRemove<T>> applicator) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                conditionList(),
                typedPassThrough(ops).fieldOf(VALUE_KEY).forGetter(fr -> fr.base),
                SINGLE_OR_SET.fieldOf("to_remove").forGetter(fr -> fr.fieldsToRemove)).apply(instance, applicator));
    }

    private static <T> MapCodec<FieldRemove<T>> makeCodec(DynamicOps<T> ops) {
        return makeCodec(ops, (conditions, base, toRemove) -> new FieldRemove<>(ops, conditions, base, toRemove));
    }

    @Override
    public ConditionalOperationCodecCache getCodecCache() {
        return CODECS;
    }

    @Override
    protected T getSuccess() {
        T value = base;
        for (String field : fieldsToRemove) {
            value = ops.remove(value, field);
        }
        return value;
    }

    @Override
    protected T getFail() {
        return base;
    }
}
