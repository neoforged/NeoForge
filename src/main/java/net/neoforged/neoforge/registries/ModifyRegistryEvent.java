package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired for every builtin registry and datapack registry after they are constructed.
 * For builtin registries, this event is fired after vanilla entries are registered but before modded entries.
 * For datapack registries, this event is fired before any entries are registered.
 * <p>
 * This event can be used to register {@linkplain IForgeRegistry#addCallback(Callback) callbacks} to the registry.
 * </p>
 *
 * <p>This event is not {@linkplain Cancelable cancellable}, and does not {@linkplain HasResult have a result}.</p>
 * <p>This event is fired on the {@linkplain FMLJavaModLoadingContext#getModEventBus() mod-specific event bus},
 * on both {@linkplain LogicalSide logical sides}.</p>
 *
 * @see NewRegistryEvent
 * @see DataPackRegistryEvent.NewRegistry
 */
public class ModifyRegistryEvent extends Event implements IModBusEvent {
    private final ResourceKey<? extends Registry<?>> registryKey;
    private final Registry<?> registry;
    private final boolean builtin;

    @ApiStatus.Internal
    public ModifyRegistryEvent(Registry<?> registry) {
        this.registryKey = registry.key();
        this.registry = registry;
        this.builtin = BuiltInRegistries.REGISTRY.containsKey(this.registryKey.location());
    }

    public ResourceKey<? extends Registry<?>> getRegistryKey() {
        return this.registryKey;
    }

    public Registry<?> getRegistry() {
        return this.registry;
    }

    /**
     * @return {@code true} if this registry is a {@linkplain net.minecraft.core.registries.BuiltInRegistries builtin registry}
     */
    public boolean isBuiltin() {
        return this.builtin;
    }

    /**
     * @param key the registry key to compare again {@link #getRegistryKey()}
     * @return The registry typed to the given registry key if it matches {@link #getRegistryKey()},
     * or {@code null} if it does not match.
     */
    @SuppressWarnings("unchecked")
    public <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return key == this.registryKey ? (Registry<T>) this.registry : null;
    }
}
