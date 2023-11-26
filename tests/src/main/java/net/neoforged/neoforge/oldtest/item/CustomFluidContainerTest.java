/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStackSimple;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CustomFluidContainerTest.MODID)
public class CustomFluidContainerTest {
    public static final String MODID = "custom_fluid_container_test";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final boolean ENABLED = true;

    public static final DeferredItem<Item> CUSTOM_FLUID_CONTAINER = ITEMS.register("custom_fluid_container", () -> new CustomFluidContainer((new Item.Properties()).stacksTo(1)));

    public CustomFluidContainerTest() {
        if (ENABLED) {
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            ITEMS.register(modEventBus);
            modEventBus.addListener(this::addCreative);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(CUSTOM_FLUID_CONTAINER);
    }

    /**
     * A custom fluid container item with a capacity of a vanilla bucket which uses the FluidUtil functionalities to pickup and place fluids.
     */
    private static class CustomFluidContainer extends Item {

        public CustomFluidContainer(Properties properties) {
            super(properties);
        }

        @Override
        @Nonnull
        public Component getName(@Nonnull ItemStack itemStack) {
            AtomicReference<String> name = new AtomicReference<>("Custom Fluid Container");
            FluidUtil.getFluidHandler(itemStack).ifPresent(fluidHandler -> {
                FluidStack fluidStack = fluidHandler.getFluidInTank(0);
                if (fluidStack.isEmpty()) {
                    name.set(name.get() + " (empty)");
                } else {
                    name.set(name.get() + " (" + fluidStack.getFluid().getFluidType().getDescription().getString() + ")");
                }
            });
            return Component.literal(name.get());
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            var itemStack = player.getItemInHand(hand);
            var result = new AtomicReference<FluidActionResult>();
            FluidUtil.getFluidHandler(itemStack).ifPresent(fluidHandler -> {
                var fluidStack = fluidHandler.getFluidInTank(0);
                if (fluidStack.isEmpty()) {
                    var blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
                    result.set(FluidUtil.tryPickUpFluid(itemStack, player, level, blockHitResult.getBlockPos(), blockHitResult.getDirection()));
                } else {
                    var blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
                    //try to place fluid in hit block (waterlogging, fill tank, ...). When no success try the block on the hit side.
                    for (BlockPos pos : Arrays.asList(blockHitResult.getBlockPos(), blockHitResult.getBlockPos().relative(blockHitResult.getDirection()))) {
                        result.set(FluidUtil.tryPlaceFluid(player, level, hand, pos, itemStack, fluidStack));
                        if (result.get().isSuccess()) {
                            break;
                        }
                    }
                }
            });
            if (result.get() != null && result.get().isSuccess()) {
                return InteractionResultHolder.sidedSuccess(result.get().getResult(), level.isClientSide());
            }
            return super.use(level, player, hand);
        }

        @Override
        public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag nbt) {
            return new FluidHandlerItemStackSimple(stack, FluidType.BUCKET_VOLUME);
        }

    }
}
