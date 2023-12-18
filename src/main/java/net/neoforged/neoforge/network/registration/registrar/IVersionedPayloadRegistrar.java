/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration.registrar;

public interface IVersionedPayloadRegistrar extends IFlowBasedPayloadRegistrar {

    IPayloadRegistrarWithAcceptableRange withVersion(int version);
}
