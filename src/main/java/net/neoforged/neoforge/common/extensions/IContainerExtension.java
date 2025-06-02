package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Extension methods for {@link Container}. These methods are used by {@link net.neoforged.neoforge.transfer.handlers.wrappers.itemsmk2.AlternateVanillaContainerResourceWrapper AlternateVanillaContainerResourceWrapper},
 * to allow containers to be integrated in a transaction.
 */
public interface IContainerExtension {


    private Container self() {
        return (Container) this;
    }

    /**
     * Variant of {@link Container#setItem(int, ItemStack)} that allows disabling side effects.
     *
     * <p>If {@code performSideEffects} is {@code false},
     * side effects (e.g. calling {@code setChanged} or making changes to the world) should not be performed.
     */
    default void setItem(int slot, ItemStack stack, boolean performSideEffects) {
        self().setItem(slot, stack);
    }

    /**
     * Perform side effects that were not done in {@link #setItem(int, ItemStack, boolean)}
     * because {@code performSideEffects} was false.
     *
     * <p>There is no need to call {@code setChanged}, as it is already called by {@link net.neoforged.neoforge.transfer.handlers.wrappers.itemsmk2.AlternateVanillaContainerResourceWrapper AlternateVanillaContainerResourceWrapper}.
     */
    default void performSideEffects(int slot, ItemStack originalStack) { }

    /**
     * Perform additional logic immediately after successful transfer (i.e. insert or extract with result > 0).
     * Any logic performed here should be fully transactional, and support being rolled back.
     */
    default void onTransfer(int slot, TransactionContext context) { }
}
