package net.minecraftforge.debug.entity.living;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.VillagerEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod("villager_change_profession_event_test")
public class VillagerChangeProfessionEventTest {
    private static final Logger LOGGER = LogUtils.getLogger();

    public VillagerChangeProfessionEventTest() {
        MinecraftForge.EVENT_BUS.addListener(this::onEvent);
    }

    public void onEvent(VillagerEvent.ChangeProfessionEvent event) {
        LOGGER.info("Changed profession from {} to {}", event.getOldProfession(), event.getNewProfession());
    }
}
