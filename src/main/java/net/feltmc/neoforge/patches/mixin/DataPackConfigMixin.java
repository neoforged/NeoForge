package net.feltmc.neoforge.patches.mixin;

import net.feltmc.neoforge.patches.interfaces.DataPackConfigInterface;
import net.minecraft.world.level.DataPackConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(DataPackConfig.class)
public class DataPackConfigMixin implements DataPackConfigInterface {

    @Shadow @Final private List<String> enabled;

    @Override
    public void addModPacks(List<String> modPacks) {
        enabled.addAll(modPacks.stream().filter(p->!enabled.contains(p)).collect(java.util.stream.Collectors.toList()));
    }
}
