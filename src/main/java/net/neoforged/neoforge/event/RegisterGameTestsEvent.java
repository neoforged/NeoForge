/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;

/**
 * Game tests are registered on client or server startup.
 * It is run in {@link net.minecraft.resources.RegistryDataLoader#load(ResourceManager, List, List)} if {@link GameTestHooks#isGametestEnabled} returns true.
 * <p>
 * Fired on the Mod bus, see {@link IModBusEvent}.
 */
public class RegisterGameTestsEvent extends Event implements IModBusEvent {
    private final WritableRegistry<TestEnvironmentDefinition> environmentsRegistry;
    private final WritableRegistry<GameTestInstance> testsRegistry;

    public RegisterGameTestsEvent(WritableRegistry<TestEnvironmentDefinition> environmentsRegistry, WritableRegistry<GameTestInstance> testsRegistry) {
        this.environmentsRegistry = environmentsRegistry;
        this.testsRegistry = testsRegistry;
    }

    public Holder<TestEnvironmentDefinition> registerEnvironment(ResourceLocation name, TestEnvironmentDefinition... definitions) {
        return registerEnvironment(name, new TestEnvironmentDefinition.AllOf(definitions));
    }

    public Holder<TestEnvironmentDefinition> registerEnvironment(ResourceLocation name, TestEnvironmentDefinition definition) {
        return environmentsRegistry.register(
                ResourceKey.create(Registries.TEST_ENVIRONMENT, name),
                definition, RegistrationInfo.BUILT_IN);
    }

    public void registerTest(ResourceLocation name, Function<TestData<Holder<TestEnvironmentDefinition>>, GameTestInstance> factory, TestData<Holder<TestEnvironmentDefinition>> testData) {
        registerTest(name, factory.apply(testData));
    }

    public void registerTest(ResourceLocation name, GameTestInstance test) {
        testsRegistry.register(
                ResourceKey.create(Registries.TEST_INSTANCE, name),
                test, RegistrationInfo.BUILT_IN);
    }
}
