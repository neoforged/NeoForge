package net.neoforged.testframework.impl.reg;

import com.google.common.base.Suppliers;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.registration.DeferredBlocks;
import net.neoforged.testframework.registration.DeferredEntityTypes;
import net.neoforged.testframework.registration.DeferredItems;
import net.neoforged.testframework.registration.RegistrationHelper;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RegistrationHelperImpl implements RegistrationHelper {
    private final TestFramework framework;
    public RegistrationHelperImpl(TestFramework framework, String modId) {
        this.framework = framework;
        this.modId = modId;
    }

    private interface DataGenProvider<T extends DataProvider> {
        T create(PackOutput output, DataGenerator generator, ExistingFileHelper existingFileHelper, String modId, List<Consumer<T>> consumers);
    }
    private static final Map<Class<?>, DataGenProvider<?>> PROVIDERS;
    static {
        final Map<Class<?>, DataGenProvider<?>> providers = new IdentityHashMap<>();
        class ProviderRegistrar {
            <T extends DataProvider> void register(Class<T> type, DataGenProvider<T> provider) {
                providers.put(type, provider);
            }
        }
        final var reg = new ProviderRegistrar();

        reg.register(LanguageProvider.class, (output, generator, existingFileHelper, modId, consumers) -> new LanguageProvider(output, modId, "en_us") {
            @Override
            protected void addTranslations() {
                consumers.forEach(c -> c.accept(this));
            }
        });
        reg.register(BlockStateProvider.class, (output, generator, existingFileHelper, modId, consumers) -> new BlockStateProvider(output, modId, existingFileHelper) {
            @Override
            protected void registerStatesAndModels() {
                existingFileHelper.trackGenerated(new ResourceLocation("testframework:block/white"), ModelProvider.TEXTURE);
                consumers.forEach(c -> c.accept(this));
            }
        });
        reg.register(ItemModelProvider.class, (output, generator, existingFileHelper, modId, consumers) -> new ItemModelProvider(output, modId, existingFileHelper) {
            @Override
            protected void registerModels() {
                consumers.forEach(c -> c.accept(this));
            }
        });

        PROVIDERS = Map.copyOf(providers);
    }

    private final String modId;
    private final ListMultimap<Class<?>, Consumer<? extends DataProvider>> providers = Multimaps.newListMultimap(new IdentityHashMap<>(), ArrayList::new);
    @Override
    public <T> DeferredRegister<T> registrar(ResourceKey<Registry<T>> registry) {
        final var reg = DeferredRegister.create(registry, modId);
        reg.register(framework.modEventBus());
        return reg;
    }

    private DeferredBlocks blocks;
    @Override
    public DeferredBlocks blocks() {
        if (blocks == null) {
            blocks = new DeferredBlocks(modId, this);
            blocks.register(framework.modEventBus());
        }
        return blocks;
    }

    private DeferredItems items;
    @Override
    public DeferredItems items() {
        if (items == null) {
            items = new DeferredItems(modId, this);
            items.register(framework.modEventBus());
        }
        return items;
    }

    private DeferredEntityTypes entityTypes;
    @Override
    public DeferredEntityTypes entityTypes() {
        if (entityTypes == null) {
            entityTypes = new DeferredEntityTypes(modId, this);
            entityTypes.register(framework.modEventBus());
        }
        return entityTypes;
    }

    @Override
    public <T extends DataProvider> void provider(Class<T> type, Consumer<T> consumer) {
        providers.put(type, consumer);
    }

    @Override
    public TestFramework framework() {
        return framework;
    }

    @SubscribeEvent
    void gather(final GatherDataEvent event) {
        providers.asMap().forEach((cls, cons) -> event.getGenerator().addProvider(true, PROVIDERS.get(cls).create(
                event.getGenerator().getPackOutput(), event.getGenerator(), event.getExistingFileHelper(), modId, (List) cons
        )));
    }
}
