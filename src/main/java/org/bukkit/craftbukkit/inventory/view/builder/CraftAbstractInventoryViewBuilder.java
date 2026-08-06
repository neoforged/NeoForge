package org.bukkit.craftbukkit.inventory.view.builder;

import com.google.common.base.Preconditions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.view.builder.InventoryViewBuilder;

public abstract class CraftAbstractInventoryViewBuilder<V extends InventoryView> implements InventoryViewBuilder<V> {

    protected final MenuType<?> handle;

    protected boolean checkReachable = false;
    protected String title = null;

    public CraftAbstractInventoryViewBuilder(MenuType<?> handle) {
        this.handle = handle;
    }

    @Override
    public InventoryViewBuilder<V> title(final String title) {
        this.title = title;
        return this;
    }

    @Override
    public V build(final HumanEntity player) {
        Preconditions.checkArgument(player != null, "The given player must not be null");
        Preconditions.checkArgument(this.title != null, "The given title must not be null");
        Preconditions.checkArgument(player instanceof CraftHumanEntity, "The given player must be a CraftHumanEntity");
        final CraftHumanEntity craftHuman = (CraftHumanEntity) player;
        Preconditions.checkArgument(craftHuman.getHandle() instanceof ServerPlayer, "The given player must be an EntityPlayer");
        final ServerPlayer serverPlayer = (ServerPlayer) craftHuman.getHandle();
        final AbstractContainerMenu abstractcontainermenu = buildContainer(serverPlayer);
        abstractcontainermenu.checkReachable = this.checkReachable;
        abstractcontainermenu.setTitle(CraftChatMessage.fromString(title)[0]);
        return (V) abstractcontainermenu.getBukkitView();
    }

    protected abstract AbstractContainerMenu buildContainer(ServerPlayer player);
}
