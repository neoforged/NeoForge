/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface GameTest {
    int timeoutTicks() default 100;

    String batch() default "defaultBatch";

    boolean skyAccess() default false;

    int rotationSteps() default 0;

    boolean required() default true;

    boolean manualOnly() default false;

    String template() default "";

    int setupTicks() default 0;

    int attempts() default 1;

    int requiredSuccesses() default 1;
}
