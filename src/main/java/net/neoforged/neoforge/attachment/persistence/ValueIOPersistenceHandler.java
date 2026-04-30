/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.persistence;

import com.mojang.logging.LogUtils;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.attachment.storage.AttachmentDataStorage;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

public class ValueIOPersistenceHandler implements AttachmentHolderPersistenceHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final IAttachmentHolder holder;
    protected final AttachmentDataStorage attachmentData;

    public ValueIOPersistenceHandler(IAttachmentHolder holder, AttachmentDataStorage attachmentData) {
        this.holder = holder;
        this.attachmentData = attachmentData;
    }

    @Override
    public void serialize(ValueOutput tag) {
        if (!attachmentData.hasAttachments()) return;
        final var types = attachmentData
                .storedTypes()
                .collect(Collectors.toUnmodifiableSet());

        for (var type : types) {
            var key = NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type);
            if (key == null)
                continue;

            type.serializer().ifPresent(s -> {
                try {
                    final var serialized = tag.child(key.toString());
                    final var value = attachmentData.getData(type);

                    //noinspection unchecked
                    boolean doSerialise = ((IAttachmentSerializer<Object>) s).write(value, serialized);
                    if (!doSerialise) {
                        tag.discard(key.toString());
                    }
                } catch (Exception exception) {
                    LOGGER.error("Failed to serialize data attachment {}. Skipping.", key, exception);
                }
            });
        }
    }

    /**
     * Reads serializable attachments from a tag previously created via {@link #serialize(ValueOutput)}.
     */
    @Override
    public final void deserialize(ValueInput input) {
        for (var key : input.keySet()) {
            // Use tryParse to not discard valid attachment type keys, even if there is a malformed key.
            Identifier keyLocation = Identifier.tryParse(key);
            if (keyLocation == null) {
                LOGGER.error("Encountered invalid data attachment key {}. Skipping.", key);
                continue;
            }

            var type = NeoForgeRegistries.ATTACHMENT_TYPES.getValue(keyLocation);
            if (type == null || type.serializer().isEmpty()) {
                LOGGER.error("Encountered unknown or non-serializable data attachment {}. Skipping.", key);
                continue;
            }

            try {
                type.serializer()
                        .map(s -> s.read(holder, input.rawChildOrEmpty(key)))
                        .ifPresent(attachment -> attachmentData.putDataNoSync(type, attachment));
            } catch (Exception exception) {
                LOGGER.error("Failed to deserialize data attachment {}. Skipping.", key, exception);
            }
        }
    }
}
