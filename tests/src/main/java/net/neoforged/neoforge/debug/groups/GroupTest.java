package net.neoforged.neoforge.debug.groups;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestGroup;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.registration.RegistrationHelper;


@ForEachTest(groups = GroupTest.groupA)
public class GroupTest {
    @TestGroup(name = "Group A", enabledByDefault = true)
    public static final String groupA = "group_a";

    private static final RegistrationHelper HELPER = RegistrationHelper.create("group_test");
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = HELPER.attachments();

    @OnInit
    static void init(final TestFramework framework) {
        var bus = framework.modEventBus();
        ATTACHMENTS.register(bus);
    }

    public static BlockPos setupLevelEnvironment(ExtendedGameTestHelper helper) {
        var blockPos = new BlockPos(1, 1, 1);
        helper.setBlock(blockPos, Blocks.FURNACE);
        return blockPos;
    }
}
