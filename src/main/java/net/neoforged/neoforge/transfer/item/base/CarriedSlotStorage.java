package net.neoforged.neoforge.transfer.item.base;

import com.google.common.collect.MapMaker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * An implementation of {@code Storage<ItemVariant>} for the carried slot of an {@link AbstractContainerMenu}.
 */
public final class CarriedSlotStorage extends SingleStackStorage {
    private static final Map<AbstractContainerMenu, CarriedSlotStorage> WRAPPERS = new MapMaker().weakValues().makeMap();

    /**
     * Return a wrapper around the carried slot of a menu,
     * i.e. the stack that can be manipulated with {@link AbstractContainerMenu#getCarried}
     * and {@link AbstractContainerMenu#setCarried}.
     */
    public static CarriedSlotStorage of(AbstractContainerMenu menu) {
        return WRAPPERS.computeIfAbsent(menu, CarriedSlotStorage::new);
    }

    private final AbstractContainerMenu menu;

    private CarriedSlotStorage(AbstractContainerMenu menu) {
        this.menu = menu;
    }

    @Override
    protected ItemStack getStack() {
        return menu.getCarried();
    }

    @Override
    protected void setStack(ItemStack stack) {
        menu.setCarried(stack);
    }

    @Override
    public String toString() {
        return "CarriedSlotStorage[" + menu + "/" + BuiltInRegistries.MENU.getId(menu.getType()) + "]";
    }
}
