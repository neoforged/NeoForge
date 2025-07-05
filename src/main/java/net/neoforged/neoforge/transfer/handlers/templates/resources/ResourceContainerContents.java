/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resources;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.transfer.IStackFactory;
import net.neoforged.neoforge.transfer.handlers.templates.fluids.FluidResourceContainerContents;
import net.neoforged.neoforge.transfer.handlers.templates.items.ItemResourceContainerContents;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import org.jetbrains.annotations.Nullable;

/**
 * A Resource backed variation of {@link ItemContainerContents}. Backed by a non-null list of ResourceStacks.
 * 
 * @param <T> The resource type that will be contained in the resource stacks
 *
 * @see ItemResourceContainerContents
 * @see FluidResourceContainerContents
 */
//Not quite ready for review, but you are welcome to look over it if you wish.
public class ResourceContainerContents<T extends IResource> implements TooltipProvider {
    private static final int NO_SLOT = -1;
    private static final int MAX_SIZE = 256;

    private final NonNullList<ResourceStack<T>> resourceStacks;
    private final IStackFactory<T, ResourceStack<T>> stackFactory;
    private final int hashCode;
    private final T emptyResource;
    @Nullable
    private final ResourceContainerContents<T> defaultedContents;
    private final Function<ResourceStack<T>, Component> hoverNameGetter;

    public static <T extends IResource> ResourceContainerContents<T> emptyOf(T emptyResource, IStackFactory<T, ResourceStack<T>> stackFactory, Function<ResourceStack<T>, Component> hoverNameGetter) {
        return new ResourceContainerContents<>(NonNullList.create(), emptyResource, stackFactory, hoverNameGetter, null);
    }

    protected ResourceContainerContents(NonNullList<ResourceStack<T>> stackList, T emptyResource, IStackFactory<T, ResourceStack<T>> stackFactory, Function<ResourceStack<T>, Component> hoverNameGetter, @Nullable ResourceContainerContents<T> defaultedContents) {
        this.defaultedContents = defaultedContents;
        if (stackList.size() > MAX_SIZE) {
            throw new IllegalArgumentException("Got %d items, but maximum is %d".formatted(stackList.size(), MAX_SIZE));
        }

        this.resourceStacks = stackList;
        this.emptyResource = emptyResource;
        this.hashCode = ResourceStack.hashTypes(stackList);
        this.stackFactory = stackFactory;
        this.hoverNameGetter = hoverNameGetter;
    }

    protected ResourceContainerContents(int size, T emptyResource, IStackFactory<T, ResourceStack<T>> stackFactory, Function<ResourceStack<T>, Component> hoverNameGetter, @Nullable ResourceContainerContents<T> defaultedContents) {
        this(NonNullList.withSize(size, stackFactory.create(emptyResource, 0)), emptyResource, stackFactory, hoverNameGetter, defaultedContents);
    }

    protected ResourceContainerContents(List<ResourceStack<T>> stackList, T emptyResource, IStackFactory<T, ResourceStack<T>> stackFactory, Function<ResourceStack<T>, Component> hoverNameGetter, @Nullable ResourceContainerContents<T> defaultedContents) {
        this(stackList.size(), emptyResource, stackFactory, hoverNameGetter, defaultedContents);
        for (var i = 0; i < stackList.size(); i++) {
            this.resourceStacks.set(i, stackList.get(i));
        }
    }

    public NonNullList<ResourceStack<T>> getCopyOfList() {
        return NonNullList.copyOf(resourceStacks);
    }

    public static <T extends IResource> ResourceContainerContents<T> fromIndices(List<Index<T>> indices, T emptyResource, IStackFactory<T, ResourceStack<T>> stackFactory, Function<ResourceStack<T>, Component> hoverNameGetter, ResourceContainerContents<T> emptyContents) {
        OptionalInt optionalint = indices.stream().mapToInt(Index::index).max();
        if (optionalint.isEmpty()) {
            return emptyContents;
        } else {
            ResourceContainerContents<T> contents = new ResourceContainerContents<>(optionalint.getAsInt() + 1, emptyResource, stackFactory, hoverNameGetter, emptyContents);

            for (Index<T> index : indices) {
                contents.resourceStacks.set(index.index(), index.resourceStack());
            }

            return contents;
        }
    }

    public static <T extends IResource> ResourceContainerContents<T> fromResourceStacks(List<ResourceStack<T>> resourceStackList, T emptyResource, IStackFactory<T, ResourceStack<T>> stackFactory, Function<ResourceStack<T>, Component> hoverNameGetter, ResourceContainerContents<T> emptyContents) {
        int i = findLastNonEmptySlot(resourceStackList);
        if (i == NO_SLOT) {
            return emptyContents;
        }

        ResourceContainerContents<T> contents = new ResourceContainerContents<>(i + 1, emptyResource, stackFactory, hoverNameGetter, emptyContents);

        for (int j = 0; j <= i; j++) {
            contents.resourceStacks.set(j, resourceStackList.get(j));
        }

        return contents;
    }

