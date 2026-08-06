package org.bukkit.craftbukkit.block;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.ShulkerBox;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;

public class CraftShulkerBox extends CraftLootable<ShulkerBoxBlockEntity> implements ShulkerBox {

    public CraftShulkerBox(World world, ShulkerBoxBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftShulkerBox(CraftShulkerBox state, Location location) {
        super(state, location);
    }

    @Override
    public Inventory getSnapshotInventory() {
        return new CraftInventory(this.getSnapshot());
    }

    @Override
    public Inventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventory(this.getTileEntity());
    }

    @Override
    public DyeColor getColor() {
        net.minecraft.world.item.DyeColor color = ((ShulkerBoxBlock) CraftBlockType.bukkitToMinecraft(this.getType())).color;

        return (color == null) ? null : DyeColor.getByWoolData((byte) color.getId());
    }

    @Override
    public void open() {
        requirePlaced();
        if (!getTileEntity().opened && getWorldHandle() instanceof net.minecraft.world.level.Level) {
            net.minecraft.world.level.Level level = getTileEntity().getLevel();
            level.blockEvent(getPosition(), getTileEntity().getBlockState().getBlock(), 1, 1);
            level.playSound(null, getPosition(), SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }
        getTileEntity().opened = true;
    }

    @Override
    public void close() {
        requirePlaced();
        if (getTileEntity().opened && getWorldHandle() instanceof net.minecraft.world.level.Level) {
            net.minecraft.world.level.Level level = getTileEntity().getLevel();
            level.blockEvent(getPosition(), getTileEntity().getBlockState().getBlock(), 1, 0);
            level.playSound(null, getPosition(), SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }
        getTileEntity().opened = false;
    }

    @Override
    public CraftShulkerBox copy() {
        return new CraftShulkerBox(this, null);
    }

    @Override
    public CraftShulkerBox copy(Location location) {
        return new CraftShulkerBox(this, location);
    }
}
