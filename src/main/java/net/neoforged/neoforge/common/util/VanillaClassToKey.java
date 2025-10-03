/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.include.com.google.common.base.Preconditions;

@ApiStatus.Internal
public class VanillaClassToKey {
    /**
     * Converts a vanilla class name into an identifier compliant with the rules set by {@link ResourceLocation}.
     * <p>
     * This conversion is done by translating all uppercase characters into an underscore plus the lowercase version of the original character.
     * 
     * @param cls The class to convert.
     * @return A lower_snake_case representation of that class's original PascalCase name.
     * @throws IllegalArgumentException if the class is not from Minecraft, or if the class does not have a {@link Class#getSimpleName() simple name}.
     */
    public static ResourceLocation convert(Class<?> cls) {
        Preconditions.checkArgument(cls.getPackageName().startsWith("net.minecraft"), "Automatic name conversion can only be applied to net.minecraft classes. Provided: " + cls.getName());
        Preconditions.checkArgument(!cls.getSimpleName().isEmpty(), "Automatic name conversion can only happen for identifiable classes (per Class#getSimpleName()). Provided: " + cls.getName());

        StringBuilder sb = new StringBuilder();
        cls.getSimpleName().codePoints().forEachOrdered(value -> {
            if (Character.isUpperCase(value)) {
                sb.append('_');
                sb.append(Character.toString(Character.toLowerCase(value)));
            } else {
                sb.append(Character.toString(value));
            }
        });

        return ResourceLocation.withDefaultNamespace(sb.substring(1)); // The string will be prefixed with an additional `_` since the first character is uppercase.
    }
}
