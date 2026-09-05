/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.capability;

import net.neoforged.neoforge.attachment.AttachmentDataAccessor;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;

public interface AttachmentHolderCapabilityExtension extends IAttachmentHolder, AttachmentDataAccessor {
    @Nullable
    default <T> T getCapability(AttachmentCapability<T, @Nullable Void> cap) {
        return cap.getCapability(this, null);
    }

    @Nullable
    default <T, C> T getCapability(AttachmentCapability<T, C> cap, C context) {
        return cap.getCapability(this, context);
    }
}
