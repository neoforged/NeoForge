/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.impl.ReflectionUtils;

public class MethodBasedEventTest extends AbstractTest.Dynamic {
    protected MethodHandle handle;
    private final Method method;

    private final Class<? extends Event> eventClass;
    private final EventBusSubscriber.Bus bus;
    private final EventPriority priority;
    private final boolean receiveCancelled;

    public MethodBasedEventTest(Method method) {
        this.method = method;
        configureFrom(AnnotationHolder.method(method));

        //noinspection unchecked
        this.eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
        this.bus = IModBusEvent.class.isAssignableFrom(eventClass) ? EventBusSubscriber.Bus.MOD : EventBusSubscriber.Bus.GAME;

        final SubscribeEvent seAnnotation = method.getAnnotation(SubscribeEvent.class);
        if (seAnnotation == null) {
            priority = EventPriority.NORMAL;
            receiveCancelled = false;
        } else {
            priority = seAnnotation.priority();
            receiveCancelled = seAnnotation.receiveCanceled();
        }

        this.handle = ReflectionUtils.handle(method);
    }

    public MethodBasedEventTest bindTo(Object target) {
        handle = handle.bindTo(target);
        return this;
    }

    @Override
    public void onEnabled(Test.EventListenerGroup buses) {
        super.onEnabled(buses);
        buses.getFor(bus).addListener(priority, receiveCancelled, eventClass, event -> {
            try {
                handle.invoke(event, this);
            } catch (Throwable throwable) {
                framework.logger().warn("Encountered exception firing event listeners for method-based event test {}: ", method, throwable);
            }
        });
    }
}
