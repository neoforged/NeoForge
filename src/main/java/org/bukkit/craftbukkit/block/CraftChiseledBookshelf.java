package org.bukkit.craftbukkit.block;

import net.minecraft.world.level.block.SelectableSlotContainer;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.block.data.Directional;
import org.bukkit.craftbukkit.inventory.CraftInventoryChiseledBookshelf;
import org.bukkit.inventory.ChiseledBookshelfInventory;
import org.bukkit.util.Vector;

public class CraftChiseledBookshelf extends CraftBlockEntityState<ChiseledBookShelfBlockEntity> implements ChiseledBookshelf {

    public CraftChiseledBookshelf(World world, ChiseledBookShelfBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftChiseledBookshelf(CraftChiseledBookshelf state, Location location) {
        super(state, location);
    }

    @Override
    public int getLastInteractedSlot() {
        return getSnapshot().getLastInteractedSlot();
    }

    @Override
    public void setLastInteractedSlot(int lastInteractedSlot) {
        getSnapshot().lastInteractedSlot = lastInteractedSlot;
    }

    @Override
    public ChiseledBookshelfInventory getSnapshotInventory() {
        return new CraftInventoryChiseledBookshelf(this.getSnapshot());
    }

    @Override
    public ChiseledBookshelfInventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventoryChiseledBookshelf(this.getTileEntity());
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
        int i = SelectableSlotContainer.getSection(1.0F - vec2.y, 2); // rows
        int j = SelectableSlotContainer.getSection(vec2.x, 3); // columns

        return j + i * 3;
    }

    @Override
    public CraftChiseledBookshelf copy() {
        return new CraftChiseledBookshelf(this, null);
    }

    @Override
    public CraftChiseledBookshelf copy(Location location) {
        return new CraftChiseledBookshelf(this, location);
    }
}
