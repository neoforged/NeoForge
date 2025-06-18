/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.resources;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.handlers.templates.fluids.FluidStackListHandler;
import net.neoforged.neoforge.transfer.handlers.templates.items.ItemStackListHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStackListHandler;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import org.jetbrains.annotations.Nullable;

public class TestResourceTemplateAttachment {
    public static final MapCodec<TestResourceTemplateAttachment> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            NonNullList.codecOf(ItemStack.OPTIONAL_CODEC).fieldOf("items").forGetter(data -> data.itemHandler.copyToList()),
            NonNullList.codecOf(IResourceStack.flatCodec(ItemResource.OPTIONAL_CODEC, ItemResource::withMutableAmount)).fieldOf("item_resources").forGetter(data -> data.itemResourceHandler.copyToList()),
            NonNullList.codecOf(FluidStack.OPTIONAL_CODEC).fieldOf("fluids").forGetter(data -> data.fluidHandler.copyToList())).apply(builder, TestResourceTemplateAttachment::new));

    public static final AttachmentType.Builder<TestResourceTemplateAttachment> BUILDER = AttachmentType.builder(TestResourceTemplateAttachment::new)
            .serialize(holderWith(TestResourceTemplateAttachment.MAP_CODEC, TestResourceTemplateAttachment::setHolder));

    public ItemStackListHandler itemHandler;
    public ResourceStackListHandler<ItemResource> itemResourceHandler;
    public FluidStackListHandler fluidHandler;

    @Nullable
    public BlockEntity blockEntity; // We want to set data, or more accurately: mark the block entity owning this data as `changed`

    public TestResourceTemplateAttachment(IAttachmentHolder holder) {
        this(
                NonNullList.withSize(20, ItemStack.EMPTY), // how many item indices (slots) there are
                NonNullList.withSize(20, ItemResource.EMPTY_MUTABLE_STACK), // how many item indices (slots) there are
                NonNullList.withSize(1, FluidStack.EMPTY)// how many fluid indices (tanks) there are
        );
        setHolder(holder); // Sets the block entity for our callback in the handler
    }

    public TestResourceTemplateAttachment(NonNullList<ItemStack> items, NonNullList<MutableResourceStack<ItemResource>> itemResources, NonNullList<FluidStack> fluids) {
        //the callback passed in allows us, in this case, to inform the block entity of being changed without making the container block entity specific
        //When wanting to handle ItemStacks of size greater than 99 a different codec is needed so it may be simpler to work with ResourceStacks
        itemHandler = new ItemStackListHandler(items, Item.DEFAULT_MAX_STACK_SIZE, this::markBlockEntityAsDirty);
        itemResourceHandler = new ResourceStackListHandler.Item(itemResources, 5000, this::markBlockEntityAsDirty);
        fluidHandler = new FluidStackListHandler(fluids, 4000, this::markBlockEntityAsDirty);
    }

    public void markBlockEntityAsDirty() {
        if (blockEntity != null)
            blockEntity.setChanged();
    }

    private void setHolder(IAttachmentHolder holder) {
        if (holder instanceof BlockEntity be) {
            blockEntity = be;
        }
    }

    public static <T> IAttachmentSerializer<T> holderWith(MapCodec<T> codec, BiConsumer<T, IAttachmentHolder> setter) {
        return new IAttachmentSerializer<>() {
            @Override
            public T read(IAttachmentHolder holder, ValueInput input) {
                final Optional<T> parsingResult = input.read(codec);
                var value = parsingResult.orElseThrow(() -> buildException("read"));
                setter.accept(value, holder);
                return value;
            }

            @Override
            public boolean write(T attachment, ValueOutput output) {
                output.store(codec, attachment);
                return true;
            }

            private RuntimeException buildException(final String operation) {
                return new IllegalStateException("Unable to " + operation + " attachment due to an internal codec error.");
            }
        };
    }
}
