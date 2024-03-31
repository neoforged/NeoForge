package net.neoforged.testframework.impl.test;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTestData;
import net.neoforged.testframework.gametest.GameTestHelperFactory;

import java.util.function.Consumer;

public class SimpleGameTestTest<T extends GameTestHelper> extends AbstractTest.Dynamic {

    private final Class<T> helperType;
    private final Consumer<T> function;

    public SimpleGameTestTest(GameTestData data, Class<T> helperType, Consumer<T> function) {
        this.gameTestData = data;
        this.helperType = helperType;
        this.function = function;

        this.visuals.description().add(Component.literal("GameTest-only"));
    }

    @Override
    public void init(TestFramework framework) {
        super.init(framework);
        onGameTest(helperType, helper -> {
            try {
                function.accept(helper);
            } catch (Throwable e) {
                throw new RuntimeException("Encountered exception running method-based test", e);
            }
        });
    }
}
