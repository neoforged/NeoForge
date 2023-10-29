/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.world.ModifiableStructureInfo.StructureInfo.Builder;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.ForgeRegistries.Keys;
import net.neoforged.neoforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>This tests the requirements of structure modifier jsons::</p>
 * <ul>
 * <li>A structure modifier json is created via datagen.</li>
 * <li>The structure modifier adds a mob spawn of the given category to the given structures.</li>
 * </ul>
 * <p>If the structure modifier is applied correctly, then strongholds should have wither skeletons spawn in them.</p>
 */
@Mod(StructureModifierTest.MODID)
public class StructureModifierTest {
    private static final Codec<HolderSet<Structure>> STRUCTURE_LIST_CODEC = RegistryCodecs.homogeneousList(Registries.STRUCTURE, Structure.DIRECT_CODEC);
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "structure_modifiers_test";
    public static final boolean ENABLED = true;
    public static final String TEST = "test";
    public static final ResourceLocation ADD_SPAWNS_TO_STRUCTURE_RL = new ResourceLocation(MODID, TEST);
    public static final String MODIFY_STRONGHOLD = "modify_stronghold";
    public static final ResourceLocation MODIFY_STRONGHOLD_RL = new ResourceLocation(MODID, MODIFY_STRONGHOLD);

    public StructureModifierTest() {
        if (!ENABLED)
            return;

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Serializer types can be registered via deferred register.
        final DeferredRegister<Codec<? extends StructureModifier>> serializers = DeferredRegister.create(Keys.STRUCTURE_MODIFIER_SERIALIZERS, MODID);
        serializers.register(modBus);
        serializers.register(TEST, TestModifier::makeCodec);

        // Structure modifiers don't need to be registered when defined in json, but they do need to be registered if we are to datagenerate the jsons.

        modBus.addListener(this::onGatherData);
    }

    private void onGatherData(GatherDataEvent event) {
/*   TODO: During the update to 1.19.3 data providers got partially turned into async executions. Creating a registry ops requires this.

     // Example of how to datagen datapack registry objects.
DataGenerator generator = event.getGenerator();
final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.BUILTIN.get());

// prepare to datagenerate our structure modifier
final StructureModifier structureModifier = new TestModifier(
      HolderSet.direct(ops.registry(Registry.STRUCTURE_REGISTRY).get().getHolder(BuiltinStructures.STRONGHOLD).orElseThrow()),
      MobCategory.MONSTER,
      new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 100, 5, 15)
);

 DataProvider structureModifierProvider =
      JsonCodecProvider.forDatapackRegistry(generator, event.getExistingFileHelper(), MODID, ops, ForgeRegistries.Keys.STRUCTURE_MODIFIERS,
    Map.of(MODIFY_STRONGHOLD_RL, structureModifier));
generator.addProvider(event.includeServer(), structureModifierProvider);*/
    }

    public record TestModifier(HolderSet<Structure> structures, MobCategory category, MobSpawnSettings.SpawnerData spawn)
            implements StructureModifier {

        private static final RegistryObject<Codec<? extends StructureModifier>> SERIALIZER = RegistryObject.create(ADD_SPAWNS_TO_STRUCTURE_RL, ForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS, MODID);

        @Override
        public void modify(Holder<Structure> structure, Phase phase, Builder builder) {
            if (phase == Phase.ADD && this.structures.contains(structure)) {
                builder.getStructureSettings()
                        .getOrAddSpawnOverrides(category)
                        .addSpawn(spawn);
            }
        }

        @Override
        public Codec<? extends StructureModifier> codec() {
            return SERIALIZER.get();
        }

        private static Codec<TestModifier> makeCodec() {
            return RecordCodecBuilder.create(builder -> builder.group(
                    STRUCTURE_LIST_CODEC.fieldOf("structures").forGetter(TestModifier::structures),
                    MobCategory.CODEC.fieldOf("category").forGetter(TestModifier::category),
                    MobSpawnSettings.SpawnerData.CODEC.fieldOf("spawn").forGetter(TestModifier::spawn)).apply(builder, TestModifier::new));
        }
    }
}
