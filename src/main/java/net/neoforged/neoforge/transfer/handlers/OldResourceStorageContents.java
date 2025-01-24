package net.neoforged.neoforge.transfer.handlers;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.ResourceStack;

// ADRIAN&SOARYN: Oh... adrian lol um, yeah we need to not store the resource stacks this way. We need a mutable resource to avoid frequent allocations

/**
 * @deprecated This is constantly allocating when setting a value, which could be quite often
 */
@Deprecated
public class OldResourceStorageContents<T extends IResource> {
    //ADRIAN&SOARYN:This is something I handled in my implementation, where the resource is mutable, but the amount is not. (MutableResourceStack) Should we consider this in scenarios of the amount being in flux but not the backing resource?
    private final NonNullList<ResourceStack<T>> stacks;

    public static <T extends IResource> Codec<OldResourceStorageContents<T>> codec(Codec<T> resourceCodec) {
        return NonNullList.codecOf(ResourceStack.codec(resourceCodec)).xmap(OldResourceStorageContents::new, contents -> contents.stacks);
    }

    public OldResourceStorageContents(int size, T emptyResource) {
        stacks = NonNullList.withSize(size, new ResourceStack<>(emptyResource, 0));
    }

    private OldResourceStorageContents(NonNullList<ResourceStack<T>> stacks) {
        this.stacks = stacks;
    }

    public ResourceStack<T> get(int index) {
        return stacks.get(index);
    }

    public OldResourceStorageContents<T> set(int index, T resource, int amount) {
        NonNullList<ResourceStack<T>> newStacks = NonNullList.copyOf(stacks);
        newStacks.set(index, new ResourceStack<>(resource, amount));
        return new OldResourceStorageContents<>(newStacks);
    }

    //ADRIAN&SOARYN: Let's avoid allocations where we can
    public static class Builder<T extends IResource> {
        private final NonNullList<ResourceStack<T>> stacks;

        public Builder(NonNullList<ResourceStack<T>> stacks) {
            this.stacks = NonNullList.copyOf(stacks);
        }

        public Builder<T> set(int index, T resource, int amount) {
            stacks.set(index, new ResourceStack<>(resource, amount));
            return this;
        }

        public ResourceStack<T> get(int index) {
            return stacks.get(index);
        }

        public OldResourceStorageContents<T> build() {
            return new OldResourceStorageContents<>(stacks);
        }
    }
}