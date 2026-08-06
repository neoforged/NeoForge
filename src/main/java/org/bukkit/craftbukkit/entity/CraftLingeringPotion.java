package org.bukkit.craftbukkit.entity;

import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.LingeringPotion;

public class CraftLingeringPotion extends CraftThrownPotion implements LingeringPotion {

    public CraftLingeringPotion(CraftServer server, AbstractThrownPotion entity) {
        super(server, entity);
    }
}
