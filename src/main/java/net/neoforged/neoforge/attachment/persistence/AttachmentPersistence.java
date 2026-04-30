/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.persistence;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForgeMod;

public class AttachmentPersistence {
    public static final String ATTACHMENTS_NBT_KEY = "neoforge:attachments";

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "data_attachments");
}
