package net.neoforged.neoforge.oldtest.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.GetEquipmentDropChanceEvent;

@Mod("equipment_drop_chance_test")
public class EquipmentDropChanceTest {
    public static final boolean ENABLE = true;

    public EquipmentDropChanceTest() {
        if (ENABLE) {
            NeoForge.EVENT_BUS.addListener(EquipmentDropChanceTest::getEquipmentDropChance);
        }
    }

    private static void getEquipmentDropChance(GetEquipmentDropChanceEvent event) {
        // make the equipment drop chance 100% when holding an anvil in the offhand
        if (event.getKillingBlow().getEntity() instanceof LivingEntity living && event.getChance() > 0 && living.getOffhandItem().is(Items.ANVIL)) {
            event.setChance(1);
        }
    }
}
