/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.event.entity.living;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * VillagerEvent is fired for events related to villagers.
 * If a method utilizes this event as its parameter, the method will
 * receive every child event of this class.
 *
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
public class VillagerEvent extends EntityEvent {
    private final Villager villager;

    public VillagerEvent(Villager villager) {
        super(villager);
        this.villager = villager;
    }

    @Override
    public Villager getEntity() {
        return villager;
    }

    /**
     * ChangeProfessionEvent is fired on the server when a Villager Entity changes profession.
     *
     * This event is fired via the {@link ForgeEventFactory#onVillagerProfessionChanged(Villager, VillagerProfession, VillagerProfession)}.
     *
     * {@link #getOldProfession()} contains the profession the villager held before the change.
     * {@link #getNewProfession()} contains the profession the villager will hold after the change.
     *
     * This event is {@link Cancelable}. If the event is canceled, no profession change will occur.
     *
     * You can change the profession that the villager will change to via {@link #setNewProfession(VillagerProfession)}.
     *
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     **/
    @Cancelable
    public static class ChangeProfessionEvent extends VillagerEvent {
        private final VillagerProfession oldProfession;
        private VillagerProfession newProfession;

        public ChangeProfessionEvent(Villager villager, VillagerProfession oldProfession, VillagerProfession newProfession) {
            super(villager);
            this.oldProfession = oldProfession;
            this.newProfession = newProfession;
        }

        public VillagerProfession getOldProfession() {
            return oldProfession;
        }

        public VillagerProfession getNewProfession() {
            return newProfession;
        }

        public void setNewProfession(VillagerProfession newProfession) {
            this.newProfession = newProfession;
        }
    }
}
