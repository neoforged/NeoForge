package net.neoforged.neoforge.debug.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.oldtest.world.LoginPacketSplitTest;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.TestListener;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.annotation.WithListener;
import org.jetbrains.annotations.Nullable;

@ForEachTest(groups = BulkKnownPackTest.GROUP, side = Dist.CLIENT)
public class BulkKnownPackTest {
    public static final String GROUP = "resources";
    private static final String NAMESPACE = "bulk_known_pack_test";

    @TestHolder(description = "Tests that KnownPacks are correctly synced even when more than the vanilla 64 are present", enabledByDefault = true)
    @WithListener(BulkKnownPackTest.Listener.class)
    static void bulkKnownPackTest(final DynamicTest test) {
        test.framework().modEventBus().addListener(AddPackFindersEvent.class, event -> {
            if (event.getPackType() == PackType.SERVER_DATA) {
                for (int i = 0; i < 128; i++) {
                    var id = "bulk_known_pack_test/" + i;
                    PackLocationInfo info = new PackLocationInfo(id, Component.literal(i + "th containing single entry"), PackSource.BUILT_IN, Optional.of(new KnownPack(NAMESPACE, id, "1.0.0")));
                    final LoginPacketSplitTest.InMemoryResourcePack pack = new LoginPacketSplitTest.InMemoryResourcePack(info);
                    generateEntry(pack, i);
                    event.addRepositorySource(packs -> packs.accept(Pack.readMetaAndCreate(
                            pack.location(),
                            BuiltInPackSource.fixedResources(pack),
                            PackType.SERVER_DATA,
                            new PackSelectionConfig(true, Pack.Position.TOP, false))));
                }
            }
        });
    }

    public static class Listener implements TestListener {
        @Override
        public void onEnabled(TestFramework framework, Test test, @Nullable Entity changer) {
            RegistryAccess access = changer.registryAccess();
            var biomes = access.registry(Registries.BIOME).orElseThrow();
            for (int i = 0; i < 128; i++) {
                var id = new ResourceLocation(NAMESPACE, "entry_" + i);
                biomes.getHolder(id).orElseThrow(() -> new IllegalStateException("Entry " + id + " that should be synced by KnownPack not found"));
            }
        }
    }

    private static void generateEntry(LoginPacketSplitTest.InMemoryResourcePack pack, int i) {
        JsonObject json = new JsonObject();
        json.addProperty("temperature", 1.0);
        json.addProperty("downfall", 1.0);
        json.addProperty("has_precipitation", false);
        JsonObject effects = new JsonObject();
        effects.addProperty("sky_color", 0.0);
        effects.addProperty("fog_color", 0.0);
        effects.addProperty("water_color", 0.0);
        effects.addProperty("water_fog_color", 0.0);
        json.add("effects", effects);
        json.add("spawners", new JsonObject());
        json.add("spawn_costs", new JsonObject());
        json.add("carvers", new JsonObject());
        json.add("features", new JsonArray());
        pack.putData(new ResourceLocation(NAMESPACE, "worldgen/biome/entry_" + i + ".json"), json);
    }
}
