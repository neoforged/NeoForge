package net.neoforged.neoforge.network.configuration;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.attachment.DataMapType;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ApiStatus.Internal
public record RegistryDataMapNegotiation(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    public static final ResourceLocation ID = new ResourceLocation("neoforge:registry_data_map_negotiation");
    public static final Type TYPE = new Type(ID);

    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        if (listener.isVanillaConnection()) {
            final var anyMandatory = RegistryManager.getDataMaps().values()
                    .stream().anyMatch(map -> map.values().stream().anyMatch(DataMapType::mandatorySync));
            if (anyMandatory) {
                // TODO - fix
                listener.disconnect(Component.literal("TODO"));
                return;
            }
        }

        final Map<ResourceKey<Registry<?>>, List<KnownRegistryDataMapsPayload.KnownDataMap>> dataMaps = new HashMap<>();
        RegistryManager.getDataMaps().forEach((key, attach) -> {
            final List<KnownRegistryDataMapsPayload.KnownDataMap> list = new ArrayList<>();
            attach.forEach((id, val) -> {
                if (val.networkCodec() != null) {
                    list.add(new KnownRegistryDataMapsPayload.KnownDataMap(id, val.mandatorySync()));
                }
            });
            dataMaps.put(key, list);
        });
        sender.accept(new KnownRegistryDataMapsPayload(dataMaps));
    }
}
