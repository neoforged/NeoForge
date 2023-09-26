package net.minecraftforge.common.context;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.MapMaker;

import net.minecraft.resources.ResourceLocation;

/**
 * A Context Key is a key used when passing arbitrary context to a method.<br>
 * It retains the ability to cast the underlying context Object to the target type.
 * 
 * @param <T> The type of the target context object.
 */
public final class ContextKey<T>
{
    private static final ConcurrentMap<InternKey<?>, ContextKey<?>> VALUES = new MapMaker().weakValues().makeMap();

    private final ResourceLocation location;
    private final Class<T> clazz;

    /**
     * Gets a context key, creating it if it does not exist. Created keys are interned and reference (==) comparable.
     * 
     * @param <T>      The type of the underlying context object.
     * @param location The ID of the context key.
     * @param clazz    The class of the underlying context object.
     * @return An interned context key.
     */
    @SuppressWarnings("unchecked")
    public static <T> ContextKey<T> getOrCreate(ResourceLocation location, Class<T> clazz)
    {
        return (ContextKey<T>) VALUES.computeIfAbsent(new InternKey<>(location, clazz), ContextKey::new);
    }

    private ContextKey(InternKey<T> key)
    {
        this.location = key.location;
        this.clazz = key.clazz;
    }

    /**
     * Creates a {@link Context} object with this key and the specified value.
     */
    public <V extends T> ContextKey.Context<T, V> createCtx(V ctx)
    {
        return new Context<>(this, ctx);
    }

    public ResourceLocation location()
    {
        return this.location;
    }

    private T cast(Object o)
    {
        return this.clazz.cast(o);
    }

    /**
     * ContextKey is interned and thus reference comparable, so additional logic is unnecessary.
     */
    @Override
    public boolean equals(Object obj)
    {
        return obj == this;
    }

    private static record InternKey<T>(ResourceLocation location, Class<T> clazz)
    {
    }

    /**
     * Bundled {@link ContextKey} and associated context object.
     * <p>
     * The generics on this class are intended for construction time type-safety, even though the receiver will have both erased to a wildcard.
     * <p>
     * The intended usage for the receiver side is as follows:
     * <p>
     * <code><pre>
     * MyContext myCtx = context.getContext(MY_CONTEXT_KEY); 
     * if (myCtx != null)
     * { 
     *     doThings(myCtx); 
     * }
     * </pre></code>
     * 
     * @param <K> Type of the ContextKey.
     * @param <V> Type of the context object, which may be a subclass of K.
     */
    public static record Context<K, V extends K>(ContextKey<K> key, V ctx)
    {
        /**
         * Performs an operation on the context object if it is the specified type.
         */
        public <C> void ifPresent(ContextKey<C> key, Consumer<C> consumer)
        {
            if (this.key == key)
            {
                consumer.accept(key.cast(this.ctx));
            }
        }

        /**
         * @return The context if it is the specified type, otherwise returns null.
         */
        @Nullable
        public <C> C get(ContextKey<C> key)
        {
            return this.key == key ? key.cast(this.ctx) : null;
        }

        /**
         * @return An {@link Optional} containing the context if it is the specified type, otherwise returns {@link Optional#empty()}.
         */
        public <C> Optional<C> getOptional(ContextKey<C> key)
        {
            return Optional.ofNullable(get(key));
        }
    }

}
