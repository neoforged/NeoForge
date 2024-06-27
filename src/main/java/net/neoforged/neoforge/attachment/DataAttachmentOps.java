/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;

public class DataAttachmentOps<T, TOwner> extends RegistryOps<T> {
    private final TOwner parent;

    protected DataAttachmentOps(RegistryOps<T> regOps, TOwner parent) {
        super(regOps);
        this.parent = parent;
    }

    public static <T, TOwner extends IAttachmentHolder> DataAttachmentOps<T, TOwner> create(HolderLookup.Provider holderLookup, DynamicOps<T> targetOps, TOwner parent) {
        return new DataAttachmentOps<>(holderLookup.createSerializationContext(targetOps), parent);
    }

    public static <O, TOwner extends IAttachmentHolder> RecordCodecBuilder<O, TOwner> holder(Class<TOwner> holder) {
        return ExtraCodecs.retrieveContext(ops -> {
            if (ops instanceof DataAttachmentOps<?, ?> dops) {
                if (holder.isInstance(dops.parent))
                    return DataResult.success(holder.cast(dops.parent));

                return DataResult.error(() -> "Wrong ops passed to function: Not the correct holder type for this attachment.");
            }

            return DataResult.error(() -> "Wrong ops passed to function: Not a data attachment ops");
        }).forGetter(o -> null);
    }
}
