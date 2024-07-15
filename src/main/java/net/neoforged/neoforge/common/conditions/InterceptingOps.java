package net.neoforged.neoforge.common.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import org.slf4j.Logger;

/**
 * Intercepts all calls to get something from a {@link T} using the wrapperCodec. If that codec succeeds the returned
 * wrapper object is unwrapped with supplied context, and parsed in place of the original structure.
 * If that fails to parse the structure then the input is just passed directly to the delegate codec.
 * <p>
 * Intended for use to allow dynamically modifying json on-parse based on the json itself, with additional outside context if needed.
 *
 * @param <T> The type of the ops, like {@link com.google.gson.JsonElement} if this wraps a {@link com.mojang.serialization.JsonOps}.
 * @param <C> The type of the context. Can be {@link Void} for no context.
 */
public class InterceptingOps<T, C> extends RegistryOps<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Codec<? extends Wrapped<T, C>> wrapperCodec;
    private final Supplier<C> context;

    /**
     * @param delegate       The {@link DynamicOps <T>} that we are wrapping.
     * @param lookupProvider The registry information. Required because we extend RegistryOps.
     * @param wrapperCodec   The {@link Codec} for the wrapper object. This will try to parse every single value that this DynamicOps comes into contact with. Not used when encoding.
     * @param context        The context that will be passed to the wrapper for each decode. Can be {@code null} and of type {@link Void} if no context is needed.
     */
    public InterceptingOps(DynamicOps<T> delegate, RegistryOps.RegistryInfoLookup lookupProvider, Codec<? extends Wrapped<T, C>> wrapperCodec, Supplier<C> context) {
        super(delegate, lookupProvider);
        if (delegate instanceof InterceptingOps<?, ?>) LOGGER.warn("Are you sure this is a good idea?");
        this.wrapperCodec = wrapperCodec;
        this.context = context;
    }

    /**
     * @param delegate     The {@link DynamicOps <T>} that we are wrapping.
     * @param regAccess    The registry access. Required because we extend RegistryOps.
     * @param wrapperCodec The {@link Codec} for the wrapper object. This will try to parse every single value that this DynamicOps comes into contact with. Not used when encoding.
     * @param context      The context that will be passed to the wrapper for each decode. Can be {@code null} and of type {@link Void} if no context is needed.
     */
    public InterceptingOps(DynamicOps<T> delegate, HolderLookup.Provider regAccess, Codec<? extends Wrapped<T, C>> wrapperCodec, Supplier<C> context) {
        this(delegate, new RegistryOps.HolderLookupAdapter(regAccess), wrapperCodec, context);
    }

    /**
     * @param delegate     The {@link RegistryOps} that we are wrapping. Will be split into its delegate and lookup provider.
     * @param wrapperCodec The {@link Codec} for the wrapper object. This will try to parse every single value that this InterceptingOps comes into contact with. Not (currently) used when encoding.
     * @param context      The context that will be passed to the {@link Wrapped} for unwrapping. Can be {@code null} and of type {@link Void} if no context is needed.
     */
    public InterceptingOps(RegistryOps<T> delegate, Codec<? extends Wrapped<T, C>> wrapperCodec, Supplier<C> context) {
        this(delegate.delegate, delegate.lookupProvider, wrapperCodec, context);
    }

    /**
     * Try to intercept a value.
     * 
     * @param intercepted The value that is being intercepted.
     * @return The result of unwrapping the wrapper codec if the wrapper codec succeeds, else the {@code intercepted} is returned.
     */
    protected DataResult<T> intercept(T intercepted) {
        // TODO: allow not suppressing errors from this. Maybe a secondary 'checker' codec, which for conditions would
        //  just contain the type field, and use the result of that as a 'should try parse as Wrapped' check.
        DataResult<? extends Wrapped<T, C>> parsed = wrapperCodec.parse(delegate, intercepted);
        if (parsed.isSuccess())
            return parsed.map(w -> w.unwrap(context.get()));
        return DataResult.success(intercepted);
    }

    /**
     * A {@link Function} returned from decoding a {@link Codec} which can modify the structure of part of a data
     * structure, based on data parsed from the data structure itself.
     *
     * @param <T> The type of the wrapped object.
     * @param <C> The context needed to unwrap the object. Can be {@link Void} if no context is required.
     */
    public interface Wrapped<T, C> extends Function<C, T> {
        T unwrap(C c);

        @Override
        default T apply(C c) {
            return unwrap(c);
        }
    }

    public InterceptingOps<T, C> withContext(Supplier<C> newContext) {
        return new InterceptingOps<>(this.delegate, this.lookupProvider, this.wrapperCodec, newContext);
    }

    @SuppressWarnings({ "unchecked" }) // the cast is fine as the ops match
    public <U> InterceptingOps<U, C> withParent(RegistryOps<U> newOps, Codec<? extends Wrapped<U, C>> newCodec) {
        return delegate == newOps ? (InterceptingOps<U, C>) this : new InterceptingOps<>(newOps, newCodec, this.context);
    }

    @SuppressWarnings({ "unchecked" }) // the cast is fine as the ops match
    public <U> InterceptingOps<U, C> withParent(DynamicOps<U> newOps, RegistryInfoLookup lookupProvider, Codec<? extends Wrapped<U, C>> newCodec) {
        return delegate == newOps ? (InterceptingOps<U, C>) this : new InterceptingOps<>(newOps, lookupProvider, newCodec, this.context);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(T input) {
        return intercept(input).flatMap(super::getBooleanValue);
    }

    @Override
    public DataResult<Stream<Pair<T, T>>> getMapValues(T input) {
        return intercept(input).flatMap(super::getMapValues);
    }

    @Override
    public DataResult<Number> getNumberValue(T input) {
        return intercept(input).flatMap(super::getNumberValue);
    }

    @Override
    public DataResult<String> getStringValue(T input) {
        return intercept(input).flatMap(super::getStringValue);
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(T input) {
        return intercept(input).flatMap(super::getByteBuffer);
    }

    @Override
    public DataResult<IntStream> getIntStream(T input) {
        return intercept(input).flatMap(super::getIntStream);
    }

    @Override
    public DataResult<Consumer<Consumer<T>>> getList(T input) {
        return intercept(input).flatMap(super::getList);
    }

    @Override
    public DataResult<LongStream> getLongStream(T input) {
        return intercept(input).flatMap(super::getLongStream);
    }

    @Override
    public DataResult<MapLike<T>> getMap(T input) {
        return intercept(input).flatMap(super::getMap);
    }

    @Override
    public DataResult<Consumer<BiConsumer<T, T>>> getMapEntries(T input) {
        return intercept(input).flatMap(super::getMapEntries);
    }

    @Override
    public DataResult<Stream<T>> getStream(T input) {
        return intercept(input).flatMap(super::getStream);
    }
}
