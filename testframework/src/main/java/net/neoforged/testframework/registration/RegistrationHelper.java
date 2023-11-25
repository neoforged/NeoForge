package net.neoforged.testframework.registration;

import net.minecraft.core.Registry;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.testframework.TestFramework;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public interface RegistrationHelper {
    <T> DeferredRegister<T> registrar(ResourceKey<Registry<T>> registry);
    DeferredBlocks blocks();
    DeferredItems items();
    DeferredEntityTypes entityTypes();

    <T extends DataProvider> void provider(Class<T> type, Consumer<T> consumer);

    @ApiStatus.Internal
    TestFramework framework();
}
