/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;

public class ServerEntitySendDirtyEntityDataEvent extends EntityEvent {
    ServerEntity serverEntity;

    public ServerEntitySendDirtyEntityDataEvent(Entity entity, ServerEntity serverEntity) {
        super(entity);
        this.serverEntity = serverEntity;
    }
}
