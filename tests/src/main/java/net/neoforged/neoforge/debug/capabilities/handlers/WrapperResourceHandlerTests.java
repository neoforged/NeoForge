package net.neoforged.neoforge.debug.capabilities.handlers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.wrappers.")
public class WrapperResourceHandlerTests {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidUtil#tryPickupFluid works correctly")
    private static void combined(ExtendedGameTestHelper helper) {
        var player = helper.makeMockPlayer();

        IResourceHandler<ItemResource> cap = player.getCapability(Capabilities.ItemHandler.ENTITY);
        helper.assertFalse(cap == null,"Player capability should be present");
        helper.assertValueEqual(ResourceHandlerUtil.insertStacking(cap, Items.APPLE.defaultResource, 400, TransferAction.EXECUTE), 400, "apples");
        helper.assertValueEqual(cap.insert(38, Items.DIAMOND_CHESTPLATE.defaultResource.with(DataComponents.DAMAGE,20), 2, TransferAction.EXECUTE), 1, "armor insert");
        helper.assertValueEqual(cap.insert(39, Items.DIAMOND_CHESTPLATE.defaultResource, 2, TransferAction.EXECUTE), 0, "armor insert");
        helper.assertValueEqual(ResourceHandlerUtil.extractFiltered(cap, itemResource -> itemResource.is(Items.DIAMOND_CHESTPLATE), 2, TransferAction.EXECUTE, ItemResource.NONE).amount(), 1, "armor extract");
        if(cap instanceof PlayerInventoryWrapper wrapper){
            wrapper.armorHandler.insert(Items.DIAMOND_BOOTS.defaultResource,1300, TransferAction.EXECUTE);
            wrapper.armorHandler.insert(Items.NETHERITE_HELMET.defaultResource,1300, TransferAction.EXECUTE);
            ResourceHandlerUtil.extractAny(wrapper.mainHandHandler, 1000, TransferAction.EXECUTE, ItemResource.NONE);
        }

        helper.succeed();
    }
}
