package net.neoforged.neoforge.common.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public class NeoForgeLootContextParamSets {
    private static final DeferredRegister<ContextKeySet> CONTEXT_KEY_SET = DeferredRegister.create(Registries.CONTEXT_KEY_SET, NeoForgeMod.MOD_ID);

    public static final DeferredHolder<ContextKeySet, ContextKeySet> DATAMAPS_FLAMMABLE = register(
            "datamaps/flammable",
            builder -> builder.required(LootContextParams.BLOCK_STATE)
                    .required(LootContextParams.ORIGIN)
                    .required(NeoForgeLootContextParams.DIRECTION)
    );

    private static DeferredHolder<ContextKeySet, ContextKeySet> register(String name, Consumer<ContextKeySet.Builder> consumer) {
        ContextKeySet.Builder builder = new ContextKeySet.Builder();
        consumer.accept(builder);
        return CONTEXT_KEY_SET.register(name, builder::build);
    }

    public static void register(IEventBus bus) {
        CONTEXT_KEY_SET.register(bus);
    }
}
