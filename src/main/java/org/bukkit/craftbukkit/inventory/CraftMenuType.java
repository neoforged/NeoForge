package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.inventory.util.CraftMenus;
import org.bukkit.craftbukkit.registry.CraftRegistryItem;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.builder.InventoryViewBuilder;

public class CraftMenuType<V extends InventoryView, B extends InventoryViewBuilder<V>> extends CraftRegistryItem<net.minecraft.world.inventory.MenuType<?>> implements MenuType.Typed<V, B> {

    private final Supplier<CraftMenus.MenuTypeData<V, B>> typeData;

    public CraftMenuType(NamespacedKey key, Holder<net.minecraft.world.inventory.MenuType<?>> handle) {
        super(key, handle);
        this.typeData = Suppliers.memoize(() -> CraftMenus.getMenuTypeData(this));
    }

    @Override
    public V create(final HumanEntity player, final String title) {
        return builder().title(title).build(player);
    }

    @Override
    public B builder() {
        return typeData.get().viewBuilder().get();
    }

    @Override
    public Typed<InventoryView, InventoryViewBuilder<InventoryView>> typed() {
        return this.typed(InventoryView.class);
    }

    @Override
    public <V extends InventoryView, B extends InventoryViewBuilder<V>> Typed<V, B> typed(Class<V> clazz) {
        if (clazz.isAssignableFrom(typeData.get().viewClass())) {
            return (Typed<V, B>) this;
        }

        throw new IllegalArgumentException("Cannot type InventoryView " + (isRegistered() ? getKeyOrThrow() : toString()) + " to InventoryView type " + clazz.getSimpleName());
    }

    @Override
    public Class<? extends InventoryView> getInventoryViewClass() {
        return typeData.get().viewClass();
    }

    @Override
    public NamespacedKey getKey() {
        return getKeyOrThrow();
    }

    public static net.minecraft.world.inventory.MenuType<?> bukkitToMinecraft(MenuType bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static MenuType minecraftToBukkit(net.minecraft.world.inventory.MenuType<?> minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.MENU, Registry.MENU);
    }

    public static MenuType minecraftHolderToBukkit(Holder<net.minecraft.world.inventory.MenuType<?>> minecraft) {
        return minecraftToBukkit(minecraft.value());
    }
}
