package net.neoforged.neoforge.debug.resources;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.VanillaReloadListeners;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@ForEachTest(groups = "reloadlistener")
public class ReloadListenerTest {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test reload listener ordering")
    static void testListenerOrdering(final DynamicTest test, final RegistrationHelper reg) {
        AtomicReference<String> lastListener = new AtomicReference<>();
        AtomicBoolean tagEmpty = new AtomicBoolean();
        NeoForge.EVENT_BUS.addListener((final AddReloadListenerEvent event) -> {
            var aId = ResourceLocation.fromNamespaceAndPath(reg.modId(), "a");
            event.addListener(aId, new ContextAwareReloadListener() {
                @Override
                public CompletableFuture<Void> reload(PreparationBarrier p_10638_, ResourceManager p_10639_, ProfilerFiller p_10640_, ProfilerFiller p_10641_, Executor p_10642_, Executor p_10643_) {
                    lastListener.compareAndSet("b", "a");
                    return CompletableFuture.<Void>completedFuture(null).thenCompose(p_10638_::wait);
                }
            }, List.of(VanillaReloadListeners.TAGS), List.of());

            var regAccess = event.getRegistryAccess();
            event.addListener(aId.withPath("b"), new ContextAwareReloadListener() {
                @Override
                public CompletableFuture<Void> reload(PreparationBarrier p_10638_, ResourceManager p_10639_, ProfilerFiller p_10640_, ProfilerFiller p_10641_, Executor p_10642_, Executor p_10643_) {
                    lastListener.compareAndSet(null, "b");
                    return CompletableFuture.<Void>completedFuture(null).thenCompose(p_10638_::wait)
                            .thenAccept(arg -> {
                                tagEmpty.set(regAccess.registryOrThrow(Registries.ITEM).getTag(ItemTags.ACACIA_LOGS).isEmpty());
                            });
                }
            }, List.of(), List.of(aId, VanillaReloadListeners.TAGS));
        });

        test.onGameTest(helper -> {
            helper.assertValueEqual(lastListener.get(), "a", "listener ordering");
            helper.assertTrue(tagEmpty.get(), "tag empty");
        });
    }
}
