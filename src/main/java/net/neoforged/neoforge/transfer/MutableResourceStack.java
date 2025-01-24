package net.neoforged.neoforge.transfer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.function.UnaryOperator;


//ADRIAN&SOARYN: PR Notes, should we make the resource be mutable in this stack? This is intended to be used internally by a resource handler, not passed to other mods per se.
/**
 * Represents an immutable resource and a mutable amount.
 * This is intended to be used when you know the amount is going be in flux, but the backing resource will be the same.
 * As an example, when storing a list of stacks, instead of creating a new object on heap, the backing int value can be mutated.
 */
public final class MutableResourceStack<T extends IResource> implements IResourceStack<T>{
    /**
     * Creates a standard stream codec for a resource amount.
     */
    public static <B extends FriendlyByteBuf, T extends IResource> StreamCodec<B, MutableResourceStack<T>> streamCodec(StreamCodec<? super B, T> resourceCodec) {
        return StreamCodec.composite(
                resourceCodec,
                MutableResourceStack::resource,
                ByteBufCodecs.VAR_INT,
                MutableResourceStack::amount,
                MutableResourceStack::new
        );
    }
    /**
     * Creates a codec with the resource being a field in the object.
     * <pre>{@code
     * {
     *     "resource": {
     *         "id": "minecraft:water",
     *         "components": { ... }
     *     },
     *     "amount": 1000
     * }
     * }</pre>
     *
     * @param resourceCodec a codec for the resource
     * @param <T>           The Resource type
     * @return A codec for a MutableResourceStack
     */
    public static <T extends IResource> Codec<MutableResourceStack<T>> codec(Codec<T> resourceCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.fieldOf("resource").forGetter(IResourceStack<T>::resource),
                Codec.INT.fieldOf("amount").forGetter(IResourceStack<T>::amount)
        ).apply(instance, MutableResourceStack::new));
    }

    /**
     * Creates a codec where the fields for the resource are at the same level as the amount
     * <pre>{@code
     * {
     *    "id": "minecraft:water",
     *    "components": { ... },
     *    "amount": 1000
     * }
     * }</pre>
     *
     * @param resourceCodec a codec for the resource
     * @param <T> The Resource type
     * @return A codec for a MutableResourceStack
     */
    public static <T extends IResource> Codec<MutableResourceStack<T>> flatCodec(MapCodec<T> resourceCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.forGetter(IResourceStack<T>::resource),
                Codec.INT.fieldOf("amount").forGetter(IResourceStack<T>::amount)
        ).apply(instance, MutableResourceStack::new));
    }

    private final T resource;
    private int amount;

    public static <TResource extends IResource> NonNullList<MutableResourceStack<TResource>> nonNullListOfSize(int count, MutableResourceStack<TResource> resourceStack) {
        return NonNullList.withSize(count, resourceStack);
    }

    public NonNullList<MutableResourceStack<T>> nonNullListOfSize(int count) {
        return nonNullListOfSize(count, this);
    }

    public MutableResourceStack(T resource, int amount) {
        Objects.requireNonNull(resource, "resource");
        this.resource = resource;
        this.amount = amount;
    }

    public static <T extends IResource> MutableResourceStack<T> of(ResourceStack<T> stack) {
        return of(stack.resource(), stack.amount());
    }

    public static <T extends IResource> MutableResourceStack<T> of(T resource, int amount) {
        return new MutableResourceStack<>(resource, amount);
    }

    @Override
    public T resource() {
        return resource;
    }

    @Override
    public int amount() {
        return amount;
    }

    /**
     * @return this instance with an updated amount.
     */
    @Override
    public MutableResourceStack<T> withAmount(int newAmount) {
        amount = newAmount;
        return this;
    }

    /**
     * @return this instance with an updated amount.
     */
    @Override
    public MutableResourceStack<T> shrink(int amount) {
        return withAmount(Math.max(this.amount - amount, 0));
    }

    /**
     * @return this instance with an updated amount.
     */
    @Override
    public MutableResourceStack<T> grow(int amount) {
        return withAmount(this.amount + amount);
    }

    /**
     * @return a copy of this instance with an updated resource.
     */
    @Override
    public MutableResourceStack<T> with(UnaryOperator<T> operator) {
        return new MutableResourceStack<>(operator.apply(resource), amount);
    }

    @Override
    public MutableResourceStack<T> mutable() {
        return this; // Already mutable, so we can return this
    }

    /**
     * @return a new immutable copy of this resource stack
     */
    @Override
    public ResourceStack<T> immutable() {
        return new ResourceStack<>(resource, amount); // Creates a new immutable resource stack with the backing data
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MutableResourceStack) obj;
        return Objects.equals(this.resource, that.resource) && this.amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, amount);
    }

    @Override
    public String toString() {
        return "MutableResourceStack[resource=%s, amount=%d]".formatted(resource, amount);
    }

}
