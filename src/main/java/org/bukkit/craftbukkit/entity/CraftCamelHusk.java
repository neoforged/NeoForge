package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.CamelHusk;

public class CraftCamelHusk extends CraftCamel implements CamelHusk {

    public CraftCamelHusk(CraftServer server, net.minecraft.world.entity.animal.camel.CamelHusk entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.camel.CamelHusk getHandle() {
        return (net.minecraft.world.entity.animal.camel.CamelHusk) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftCamelHusk";
    }
}
