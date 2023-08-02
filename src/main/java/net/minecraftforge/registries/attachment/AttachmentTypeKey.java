package net.minecraftforge.registries.attachment;

import com.google.common.collect.MapMaker;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.ConcurrentMap;

public final class AttachmentTypeKey<T> {
    private static final ConcurrentMap<ResourceLocation, AttachmentTypeKey<?>> VALUES = new MapMaker().weakValues().makeMap();

    private final ResourceLocation id;

    private AttachmentTypeKey(ResourceLocation id) {
        this.id = id;
    }

    public static <T> AttachmentTypeKey<T> get(ResourceLocation id) {
        return (AttachmentTypeKey<T>) VALUES.computeIfAbsent(id, AttachmentTypeKey::new);
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public String toString() {
        return "AttachmentTypeKey[" + id + "]";
    }
}
