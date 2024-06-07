/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers;

import net.neoforged.neoforge.transfer.IResource;

public interface IResourceHandlerModifiable<T extends IResource> extends IResourceHandler<T> {
    void set(int index, T resource, int amount);
}
