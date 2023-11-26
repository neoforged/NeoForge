/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.impl.test.AbstractTest;

/**
 * Annotation used by {@link AbstractTest} in order to collect information about a test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface TestHolder {
    /**
     * {@return the test's ID}
     */
    String value() default "compute";

    /**
     * Returns the groups the test is in. <br>
     * Note: if empty, it defaults to {@code ungrouped}.
     *
     * @return the groups the test is in
     * @see Test#groups()
     */
    String[] groups() default {};

    /**
     * {@return the human-readable title of the test}
     */
    String title() default "";

    /**
     * This usually contains instructions on how to use the test.
     * {@return the human-readable description of the test}
     */
    String[] description() default {};

    /**
     * {@return if this test is enabled by default}
     */
    boolean enabledByDefault() default false;

    /**
     * {@return the sides this test should be registered on}
     */
    Dist[] side() default { Dist.CLIENT, Dist.DEDICATED_SERVER };
}
