package org.bukkit.craftbukkit.block;

import net.minecraft.world.level.block.SelectableSlotContainer;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Shelf;
import org.bukkit.block.data.Directional;
import org.bukkit.craftbukkit.inventory.CraftInventoryShelf;
import org.bukkit.inventory.ShelfInventory;
import org.bukkit.util.Vector;

public class CraftShelf extends CraftBlockEntityState<ShelfBlockEntity> implements Shelf {

    public CraftShelf(World world, ShelfBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftShelf(CraftShelf state, Location location) {
        super(state, location);
    }

    @Override
    public ShelfInventory getSnapshotInventory() {
        return new CraftInventoryShelf(this.getSnapshot());
    }

    @Override
    public ShelfInventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventoryShelf(this.getTileEntity());
    }

    @Override
    public int getSlot(Vector clickVector) {
        BlockFace facing = ((Directional) this.getBlockData()).getFacing();

        Vec2 faceVector;
        switch (facing) {
        case NORTH:
            faceVector = new Vec2((float) (1.0f - clickVector.getX()), (float) clickVector.getY());
            break;
        case SOUTH:
            faceVector = new Vec2((float) clickVector.getX(), (float) clickVector.getY());
            break;
        case WEST:
            faceVector = new Vec2((float) clickVector.getZ(), (float) clickVector.getY());
            break;
        case EAST:
            faceVector = new Vec2((float) (1f - clickVector.getZ()), (float) clickVector.getY());
            break;
        case DOWN:
        case UP:
        default:
            return -1;
        }

        return getHitSlot(faceVector);
    }

    private static int getHitSlot(Vec2 vec2) {
        int i = SelectableSlotContainer.getSection(1.0F - vec2.y, 1); // rows
        int j = SelectableSlotContainer.getSection(vec2.x, 3); // columns

        return j + i * 3;
    }

    @Override
    public CraftShelf copy() {
        return new CraftShelf(this, null);
    }

    @Override
    public CraftShelf copy(Location location) {
        return new CraftShelf(this, location);
    }
}
