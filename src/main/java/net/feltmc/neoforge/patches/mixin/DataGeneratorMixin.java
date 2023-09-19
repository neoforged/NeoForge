package net.feltmc.neoforge.patches.mixin;

import net.feltmc.neoforge.patches.interfaces.DataGeneratorInterface;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

@Mixin(DataGenerator.class)
public class DataGeneratorMixin implements DataGeneratorInterface {

    @Shadow @Final private PackOutput vanillaPackOutput;

    @Shadow @Final private Path rootOutputFolder;

    @Shadow @Final private Set<String> allProviderIds;

    @Shadow @Final private Map<String, DataProvider> providersToRun;
    private final Map<String, DataProvider> providersView = java.util.Collections.unmodifiableMap(this.providersToRun);

    @Override
    public Map<String, DataProvider> getProvidersView() {
        return this.providersView;
    }

    @Override
    public PackOutput getPackOutput() {
        return this.vanillaPackOutput;
    }

    @Override
    public PackOutput getPackOutput(String path) {
        return new PackOutput(rootOutputFolder.resolve(path));
    }

    @Override
    public <T extends DataProvider> T addProvider(boolean run, DataProvider.Factory<T> factory) {
        return addProvider(run, factory.create(this.vanillaPackOutput));
    }

    @Override
    public <T extends DataProvider> T addProvider(boolean run, T provider) {
        String id = provider.getName();
        if (!this.allProviderIds.add(id))
            throw new IllegalStateException("Duplicate provider: " + id);
        if (run)
            this.providersToRun.put(id, provider);
        return provider;
    }
}
