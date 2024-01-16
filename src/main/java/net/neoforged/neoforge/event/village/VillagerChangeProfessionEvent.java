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

/**
 * ChangeProfessionEvent is fired on the server when a Villager Entity changes profession.
 * This event is fired via the {@link EventHooks#onVillagerProfessionChanged(Villager, VillagerProfession, VillagerProfession)}.
 * This event is {@link ICancellableEvent cancellable}. If the event is canceled, no profession change will occur.
 * You can change the profession that the villager will change to via {@link #setNewProfession(VillagerProfession)}.
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 */
public class VillagerChangeProfessionEvent extends EntityEvent implements ICancellableEvent {
    private final Villager villager;
    private final VillagerProfession oldProfession;
    private VillagerProfession newProfession;

    public VillagerChangeProfessionEvent(Villager villager, VillagerProfession oldProfession, VillagerProfession newProfession) {
        super(villager);
        this.villager = villager;
        this.oldProfession = oldProfession;
        this.newProfession = newProfession;
    }

    @Override
    public Villager getEntity() {
        return villager;
    }

    /**
     * contains the profession the villager held before the change.
     * 
     * @return the profession before the change
     */
    public VillagerProfession getOldProfession() {
        return oldProfession;
    }

    /**
     * contains the profession the villager will hold after the change
     * 
     * @return the profession after the change
     */
    public VillagerProfession getNewProfession() {
        return newProfession;
    }

    public void setNewProfession(VillagerProfession newProfession) {
        this.newProfession = newProfession;
    }
}
