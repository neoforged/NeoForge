package net.feltmc.neoforge.patches.interfaces;

import net.feltmc.neoforge.FeltVars;

@SuppressWarnings("DeprecatedIsStillUsed")
public interface MappedRegistryInterface {
	
	default void markKnown() {
		throw new RuntimeException(FeltVars.mixinOverrideException);
	}
	
	@Deprecated
	public default void unfreeze() {
		throw new RuntimeException(FeltVars.mixinOverrideException);
	}
	
}
