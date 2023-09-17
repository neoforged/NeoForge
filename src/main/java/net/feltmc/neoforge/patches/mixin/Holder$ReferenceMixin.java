package net.feltmc.neoforge.patches.mixin;

import net.feltmc.neoforge.patches.interfaces.Holder$ReferenceInterface;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Holder.Reference.class)
public class Holder$ReferenceMixin<T> implements Holder$ReferenceInterface<T> {
	
	@Shadow @Final private Holder.Reference.Type type;
	
	@Override
	public Holder.Reference.Type getType() {
		return type;
	}
	
}
