/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.FluidUtil;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerContext;
import net.neoforged.neoforge.transfer.handlers.templates.fluids.SteppedItemContextFluidHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ItemContextResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

@Mod(CustomFluidContainerTest.MOD_ID)
public class CustomFluidContainerTest {
    public static final String MOD_ID = "custom_fluid_container_test";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<DataComponentType<?>> COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    public static final boolean ENABLED = true;

    public static final DeferredItem<Item> CUSTOM_FLUID_CONTAINER = ITEMS.registerItem("custom_fluid_container", props -> new CustomFluidContainer(props.stacksTo(1)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContextResourceHandler.Component<FluidResource>>> SIMPLE_FLUID_CONTENT = COMPONENT_TYPES.register("simple_fluid_content", () -> DataComponentType.<ItemContextResourceHandler.Component<FluidResource>>builder()
            .persistent(ItemContextResourceHandler.Component.codec(ResourceStack.flatCodec(FluidResource.OPTIONAL_CODEC)))
            .networkSynchronized(ItemContextResourceHandler.Component.streamCodec(ResourceStack.streamCodec(FluidResource.STREAM_CODEC))).build());

    public CustomFluidContainerTest(IEventBus modEventBus) {
        if (ENABLED) {
            ITEMS.register(modEventBus);
            COMPONENT_TYPES.register(modEventBus);
            modEventBus.addListener(this::addCreative);
            modEventBus.addListener(this::registerCaps);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(CUSTOM_FLUID_CONTAINER);
    }

    private void registerCaps(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new SteppedItemContextFluidHandler.Consumable(ctx, SIMPLE_FLUID_CONTENT.get(), FluidType.BUCKET_VOLUME), CUSTOM_FLUID_CONTAINER.get());
    }

    /**
     * A custom fluid container item with a capacity of a vanilla bucket which uses the FluidUtil functionalities to pickup and place fluids.
     */
    private static class CustomFluidContainer extends Item {
        public CustomFluidContainer(Properties properties) {
            super(properties);
        }

        @Override
        public Component getName(ItemStack itemStack) {
            AtomicReference<String> name = new AtomicReference<>("Custom Fluid Container");
            var fluidStack = FluidUtil.getFluidContained(itemStack);
            if (fluidStack.isEmpty()) {
                name.set(name.get() + " (empty)");
            } else {
                name.set(name.get() + " (" + fluidStack.getFluidType().getDescription().getString() + ")");
            }
            return net.minecraft.network.chat.Component.literal(name.get());
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            var context = PlayerContext.ofHand(player, hand);
            var handler = context.getCapability(Capabilities.FluidHandler.ITEM);
            if (handler == null) return super.use(level, player, hand);
            var fluidStack = FluidUtil.getFluidContained(context);
            if (fluidStack.isEmpty()) {
                var blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
                if (FluidUtil.tryPickupFluid(player, hand, level, blockHitResult.getBlockPos())) {
                    return InteractionResult.SUCCESS;
                }
            } else {
                var blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
                //try to place fluid in hit block (waterlogging, fill tank, ...). When no success try the block on the hit side.
                for (BlockPos pos : Arrays.asList(blockHitResult.getBlockPos(), blockHitResult.getBlockPos().relative(blockHitResult.getDirection()))) {
                    if (FluidUtil.tryPlaceFluid(player, hand, level, pos)) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return super.use(level, player, hand);
        }
    }
}
