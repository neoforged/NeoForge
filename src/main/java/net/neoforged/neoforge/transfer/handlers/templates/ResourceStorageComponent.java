package net.neoforged.neoforge.transfer.handlers.templates;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

public final class ResourceStorageComponent<T extends IResource> implements IResourceData<T> {
    private final NonNullList<ResourceStack<T>> stacks;
    private final int hashCode;
    private final int size;

    /**
     * Intended to be used as a data component on an ItemStack. This stores a immutable list, and any changes needed, create a new list.
     */
    public ResourceStorageComponent(NonNullList<ResourceStack<T>> stacks) {
        this.stacks = stacks;
        int i = 0;
        for (IResourceStack<T> itemstack : stacks) {
            i = i * 31 + itemstack.resource().hashCode();
        }
        this.hashCode = i;
        this.size = stacks.size();
    }

    public ResourceStorageComponent(int size, T emptyResource) {
        this(NonNullList.withSize(size, new ResourceStack<>(emptyResource, 0)));
    }

    public static <T extends IResource> Codec<ResourceStorageComponent<T>> codec(Codec<T> resourceCodec) {
        return NonNullList.codecOf(ResourceStack.codec(resourceCodec)).xmap(ResourceStorageComponent::new, contents -> contents.stacks);
    }

    public static <T extends IResource> StreamCodec<RegistryFriendlyByteBuf, ResourceStorageComponent<T>> streamCodec(StreamCodec<RegistryFriendlyByteBuf, ResourceStack<T>> resourceCodec) {
        return resourceCodec.apply(ByteBufCodecs.collection(NonNullList::<ResourceStack<T>>createWithCapacity)).map(ResourceStorageComponent::new, component -> component.stacks);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public ResourceStack<T> get(int index) {
        return stacks.get(index);
    }
    @Override
    public ResourceStorageComponent<T> modify(int index, T resource, int amount) {
        var list = NonNullList.<ResourceStack<T>>createWithCapacity(stacks.size());
        for (IResourceStack<T> stack : stacks) {
            list.add(stack.immutable());
        }
        list.set(index, new ResourceStack<>(resource, amount));
        return new ResourceStorageComponent<>(list);
    }

    @Override
    public ResourceStorageComponent<T> component() {
        return this;
    }

    @Override
    public ResourceStorageAttachment<T> attachment() {
        var list = NonNullList.<MutableResourceStack<T>>createWithCapacity(stacks.size());
        for (ResourceStack<T> stack : stacks) {
            list.add(stack.mutable());
        }
        return new ResourceStorageAttachment<>(list);
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object otherObj) {
        return IResourceData.equals(this, otherObj);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "ResourceStorageComponent[" + stacks + ']';
    }
}
