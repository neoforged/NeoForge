/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.impl.TestFrameworkInternal;

/**
 * Annotate a static method accepting exactly one parameter of {@linkplain TestFrameworkInternal} (or parent interfaces) to
 * register that method as an on-init listener, which will be called in {@link TestFrameworkInternal#init(IEventBus, ModContainer)}.
 * The time when it will be called depends on the {@linkplain #value() stage} given as an annotation parameter.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnInit {
    /**
     * {@return the stage during which to run this listener}
     */
    Stage value() default Stage.BEFORE_SETUP;

    enum Stage {
        /**
         * This stage happens before tests are collected, but after the {@linkplain TestFramework#modEventBus() mod event bus} is configured.
         */
        BEFORE_SETUP,

        /**
         * This stage happens after tests are collected and {@linkplain RegisterStructureTemplate structure templates} are registered.
         */
        AFTER_SETUP
    }
}
