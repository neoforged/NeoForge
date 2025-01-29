/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.container.IHandleIOBehaviour;
import net.neoforged.neoforge.transfer.handlers.templates.container.ResourceContainer;
import net.neoforged.neoforge.transfer.handlers.templates.container.SimpleFluidResourceContainer;
import net.neoforged.neoforge.transfer.handlers.templates.container.SimpleItemResourceContainer;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import org.jetbrains.annotations.Nullable;

// This is the XyCraft example of how a container is used along with the helper (`holderWith`) of how to set the holder during deserializing.
public class TestResourceContainerAttachment {
    public static final Codec<TestResourceContainerAttachment> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceContainer.Codecs.itemResourcesOf("item_resources", data -> data.itemContainer),
            ResourceContainer.Codecs.fluidResourcesOf("fluid_resources", data -> data.fluidContainer),
            Codec.INT.fieldOf("tank_capacity").forGetter(data -> data.fluidContainer.getCapacity(0))).apply(builder, TestResourceContainerAttachment::new));

    public static final AttachmentType.Builder<TestResourceContainerAttachment> BUILDER = AttachmentType.builder(TestResourceContainerAttachment::new).serialize(holderWith(TestResourceContainerAttachment.CODEC, TestResourceContainerAttachment::setHolder));

    public final ResourceContainer<ItemResource> itemContainer;
    public final SimpleFluidResourceContainer fluidContainer;

    public final IResourceHandlerModifiable<ItemResource> input;
    public final IResourceHandlerModifiable<ItemResource> output;
    public final IResourceHandlerModifiable<ItemResource> both;

    public final IResourceHandlerModifiable<FluidResource> fluidHandler;

    @Nullable
    public BlockEntity blockEntity; // We want to set data, or more accurately: mark the block entity owning this data as `changed`

    public TestResourceContainerAttachment(IAttachmentHolder holder) {
        this(
                MutableResourceStack.nonNullListOfSize(20, ItemResource.NONE), // how many item indices (slots) there are
                MutableResourceStack.nonNullListOfSize(1, FluidResource.NONE),// how many fluid indices (tanks) there are
                FluidType.BUCKET_VOLUME * 4 // The amount of fluid each fluid index can hold
        );
        setHolder(holder); // Sets the block entity for our callback in the handler
    }

    public TestResourceContainerAttachment(NonNullList<MutableResourceStack<ItemResource>> items, NonNullList<MutableResourceStack<FluidResource>> fluids, int capacity) {
        //the callback passed in allows us, in this case, to inform the block entity of being changed without making the container block entity specific
        itemContainer = SimpleItemResourceContainer
                .from(items)
                .onChange(this::markBlockEntityAsDirty)
                .build();

        //This shows we can serialize the capacity and set it when we get it back from the codec.
        // The value is set in the instancing constructor above.
        fluidContainer = SimpleFluidResourceContainer.from(fluids).capacity(capacity).onChange(this::markBlockEntityAsDirty).build();

        //Creates a handler from the container from slots [0,10) that allows insert only
        input = itemContainer.slice(0, 10).asHandler(IHandleIOBehaviour.INSERT_ONLY);
        //then creates another slice from slots [10, 20)
        output = itemContainer.slice(10, 20).asHandler(IHandleIOBehaviour.EXTRACT_ONLY);

        //Another way to make a handler from the same backing container with slots [9,...) that allows only extraction
        //noinspection unused
        var alternateExampleOutput = itemContainer.asHandler(new IHandleIOBehaviour() {
            @Override
            public boolean canInsert(int slot) {
                return false;
            }

            @Override
            public boolean canExtract(int slot) {
                return slot >= 9;
            }
        });

        //Creates a handler from a container that allows insertion and extraction but indicates which slots do which
        both = itemContainer.asHandler(new IHandleIOBehaviour() {
            @Override
            public boolean canInsert(int slot) {
                return slot < 10;
            }

            @Override
            public boolean canExtract(int slot) {
                return slot >= 10;
            }
        });

        fluidHandler = fluidContainer.asHandler();
    }

    public void markBlockEntityAsDirty() {
        if (blockEntity != null)
            blockEntity.setChanged();
    }

    private void setHolder(IAttachmentHolder holder) {
        if (holder instanceof BlockEntity be)
            blockEntity = be;
    }

    public static <T> IAttachmentSerializer<Tag, T> holderWith(Codec<T> codec, BiConsumer<T, IAttachmentHolder> setter) {
        return new IAttachmentSerializer<>() {
            @Override
            public T read(IAttachmentHolder holder, Tag tag, HolderLookup.Provider provider) {
                var parse = codec.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag);
                if (parse.error().isPresent()) {
                    throw new RuntimeException(parse.error().get().toString());
                }
                if (parse.result().isEmpty())
                    throw new RuntimeException("Result not present");

                var data = parse.result().get();
                setter.accept(data, holder);
                return data;
            }

            @Override
            public Tag write(T attachment, HolderLookup.Provider provider) {
                var encode = codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), attachment);
                if (encode.error().isPresent()) {
                    throw new RuntimeException(encode.error().get().toString());
                }
                if (encode.result().isEmpty())
                    throw new RuntimeException("Result not present");

                return encode.result().get();
            }
        };
    }
}
