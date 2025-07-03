/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.resources;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

/**
 * Demonstrates a possible option of how to make an IResource using an Enum.
 * It is advised when taking this approach to not use ordinal as the backing serialized id, given you may reorder the entries.
 */
public enum TestElementResource implements IResource, StringRepresentable {
    NONE("none"),
    FIRE("fire"),
    WATER("water"),
    EARTH("earth"),
    AIR("air");

    private final String elementName;
    private static final ResourceStack<TestElementResource> EMPTY = ResourceStack.constructEmptyReference(NONE);

    TestElementResource(String elementName) {
        this.elementName = elementName;
    }

    @Override
    public boolean isEmpty() {
        return this == NONE;
    }

    @Override
    public ResourceStack<? extends IResource> withAmount(int amount) {
        if (amount == 0 || this.isEmpty()) return EMPTY;
        return ResourceStack.of(this, amount, EMPTY);
    }

    @Override
    public String getSerializedName() {
        return elementName;
    }

    public static final StringRepresentableCodec<TestElementResource> CODEC = StringRepresentable.fromEnum(TestElementResource::values);
    public static final StreamCodec<FriendlyByteBuf, TestElementResource> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(TestElementResource.class);
}
