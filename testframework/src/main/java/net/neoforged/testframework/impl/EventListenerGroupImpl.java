/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.testframework.Test;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EventListenerGroupImpl implements Test.EventListenerGroup {
    private final EventListenerCollectorImpl mod = new EventListenerCollectorImpl(),
            game = new EventListenerCollectorImpl();

    @Override
    public EventListenerCollector mod() {
        return mod;
    }

    @Override
    public EventListenerCollector forge() {
        return game;
    }

    public void unregister(BusSet set) {
        mod.unregisterAll(set.mod);
        game.unregisterAll(set.game);
    }

    public void register(BusSet set) {
        mod.registerAll(set.mod);
        game.registerAll(set.game);
    }

    public void copyFrom(EventListenerGroupImpl other) {
        this.mod.subscribeActions.addAll(other.mod.subscribeActions);
        this.mod.subscribers.addAll(other.mod.subscribers);

        this.game.subscribeActions.addAll(other.game.subscribeActions);
        this.game.subscribers.addAll(other.game.subscribers);
    }

    private static final class EventListenerCollectorImpl implements EventListenerCollector {
        private final List<Consumer<IEventBus>> subscribeActions = new ArrayList<>();
        private final List<Object> subscribers = new ArrayList<>();

        @Override
        public void register(Object object) {
            subscribeActions.add(bus -> bus.register(object));
            subscribers.add(object);
        }

        @Override
        public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Class<T> eventType, Consumer<T> consumer) {
            subscribeActions.add(bus -> bus.addListener(priority, receiveCancelled, eventType, consumer));
            subscribers.add(consumer);
        }

        @Override
        public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> consumer) {
            subscribeActions.add(bus -> bus.addListener(priority, receiveCancelled, consumer));
            subscribers.add(consumer);
        }

        @Override
        public void unregisterAll(IEventBus bus) {
            subscribers.forEach(bus::unregister);
            subscribers.clear();
            subscribeActions.clear();
        }

        @Override
        public void registerAll(IEventBus bus) {
            subscribeActions.forEach(c -> c.accept(bus));
        }
    }

    public record BusSet(IEventBus mod, IEventBus game) {}
}
