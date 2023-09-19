package net.feltmc.neoforge.patches.interfaces;

import net.feltmc.neoforge.FeltVars;

public interface ClientIntentionPacketInterface {
    default String getFMLVersion() {
        throw new RuntimeException(FeltVars.mixinOverrideException);
    }
}
