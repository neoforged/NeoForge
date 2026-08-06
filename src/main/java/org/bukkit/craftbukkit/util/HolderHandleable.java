package org.bukkit.craftbukkit.util;

import net.minecraft.core.Holder;

public interface HolderHandleable<M> extends Handleable<M> {

    Holder<M> getHandleHolder();
}
