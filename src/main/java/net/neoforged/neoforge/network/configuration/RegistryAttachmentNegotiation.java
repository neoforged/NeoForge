package net.neoforged.neoforge.network.configuration;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.payload.KnownRegistryAttachmentsPayload;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.attachment.RegistryAttachment;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ApiStatus.Internal
public record RegistryAttachmentNegotiation(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    public static final ResourceLocation ID = new ResourceLocation("neoforge:registry_attachment_negotiation");
    public static final Type TYPE = new Type(ID);

    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        if (listener.isVanillaConnection()) {
            final var anyMandatory = RegistryManager.getAttachments().values()
                    .stream().anyMatch(map -> map.values().stream().anyMatch(RegistryAttachment::mandatorySync));
            if (anyMandatory) {
                // TODO - fix
                listener.disconnect(Component.literal("TODO"));
                return;
            }
        }

        final Map<ResourceKey<Registry<?>>, List<KnownRegistryAttachmentsPayload.KnownAttachment>> attachments = new HashMap<>();
        RegistryManager.getAttachments().forEach((key, attach) -> {
            final List<KnownRegistryAttachmentsPayload.KnownAttachment> list = new ArrayList<>();
            attach.forEach((id, val) -> {
                if (val.networkCodec() != null) {
                    list.add(new KnownRegistryAttachmentsPayload.KnownAttachment(id, val.mandatorySync()));
                }
            });
            attachments.put(key, list);
        });
        sender.accept(new KnownRegistryAttachmentsPayload(attachments));
    }
}
