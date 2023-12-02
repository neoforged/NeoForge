/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Represents a pair of chunk-loaded ticket sets.
 *
 * @param nonTicking the non-fully ticking tickets
 * @param ticking    the fully ticking tickets
 */
public record TicketSet(LongSet nonTicking, LongSet ticking) {}
