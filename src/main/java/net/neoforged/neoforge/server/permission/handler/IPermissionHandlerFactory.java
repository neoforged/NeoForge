/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.permission.handler;

import java.util.Collection;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

@FunctionalInterface
public interface IPermissionHandlerFactory {
    IPermissionHandler create(Collection<PermissionNode<?>> permissions);
}
