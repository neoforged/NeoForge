/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.testframework.client.FrameworkClientImpl;
import net.neoforged.testframework.conf.ClientConfiguration;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface FrameworkClient {
    void init(IEventBus modBus, ModContainer container);

    interface Factory {
        FrameworkClient create(TestFrameworkInternal impl, ClientConfiguration clientConfiguration);
    }

    static Optional<Factory> factory() {
        return Optional.of(new FrameworkClientImpl.Factory());
//        return ServiceLoader.load(Factory.class).findFirst();
    }
}
