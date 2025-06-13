/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers;

/**
 * A way to group the IEnergyHandler and IResourceHandler.
 * This is an ongoing experiment, but they have been pushed back until later.
 * The goal was to provide a simple utility for composing a transaction; however,
 * that is not feasible in the time constraint of 1.21.6. This interface provides no impedance
 * for addition though, allowing work to be done mod side in the meantime.
 */
public interface ITransactionHandler {}