    private static <T extends IResource> int findLastNonEmptySlot(List<ResourceStack<T>> list) {
        return IntStream.iterate(list.size() - 1, i -> i >= 0, i -> i - 1).filter(i -> !list.get(i).isEmpty()).findFirst().orElse(NO_SLOT);
    }

    public List<Index<T>> asSlots() {
        List<Index<T>> list = new ArrayList<>();

        for (int i = 0; i < this.resourceStacks.size(); i++) {
            ResourceStack<T> resourceStack = this.resourceStacks.get(i);
            if (!resourceStack.isEmpty()) {
                list.add(new Index<>(i, resourceStack, emptyResource, stackFactory));
            }
        }

        return list;
    }

    public void copyInto(NonNullList<ResourceStack<T>> list) {
        for (int i = 0; i < list.size(); i++) {
            ResourceStack<T> resourceStack = i < this.resourceStacks.size() ? this.resourceStacks.get(i) : stackFactory.create(emptyResource, 0);
            //We don't need to copy since the resource stack is immutable
            list.set(i, resourceStack);
        }
    }

    public ResourceStack<T> copyOne() {
        return this.resourceStacks.isEmpty() ? stackFactory.create(emptyResource, 0) : this.resourceStacks.getFirst();
    }

    public Stream<ResourceStack<T>> stream() {
        return this.resourceStacks.stream();
    }

    public Stream<ResourceStack<T>> nonEmptyStream() {
        return this.resourceStacks.stream().filter(p_331322_ -> !p_331322_.isEmpty());
    }

    public Iterable<ResourceStack<T>> nonEmptyResourceStacks() {
        return Iterables.filter(this.resourceStacks, p_331420_ -> !p_331420_.isEmpty());
    }

    @Override
    public boolean equals(Object p_331711_) {
        return this == p_331711_
                || p_331711_ instanceof ResourceContainerContents<?> containerContents
                        && this.resourceStacks.equals(containerContents.resourceStacks);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> toolTipAdder, TooltipFlag tooltipFlag, DataComponentGetter dataGetter) {
        int i = 0;
        int j = 0;

        //While this is odd, this is matching vanilla's methodology. We may re-evaluate this later.
        for (ResourceStack<T> resourceStack : this.nonEmptyResourceStacks()) {
            j++;
            if (i <= 4) {
                i++;
                //TODO how do we add lang in neo? (both of these will likely need one) Or should we try to use the defaulted ones?
                // ItemContainerContents lang: `item.container.item_count` & `item.container.more_items`
                toolTipAdder.accept(Component.translatable("resource.container.stack_count", hoverNameGetter.apply(resourceStack), resourceStack.amount()));
            }
        }

        if (j - i > 0) {
            toolTipAdder.accept(Component.translatable("resource.container.more_stacks", j - i).withStyle(ChatFormatting.ITALIC));
        }
    }

    /**
     * {@return the number of slots in this container}
     */
    public int getSlots() {
        return this.resourceStacks.size();
    }

    /**
     * Gets a copy of the resource stack at a particular index.
     *
     * @param index The index to check. Must be within [0, {@link #getSlots()}]
     * @return A copy of the stack in that index
     * @throws IndexOutOfBoundsException if the provided index is out-of-bounds.
     */
    public ResourceStack<T> getStackInSlot(int index) {
        Objects.checkIndex(index, this.getSlots());
        return this.resourceStacks.get(index);
    }

    public ResourceContainerContents<T> with(int size, int index, T resource, int amount) {
        return with(size, index, stackFactory.create(resource, amount));
    }

    public ResourceContainerContents<T> with(int size, int index, ResourceStack<T> stack) {
        NonNullList<ResourceStack<T>> list = NonNullList.withSize(size, stackFactory.create(emptyResource, 0));
        copyInto(list);
        list.set(index, stack);
        return fromResourceStacks(list, emptyResource, stackFactory, hoverNameGetter, defaultedContents == null ? this : defaultedContents);
    }

    public record Index<T extends IResource>(int index, ResourceStack<T> resourceStack, T emptyResource,
            IStackFactory<T, ResourceStack<T>> stackFactory) {
        public static <T extends IResource> Codec<Index<T>> codec(Codec<T> stackCodec, T emptyResource, IStackFactory<T, ResourceStack<T>> stackFactory) {
            return RecordCodecBuilder.create(
                    p_331695_ -> p_331695_.group(
                            Codec.intRange(0, 255).fieldOf("index").forGetter(Index::index),
                            ResourceStack.codec(stackCodec, stackFactory).fieldOf("resource").forGetter(Index::resourceStack)).apply(p_331695_, (index, stack) -> new Index<>(index, stack, emptyResource, stackFactory)));
        }
    }
}
