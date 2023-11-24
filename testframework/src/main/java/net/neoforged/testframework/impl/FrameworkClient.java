package net.neoforged.testframework.impl;

import net.neoforged.testframework.client.FrameworkClientImpl;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

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
