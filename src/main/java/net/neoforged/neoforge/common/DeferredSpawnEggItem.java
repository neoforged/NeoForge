/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class DeferredSpawnEggItem extends SpawnEggItem {
    private static final List<DeferredSpawnEggItem> MOD_EGGS = new ArrayList<>();
    private static final Map<EntityType<? extends Mob>, DeferredSpawnEggItem> TYPE_MAP = new IdentityHashMap<>();
    private final Supplier<? extends EntityType<? extends Mob>> typeSupplier;

    public DeferredSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Properties props) {
        super((EntityType<? extends Mob>) null, backgroundColor, highlightColor, props);
        this.typeSupplier = type;

        MOD_EGGS.add(this);
    }

    @Nullable
    protected DispenseItemBehavior createDispenseBehavior() {
        return DEFAULT_DISPENSE_BEHAVIOR;
    }

    @ApiStatus.Internal
    @Nullable
    public static SpawnEggItem deferredOnlyById(@Nullable EntityType<?> type) {
        return TYPE_MAP.get(type);
    }

    @Override
    protected EntityType<?> getDefaultType() {
        return this.typeSupplier.get();
    }

    private static final DispenseItemBehavior DEFAULT_DISPENSE_BEHAVIOR = (source, stack) -> {
        Direction face = source.state().getValue(DispenserBlock.FACING);
        EntityType<?> type = ((SpawnEggItem) stack.getItem()).getType(stack);

        try {
            type.spawn(source.level(), stack, null, source.pos().relative(face), MobSpawnType.DISPENSER, face != Direction.UP, false);
        } catch (Exception exception) {
            DispenseItemBehavior.LOGGER.error("Error while dispensing spawn egg from dispenser at {}", source.pos(), exception);
            return ItemStack.EMPTY;
        }

        stack.shrink(1);
        source.level().gameEvent(GameEvent.ENTITY_PLACE, source.pos(), GameEvent.Context.of(source.state()));
        return stack;
    };

    @EventBusSubscriber(modid = "neoforge", bus = EventBusSubscriber.Bus.MOD)
    private static class CommonHandler {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                MOD_EGGS.forEach(egg -> {
                    DispenseItemBehavior dispenseBehavior = egg.createDispenseBehavior();
                    if (dispenseBehavior != null) {
                        DispenserBlock.registerBehavior(egg, dispenseBehavior);
                    }

                    TYPE_MAP.put(egg.typeSupplier.get(), egg);
                });
            });
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = "neoforge", bus = EventBusSubscriber.Bus.MOD)
    private static class ColorRegisterHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void registerSpawnEggColors(RegisterColorHandlersEvent.Item event) {
            MOD_EGGS.forEach(egg -> event.register((stack, layer) -> egg.getColor(layer), egg));
        }
    }
}
