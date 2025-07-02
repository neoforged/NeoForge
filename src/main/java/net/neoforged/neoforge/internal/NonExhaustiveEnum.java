/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Target(ElementType.TYPE)
public @interface NonExhaustiveEnum {}
