package net.neoforged.neoforge.transfer.handlermk2;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import org.jetbrains.annotations.Range;

/**
 * A generic handler for handling a {@link IResource resource} of type {@link T} allowing for direct mutations of a specific slot.
 * It is advised to avoid calling {@link #set} on handlers that are not your own.
 *
 * @param <T> The type of {@link IResource resource} this handler manages.
 */
public interface IResourceHandlerModifiableTransaction<T extends IResource> extends IResourceHandlerTransaction<T> {
    /**
     * Sets the resource and amount at the given index to the given resource and amount. This bypasses all validation methods. This is intended for more internal use or testing specific scenarios.
     *
     * @param index    The index to set the resource at.
     * @param resource The resource to set.
     * @param amount   The amount of the resource to set.
     */
    void set(int index, T resource, @Range(from = 0, to = ResourceHandlerUtil.MAX_RESOURCE_SIZE) int amount);
}
