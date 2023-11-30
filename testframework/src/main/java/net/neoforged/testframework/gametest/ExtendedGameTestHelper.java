package net.neoforged.testframework.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;

public class ExtendedGameTestHelper extends GameTestHelper implements IGameTestHelperExtension {
    public ExtendedGameTestHelper(GameTestInfo p_127597_) {
        super(p_127597_);
    }

    @Override
    public ExtendedGameTestHelper self() {
        return this;
    }

    @Override
    public ExtendedSequence startSequence() {
        final var sq = new ExtendedSequence(testInfo);
        testInfo.sequences.add(sq);
        return sq;
    }
}
