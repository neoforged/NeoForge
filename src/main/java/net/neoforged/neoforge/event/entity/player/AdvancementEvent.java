/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Base class used for advancement-related events. Should not be used directly.
 * 
 * @see AdvancementEarnEvent
 * @see AdvancementProgressEvent
 */
public abstract class AdvancementEvent extends ServerPlayerEvent {
    private final AdvancementHolder advancement;

    public AdvancementEvent(ServerPlayer player, AdvancementHolder advancement) {
        super(player);
        this.advancement = advancement;
    }

    public AdvancementHolder getAdvancement() {
        return advancement;
    }

    /**
     * Fired when the player earns an advancement. An advancement is earned once its requirements are complete.
     *
     * <p>Note that advancements may be hidden from the player or used in background mechanics, such as recipe
     * advancements for unlocking recipes in the recipe book.</p>
     *
     * <p>This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and does not have a result.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain net.neoforged.fml.LogicalSide#SERVER logical server}.</p>
     *
     * @see AdvancementProgress#isDone()
     */
    public static class AdvancementEarnEvent extends AdvancementEvent {
        public AdvancementEarnEvent(ServerPlayer player, AdvancementHolder earned) {
            super(player, earned);
        }
    }

    /**
     * Fired when the player's progress on an advancement criterion is granted or revoked.
     *
     * <p>This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and does not have a result.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain net.neoforged.fml.LogicalSide#SERVER logical server}.</p>
     *
     * @see AdvancementEarnEvent
     * @see net.minecraft.server.PlayerAdvancements#award(AdvancementHolder, String)
     * @see net.minecraft.server.PlayerAdvancements#revoke(AdvancementHolder, String)
     */
    public static class AdvancementProgressEvent extends AdvancementEvent {
        private final AdvancementProgress advancementProgress;
        private final String criterionName;
        private final AdvancementEvent.AdvancementProgressEvent.ProgressType progressType;

        public AdvancementProgressEvent(ServerPlayer player, AdvancementHolder progressed, AdvancementProgress advancementProgress, String criterionName, AdvancementEvent.AdvancementProgressEvent.ProgressType progressType) {
            super(player, progressed);
            this.advancementProgress = advancementProgress;
            this.criterionName = criterionName;
            this.progressType = progressType;
        }

        /**
         * {@return the progress of the advancement}
         */
        public AdvancementProgress getAdvancementProgress() {
            return advancementProgress;
        }

        /**
         * {@return name of the criterion that was progressed}
         */
        public String getCriterionName() {
            return criterionName;
        }

        /**
         * {@return The type of progress for the criterion in this event}
         */
        public ProgressType getProgressType() {
            return progressType;
        }

        public enum ProgressType {
            GRANT, REVOKE
        }
    }
}
