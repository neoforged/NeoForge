package net.feltmc.neoforge.patches.interfaces;

import net.feltmc.neoforge.FeltVars;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.Map;

public interface DataGeneratorInterface {
        default Map<String, DataProvider> getProvidersView() {
            throw new RuntimeException(FeltVars.mixinOverrideException);
        }

        default PackOutput getPackOutput() {
            throw new RuntimeException(FeltVars.mixinOverrideException);
        }

        default PackOutput getPackOutput(String path) {
            throw new RuntimeException(FeltVars.mixinOverrideException);
        }

        default <T extends DataProvider> T addProvider(boolean run, DataProvider.Factory<T> factory) {
            throw new RuntimeException(FeltVars.mixinOverrideException);
        }

        default <T extends DataProvider> T addProvider(boolean run, T provider) {
            throw new RuntimeException(FeltVars.mixinOverrideException);
        }
}
