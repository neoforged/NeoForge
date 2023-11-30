package net.neoforged.testframework.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.impl.TestFrameworkInternal;
import net.neoforged.testframework.impl.TestFrameworkMod;

public record TestEnabledLootCondition(TestFramework framework, String testId) implements LootItemCondition {
    public static final Codec<TestEnabledLootCondition> CODEC = RecordCodecBuilder.create(in -> in.group(
            TestFrameworkInternal.REFERENCE_CODEC.fieldOf("framework").forGetter(TestEnabledLootCondition::framework),
            Codec.STRING.fieldOf("test").forGetter(TestEnabledLootCondition::testId)
    ).apply(in, TestEnabledLootCondition::new));

    public TestEnabledLootCondition(DynamicTest test) {
        this(test.framework(), test.id());
    }

    @Override
    public LootItemConditionType getType() {
        return TestFrameworkMod.TEST_ENABLED.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        return framework.tests().isEnabled(testId);
    }
}
