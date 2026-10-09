package net.neoforged.neoforge.common.loot;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.function.Consumer;

public class NeoForgeLootContextParamSets {

    public static final ContextKeySet DATAMAPS_FLAMMABLE = register(
            "datamaps/flammable",
            builder -> builder.required(LootContextParams.BLOCK_STATE)
                    .required(LootContextParams.ORIGIN)
                    .required(net.neoforged.neoforge.common.loot.NeoForgeLootContextParams.DIRECTION)
    );

    private static ContextKeySet register(String name, Consumer<ContextKeySet.Builder> consumer) {
        ResourceKey<ContextKeySet> key = ResourceKey.create(Registries.CONTEXT_KEY_SET, Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID,name));
        return register(consumer, key);
    }

    private static ContextKeySet register(Consumer<ContextKeySet.Builder> consumer, ResourceKey<ContextKeySet> key) {
        ContextKeySet.Builder builder = new ContextKeySet.Builder();
        consumer.accept(builder);
        return Registry.register(BuiltInRegistries.CONTEXT_KEY_SET, key, builder.build());
    }
}
