/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.village;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * VillagerChangeProfessionEvent is fired on the server when a Villager Entity changes profession.
 * This event is fired via the {@link EventHooks#onVillagerProfessionChanged(Villager, VillagerProfession, VillagerProfession)}.
 * You can change the profession that the villager will change to via {@link #setNewProfession(VillagerProfession)}.
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 */
public class VillagerChangeProfessionEvent extends EntityEvent implements ICancellableEvent {
    private final VillagerProfession oldProfession;
    private VillagerProfession newProfession;

    @ApiStatus.Internal
    public VillagerChangeProfessionEvent(Villager villager, VillagerProfession oldProfession, VillagerProfession newProfession) {
        super(villager);
        this.oldProfession = oldProfession;
        this.newProfession = newProfession;
    }

    @Override
    public Villager getEntity() {
        return ((Villager) super.getEntity());
    }

    /**
     * contains the {@link VillagerProfession} the villager held before the change.
     * 
     * @return the {@link VillagerProfession} before the change
     */
    public VillagerProfession getOldProfession() {
        return oldProfession;
    }

    /**
     * contains the {@link VillagerProfession} the villager will hold after the change
     * 
     * @return the {@link VillagerProfession} after the change
     */
    public VillagerProfession getNewProfession() {
        return newProfession;
    }

    public void setNewProfession(VillagerProfession newProfession) {
        this.newProfession = newProfession;
    }
}
