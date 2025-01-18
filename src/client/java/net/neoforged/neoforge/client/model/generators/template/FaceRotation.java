/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

public enum FaceRotation {
    ZERO(0),
    CLOCKWISE_90(90),
    UPSIDE_DOWN(180),
    COUNTERCLOCKWISE_90(270),
    ;

    final int rotation;

    FaceRotation(int rotation) {
        this.rotation = rotation;
    }
}
