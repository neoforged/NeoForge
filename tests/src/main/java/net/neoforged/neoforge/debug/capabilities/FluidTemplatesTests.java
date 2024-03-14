package net.neoforged.neoforge.debug.capabilities;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@ForEachTest(groups = "capabilities.fluidtemplates")
public class FluidTemplatesTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidHandlerItemStack works")
    public static void testFluidHandlerItemStack(ExtendedGameTestHelper helper) {
        ItemStack stack = Items.APPLE.getDefaultInstance();
        int capacity = 2 * FluidType.BUCKET_VOLUME;
        var fluidHandler = new FluidHandlerItemStack(stack, capacity);

        if (fluidHandler.getTanks() != 1) {
            helper.fail("Expected a single tank");
        }
        if (fluidHandler.getTankCapacity(0) != capacity) {
            helper.fail("Expected tank capacity of " + capacity);
        }
        if (fluidHandler.getFluidInTank(0).getAmount() != 0) {
            helper.fail("Expected empty tank");
        }
        if (stack.has(NeoForgeMod.FLUID_STACK_COMPONENT.get())) {
            helper.fail("Expected no fluid stack component");
        }

        var waterStack = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
        if (fluidHandler.fill(waterStack, IFluidHandler.FluidAction.EXECUTE) != FluidType.BUCKET_VOLUME) {
            helper.fail("Expected to be able to fill a bucket of water");
        }
        if (!stack.has(NeoForgeMod.FLUID_STACK_COMPONENT.get())) {
            helper.fail("Expected fluid stack component");
        }
        if (fluidHandler.getFluidInTank(0).getAmount() != FluidType.BUCKET_VOLUME) {
            helper.fail("Expected a bucket of water");
        }

        var drained = fluidHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
        if (!FluidStack.matches(drained, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME))) {
            helper.fail("Expected to drain a bucket of water");
        }
        if (fluidHandler.getFluidInTank(0).getAmount() != 0) {
            helper.fail("Expected empty tank");
        }
        if (stack.has(NeoForgeMod.FLUID_STACK_COMPONENT.get())) {
            helper.fail("Expected no fluid stack component");
        }

        helper.succeed();
    }
}
