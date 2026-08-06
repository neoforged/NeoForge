package org.bukkit.craftbukkit.packs;

import net.minecraft.server.packs.metadata.pack.PackFormat;
import org.bukkit.packs.DataPackFormat;

public class CraftDataPackFormat implements DataPackFormat {

    private final PackFormat handle;

    public CraftDataPackFormat(PackFormat handle) {
        this.handle = handle;
    }

    @Override
    public int getMajor() {
        return handle.major();
    }

    @Override
    public int getMinor() {
        return handle.minor();
    }

    @Override
    public int compareTo(DataPackFormat o) {
        return handle.compareTo(((CraftDataPackFormat) o).handle);
    }
}
