package net.feltmc.neoforge.patches.interfaces;

@SuppressWarnings("DeprecatedIsStillUsed")
public interface MappedRegistryInterface {
	
	default void markKnown() {
		throw new RuntimeException();
	}
	
	@Deprecated
	public default void unfreeze() {
		throw new RuntimeException();
	}
	
}
