/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;

public class AttachmentOps<T, TOwner> extends RegistryOps<T> {
    final TOwner parent;

    protected AttachmentOps(RegistryOps<T> regOps, TOwner parent) {
        super(regOps);
        this.parent = parent;
    }

    public static <T, TOwner extends IAttachmentHolder> AttachmentOps<T, TOwner> create(HolderLookup.Provider holderLookup, DynamicOps<T> targetOps, TOwner parent) {
        return new AttachmentOps<>(holderLookup.createSerializationContext(targetOps), parent);
    }
}
