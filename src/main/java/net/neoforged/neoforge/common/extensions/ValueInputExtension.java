package net.neoforged.neoforge.common.extensions;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Extension class for {@link ValueInput}
 */
public interface ValueInputExtension {
    private ValueInput self() {
        return (ValueInput) this;
    }

    /**
     * {@return the top-level keys of this object}
     */
    default Set<String> keySet() {
        //noinspection deprecation
        return self().read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseThrow().keySet();
    }
}
