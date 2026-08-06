package org.bukkit.craftbukkit.inventory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ItemMetaKey {

    @Retention(value = RetentionPolicy.SOURCE)
    @Target(value = ElementType.FIELD)
    @interface Specific {

        public static enum To {
            BUKKIT, NBT
        }

        To value();
    }
    public final String BUKKIT;
    public final String NBT;

    public ItemMetaKey(final String both) {
        this(both, both);
    }

    public ItemMetaKey(final String nbt, final String bukkit) {
        this.NBT = nbt;
        this.BUKKIT = bukkit;
    }
}
