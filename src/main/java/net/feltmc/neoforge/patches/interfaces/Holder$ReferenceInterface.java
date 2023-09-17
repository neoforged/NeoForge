package net.feltmc.neoforge.patches.interfaces;

import net.minecraft.core.Holder;

public interface Holder$ReferenceInterface<T> {
	
	default Holder.Reference.Type getType() {
		throw new RuntimeException();
	}
	
}
