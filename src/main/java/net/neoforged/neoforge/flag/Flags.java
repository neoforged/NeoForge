/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/**
 * Public facing flag utilities
 * <p>
 * All utilities are only to be used during a servers life cycle,
 * errors, bugs and issues may occur outside of this time frame
 * <p>
 * <h6>Example flag usage</h6>
 * 
 * <pre>{@code
 * @Mod(MyMod.ID)
 * public class MyMod {
 *     public static final String ID = "my_mod";
 *     // constructing your flag is as simple as defining the id (ResourceLocation)
 *     // this flag will be enabled if it is contained within any datapacks `flags.json` file
 *     // during development you can use the `/neoforge flag [enable|disable]` commands for easier testing
 *     public static final ResourceLocation MY_FLAG = ResourceLocation.fromNamespaceAndPath(ID, "my_flag_name");
 *
 *     public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
 *     // to use the flag simply pass it along to what ever flagged element during registration
 *     // in this example an item, pass the along via the properties
 *     public static final DeferredItem<Item> MY_ITEM = ITEMS.registerSimpleItem("my_flagged_item", new Item.Properties().requiredFlags(MY_FLAG));
 *
 *     public MyMod(IEventBus modBus) {
 *         ITEMS.register(modBus);
 *     }
 * }
 * }</pre>
 */
public interface Flags {
    /**
     * @return Set of all known flags
     */
    static Set<ResourceLocation> getFlags() {
        return FlagManager.INSTANCE.enabledFlagsView.keySet();
    }

    /**
     * @param flag Flag to be validated
     * @return {@code true} if the given flag is enabled, {@code false} otherwise.
     */
    static boolean isEnabled(ResourceLocation flag) {
        return FlagManager.INSTANCE.enabledFlagsView.getOrDefault(flag, false);
    }

    /**
     * @param flags Array of flags to be validated
     * @return {@code true} if all provided flags are enabled, {@code false} otherwise.
     */
    static boolean isEnabled(ResourceLocation... flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }

    /**
     * @param flags Iterable of flags to be validated
     * @return {@code true} if all provided flags are enabled, {@code false} otherwise.
     */
    static boolean isEnabled(Iterable<ResourceLocation> flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }
}
