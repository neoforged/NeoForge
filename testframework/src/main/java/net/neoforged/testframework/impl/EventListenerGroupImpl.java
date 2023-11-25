package net.neoforged.testframework.impl;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.testframework.Test;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ApiStatus.Internal
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EventListenerGroupImpl implements Test.EventListenerGroup {
    private final Map<Mod.EventBusSubscriber.Bus, EventListenerCollectorImpl> collectors = new HashMap<>();

    @Override
    public EventListenerCollectorImpl getFor(Mod.EventBusSubscriber.Bus bus) {
        return collectors.computeIfAbsent(bus, it -> new EventListenerCollectorImpl());
    }

    public void unregister(Map<Mod.EventBusSubscriber.Bus, IEventBus> buses) {
        collectors.forEach((bus, col) -> col.unregisterAll(buses.get(bus)));
    }

    public void register(Map<Mod.EventBusSubscriber.Bus, IEventBus> buses) {
        collectors.forEach((bus, col) -> col.registerAll(buses.get(bus)));
    }

    public void copyFrom(EventListenerGroupImpl other) {
        other.collectors.forEach((bus, eventListenerCollector) -> {
            final var ours = getFor(bus);
            ours.subscribeActions.addAll(eventListenerCollector.subscribeActions);
            ours.subscribers.addAll(eventListenerCollector.subscribers);
        });
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
}
