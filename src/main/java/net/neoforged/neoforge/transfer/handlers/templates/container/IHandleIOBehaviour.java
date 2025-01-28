package net.neoforged.neoforge.transfer.handlers.templates.container;

import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import org.jetbrains.annotations.Contract;

/**
 * Control logic for a {@link IResourceContainer} to handle a per slot interaction validation.
 * Unlike the {@link IResourceHandler#allowsInsertion() allows} methods, this is intended to be used during insert/extract, but still isn't expected to dynamically change
 */
public interface IHandleIOBehaviour {
    IHandleIOBehaviour DEFAULT = new IHandleIOBehaviour() {};
    IHandleIOBehaviour EXTRACT_ONLY = new IHandleIOBehaviour() {
        @Override
        public boolean canInsert(int slot) {
            return false;
        }
    };
    IHandleIOBehaviour INSERT_ONLY = new IHandleIOBehaviour() {
        @Override
        public boolean canExtract(int slot) {
            return false;
        }
    };

    @Contract(pure = true)
    default boolean canInsert(int slot) {
        return true;
    }

    @Contract(pure = true)
    default boolean canExtract(int slot) {
        return true;
    }
}
