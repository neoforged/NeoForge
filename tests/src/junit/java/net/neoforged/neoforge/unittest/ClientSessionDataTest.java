/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mojang.serialization.Codec;
import java.nio.file.Path;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.attachment.ClientSessionData;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ClientSessionDataTest {
    @Test
    void storageKeyUsesHostAndPortForServers() {
        assertThat(ClientSessionData.storageKey(new ServerData("Name", "Example.com", ServerData.Type.OTHER))).isEqualTo("example.com_25565");
        assertThat(ClientSessionData.storageKey(new ServerData("Name", "example.com:25566", ServerData.Type.OTHER))).isEqualTo("example.com_25566");
    }

    @Test
    void storageKeyUsesNamesForRealmsAndLanGames() {
        // Realm addresses change between sessions
        assertThat(ClientSessionData.storageKey(new ServerData("My Realm", "1.2.3.4:5", ServerData.Type.REALM))).isEqualTo("realm_my_realm");
        // LAN ports are random, the name (the host's MOTD) identifies the world
        assertThat(ClientSessionData.storageKey(new ServerData("Steve - World", "192.168.0.5:51234", ServerData.Type.LAN))).isEqualTo("lan_192.168.0.5_steve_-_world");
        // Names must never introduce path separators
        assertThat(ClientSessionData.storageKey(new ServerData("a/b", "1.2.3.4:5", ServerData.Type.REALM))).isEqualTo("realm_a_b");
    }

    @Test
    void storageKeyClampsLongKeysWithAHash() {
        // Labels are limited to 63 characters, so build a long but valid host from several
        String longHost = ("a".repeat(60) + ".").repeat(2) + "example.com";
        String key = ClientSessionData.storageKey(new ServerData("Name", longHost, ServerData.Type.OTHER));
        assertThat(key).hasSize(64).startsWith("a".repeat(55) + "-").matches("[a-z0-9_.\\-]+");
        // Same sanitized prefix, different origin: the hash keeps them apart
        String otherKey = ClientSessionData.storageKey(new ServerData("Name", ("a".repeat(60) + ".").repeat(2) + "example.org", ServerData.Type.OTHER));
        assertThat(otherKey).hasSize(64).isNotEqualTo(key);
    }

    @Test
    void resolveFileUsesTheGameDirectoryForServers(@TempDir Path gameDirectory) {
        var file = ClientSessionData.resolveFile(gameDirectory, null, new ServerData("Name", "example.com", ServerData.Type.OTHER));
        assertThat(file).isEqualTo(gameDirectory.resolve("neoforge/client_session_attachments/example.com_25565.dat"));
        assertThatThrownBy(() -> ClientSessionData.resolveFile(gameDirectory, null, null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyNonSyncedAttachmentsRoundTripThroughTheFile(@TempDir Path directory) {
        var lookup = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var file = directory.resolve("session.dat");

        var data = new ClientSessionData(lookup, file);
        data.save();
        assertThat(file).as("no file is created while there is nothing to persist").doesNotExist();

        data.setData(TestMod.PERSISTED, 42);
        data.setData(TestMod.SYNCED, 7);
        data.save();
        assertThat(file).exists();

        var loaded = new ClientSessionData(lookup, file);
        loaded.load();
        assertThat(loaded.getExistingDataOrNull(TestMod.PERSISTED)).isEqualTo(42);
        assertThat(loaded.hasData(TestMod.SYNCED)).as("synced attachments are not client-local").isFalse();

        loaded.removeData(TestMod.PERSISTED);
        loaded.save();
        var reloaded = new ClientSessionData(lookup, file);
        reloaded.load();
        assertThat(reloaded.hasAttachments()).as("removals are persisted once the file exists").isFalse();
    }

    @Mod(TestMod.MOD_ID)
    public static class TestMod {
        public static final String MOD_ID = "client_session_data_test";
        private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
        public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> PERSISTED = ATTACHMENT_TYPES.register("persisted",
                () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("value")).build());
        public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> SYNCED = ATTACHMENT_TYPES.register("synced",
                () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("value")).sync(ByteBufCodecs.VAR_INT).build());

        public TestMod(IEventBus modBus) {
            ATTACHMENT_TYPES.register(modBus);
        }
    }
}
