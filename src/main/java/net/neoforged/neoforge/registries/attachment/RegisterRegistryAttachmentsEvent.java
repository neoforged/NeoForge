package net.neoforged.neoforge.registries.attachment;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public class RegisterRegistryAttachmentsEvent extends Event implements IModBusEvent {
    private final Map<ResourceKey<Registry<?>>, Map<ResourceLocation, RegistryAttachment<?, ?, ?>>> attachments;

    @ApiStatus.Internal
    public RegisterRegistryAttachmentsEvent(Map<ResourceKey<Registry<?>>, Map<ResourceLocation, RegistryAttachment<?, ?, ?>>> attachments) {
        this.attachments = attachments;
    }

    public <T, R> void register(ResourceKey<Registry<R>> registry, RegistryAttachment<T, R, ?> attachment) {
        final var map = attachments.computeIfAbsent((ResourceKey)registry, k -> new HashMap<>());
        if (map.containsKey(attachment.id())) {
            throw new IllegalArgumentException("Tried to register attachment with ID " + attachment.id() + " to registry " + registry.location() + " twice");
        }
        map.put(attachment.id(), attachment);
    }
}
