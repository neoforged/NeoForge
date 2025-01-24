package net.neoforged.neoforge.transfer;

import java.util.function.UnaryOperator;

/**
 * Represents the underlying instruction set for mutable and immutable resource stacks.
 * @param <T> resource type
 */
public interface IResourceStack<T extends IResource> {
    /**
     * @return the backing resource of the stack.
     */
    T resource();

    /**
     * @return the amount currently set in the stack
     */
    int amount();

    /**
     * Checks if this is empty, meaning that the amount is not positive
     * or that the resource is {@link IResource#isEmpty() blank}.
     *
     * @return {@code true} if empty
     */
    default boolean isEmpty() {
        return amount() <= 0 || resource().isEmpty();
    }

    IResourceStack<T> withAmount(int newAmount);

    IResourceStack<T> shrink(int amount);

    IResourceStack<T> grow(int amount);

    IResourceStack<T> with(UnaryOperator<T> operator);

    /**
     * @return a mutable resource stack that allows the amount to be changeable without the underlying resource data being set.
     */
    MutableResourceStack<T> mutable();

    /**
     * @return an immutable resource stack
     */
    ResourceStack<T> immutable();
}
