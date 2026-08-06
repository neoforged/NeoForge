package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.world.effect.MobEffects;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;

public class CraftVillagerZombie extends CraftZombie implements ZombieVillager {

    public CraftVillagerZombie(CraftServer server, net.minecraft.world.entity.monster.zombie.ZombieVillager entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.monster.zombie.ZombieVillager getHandle() {
        return (net.minecraft.world.entity.monster.zombie.ZombieVillager) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftVillagerZombie";
    }

    @Override
    public Villager.Profession getVillagerProfession() {
        return CraftVillager.CraftProfession.minecraftHolderToBukkit(getHandle().getVillagerData().profession());
    }

    @Override
    public void setVillagerProfession(Villager.Profession profession) {
        Preconditions.checkArgument(profession != null, "Villager.Profession cannot be null");
        getHandle().setVillagerData(getHandle().getVillagerData().withProfession(CraftVillager.CraftProfession.bukkitToMinecraftHolder(profession)));
    }

    @Override
    public Villager.Type getVillagerType() {
        return CraftVillager.CraftType.minecraftHolderToBukkit(getHandle().getVillagerData().type());
    }

    @Override
    public void setVillagerType(Villager.Type type) {
        Preconditions.checkArgument(type != null, "Villager.Type cannot be null");
        getHandle().setVillagerData(getHandle().getVillagerData().withType(CraftVillager.CraftType.bukkitToMinecraftHolder(type)));
    }

    @Override
    public boolean isConverting() {
        return getHandle().isConverting();
    }

    @Override
    public int getConversionTime() {
        Preconditions.checkState(isConverting(), "Entity not converting");

        return getHandle().villagerConversionTime;
    }

    @Override
    public void setConversionTime(int time) {
        if (time < 0) {
            getHandle().villagerConversionTime = -1;
            getHandle().getEntityData().set(net.minecraft.world.entity.monster.zombie.ZombieVillager.DATA_CONVERTING_ID, false);
            getHandle().conversionStarter = null;
            getHandle().removeEffect(MobEffects.STRENGTH, org.bukkit.event.entity.EntityPotionEffectEvent.Cause.CONVERSION);
        } else {
            getHandle().startConverting(null, time);
        }
    }

    @Override
    public OfflinePlayer getConversionPlayer() {
        return (getHandle().conversionStarter == null) ? null : Bukkit.getOfflinePlayer(getHandle().conversionStarter);
    }

    @Override
    public void setConversionPlayer(OfflinePlayer conversionPlayer) {
        if (!this.isConverting()) return;
        getHandle().conversionStarter = (conversionPlayer == null) ? null : conversionPlayer.getUniqueId();
    }
}
