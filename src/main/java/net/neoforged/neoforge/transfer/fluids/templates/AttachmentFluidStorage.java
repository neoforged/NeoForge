/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluids.templates;

import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;

import java.util.function.Predicate;

public class AttachmentFluidStorage implements ISingleResourceHandler<FluidResource> {
    private final AttachmentHolder holder;
    private final AttachmentType<SimpleFluidContent> attachmentType;
    private final int limit;
    private Predicate<FluidResource> validator = r -> true;

    public AttachmentFluidStorage(AttachmentHolder holder, AttachmentType<SimpleFluidContent> attachmentType, int limit) {
        this.holder = holder;
        this.attachmentType = attachmentType;
        this.limit = limit;
    }

    public AttachmentFluidStorage setValidator(Predicate<FluidResource> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public FluidResource getResource() {
        return holder.getData(attachmentType).getResource();
    }

    @Override
    public int getAmount() {
        return holder.getData(attachmentType).getAmount();
    }

    @Override
    public int getCapacity(FluidResource resource) {
        return limit;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public boolean isValid(FluidResource resource) {
        return validator.test(resource);
    }

    public boolean isEmpty() {
        return holder.getData(attachmentType).isEmpty();
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0 || !isValid(resource) || (!isEmpty() && !getResource().equals(resource))) return 0;
        int inserted = Math.min(amount, getCapacity(resource) - getAmount());
        if (inserted > 0 && action.isExecuting()) {
            holder.setData(attachmentType, SimpleFluidContent.of(resource, getAmount() + inserted));
        }
        return inserted;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0 || !isValid(resource) || (isEmpty() || !getResource().equals(resource))) return 0;
        int extracted = Math.min(amount, getAmount());
        if (extracted > 0 && action.isExecuting()) {
            int newAmount = getAmount() - extracted;
            holder.setData(attachmentType, newAmount <= 0 ? SimpleFluidContent.EMPTY : SimpleFluidContent.of(resource, newAmount));
        }
        return extracted;
    }

    @Override
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }
}
