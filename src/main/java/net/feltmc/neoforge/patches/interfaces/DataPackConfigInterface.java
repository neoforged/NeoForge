package net.feltmc.neoforge.patches.interfaces;

import net.feltmc.neoforge.FeltVars;

import java.util.List;

public interface DataPackConfigInterface {
    default void addModPacks(List<String> modPacks) {
        throw new RuntimeException(FeltVars.mixinOverrideException);
    }
}
