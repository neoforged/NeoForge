package net.neoforged.neoforge.transfer.handlers;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.ResourceStack;

public class ResourceStorageContents<T extends IResource> {
    private final NonNullList<ResourceStack<T>> stacks;

    public static <T extends IResource> Codec<ResourceStorageContents<T>> codec(Codec<T> resourceCodec) {
        return NonNullList.codecOf(ResourceStack.codec(resourceCodec)).xmap(ResourceStorageContents::new, contents -> contents.stacks);
    }

    public ResourceStorageContents(int size, T emptyResource) {
        stacks = NonNullList.withSize(size, new ResourceStack<>(emptyResource, 0));
    }

    private ResourceStorageContents(NonNullList<ResourceStack<T>> stacks) {
        this.stacks = stacks;
    }

    public ResourceStack<T> get(int index) {
        return stacks.get(index);
    }

    public ResourceStorageContents<T> set(int index, T resource, int amount) {
        NonNullList<ResourceStack<T>> newStacks = NonNullList.copyOf(stacks);
        newStacks.set(index, new ResourceStack<>(resource, amount));
        return new ResourceStorageContents<>(newStacks);
    }

    public Builder<T> builder() {
        return new Builder<>(stacks);
    }

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

        public ResourceStorageContents<T> build() {
            return new ResourceStorageContents<>(stacks);
        }
    }
}