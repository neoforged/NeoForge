package net.feltmc.neoforge.patches.mixin;

import net.feltmc.neoforge.patches.interfaces.PackRepositoryInterface;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(PackRepository.class)
public class PackRepositoryMixin implements PackRepositoryInterface {
    @Shadow @Final private Set<RepositorySource> sources;

    @Override
    public synchronized void addPackFinder(RepositorySource packFinder) {
        this.sources.add(packFinder);
    }
}
