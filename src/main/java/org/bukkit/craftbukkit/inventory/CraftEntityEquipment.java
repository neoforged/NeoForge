package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class CraftEntityEquipment implements EntityEquipment {

    private final CraftLivingEntity entity;

    public CraftEntityEquipment(CraftLivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void setItem(org.bukkit.inventory.EquipmentSlot slot, ItemStack item) {
        this.setItem(slot, item, false);
    }

    @Override
    public void setItem(org.bukkit.inventory.EquipmentSlot slot, ItemStack item, boolean silent) {
        Preconditions.checkArgument(slot != null, "slot must not be null");
        EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
        setEquipment(nmsSlot, item, silent);
    }

    @Override
    public ItemStack getItem(org.bukkit.inventory.EquipmentSlot slot) {
        Preconditions.checkArgument(slot != null, "slot must not be null");
        EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
        return getEquipment(nmsSlot);
    }

    @Override
    public ItemStack getItemInMainHand() {
        return getEquipment(EquipmentSlot.MAINHAND);
    }

    @Override
    public void setItemInMainHand(ItemStack item) {
        this.setItemInMainHand(item, false);
    }

    @Override
    public void setItemInMainHand(ItemStack item, boolean silent) {
        setEquipment(EquipmentSlot.MAINHAND, item, silent);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return getEquipment(EquipmentSlot.OFFHAND);
    }

    @Override
    public void setItemInOffHand(ItemStack item) {
        this.setItemInOffHand(item, false);
    }

    @Override
    public void setItemInOffHand(ItemStack item, boolean silent) {
        setEquipment(EquipmentSlot.OFFHAND, item, silent);
    }

    @Override
    public ItemStack getItemInHand() {
        return getItemInMainHand();
    }

    @Override
    public void setItemInHand(ItemStack stack) {
        setItemInMainHand(stack);
    }

    @Override
    public ItemStack getHelmet() {
        return getEquipment(EquipmentSlot.HEAD);
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        this.setHelmet(helmet, false);
    }

    @Override
    public void setHelmet(ItemStack helmet, boolean silent) {
        setEquipment(EquipmentSlot.HEAD, helmet, silent);
    }

    @Override
    public ItemStack getChestplate() {
        return getEquipment(EquipmentSlot.CHEST);
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        this.setChestplate(chestplate, false);
    }

    @Override
    public void setChestplate(ItemStack chestplate, boolean silent) {
        setEquipment(EquipmentSlot.CHEST, chestplate, silent);
    }

    @Override
    public ItemStack getLeggings() {
        return getEquipment(EquipmentSlot.LEGS);
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        this.setLeggings(leggings, false);
    }

    @Override
    public void setLeggings(ItemStack leggings, boolean silent) {
        setEquipment(EquipmentSlot.LEGS, leggings, silent);
    }

    @Override
    public ItemStack getBoots() {
        return getEquipment(EquipmentSlot.FEET);
    }

    @Override
    public void setBoots(ItemStack boots) {
        this.setBoots(boots, false);
    }

    @Override
    public void setBoots(ItemStack boots, boolean silent) {
        setEquipment(EquipmentSlot.FEET, boots, silent);
    }

    @Override
    public ItemStack[] getArmorContents() {
        ItemStack[] armor = new ItemStack[]{
                getEquipment(EquipmentSlot.FEET),
                getEquipment(EquipmentSlot.LEGS),
                getEquipment(EquipmentSlot.CHEST),
                getEquipment(EquipmentSlot.HEAD),
        };
        return armor;
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        setEquipment(EquipmentSlot.FEET, items.length >= 1 ? items[0] : null, false);
        setEquipment(EquipmentSlot.LEGS, items.length >= 2 ? items[1] : null, false);
        setEquipment(EquipmentSlot.CHEST, items.length >= 3 ? items[2] : null, false);
        setEquipment(EquipmentSlot.HEAD, items.length >= 4 ? items[3] : null, false);
    }

    private ItemStack getEquipment(EquipmentSlot slot) {
        return CraftItemStack.asBukkitCopy(entity.getHandle().getItemBySlot(slot));
    }

    private void setEquipment(EquipmentSlot slot, ItemStack stack, boolean silent) {
        entity.getHandle().setItemSlot(slot, CraftItemStack.asNMSCopy(stack), silent);
    }

    @Override
    public void clear() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setEquipment(slot, null, false);
        }
    }

    @Override
    public Entity getHolder() {
        return entity;
    }

    @Override
    public float getItemInHandDropChance() {
        return getItemInMainHandDropChance();
    }

    @Override
    public void setItemInHandDropChance(float chance) {
        setItemInMainHandDropChance(chance);
    }

    @Override
    public float getItemInMainHandDropChance() {
       return getDropChance(EquipmentSlot.MAINHAND);
    }

    @Override
    public void setItemInMainHandDropChance(float chance) {
        setDropChance(EquipmentSlot.MAINHAND, chance);
    }

    @Override
    public float getItemInOffHandDropChance() {
        return getDropChance(EquipmentSlot.OFFHAND);
    }

    @Override
    public void setItemInOffHandDropChance(float chance) {
        setDropChance(EquipmentSlot.OFFHAND, chance);
    }

    @Override
    public float getHelmetDropChance() {
        return getDropChance(EquipmentSlot.HEAD);
    }

    @Override
    public void setHelmetDropChance(float chance) {
        setDropChance(EquipmentSlot.HEAD, chance);
    }

    @Override
    public float getChestplateDropChance() {
        return getDropChance(EquipmentSlot.CHEST);
    }

    @Override
    public void setChestplateDropChance(float chance) {
        setDropChance(EquipmentSlot.CHEST, chance);
    }

    @Override
    public float getLeggingsDropChance() {
        return getDropChance(EquipmentSlot.LEGS);
    }

    @Override
    public void setLeggingsDropChance(float chance) {
        setDropChance(EquipmentSlot.LEGS, chance);
    }

    @Override
    public float getBootsDropChance() {
        return getDropChance(EquipmentSlot.FEET);
    }

    @Override
    public void setBootsDropChance(float chance) {
        setDropChance(EquipmentSlot.FEET, chance);
    }

    private void setDropChance(EquipmentSlot slot, float chance) {
        Preconditions.checkArgument(entity.getHandle() instanceof Mob, "Cannot set drop chance for non-Mob entity");

        ((Mob) entity.getHandle()).setDropChance(slot, chance);
    }

    private float getDropChance(EquipmentSlot slot) {
        if (!(entity.getHandle() instanceof Mob)) {
            return 1;
        }

        return ((Mob) entity.getHandle()).getDropChances().byEquipment(slot);
    }
}
