/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

/**
 * Put this annotation on a static field containing either a {@link StructureTemplate}, a {@link Supplier} of {@linkplain StructureTemplate}
 * or a {@link StructureTemplateBuilder} in order to automatically register that code-defined template.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterStructureTemplate {
    /**
     * {@return the ID of the template}
     */
    String value();
}
