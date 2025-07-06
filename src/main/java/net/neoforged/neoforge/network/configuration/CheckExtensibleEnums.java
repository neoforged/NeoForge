/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.fml.common.asm.enumextension.NetworkedEnum;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.ExtensibleEnumAcknowledgePayload;
import net.neoforged.neoforge.network.payload.ExtensibleEnumDataPayload;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

@ApiStatus.Internal
public record CheckExtensibleEnums(ServerConfigurationPacketListener listener) implements ConfigurationTask {
    public static final Type TYPE = new Type(ResourceLocation.fromNamespaceAndPath("neoforge", "check_extensible_enum"));
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final org.objectweb.asm.Type NETWORKED_ENUM = org.objectweb.asm.Type.getType(NetworkedEnum.class);
    private static final List<? extends Class<? extends Enum<?>>> NETWORKED_EXTENSIBLE_ENUM_CLASSES = collectNetworkedEnumClasses();
    private static Map<String, EnumEntry> enumEntries = null;

    @Override
    public void start(Consumer<Packet<?>> packetSender) {
        if (listener.getConnection().isMemoryConnection()) {
            listener.finishCurrentTask(TYPE);
            return;
        }

        Map<String, EnumEntry> enumEntries = getEnumEntries();
        if (listener.getConnectionType().isOther()) {
            List<EnumEntry> extendedClientboundEnums = enumEntries.values()
                    .stream()
                    .filter(entry -> entry.isClientbound() && entry.isExtended())
                    .toList();
            if (!extendedClientboundEnums.isEmpty()) {
                // Use plain components as vanilla connections will be missing our translation keys
                listener.disconnect(Component.literal("This server does not support vanilla clients as it has extended enums used in clientbound networking"));
            } else {
                listener.finishCurrentTask(TYPE);
            }
            return;
        }

        packetSender.accept(new ExtensibleEnumDataPayload(enumEntries).toVanillaClientbound());
    }

    public static void handleClientboundPayload(ExtensibleEnumDataPayload payload, IPayloadContext context) {
        Map<String, EnumEntry> localEnumEntries = getEnumEntries();
        Map<String, EnumEntry> remoteEnumEntries = payload.enumEntries();

        Map<String, Mismatch> mismatched = new HashMap<>();
        for (String className : Sets.union(localEnumEntries.keySet(), remoteEnumEntries.keySet())) {
            EnumEntry localEntry = localEnumEntries.get(className);
            EnumEntry remoteEntry = remoteEnumEntries.get(className);
            if ((localEntry == null && remoteEntry.isExtended()) || (remoteEntry == null && localEntry.isExtended())) {
                mismatched.put(className, Mismatch.EXTENSIBILITY);
                continue;
            }

            if ((localEntry == null || !localEntry.isExtended()) && (remoteEntry == null || !remoteEntry.isExtended())) {
                continue;
            }

            if (localEntry.networkCheck != remoteEntry.networkCheck) {
                mismatched.put(className, Mismatch.NETWORK_CHECK);
                continue;
            }

            if (localEntry.isExtended() != remoteEntry.isExtended()) {
                mismatched.put(className, Mismatch.EXTENSION);
                continue;
            }

            ExtensionData localData = localEntry.data.orElseThrow();
            ExtensionData remoteData = remoteEntry.data.orElseThrow();
            if (localData.vanillaCount != remoteData.vanillaCount || localData.totalCount != remoteData.totalCount) {
                mismatched.put(className, Mismatch.ENTRY_COUNT);
                continue;
            }

            List<String> localValues = localData.entries;
            List<String> remoteValues = remoteData.entries;
            for (int i = 0; i < localData.totalCount - localData.vanillaCount; i++) {
                if (!localValues.get(i).equals(remoteValues.get(i))) {
                    mismatched.put(className, Mismatch.ENTRY_MISMATCH);
                    break;
                }
            }
        }

        if (!mismatched.isEmpty()) {
            context.disconnect(Component.translatable("neoforge.network.extensible_enums.enum_entry_mismatch"));
            StringBuilder message = new StringBuilder("The configuration or set of values added to extensible enums on the client and server do not match");
            for (Map.Entry<String, Mismatch> entry : mismatched.entrySet()) {
                String enumClass = entry.getKey();
                message.append("\n").append(enumClass).append(": ");
                switch (entry.getValue()) {
                    case EXTENSIBILITY -> {
                        if (remoteEnumEntries.containsKey(enumClass)) {
                            message.append("Enum is extensible on the server but not on the client");
                        } else {
                            message.append("Enum is extensible on the client but not on the server");
                        }
                    }
                    case NETWORK_CHECK -> message.append("Mismatched NetworkCheck (server: ")
                            .append(remoteEnumEntries.get(enumClass).networkCheck)
                            .append(", client: ")
                            .append(localEnumEntries.get(enumClass).networkCheck)
                            .append(")");
                    case EXTENSION -> {
                        if (remoteEnumEntries.get(enumClass).isExtended()) {
                            message.append("Enum has additional entries on the server but not on the client");
                        } else {
                            message.append("Enum has additional entries on the client but not on the server");
                        }
                    }
                    case ENTRY_COUNT, ENTRY_MISMATCH -> message.append("Set of entries does not match (server: ")
                            .append(remoteEnumEntries.get(enumClass).data.orElseThrow().entries)
                            .append(", client: ")
                            .append(localEnumEntries.get(enumClass).data.orElseThrow().entries)
                            .append(")");
                }
            }
            LOGGER.warn(message.toString());
            return;
        }

        context.reply(ExtensibleEnumAcknowledgePayload.INSTANCE);
    }

