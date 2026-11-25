/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.misc;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.NewDatapackRegistryEvent;
import org.slf4j.Logger;

/**
 * <p>This test class shows an example of how to register unsynced and synced datapack registries,
 * and how to generate an entry for the unsynced registry.
 * It also verifies that data and tags are loaded and synced correctly.
 * Data is loaded from the following folders in test resources:</p>
 * <ul>
 * <li><code>data/data_pack_registries_test/data_pack_registries_test/unsyncable/test.json</code></li>
 * <li><code>data/data_pack_registries_test/data_pack_registries_test/unsyncable/datagen_test.json</code> (generated)</li>
 * <li><code>data/data_pack_registries_test/tags/data_pack_registries_test/unsyncable/test.json</code></li>
 * <li><code>data/data_pack_registries_test/data_pack_registries_test/syncable/test.json</code></li>
 * <li><code>data/data_pack_registries_test/tags/data_pack_registries_test/syncable/test.json</code></li>
 * </ul>
 */
@Mod(DataPackRegistriesTest.MODID)
public class DataPackRegistriesTest {
    // Enable for manual sync testing with a client that also has this test mod.
    private static final boolean ENABLE_SYNC_TEST = false;
    public static final String MODID = "data_pack_registries_test";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier TEST_RL = Identifier.fromNamespaceAndPath(MODID, "test");

    private static final ResourceKey<Unsyncable> DATAGEN_TEST_KEY = ResourceKey.create(Unsyncable.REGISTRY_KEY, Identifier.fromNamespaceAndPath(MODID, "datagen_test"));

    public DataPackRegistriesTest(IEventBus modBus) {
        modBus.addListener((NewDatapackRegistryEvent event) -> {
            event.worldRegistry(Unsyncable.REGISTRY_KEY, Unsyncable.DIRECT_CODEC);
            if (ENABLE_SYNC_TEST) {
                event.worldRegistry(Syncable.REGISTRY_KEY, Syncable.DIRECT_CODEC, Syncable.DIRECT_CODEC);
            }
        });

        modBus.addListener((GatherDataRegistryEntriesEvent event) -> event.add(Unsyncable.REGISTRY_KEY,
                context -> context.register(DATAGEN_TEST_KEY, new Unsyncable("Datagen Success"))));
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        if (ENABLE_SYNC_TEST && FMLEnvironment.getDist().isClient()) {
            ClientEvents.subscribeClientEvents();
        }
    }

    private void onServerStarting(final ServerStartingEvent event) {
        // Assert existence of json objects and tags.
        final RegistryAccess registries = event.getServer().registryAccess();
        final Registry<Unsyncable> registry = registries.lookupOrThrow(Unsyncable.REGISTRY_KEY);
        final ResourceKey<Unsyncable> key = ResourceKey.create(Unsyncable.REGISTRY_KEY, TEST_RL);
        final Holder<Unsyncable> holder = registry.getOrThrow(key);
        final Unsyncable testObject = registry.getValue(TEST_RL);
        if (!testObject.value().equals("success"))
            throw new IllegalStateException("Incorrect value loaded: " + testObject.value());
        final TagKey<Unsyncable> tag = TagKey.create(Unsyncable.REGISTRY_KEY, TEST_RL);
        if (!registry.get(tag).get().contains(holder))
            throw new IllegalStateException(String.format(Locale.ENGLISH, "Tag %s does not contain %s", tag, TEST_RL));

        final Unsyncable generated = registry.getValueOrThrow(DATAGEN_TEST_KEY);
        if (!generated.value().equals("Datagen Success"))
            throw new IllegalStateException("Incorrect value loaded for " + DATAGEN_TEST_KEY.identifier() + ": " + generated.value());

        LOGGER.info("DataPackRegistriesTest server data loaded successfully!");
    }

    public static class ClientEvents {
        private static void subscribeClientEvents() {
            NeoForge.EVENT_BUS.addListener(ClientEvents::onClientTagsUpdated);
        }

        private static void onClientTagsUpdated(final TagsUpdatedEvent event) {
            // We want to check whether tags have been synced after the player logs in.
            // Tags are synced late in the login process and many relevant events fire before tags are synced.
            // TagsUpdatedEvent has the correct timing, but fires on both server and render thread,
            // so we need to make sure we're on the render thread.
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || EffectiveSide.get().isServer())
                return;

            // Assert existence of synced objects and tags. The TagsUpdatedEvent has its own RegistryAccess,
            // but we should check the player's connection's RegistryAccess as that's where the client's copy
            // lives during game runtime, and where mods should be querying on the client in most cases.
            RegistryAccess registries = player.connection.registryAccess();
            final Registry<Syncable> registry = registries.lookupOrThrow(Syncable.REGISTRY_KEY);
            final ResourceKey<Syncable> key = ResourceKey.create(Syncable.REGISTRY_KEY, TEST_RL);
            final Holder<Syncable> holder = registry.getOrThrow(key);
            final Syncable testObject = registry.getValue(TEST_RL);
            if (!testObject.value().equals("success"))
                throw new IllegalStateException("Incorrect value synced: " + testObject.value());
            final TagKey<Syncable> tag = TagKey.create(Syncable.REGISTRY_KEY, TEST_RL);
            if (!registry.get(tag).get().contains(holder))
                throw new IllegalStateException(String.format(Locale.ENGLISH, "Tag %s does not contain %s", tag, TEST_RL));

            LOGGER.info("DataPackRegistriesTest client data synced successfully!");
        }
    }

    public static class Unsyncable {
        public static final ResourceKey<Registry<Unsyncable>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MODID, "unsyncable"));
        public static final Codec<Unsyncable> DIRECT_CODEC = Codec.STRING.fieldOf("value").codec().xmap(Unsyncable::new, Unsyncable::value);

        private final String value;

        public Unsyncable(final String stringValue) {
            this.value = stringValue;
        }

        public String value() {
            return this.value;
        }
    }

    public static class Syncable {
        public static final ResourceKey<Registry<Syncable>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MODID, "syncable"));
        public static final Codec<Syncable> DIRECT_CODEC = Codec.STRING.fieldOf("value").codec().xmap(Syncable::new, Syncable::value);

        private final String value;

        public Syncable(final String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }
}
