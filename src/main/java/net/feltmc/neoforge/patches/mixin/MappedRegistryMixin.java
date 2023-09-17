package net.feltmc.neoforge.patches.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;
import io.github.feltmc.feltasm.asm.CreateStatic;
import net.feltmc.neoforge.patches.interfaces.MappedRegistryInterface;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@SuppressWarnings({"MissingUnique", "AddedMixinMembersNamePattern", "rawtypes"})
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements MappedRegistryInterface {
	
	@Shadow public abstract ResourceKey<? extends Registry<T>> key();
	
	@Shadow private boolean frozen;
	
	@Shadow @Nullable public Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
	
	@CreateStatic
	private static final Set<ResourceLocation> KNOWN = new LinkedHashSet<>(); // LinkedHashSet may not be best choice here
	
	// TODO: this doesn't work for dev
	@CreateStatic
	private static Set<ResourceLocation> getKnownRegistries() {
		return java.util.Collections.unmodifiableSet(KNOWN);
	}
	
	@Override
	public final void markKnown() {
		KNOWN.add(key().location());
	}
	
	@Inject(method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder$Reference;", at = @At("HEAD"))
	public void registerMapping$markKnown(int i, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle, CallbackInfoReturnable<Holder.Reference<T>> cir) {
		markKnown();
	}
	
	// TODO: @WrapOperation may be better than @Inject for this
	@Inject(method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder$Reference;", at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", shift = At.Shift.AFTER))
	public void registerMapping$bindValueImmediately(int i, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle, CallbackInfoReturnable<Holder.Reference<T>> cir, @Local Holder.Reference reference) {
		//noinspection unchecked
		reference.bindValue(object);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void unfreeze() {
		this.frozen = false;
	}
	
	@Redirect(method = "freeze", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
	public void freeze$forEach(Map instance, BiConsumer biConsumer) {}
	
	@Inject(method = "freeze", at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z", shift = At.Shift.BEFORE), cancellable = true)
	public void freeze$unregisteredIntrusiveHolders$isEmpty(CallbackInfoReturnable<Registry<T>> cir) {
		assert unregisteredIntrusiveHolders != null;
		if (unregisteredIntrusiveHolders.isEmpty()) {
			cir.cancel();
			//noinspection unchecked
			cir.setReturnValue((Registry<T>) (Object) this);
		}
	}
	
}