    public static void handleServerboundPayload(@SuppressWarnings("unused") ExtensibleEnumAcknowledgePayload payload, IPayloadContext context) {
        context.finishCurrentTask(TYPE);
    }

    public static boolean handleVanillaServerConnection(ClientConfigurationPacketListener listener) {
        Collection<EnumEntry> enumEntries = getEnumEntries().values();
        if (enumEntries.stream().anyMatch(entry -> entry.isServerbound() && entry.isExtended())) {
            listener.disconnect(Component.translatable("neoforge.network.extensible_enums.no_vanilla_server"));
            return false;
        }
        return true;
    }

    private static synchronized Map<String, EnumEntry> getEnumEntries() {
        if (enumEntries == null) {
            Map<String, EnumEntry> entries = new HashMap<>();
            for (Class<? extends Enum<?>> enumClass : NETWORKED_EXTENSIBLE_ENUM_CLASSES) {
                Optional<ExtensionData> extData = Optional.empty();
                ExtensionInfo extInfo = getEnumExtensionInfo(enumClass);
                if (extInfo.extended()) {
                    List<String> values = new ArrayList<>(extInfo.totalCount() - extInfo.vanillaCount());
                    Enum<?>[] constants = enumClass.getEnumConstants();
                    for (int i = extInfo.vanillaCount(); i < extInfo.totalCount(); i++) {
                        values.add(constants[i].name());
                    }
                    extData = Optional.of(new ExtensionData(extInfo.vanillaCount(), extInfo.totalCount(), values));
                }
                String name = enumClass.getName();
                entries.put(name, new EnumEntry(
                        name,
                        Preconditions.checkNotNull(extInfo.netCheck(), "Enum %s does not have a NetworkCheck value", name),
                        extData));
            }
            enumEntries = entries;
        }
        return enumEntries;
    }

    private static ExtensionInfo getEnumExtensionInfo(Class<? extends Enum<?>> enumClass) {
        try {
            Method mth = enumClass.getDeclaredMethod("getExtensionInfo");
            return (ExtensionInfo) mth.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<? extends Class<? extends Enum<?>>> collectNetworkedEnumClasses() {
        return ModList.get()
                .getAllScanData()
                .stream()
                .map(ModFileScanData::getAnnotations)
                .flatMap(Set::stream)
                .filter(a -> NETWORKED_ENUM.equals(a.annotationType()))
                .map(a -> classForName(a.clazz().getClassName()))
                .filter(Class::isEnum)
                .filter(IExtensibleEnum.class::isAssignableFrom)
                .map(c -> (Class<? extends Enum<?>>) c)
                .toList();
    }

    private static Class<?> classForName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load class specified by annotation data", e);
        }
    }

    @Override
    public Type type() {
        return TYPE;
    }

    public record EnumEntry(String className, NetworkedEnum.NetworkCheck networkCheck, Optional<ExtensionData> data) {
        public static final StreamCodec<ByteBuf, EnumEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                EnumEntry::className,
                ByteBufCodecs.STRING_UTF8.map(NetworkedEnum.NetworkCheck::valueOf, NetworkedEnum.NetworkCheck::name),
                EnumEntry::networkCheck,
                ByteBufCodecs.optional(ExtensionData.STREAM_CODEC),
                EnumEntry::data,
                EnumEntry::new);

        public boolean isClientbound() {
            return networkCheck == NetworkedEnum.NetworkCheck.CLIENTBOUND || networkCheck == NetworkedEnum.NetworkCheck.BIDIRECTIONAL;
        }

        public boolean isServerbound() {
            return networkCheck == NetworkedEnum.NetworkCheck.SERVERBOUND || networkCheck == NetworkedEnum.NetworkCheck.BIDIRECTIONAL;
        }

        public boolean isExtended() {
            return data.isPresent();
        }
    }

    public record ExtensionData(int vanillaCount, int totalCount, List<String> entries) {
        public static final StreamCodec<ByteBuf, ExtensionData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                ExtensionData::vanillaCount,
                ByteBufCodecs.VAR_INT,
                ExtensionData::totalCount,
                ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                ExtensionData::entries,
                ExtensionData::new);
    }

    private enum Mismatch {
        EXTENSIBILITY,
        NETWORK_CHECK,
        EXTENSION,
        ENTRY_COUNT,
        ENTRY_MISMATCH,
    }
}
