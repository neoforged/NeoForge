/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class DataComponentUtil {
    /**
     * Wraps encoding exceptions and adds additional logging for a DataComponentHolder that failed to save.
     */
    public static <T extends DataComponentHolder> Tag wrapEncodingExceptions(T componentHolder, Codec<T> codec, HolderLookup.Provider provider, Tag tag) {
        try {
            return codec.encode(componentHolder, provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
        } catch (Exception exception) {
            logDataComponentSaveError(componentHolder, exception, tag);
            throw exception;
        }
    }

    /**
     * Wraps encoding exceptions and adds additional logging for a DataComponentHolder that failed to save.
     */
    public static <T extends DataComponentHolder> Tag wrapEncodingExceptions(T componentHolder, Codec<T> codec, HolderLookup.Provider provider) {
        try {
            return codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), componentHolder).getOrThrow();
        } catch (Exception exception) {
            logDataComponentSaveError(componentHolder, exception, null);
            throw exception;
        }
    }

    /**
     * Logs component information and tag data for a DataComponentHolder that failed to save.
     * See {@link ItemStack#save } or {@link FluidStack#save }
     * 
     * <pre>
     * Example:
     * Error saving [1 minecraft:dirt]. Original cause: java.lang.NullPointerException
     * With components:
     * {
     *    neoforge:test=>Test[s=null]
     *    minecraft:max_stack_size=>64
     *    minecraft:lore=>ItemLore[lines=[], styledLines=[]]
     *    minecraft:enchantments=>ItemEnchantments{enchantments={}, showInTooltip=true}
     *    minecraft:repair_cost=>0
     *    minecraft:attribute_modifiers=>ItemAttributeModifiers[modifiers=[], showInTooltip=true]
     *    minecraft:rarity=>COMMON
     * }
     * With tag: {}
     * </pre>
     */
    public static void logDataComponentSaveError(DataComponentHolder componentHolder, Exception original, @Nullable Tag tag) {
        StringBuilder cause = new StringBuilder("Error saving [" + componentHolder + "]. Original cause: " + original);

        cause.append("\nWith components:\n{");
        componentHolder.getComponents().forEach((component) -> {
            cause.append("\n\t").append(component);
        });
        cause.append("\n}");
        if (tag != null) {
            cause.append("\nWith tag: ").append(tag);
        }
        Util.logAndPauseIfInIde(cause.toString());
    }
}
