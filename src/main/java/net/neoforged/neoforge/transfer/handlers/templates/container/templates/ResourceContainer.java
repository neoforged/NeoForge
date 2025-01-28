package net.neoforged.neoforge.transfer.handlers.templates.container.templates;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.templates.container.IResourceContainer;
import net.neoforged.neoforge.transfer.handlers.templates.container.adapters.ItemContainerToVanillaAdapter;
import net.neoforged.neoforge.transfer.resources.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

/**
 * A data storage for mutable resource stacks. This data can be put anywhere (with limited exceptions such as DataComponents), but it was designed with {@link net.neoforged.neoforge.attachment.AttachmentType DataAttachments} in mind.
 * You are able to build new containers, slice existing ones, as well as convert them to other types such as an {@link net.neoforged.neoforge.transfer.handlers.IResourceHandler IResourceHandler}.
 * To be more clear, the container itself, but by calling {@link #asHandler()} it will create one, though it is recommended you cache this rather than call it every time you need a handler of the container.
 *
 * <strong>Example Usage</strong>
 * <pre>
 * {@code
 *   var container = SimpleItemResourceContainer.from(someSerializedList)
 *          .onChange(this::markHolderAsDirty)
 *          .build();
 *   IResourceHandler<ItemResource> handler = container.asHandler();
 *   var outputContainer = container.slice(3, 4);
 *   var outputHandler = outputContainer.asHandler(IHandleIOBehaviour.EXTRACT_ONLY);
 * }
 * </pre>
 * <p>
 * To reiterate, this can work anywhere in a mutable context, but things like {@link net.minecraft.core.component.DataComponentType DataComponents} that require an immutable scope will not work properly.
 *
 * @param <T> resource type
 */
public class ResourceContainer<T extends IResource> implements IResourceContainer<T> {
    private final NonNullList<MutableResourceStack<T>> resourceStacks;
    private final @Nullable Runnable updateCallback;
    private final ResourceStack<T> emptyStack;
    private final int size;
    private final int capacity;

    public ResourceContainer(NonNullList<MutableResourceStack<T>> resourceStacks, ResourceStack<T> emptyStack, int capacity, @Nullable Runnable updateCallback) {
        Objects.requireNonNull(resourceStacks);
        Objects.requireNonNull(emptyStack);
        if (!emptyStack.isEmpty())
            throw new IllegalArgumentException("`emptyStack` for container must be empty!");

        Objects.checkIndex(0, resourceStacks.size());
        this.size = resourceStacks.size();
        this.resourceStacks = resourceStacks;
        this.updateCallback = updateCallback;
        this.emptyStack = emptyStack;
        this.capacity = capacity;
    }

    public static final class Codecs {

        public static <TAttachment, TResource extends IResource>
        RecordCodecBuilder<TAttachment, NonNullList<MutableResourceStack<TResource>>>
        resourcesOf(String key, Codec<TResource> codec, Function<TAttachment, IResourceContainer<TResource>> containerToStackList) {
            return NonNullList.codecOf(MutableResourceStack.flatCodec(codec)).fieldOf(key).forGetter(attachment -> containerToStackList.apply(attachment).copyToList());
        }
        public static <TAttachment> RecordCodecBuilder<TAttachment, NonNullList<MutableResourceStack<ItemResource>>>
        itemResourcesOf(String key, Function<TAttachment, IResourceContainer<ItemResource>> containerToStackList) {
            return resourcesOf(key, ItemResource.OPTIONAL_CODEC, containerToStackList);
        }
        public static <TAttachment> RecordCodecBuilder<TAttachment, NonNullList<MutableResourceStack<FluidResource>>>
        fluidResourcesOf(String key, Function<TAttachment, IResourceContainer<FluidResource>> containerToStackList) {
            return resourcesOf(key, FluidResource.OPTIONAL_CODEC, containerToStackList);
        }
    }


    public ResourceStack<T> defaultResource() {
        return emptyStack;
    }

    @Override
    public int size() {
        return size;
    }
    @Override
    public int getCapacity(int index) {
        return capacity;
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        return true;
    }


    //An item resource container will override this and use which ever is smaller, the resource stack or the capacity
    @Override
    public int getCapacity(int index, T resource) {
        return capacity;
    }

    @Override
    public MutableResourceStack<T> get(int index) {
        Objects.checkIndex(index, size());
        return resourceStacks.get(index);
    }

    @Override
    public void set(int index, MutableResourceStack<T> stack) {
        Objects.checkIndex(index, size());
        resourceStacks.set(index, stack);
        if (updateCallback != null)
            updateCallback.run();
    }

