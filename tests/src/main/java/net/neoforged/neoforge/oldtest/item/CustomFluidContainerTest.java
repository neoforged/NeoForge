/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import java.util.Arrays;
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
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;

@Mod(CustomFluidContainerTest.MODID)
public class CustomFluidContainerTest {
    public static final String MODID = "custom_fluid_container_test";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<DataComponentType<?>> COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final boolean ENABLED = true;

    public static final DeferredItem<Item> CUSTOM_FLUID_CONTAINER = ITEMS.registerItem("custom_fluid_container", props -> new CustomFluidContainer(props.stacksTo(1)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> SIMPLE_FLUID_CONTENT = COMPONENT_TYPES.register("simple_fluid_content", () -> DataComponentType.<SimpleFluidContent>builder()
            .persistent(SimpleFluidContent.CODEC)
            .networkSynchronized(SimpleFluidContent.STREAM_CODEC).build());

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
        event.registerItem(Capabilities.Fluid.ITEM, (stack, itemAccess) -> new ItemAccessFluidHandler(itemAccess, SIMPLE_FLUID_CONTENT.get(), FluidType.BUCKET_VOLUME), CUSTOM_FLUID_CONTAINER.get());
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
            var fluidStack = FluidUtil.getFirstStackContained(itemStack);
            String name = "Custom Fluid Container";
            if (fluidStack.isEmpty()) {
                name = name + " (empty)";
            } else {
                name = name + " (" + fluidStack.getFluidType().getDescription().getString() + ")";
            }
            return Component.literal(name);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            var handler = ItemAccess.forPlayerInteraction(player, hand).oneByOne().getCapability(Capabilities.Fluid.ITEM);

            var blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            boolean success = !FluidUtil.tryPickupFluid(handler, player, level, blockHitResult.getBlockPos(), blockHitResult.getDirection()).isEmpty();

            if (!success) {
                blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
                //try to place fluid in hit block (waterlogging, fill tank, ...). When no success try the block on the hit side.
                for (BlockPos pos : Arrays.asList(blockHitResult.getBlockPos(), blockHitResult.getBlockPos().relative(blockHitResult.getDirection()))) {
                    success = !FluidUtil.tryPlaceFluid(handler, player, level, hand, pos).isEmpty();
                    if (success) break;
                }
            }

            if (success) {
                return InteractionResult.SUCCESS;
            }
            return super.use(level, player, hand);
        }
    }
}
