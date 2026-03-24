/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import it.unimi.dsi.fastutil.objects.ObjectBooleanBiConsumer;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientDebugSubscriber;
import net.minecraft.util.debug.DebugSubscription;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event allows mods to register client-side {@linkplain DebugSubscription debug subscription} flags.
 * <p>
 * These flags are used to determine when subscriptions are allowed to register and store debug renderer values.
 * If no flag is registered for a given debug subscription then it will be treated as a {@code always-disabled} flag and no debug values will be stored for it.
 * <p>
 * This event is fired once per tick during {@linkplain ClientDebugSubscriber#requestedSubscriptions()}.
 * <p>
 * This event is fired on the {@linkplain LogicalSide#CLIENT logical client}.
 *
 * @apiNote Each {@linkplain DebugSubscription debug subscription} should only be registered once.
 */
public final class AddDebugSubscriptionFlagsEvent extends Event {
    private final ObjectBooleanBiConsumer<DebugSubscription<?>> registrar;

    @ApiStatus.Internal
    public AddDebugSubscriptionFlagsEvent(ObjectBooleanBiConsumer<DebugSubscription<?>> registrar) {
        this.registrar = registrar;
    }

    /**
     * Register a new flag for the given {@linkplain DebugSubscription debug subscription}.
     *
     * @param subscription {@linkplain DebugSubscription debug subscription} to register flag for.
     * @param flag         Flag used to conditionally enable or disable the given {@linkplain DebugSubscription debug subscription}.
     */
    public void addFlag(DebugSubscription<?> subscription, boolean flag) {
        registrar.accept(subscription, flag);
    }

    /**
     * Register a new flag for the given {@linkplain DebugSubscription debug subscription}.
     *
     * @param subscription {@linkplain DebugSubscription Debug subscription} to register flag for.
     * @param flag         Flag used to conditionally enable or disable the given {@linkplain DebugSubscription debug subscription}.
     */
    public void addFlag(Supplier<? extends DebugSubscription<?>> subscription, boolean flag) {
        addFlag(subscription.get(), flag);
    }

    /**
     * Mark the given {@linkplain DebugSubscription debug subscription} as only active in dev.
     * <p>
     * Using this method will mark your {@linkplain DebugSubscription debug subscription} as only being enabled in dev a.k.a when {@linkplain FMLEnvironment#isProduction()} == {@code false}.
     *
     * @param subscription {@linkplain DebugSubscription debug subscription} to register flag for.
     */
    public void addActiveInDev(DebugSubscription<?> subscription) {
        addFlag(subscription, !FMLEnvironment.isProduction());
    }

    /**
     * Mark the given {@linkplain DebugSubscription debug subscription} as only active in dev.
     * <p>
     * Using this method will mark your {@linkplain DebugSubscription debug subscription} as only being enabled in dev a.k.a when {@linkplain FMLEnvironment#isProduction()} == {@code false}.
     *
     * @param subscription {@linkplain DebugSubscription Debug subscription} to register flag for.
     */
    public void addActiveInDev(Supplier<? extends DebugSubscription<?>> subscription) {
        addActiveInDev(subscription.get());
    }
}
