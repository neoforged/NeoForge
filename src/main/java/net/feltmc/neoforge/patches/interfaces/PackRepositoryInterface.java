package net.feltmc.neoforge.patches.interfaces;

import net.feltmc.neoforge.FeltVars;
import net.minecraft.server.packs.repository.RepositorySource;

public interface PackRepositoryInterface {
    default void addPackFinder(RepositorySource packFinder) {
        throw new RuntimeException(FeltVars.mixinOverrideException);
    }
}
