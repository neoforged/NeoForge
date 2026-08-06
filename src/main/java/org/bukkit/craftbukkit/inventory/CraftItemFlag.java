package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.legacy.FieldRename;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemFlag;

public class CraftItemFlag {

    private static final BiMap<ItemFlag, DataComponentType<?>> BUKKIT_TO_NMS = HashBiMap.create();
    private static final Collection<DataComponentType<?>> HIDE_ADDITIONAL_TOOLTIP = Arrays.asList(
            DataComponents.POTION_CONTENTS, DataComponents.WRITABLE_BOOK_CONTENT, DataComponents.WRITTEN_BOOK_CONTENT,
            DataComponents.FIREWORKS, DataComponents.FIREWORK_EXPLOSION,
            DataComponents.MAP_COLOR, DataComponents.MAP_DECORATIONS, DataComponents.MAP_ID, DataComponents.MAP_POST_PROCESSING,
            DataComponents.BANNER_PATTERNS,
            DataComponents.STORED_ENCHANTMENTS
    );

    static {
        BUKKIT_TO_NMS.put(ItemFlag.HIDE_ARMOR_TRIM, DataComponents.TRIM);
        BUKKIT_TO_NMS.put(ItemFlag.HIDE_ATTRIBUTES, DataComponents.ATTRIBUTE_MODIFIERS);
        BUKKIT_TO_NMS.put(ItemFlag.HIDE_DESTROYS, DataComponents.CAN_BREAK);
        BUKKIT_TO_NMS.put(ItemFlag.HIDE_DYE, DataComponents.DYED_COLOR);
        BUKKIT_TO_NMS.put(ItemFlag.HIDE_ENCHANTS, DataComponents.ENCHANTMENTS);
        BUKKIT_TO_NMS.put(ItemFlag.HIDE_PLACED_ON, DataComponents.CAN_PLACE_ON);
        BUKKIT_TO_NMS.put(ItemFlag.HIDE_UNBREAKABLE, DataComponents.UNBREAKABLE);
    }

    public static Collection<DataComponentType<?>> bukkitToNMS(ItemFlag bukkit) {
        if (bukkit == ItemFlag.HIDE_ADDITIONAL_TOOLTIP) {
            return HIDE_ADDITIONAL_TOOLTIP;
        }

        DataComponentType<?> type = BUKKIT_TO_NMS.get(bukkit);
        if (type == null) {
            NamespacedKey key = bukkit.getComponent();
            Preconditions.checkArgument(key != null, "Unknown flag %s must have component key", bukkit);

            type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(CraftNamespacedKey.toMinecraft(key));
        }

        Preconditions.checkArgument(type != null, "Unknown flag %s", bukkit);
        return Arrays.asList(type);
    }

    public static ItemFlag nmsToBukkit(DataComponentType<?> nms) {
        ItemFlag inverse = BUKKIT_TO_NMS.inverse().get(nms);

        if (inverse == null) {
            Identifier key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(nms);
            if (key == null) {
                throw new IllegalArgumentException("Unregistered component " + nms);
            }

            inverse = ItemFlag.valueOf("HIDE_" + key.getPath().toUpperCase(Locale.ROOT).replace('/', '_'));
        }

        return inverse;
    }

    public static String bukkitToString(ItemFlag bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return bukkit.name();
    }

    public static ItemFlag stringToBukkit(String string) {
        Preconditions.checkArgument(string != null);

        // We currently do not have any version-dependent remapping, so we can use current version
        string = FieldRename.convertItemFlagName(ApiVersion.CURRENT, string);
        return ItemFlag.valueOf(string);
    }
}
