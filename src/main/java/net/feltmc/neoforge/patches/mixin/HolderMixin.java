package net.feltmc.neoforge.patches.mixin;

import net.feltmc.neoforge.patches.interfaces.HolderInterface;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.stream.Stream;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Holder.class)
public abstract class HolderMixin<T> implements HolderInterface<T> {
	
	@Shadow public abstract boolean is(TagKey<T> var1);
	
	@Shadow public abstract Stream<TagKey<T>> tags();
	
	@Shadow public abstract T value();
	
	@Override
	public boolean containsTag(TagKey<T> key) {
		return this.is(key);
	}
	
	@Override
	public Stream<TagKey<T>> getTagKeys() {
		return this.tags();
	}
	
	@Override
	public T get() {
		return this.value();
	}
	
}
