/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class HandlerUtils {

    /**
     * Checks if an {@link IResourceHandler} is empty.
     *
     * <p>An {@link IResourceHandler} is considered empty if all of its indices
     * contain either a blank resource or have an amount less than or equal to zero.
     *
     * @param handler the {@link IResourceHandler} to check for emptiness
     * @return {@code true} if the {@link IResourceHandler} is empty, {@code false} otherwise
     */
    public static boolean isEmpty(IResourceHandler<? extends IResource> handler) {
        for (int i = 0; i < handler.size(); i++) {
            if (!isIndexEmpty(handler, i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if an {@link IResourceHandler} is full.
     *
     * <p>An {@code IResourceHandler} is considered full if all of its indices contain resources with amounts
     * greater than or equal to their respective limits.
     *
     * @param handler the {@link IResourceHandler} to check
     * @return {@code true} if the {@link IResourceHandler} is full, {@code false} otherwise
     */
    public static boolean isFull(IResourceHandler<? extends IResource> handler) {
        for (int i = 0; i < handler.size(); i++) {
            if (!isIndexFull(handler, i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a specific index of an {@link IResourceHandler} is empty.
     *
     * <p>An index is considered empty if the resource at the specified index is either blank or
     * the amount of the resource is less than or equal to zero.
     *
     * @param handler the {@link IResourceHandler} to check
     * @param index   the index of the resource to check
     * @return {@code true} if the resource at the specified index is empty, {@code false} otherwise
     */
    public static boolean isIndexEmpty(IResourceHandler<? extends IResource> handler, int index) {
        return handler.getResource(index).isBlank() || handler.getAmount(index) <= 0;
    }

    /**
     * Checks if a specific index of an {@link IResourceHandler} is full.
     *
     * <p>An index is considered full if the amount of the resource at the specified index is greater than or equal to
     * the limit of the resource at the specified index.
     *
     * @param handler the {@link IResourceHandler} to check
     * @param index   the index of the resource to check
     * @return {@code true} if the resource at the specified index is full, {@code false} otherwise
     */
    public static <T extends IResource> boolean isIndexFull(IResourceHandler<T> handler, int index) {
        return handler.getAmount(index) >= handler.getLimit(index, handler.getResource(index));
    }

    /**
     * Calculates the redstone signal strength based on the given resource handler. This value is between 0 and 15.
     * This method is based off of {@link AbstractContainerMenu#getRedstoneSignalFromContainer(Container)}
     *
     * @param handler the resource handler to calculate the signal from
     * @param <T>     the type of resource handled by the handler
     * @return the redstone signal strength
     */
    public static <T extends IResource> int getRedstoneSignalFromHandler(IResourceHandler<T> handler) {
        float proportion = 0.0F;
        int size = handler.size();

        for (int index = 0; index < size; ++index) {
            int indexFill = handler.getAmount(index);
            if (indexFill > 0) {
                proportion += (float) indexFill / handler.getLimit(index, handler.getResource(index));
            }
        }

        proportion /= size;
        return Mth.lerpDiscrete(proportion, 0, 15);
    }

    /**
     * Inserts a resource into an {@link IResourceHandler} using stacking logic.
     * Resources will be inserted into filled slot(s) first, then empty slot(s).
     *
     * @param <T>      the type of resource handled by the handler
     * @param handler  the {@link IResourceHandler} to insert the resource into
     * @param resource the resource to insert
     * @param amount   the desired amount of the resource to insert
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource that was (or would have been, if simulated) inserted
     */
    public static <T extends IResource> int insertStacking(IResourceHandler<T> handler, T resource, int amount, TransferAction action) {
        int inserted = 0;
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            if (HandlerUtils.isIndexEmpty(handler, index)) continue;
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }

        for (int index = 0; index < size; index++) {
            if (!HandlerUtils.isIndexEmpty(handler, index)) continue;
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }

        return inserted;
    }

    /**
     * Inserts a resource into an {@link IResourceHandler} using non-stacking logic.
     * Resources will be inserted into the first slot(s) that can accept the resource.
     *
     * @param <T>      the type of resource handled by the handler
     * @param handler  the {@link IResourceHandler} to insert the resource into
     * @param resource the resource to insert
     * @param amount   the desired amount of the resource to insert
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource that was (or would have been, if simulated) inserted
     */
    public static <T extends IResource> int insert(IResourceHandler<T> handler, T resource, int amount, TransferAction action) {
        int inserted = 0;
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }

        return inserted;
    }

    /**
     * Extracts a resource from an {@link IResourceHandler}
     * Resources will be extracted from the first slot(s) that contain the resource.
     *
     * @param <T>      the type of resource handled by the handler
     * @param handler  the {@link IResourceHandler} to extract the resource from
     * @param resource the resource to extract
     * @param amount   the desired amount of the resource to extract
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> int extract(IResourceHandler<T> handler, T resource, int amount, TransferAction action) {
        int extracted = 0;
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            extracted += handler.extract(index, resource, amount - extracted, action);
            if (extracted >= amount) {
                return extracted;
            }
        }

        return extracted;
    }


    /**
     * Extracts the first resource from an {@link IResourceHandler} that matches the given filter.
     *
     * @param <T>      the type of resource handled by the handler
     * @param handler  the {@link IResourceHandler} to extract the resource from
     * @param filter   the filter to apply to the resources
     * @param amount   the desired amount of the resource to extract
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource that was (or would have been, if simulated) extracted
     */
    @Nullable
    public static <T extends IResource> ResourceStack<T> extractFiltered(IResourceHandler<T> handler, Predicate<T> filter, int amount, TransferAction action) {
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            T resource = handler.getResource(index);
            if (!filter.test(resource)) continue;
            int extract = handler.extract(resource, amount, action);
            if (extract > 0) {
                return new ResourceStack<>(resource, extract);
            }
        }
        return null;
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that is not blank.
     *
     * @param <T>      the type of resource handled by the handler
     * @param handler  the {@link IResourceHandler} to extract the resource from
     * @param amount   the desired amount of the resource to extract
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource and the resource itself that was (or would have been, if simulated) extracted
     */
    @Nullable
    public static <T extends IResource> ResourceStack<T> extractAny(IResourceHandler<T> handler, int amount, TransferAction action) {
        return extractFiltered(handler, Predicate.not(IResource::isBlank), amount, action);
    }

    /**
     * Moves a resource from one {@link IResourceHandler} to another.
     *
     * @param <T>    the type of resource handled by the handlers
     * @param from   the {@link IResourceHandler} to move the resource from
     * @param to     the {@link IResourceHandler} to move the resource to
     * @param amount the desired amount of the resource to move
     * @param action the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *               while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource and the resource itself that was (or would have been, if simulated) moved
     */
    @Nullable
    public static <T extends IResource> ResourceStack<T> moveFiltered(IResourceHandler<T> from, IResourceHandler<T> to, Predicate<T> filter, int amount, TransferAction action) {
        for (int index = 0; index < from.size(); index++) {
            T resource = from.getResource(index);
            if (!filter.test(resource)) continue;
            int extracted = from.extract(resource, amount, TransferAction.SIMULATE);
            int inserted = to.insert(resource, extracted, TransferAction.SIMULATE);
            if (extracted > 0 && inserted > 0) {
                while (extracted != inserted) {
                    extracted = from.extract(resource, inserted, TransferAction.SIMULATE);
                    inserted = to.insert(resource, extracted, TransferAction.SIMULATE);
                    if (extracted == 0 || inserted == 0) {
                        break;
                    }
                }
                if (inserted == 0) continue;
                if (action.isExecuting()) {
                    from.extract(resource, inserted, TransferAction.EXECUTE);
                    to.insert(resource, inserted, TransferAction.EXECUTE);
                }
                return new ResourceStack<>(resource, inserted);
            }
        }
        return null;
    }

    /**
     * Moves a resource from one {@link IResourceHandler} to another.
     *
     * @param <T>    the type of resource handled by the handlers
     * @param from   the {@link IResourceHandler} to move the resource from
     * @param to     the {@link IResourceHandler} to move the resource to
     * @param amount the desired amount of the resource to move
     * @param action the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *               while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource and the resource itself that was (or would have been, if simulated) moved
     */
    @Nullable
    public static <T extends IResource> ResourceStack<T> moveAny(IResourceHandler<T> from, IResourceHandler<T> to, int amount, TransferAction action) {
        return moveFiltered(from, to, Predicate.not(IResource::isBlank), amount, action);
    }
}