    @Override
    public void clear() {
        Collections.fill(resourceStacks, defaultResource().mutable());
        if (updateCallback != null)
            updateCallback.run();
    }

    @Override
    public IResourceContainer<T> slice(int from, int to) {
        Objects.checkFromToIndex(from, to, size());
        return new Slice(from, to - from);
    }


    /**
     * Creates a vanilla {@link Container} instance that reflects this item holder.
     *
     * @return The container.
     */
    @Contract(pure = true)
    public Container asVanillaContainer() {
        if (defaultResource().resource() instanceof ItemResource) {
            //noinspection unchecked
            return new ItemContainerToVanillaAdapter((ResourceContainer<ItemResource>) this);
        }

        return EMPTY;
    }

    private static final Container EMPTY = new Container() {
        @Override
        public void clearContent() {

        }
        @Override
        public int getContainerSize() {
            return 0;
        }
        @Override
        public boolean isEmpty() {
            return false;
        }
        @Override
        public ItemStack getItem(int p_18941_) {
            return ItemStack.EMPTY;
        }
        @Override
        public ItemStack removeItem(int p_18942_, int p_18943_) {
            return ItemStack.EMPTY;
        }
        @Override
        public ItemStack removeItemNoUpdate(int p_18951_) {
            return ItemStack.EMPTY;
        }
        @Override
        public void setItem(int p_18944_, ItemStack p_18945_) {

        }
        @Override
        public void setChanged() {

        }
        @Override
        public boolean stillValid(Player p_18946_) {
            return false;
        }
    };


    private class Slice implements IResourceContainer<T> {
        private final int start, length;

        public Slice(int start, int length) {
            this.start = start;
            this.length = length;
        }

        @Override
        public int size() {
            return length;
        }

        @Override
        public MutableResourceStack<T> get(int index) {
            Objects.checkIndex(index, size()); //audit called in the this.get
            return ResourceContainer.this.get(index + start);
        }

        @Override
        public void set(int index, MutableResourceStack<T> stack) {
            Objects.checkIndex(index, size()); //audit called in the this.get
            ResourceContainer.this.set(index + start, stack);
        }

        @Override
        public int getCapacity(int index, T resource) {
            Objects.checkIndex(index, size()); //audit called in the this.get
            return ResourceContainer.this.getCapacity(index + start, resource);
        }

        @Override
        public int getCapacity(int index) {
            Objects.checkIndex(index, size()); //audit called in the this.get
            return ResourceContainer.this.getCapacity(index + start);
        }

        @Override
        public boolean isValid(int index, T stack) {
            Objects.checkIndex(index, size()); //audit called in the this.get
            return ResourceContainer.this.isValid(index + start, stack);
        }

        @Override
        public void clear() {
            for (int i = 0; i < length; i++)
                ResourceContainer.this.resourceStacks.set(i + start, defaultResource().mutable());
            if (ResourceContainer.this.updateCallback != null)
                ResourceContainer.this.updateCallback.run();
        }

        @Override
        public IResourceContainer<T> slice(int from, int to) {
            Objects.checkFromToIndex(from, to, length);
            return new Slice(this.start + from, to - from);
        }

        @Override
        public ResourceStack<T> defaultResource() {
            return ResourceContainer.this.defaultResource();
        }
    }

    public static class Builder<T extends IResource, TBuilder extends Builder<T, TBuilder>> {
        protected int capacity;
        protected @Nullable Runnable updateCallback;
        protected NonNullList<MutableResourceStack<T>> stacks;
        protected ResourceStack<T> emptyStack;

        public Builder(ResourceStack<T> emptyStack) {
            this.emptyStack = emptyStack;
        }

        private TBuilder self() {
            //noinspection unchecked
            return (TBuilder) this;
        }

        public TBuilder size(int size) {
            this.stacks = NonNullList.withSize(size, emptyStack.mutable());
            return self();
        }

        public TBuilder capacity(int capacity) {
            this.capacity = capacity;
            return self();
        }

        public TBuilder onChange(Runnable updateCallback) {
            this.updateCallback = updateCallback;
            return self();
        }

        public TBuilder from(NonNullList<MutableResourceStack<T>> stacks) {
            this.stacks = stacks;
            return self();
        }

        public ResourceContainer<T> build() {
            return new ResourceContainer<>(stacks, emptyStack, capacity, updateCallback);
        }

        public final ResourceContainer<T> buildRaw() {
            return new ResourceContainer<>(stacks, emptyStack, capacity, updateCallback);
        }
    }
}