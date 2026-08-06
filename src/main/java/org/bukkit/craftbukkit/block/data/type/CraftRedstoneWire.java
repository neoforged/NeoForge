package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftRedstoneWire extends CraftBlockData implements RedstoneWire {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Connection> NORTH = getEnum("north", Connection.class);
    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Connection> EAST = getEnum("east", Connection.class);
    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Connection> SOUTH = getEnum("south", Connection.class);
    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Connection> WEST = getEnum("west", Connection.class);

    @Override
    public Connection getFace(org.bukkit.block.BlockFace face) {
        switch (face) {
            case NORTH:
                return get(NORTH);
            case EAST:
                return get(EAST);
            case SOUTH:
                return get(SOUTH);
            case WEST:
                return get(WEST);
            default:
                throw new IllegalArgumentException("Cannot have face " + face);
        }
    }

    @Override
    public void setFace(org.bukkit.block.BlockFace face, Connection connection) {
        switch (face) {
            case NORTH:
                set(NORTH, connection);
                break;
            case EAST:
                set(EAST, connection);
                break;
            case SOUTH:
                set(SOUTH, connection);
                break;
            case WEST:
                set(WEST, connection);
                break;
            default:
                throw new IllegalArgumentException("Cannot have face " + face);
        }
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getAllowedFaces() {
        return com.google.common.collect.ImmutableSet.of(org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.WEST);
    }
}
