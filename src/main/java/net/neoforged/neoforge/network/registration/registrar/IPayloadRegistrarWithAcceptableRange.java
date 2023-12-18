/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration.registrar;

public interface IPayloadRegistrarWithAcceptableRange extends IVersionedPayloadRegistrar {

    IPayloadRegistrarWithAcceptableRange withMinimalVersion(int min);

    IPayloadRegistrarWithAcceptableRange withMaximalVersion(int max);

    IPayloadRegistrarWithAcceptableRange optional();

    default IPayloadRegistrarWithAcceptableRange withAcceptableRange(int min, int max) {
        return withMinimalVersion(min).withMaximalVersion(max);
    }
}
