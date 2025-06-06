/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class ItemContextResourceHandler<T extends IResource> implements ISingleResourceHandler<T> {
    protected final IItemContext itemContext;
    protected final DataComponentType<Component<T>> componentType;
    protected final Component<T> defaultComponent;
    protected final Predicate<T> validator;

    public ItemContextResourceHandler(IItemContext itemContext, DataComponentType<Component<T>> componentType, Component<T> defaultComponent) {
        this(itemContext, componentType, defaultComponent, r -> true);
    }

    public ItemContextResourceHandler(IItemContext itemContext, DataComponentType<Component<T>> componentType, Component<T> defaultComponent, Predicate<T> validator) {
        this.itemContext = itemContext;
        this.componentType = componentType;
        this.defaultComponent = defaultComponent;
        this.validator = validator;
    }

    private Component<T> getComponent() {
        return itemContext.getResource().getOrDefault(componentType, defaultComponent);
    }

    @Override
    public T getResource(int index) {
        return getComponent().resourceStack().resource();
    }

    @Override
    public int getAmount(int index) {
        return getSingleItemAmount() * itemContext.getAmount();
    }

    protected int getSingleItemAmount() {
        return getComponent().resourceStack().amount();
    }

    @Override
    public int getCapacity(int index, T resource) {
        //This ignores say the resource size limits at the moment. As well as possibly able to overflow if done incorrectly
        return getSingleItemLimit() * itemContext.getAmount();
    }

    private int getSingleItemLimit() {
        return getComponent().singleItemLimit();
    }

    @Override
    public boolean isValid(int index, T resource) {
        return validator.test(resource);
    }

    public boolean isEmpty() {
        return !itemContext.getResource().has(componentType);
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount) || !isValid(0, resource)) return 0;
        T presentResource = getResource(0);
        var singleItemLimit = getSingleItemLimit();

        if (presentResource.isEmpty()) {
            if (amount < singleItemLimit)
                return setPartial(resource, amount, singleItemLimit, transaction) == 1 ? amount : 0;
            return setFull(resource, amount / singleItemLimit, singleItemLimit, transaction) * singleItemLimit;
        }

        if (!presentResource.equals(resource)) return 0;

        int containerFill = getSingleItemAmount();
        int spaceLeft = singleItemLimit - containerFill;
        if (amount < spaceLeft)
            return setPartial(resource, amount + containerFill, singleItemLimit, transaction) == 1 ? amount : 0;
        return setFull(resource, amount / spaceLeft, singleItemLimit, transaction) * spaceLeft;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount) || isEmpty() || !getResource(0).equals(resource)) return 0;
        int containerFill = getSingleItemAmount();
        if (amount < containerFill) {
            var singleItemLimit = getSingleItemLimit();

            int exchanged = setPartial(resource, containerFill - amount, singleItemLimit, transaction);
            return exchanged == 1 ? amount : 0;
        } else {
            int extractedCount = amount / containerFill;
            int exchanged = empty(extractedCount, transaction);
            return exchanged * containerFill;
        }
    }

    protected int empty(int count, TransactionContext transaction) {
        ItemResource emptiedContainer = itemContext.getResource().without(componentType);
        return itemContext.exchange(emptiedContainer, count, transaction);
    }

    protected int setFull(T resource, int count, int singleItemLimit, TransactionContext transaction) {
        ItemResource filledContainer = itemContext.getResource().with(componentType, new Component<>(new ResourceStack<>(resource, singleItemLimit), singleItemLimit));
        return itemContext.exchange(filledContainer, count, transaction);
    }

    protected int setPartial(T resource, int amount, int singleItemLimit, TransactionContext transaction) {
        ItemResource filledContainer = itemContext.getResource().with(componentType, new Component<>(new ResourceStack<>(resource, amount), singleItemLimit));
        return itemContext.exchange(filledContainer, 1, transaction);
    }

    public record Component<T extends IResource>(ResourceStack<T> resourceStack, int singleItemLimit) {
        public static <T extends IResource> Codec<Component<T>> codec(Codec<ResourceStack<T>> codec) {
            return RecordCodecBuilder.create(instance -> instance.group(
                    codec.fieldOf("resource_stack").forGetter(Component::resourceStack),
                    Codec.INT.fieldOf("single_item_limit").forGetter(Component::singleItemLimit)).apply(instance, Component::new));
        }

        public static <T extends IResource> StreamCodec<RegistryFriendlyByteBuf, Component<T>> streamCodec(StreamCodec<RegistryFriendlyByteBuf, ResourceStack<T>> streamCodec) {
            return StreamCodec.composite(
                    streamCodec, Component::resourceStack,
                    ByteBufCodecs.INT, Component::singleItemLimit,
                    Component::new);
        }
    }
}
