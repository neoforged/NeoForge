/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
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

@ApiStatus.Internal
public record CheckExtensibleEnums(ServerConfigurationPacketListener listener) implements ConfigurationTask {
    public static final Type TYPE = new Type(ResourceLocation.fromNamespaceAndPath("neoforge", "check_extensible_enum"));
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
            List<EnumEntry> extendedServerboundEnums = enumEntries.values()
                    .stream()
                    .filter(entry -> entry.isClientbound() && entry.isExtended())
                    .toList();
            if (!extendedServerboundEnums.isEmpty()) {
                // TODO: proper translated message
                listener.disconnect(Component.literal("Server has extended enums"));
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

        Set<String> keyDiff = Sets.symmetricDifference(localEnumEntries.keySet(), remoteEnumEntries.keySet());
        if (!keyDiff.isEmpty()) {
            // TODO: proper translated message
            context.disconnect(Component.literal("Set of extensible enums doesn't match"));
            return;
        }

        Map<String, Mismatch> mismatched = new HashMap<>();
        for (EnumEntry localEntry : localEnumEntries.values()) {
            EnumEntry remoteEntry = remoteEnumEntries.get(localEntry.className);
            if (!localEntry.isExtended() && !remoteEntry.isExtended()) {
                continue;
            }

            if (localEntry.networkCheck != remoteEntry.networkCheck) {
                mismatched.put(localEntry.className, Mismatch.NETWORK_CHECK);
                continue;
            }

            if (localEntry.isExtended() != remoteEntry.isExtended()) {
                mismatched.put(localEntry.className, Mismatch.EXTENSION);
                continue;
            }

            ExtensionData localData = localEntry.data.orElseThrow();
            ExtensionData remoteData = remoteEntry.data.orElseThrow();
            if (localData.vanillaCount != remoteData.vanillaCount || localData.totalCount != remoteData.totalCount) {
                mismatched.put(localEntry.className, Mismatch.ENTRY_COUNT);
                continue;
            }

            List<String> localValues = localData.entries;
            List<String> remoteValues = remoteData.entries;
            for (int i = 0; i < localData.totalCount - localData.vanillaCount; i++) {
                if (!localValues.get(i).equals(remoteValues.get(i))) {
                    mismatched.put(localEntry.className, Mismatch.ENTRY_MISMATCH);
                    break;
                }
            }
        }

        if (!mismatched.isEmpty()) {
            // TODO: proper translated message
            context.disconnect(Component.literal("Enum entry mismatch between server and client"));
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
            // TODO: proper translated message
            listener.disconnect(Component.literal("Client has extended enums"));
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
        NETWORK_CHECK,
        EXTENSION,
        ENTRY_COUNT,
        ENTRY_MISMATCH,
    }
}
