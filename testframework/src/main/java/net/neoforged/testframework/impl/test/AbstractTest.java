package net.neoforged.testframework.impl.test;

import com.google.common.base.Suppliers;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.TestListener;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.annotation.WithListener;
import net.neoforged.testframework.gametest.GameTestData;
import net.neoforged.testframework.impl.EventListenerGroupImpl;
import net.neoforged.testframework.impl.HackyReflection;
import net.neoforged.testframework.impl.TestFrameworkImpl;
import net.neoforged.testframework.impl.TestFrameworkInternal;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AbstractTest implements Test {
    protected TestFramework framework;
    protected String id;
    protected final List<String> groups = new ArrayList<>();
    protected boolean enabledByDefault;
    protected Visuals visuals;
    @Nullable
    protected GameTestData gameTestData;
    protected final Set<TestListener> listeners = new HashSet<>();

    protected AbstractTest() {
        configureFrom(AnnotationHolder.clazz(getClass()));

        try {
            final Method onGameTestMethod = getClass().getDeclaredMethod("onGameTest", GameTestHelper.class);
            configureGameTest(onGameTestMethod.getAnnotation(GameTest.class));
        } catch (Exception ignored) {}
    }

    protected final void configureFrom(AnnotationHolder holder) {
        ForEachTest parent = holder.parent().get(ForEachTest.class);
        if (parent == null) parent = ForEachTest.DEFAULT;

        final TestHolder marker = holder.get(TestHolder.class);
        if (marker != null) {
            id = parent.idPrefix() + (marker.value().equals("compute") ? holder.getName() : marker.value());
            enabledByDefault = marker.enabledByDefault();
            visuals = new Visuals(
                    Component.literal(marker.title().isBlank() ? TestFrameworkImpl.capitaliseWords(id(), "_") : marker.title()),
                    Stream.of(marker.description()).<Component>map(Component::literal).toList()
            );
            groups.addAll(List.of(marker.groups()));
        }

        final WithListener withListener = holder.get(WithListener.class);
        if (withListener != null) {
            listeners.addAll(Stream.of(withListener.value()).map(TestListener::instantiate).toList());
        }

        if (parent.groups().length > 0) groups.addAll(List.of(parent.groups()));
        listeners.addAll(Stream.of(parent.listeners()).map(TestListener::instantiate).toList());
    }

    protected final void configureGameTest(@Nullable GameTest gameTest) {
        if (gameTest == null) return;
        this.gameTestData = new GameTestData(
                gameTest.batch().equals("defaultBatch") ? null : gameTest.batch(),
                gameTest.templateNamespace().isBlank() ? gameTest.template() : new ResourceLocation(gameTest.templateNamespace(), gameTest.template()).toString(),
                gameTest.required(), gameTest.attempts(), gameTest.requiredSuccesses(),
                this::onGameTest, gameTest.timeoutTicks(), gameTest.setupTicks(),
                StructureUtils.getRotationForRotationSteps(gameTest.rotationSteps())
        );
    }

    @Override
    public void init(TestFramework framework) {
        this.framework = framework;
    }

    @Override
    public void onDisabled() {}
    @Override
    public void onEnabled(EventListenerGroup buses) {}

    protected void onGameTest(GameTestHelper helper) {}

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean enabledByDefault() {
        return enabledByDefault;
    }

    @Override
    public Visuals visuals() {
        return visuals;
    }

    @Override
    public @Nullable GameTestData asGameTest() {
        return gameTestData;
    }

    @Override
    public Set<TestListener> listeners() {
        return listeners;
    }

    @Override
    public List<String> groups() {
        return groups;
    }

    public boolean isEnabled() {
        return framework.tests().isEnabled(id());
    }

    public void enable() {
        framework.setEnabled(this, true, null);
    }

    public Logger logger() {
        return framework.logger();
    }

    public Status status() {
        return framework.tests().getStatus(id());
    }

    public void updateStatus(Status status, @Nullable Entity changer) {
        framework.changeStatus(this, status, changer);
    }

    protected void fail(String message) {
        updateStatus(new Status(Result.FAILED, message), null);
    }

    protected void pass() {
        updateStatus(new Status(Result.PASSED, ""), null);
    }

    protected final void requestConfirmation(Player player, Component message) {
        if (framework instanceof TestFrameworkInternal internal) {
            player.sendSystemMessage(message.copy().append(" ").append(
                    Component.literal("Yes").withStyle(style ->
                            style.withColor(ChatFormatting.GREEN).withBold(true)
                                    .withClickEvent(internal.setStatusCommand(
                                            id(), Result.PASSED, ""
                                    ))
            ).append(" ").append(
                    Component.literal("No").withStyle(style ->
                            style.withColor(ChatFormatting.RED).withBold(true)
                                    .withClickEvent(internal.setStatusCommand(
                                            id(), Result.FAILED, player.getGameProfile().getName() + " denied seeing the effects of the test"
                                    ))
            ))));
        }
    }

    @ParametersAreNonnullByDefault
    public static abstract class Dynamic extends AbstractTest implements DynamicTest {
        @Override
        public TestFramework framework() {
            return framework;
        }

        private final List<EnabledListener> enabledListeners = new ArrayList<>();
        @Override
        public void whenEnabled(EnabledListener whenEnabled) {
            this.enabledListeners.add(whenEnabled);
        }

        private final Supplier<EventListenerGroup> eventListeners = Suppliers.memoize(() -> {
            final var col = new EventListenerGroupImpl();
            whenEnabled(l -> ((EventListenerGroupImpl) l).copyFrom(col));
            return col;
        });

        @Override
        public EventListenerGroup eventListeners() {
            return eventListeners.get();
        }

        private final List<Runnable> disabledListeners = new ArrayList<>();
        @Override
        public void whenDisabled(Runnable whenDisabled) {
            this.disabledListeners.add(whenDisabled);
        }

        @Override
        public void onEnabled(EventListenerGroup buses) {
            super.onEnabled(buses);
            enabledListeners.forEach(listener -> listener.onEnabled(buses));
        }

        @Override
        public void onDisabled() {
            super.onDisabled();
            disabledListeners.forEach(Runnable::run);
        }

        private final List<Consumer<GameTestHelper>> onGameTest = new ArrayList<>();
        @Override
        public void onGameTest(Consumer<GameTestHelper> consumer) {
            this.onGameTest.add(consumer);
        }

        private boolean isDuringGameTest;
        @Override
        protected void onGameTest(GameTestHelper helper) {
            isDuringGameTest = true;
            super.onGameTest(helper);
            HackyReflection.addListener(helper, new GameTestListener() {
                @Override
                public void testStructureLoaded(GameTestInfo pTestInfo) {}

                @Override
                public void testPassed(GameTestInfo pTestInfo) {
                    isDuringGameTest = false;
                }

                @Override
                public void testFailed(GameTestInfo pTestInfo) {
                    isDuringGameTest = false;
                }
            });
            this.onGameTest.forEach(test -> test.accept(helper));
        }

        @Override
        public boolean isDuringGameTest() {
            return isDuringGameTest;
        }

        @Override
        public void fail(String message) {
            DynamicTest.super.fail(message);
        }

        @Override
        public void pass() {
            DynamicTest.super.pass();
        }
    }

    protected interface AnnotationHolder {
        @Nullable <T extends Annotation> T get(Class<T> type);

        AnnotationHolder parent();

        static AnnotationHolder clazz(Class<?> clazz) {
            return new AnnotationHolder() {
                @Override
                public <T extends Annotation> @Nullable T get(Class<T> type) {
                    return clazz.getAnnotation(type);
                }

                @Override
                public AnnotationHolder parent() {
                    return clazz(HackyReflection.parentOrTopLevel(clazz));
                }

                @Override
                public String getName() {
                    return clazz.getName();
                }
            };
        }

        static AnnotationHolder method(Method method) {
            return new AnnotationHolder() {
                @Override
                public <T extends Annotation> @Nullable T get(Class<T> type) {
                    return method.getAnnotation(type);
                }

                @Override
                public AnnotationHolder parent() {
                    return clazz(method.getDeclaringClass());
                }

                @Override
                public String getName() {
                    return method.getName();
                }
            };
        }

        String getName();
    }
}
