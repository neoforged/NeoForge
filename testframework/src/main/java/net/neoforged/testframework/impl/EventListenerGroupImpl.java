package net.neoforged.testframework.impl;

import net.neoforged.testframework.Test;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
class EventListenerGroupImpl implements Test.EventListenerGroup {
    private final Map<Mod.EventBusSubscriber.Bus, IEventBus> buses = new HashMap<>();
    private final Map<Mod.EventBusSubscriber.Bus, EventListenerCollectorImpl> collectors = new HashMap<>();
    public EventListenerGroupImpl add(Mod.EventBusSubscriber.Bus type, IEventBus bus) {
        buses.put(type, bus);
        return this;
    }

    @Override
    public EventListenerCollector getFor(Mod.EventBusSubscriber.Bus bus) {
        return collectors.computeIfAbsent(bus, it -> new EventListenerCollectorImpl(buses.get(bus)));
    }

    public void unregister() {
        collectors.values().forEach(EventListenerCollectorImpl::unregisterAll);
    }

    private static final class EventListenerCollectorImpl implements EventListenerCollector {
        private final IEventBus bus;
        private final List<Object> subscribers = new ArrayList<>();

        private EventListenerCollectorImpl(IEventBus bus) {
            this.bus = bus;
        }

        @Override
        public void register(Object object) {
            bus.register(object);
            subscribers.add(object);
        }

        @Override
        public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Class<T> eventType, Consumer<T> consumer) {
            bus.addListener(priority, receiveCancelled, eventType, consumer);
            subscribers.add(consumer);
        }

        @Override
        public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> consumer) {
            bus.addListener(priority, receiveCancelled, consumer);
            subscribers.add(consumer);
        }

        @Override
        public void unregisterAll() {
            subscribers.forEach(bus::unregister);
            subscribers.clear();
        }
    }
}
