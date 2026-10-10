/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.storage;

import java.util.stream.Stream;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.ApiStatus;

public interface AttachmentDataStorage extends AttachmentDataReader, AttachmentDataWriter {
    int size();

    Stream<AttachmentType<?>> storedTypes();

    @ApiStatus.Internal
    void putDataNoSync(AttachmentType<?> type, Object copy);
}
