package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import net.minecraft.world.level.Explosion;
import org.bukkit.ExplosionResult;

public final class CraftExplosionResult {

    private CraftExplosionResult() {}

    public static ExplosionResult toBukkit(Explosion.BlockInteraction blockinteraction) {
        Preconditions.checkArgument(blockinteraction != null, "explosion effect cannot be null");

        switch (blockinteraction) {
            case KEEP:
                return ExplosionResult.KEEP;
            case DESTROY:
                return ExplosionResult.DESTROY;
            case DESTROY_WITH_DECAY:
                return ExplosionResult.DESTROY_WITH_DECAY;
            case TRIGGER_BLOCK:
                return ExplosionResult.TRIGGER_BLOCK;
            default:
                throw new IllegalArgumentException("There is no ExplosionResult which matches " + blockinteraction);
        }
    }
}
