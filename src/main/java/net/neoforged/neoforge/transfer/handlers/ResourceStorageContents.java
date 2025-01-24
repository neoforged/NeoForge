package net.neoforged.neoforge.transfer.handlers;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.MutableResourceStack;


/**
 * Data structure used to store a list of resource stacks that can be serialized either by Component or DataAttachment
 * @param stacks a list of MutableResourceStacks. The stacks are expected to have their amount mutated internally never externally.
 * @param <T> type of resource
 */
public record ResourceStorageContents<T extends IResource>(NonNullList<MutableResourceStack<T>> stacks) {
    public static <T extends IResource> Codec<ResourceStorageContents<T>> codec(Codec<T> resourceCodec) {
        return NonNullList.codecOf(MutableResourceStack.codec(resourceCodec)).xmap(ResourceStorageContents::new, contents -> contents.stacks);
    }

    public static <T extends IResource> ResourceStorageContents<T> of(int size, T emptyResource) {
        return new ResourceStorageContents<>( MutableResourceStack.nonNullListOfSize(size, MutableResourceStack.of(emptyResource,0)));
    }

    public MutableResourceStack<T> get(int index) {
        return stacks.get(index);
    }

    public MutableResourceStack<T> set(int index, MutableResourceStack<T> stack) {
        stacks.set(index, stack);
        return stack;
    }
}