package net.neoforged.neoforge.transfer.handlers.resources;

import net.neoforged.neoforge.transfer.resources.IResource;

public record ISimplifiedHandler<T extends IResource>(IResourceHandler<T> handler) {

}
