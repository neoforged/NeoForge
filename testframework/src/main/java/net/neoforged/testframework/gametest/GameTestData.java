package net.neoforged.testframework.gametest;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record GameTestData(
        @Nullable String batchName, String structureName, boolean required, int maxAttempts,
        int requiredSuccesses, Consumer<GameTestHelper> function, int maxTicks,
        long setupTicks, Rotation rotation
) {
}
