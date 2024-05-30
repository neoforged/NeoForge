package net.neoforged.neoforge.transfer.storage.templates;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.storage.IResourceHandler;
import net.neoforged.neoforge.transfer.storage.IResourceHandlerModifiable;

public class AggregateResourceHandlerModifiable<T extends IResource> extends AggregateResourceHandler<T> implements IResourceHandlerModifiable<T> {
    @SafeVarargs
    public AggregateResourceHandlerModifiable(IResourceHandlerModifiable<T>... handlers) {
        super(handlers);
    }

    @Override
    public void set(int index, T resource, int amount) {
        for (IResourceHandler<T> storage : handlers) {
            if (index < storage.size()) {
                ((IResourceHandlerModifiable<T>) storage).set(index, resource, amount);
                return;
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }
}
