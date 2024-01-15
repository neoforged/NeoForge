/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import com.google.common.collect.Sets;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.TestListener;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.gametest.DynamicStructureTemplates;
import net.neoforged.testframework.group.Group;
import net.neoforged.testframework.impl.packet.ChangeEnabledPayload;
import net.neoforged.testframework.impl.packet.ChangeStatusPayload;
import net.neoforged.testframework.impl.packet.TestFrameworkPayloadInitialization;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiStatus.Internal
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TestFrameworkImpl implements MutableTestFramework {
    static final Set<TestFrameworkImpl> FRAMEWORKS = Collections.synchronizedSet(new HashSet<>());

    //@formatter:off
    private static final DateTimeFormatter LOG_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 2)
                .appendLiteral('-')
                .appendValue(YEAR, 4)
            .optionalStart()
                .appendLiteral('_')
                .parseLenient()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral('-')
                .appendValue(MINUTE_OF_HOUR, 2)
            .optionalEnd()
            .toFormatter()
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());
    //@formatter:on

    private final FrameworkConfiguration configuration;
    private final @Nullable FrameworkClient client;

    private final Logger logger;
    private final ResourceLocation id;
    private final TestsImpl tests = new TestsImpl();

    private @Nullable MinecraftServer server;
    private final DynamicStructureTemplates structures;
    private final SummaryDumper summaryDumper;

    private String commandName;

    public TestFrameworkImpl(FrameworkConfiguration configuration) {
        FRAMEWORKS.add(this);

        this.configuration = configuration;
        this.id = configuration.id();
        this.structures = new DynamicStructureTemplates();
        this.summaryDumper = new SummaryDumper(this);

        this.logger = LoggerFactory.getLogger("TestFramework " + this.id);
        new LoggerSetup(this).prepareLogger();

        if (FMLLoader.getDist().isClient() && configuration.clientConfiguration() != null) {
            this.client = FrameworkClient.factory().map(it -> it.create(this, configuration.clientConfiguration().get())).orElse(null);
        } else {
            this.client = null;
        }

        NeoForge.EVENT_BUS.addListener((final ServerStartedEvent event) -> {
            server = event.getServer();
            tests.initialiseDefaultEnabledTests();

            try {
                structures.setup(event.getServer().getStructureManager());
            } catch (Throwable exception) {
                throw new RuntimeException(exception);
            }
        });
        NeoForge.EVENT_BUS.addListener((final ServerStoppedEvent event) -> {
            server = event.getServer() == server ? null : server;

            if (!configuration().isEnabled(Feature.SUMMARY_DUMP)) return;

            // Summarise test results
            logger().info("Test summary processing...");
            logger().info("Test summary:\n{}", summaryDumper.createLoggingSummary());

            final Path dumpPath = Path.of("logs/tests/" + id().toString().replace(":", "_") + "/summary_" + LOG_FORMAT.format(ZonedDateTime.now()) + ".md");
            final String summary = """
                    # Test Summary

                    ## Disabled Tests
                    %s

                    ## Enabled Tests
                    %s

                    %s"""
                    .formatted(summaryDumper.createDisabledList(), summaryDumper.createEnabledList(), summaryDumper.dumpTable());
            LamdbaExceptionUtils.uncheck(() -> Files.createDirectories(dumpPath.getParent()));
            LamdbaExceptionUtils.uncheck(() -> Files.writeString(dumpPath, summary));
            logger().info("Dumped test summary to {}", dumpPath);
            logger().info("Test Framework finished.");
        });

        if (configuration().isEnabled(Feature.TEST_STORE)) {
            NeoForge.EVENT_BUS.addListener((final PlayerEvent.PlayerLoggedOutEvent event) -> playerTestStore().put(event.getEntity().getUUID(), tests.tests.keySet()));
            NeoForge.EVENT_BUS.addListener((final PlayerEvent.PlayerLoggedInEvent event) -> {
                final Set<String> lastTests = playerTestStore().getLast(event.getEntity().getUUID());
                if (lastTests == null) return;

                final Set<String> newTests = Sets.difference(tests.tests.keySet(), lastTests);
                if (newTests.isEmpty()) return;

                MutableComponent message = Component.literal("Welcome, ").append(event.getEntity().getName()).append("!")
                        .append("\nThis server has the test framework enabled, so here are some of the tests that were added in your absence:\n");
                final Iterator<Component> tests = newTests.stream()
                        .limit(20)
                        .flatMap(it -> tests().byId(it).stream())
                        .<Component>map(it -> Component.literal("- ")
                                .append(it.visuals().title()).append(" - ").append(tests().isEnabled(it.id()) ? Component.literal("disable")
                                        .withStyle(style -> style
                                                .withColor(ChatFormatting.RED).withBold(true)
                                                .withClickEvent(disableCommand(it.id())))
                                        : Component.literal("enable")
                                                .withStyle(style -> style
                                                        .withColor(ChatFormatting.GREEN).withBold(true)
                                                        .withClickEvent(enableCommand(it.id())))))
                        .iterator();
                while (tests.hasNext()) {
                    final Component current = tests.next();
                    message = message.append(current);
                    if (tests.hasNext()) {
                        message = message.append("\n");
                    }
                }
                event.getEntity().sendSystemMessage(message);
            });
        }
    }

    @Override
    public void registerCommands(LiteralArgumentBuilder<CommandSourceStack> node) {
        commandName = node.getLiteral();
        new Commands(this).register(node);
    }

    @Override
    public PlayerTestStore playerTestStore() {
        return server.overworld().getDataStorage()
                .computeIfAbsent(PlayerTestStore.FACTORY, "tests/" + id().getNamespace() + "_" + id().getPath());
    }

    @Override
    public DynamicStructureTemplates dynamicStructures() {
        return structures;
    }

    @Override
    public String commandName() {
        return commandName;
    }

    @Override
    public FrameworkConfiguration configuration() {
        return configuration;
    }

    private IEventBus modBus;
    private ModContainer container;

    @Override
    public void init(final IEventBus modBus, final ModContainer container) {
        this.container = container;
        final var byStage = FrameworkCollectors
                .onInitMethodsWithAnnotation(container);

        this.modBus = modBus;
        tests.buses = Map.of(Mod.EventBusSubscriber.Bus.FORGE, NeoForge.EVENT_BUS, Mod.EventBusSubscriber.Bus.MOD, modBus);

        byStage.get(OnInit.Stage.BEFORE_SETUP).forEach(cons -> cons.accept(this));

        final List<Test> collected = collectTests(container);
        logger.info("Found {} tests: {}", collected.size(), String.join(", ", collected.stream().map(Test::id).toList()));
        collected.forEach(tests()::register);

        FrameworkCollectors.groupsWithAnnotation(container, data -> {
            final Group group = tests().getOrCreateGroup(data.id());
            group.setTitle(data.title());
            group.setEnabledByDefault(data.isEnabledByDefault());
            for (final String parent : data.parents()) {
                tests().getOrCreateGroup(parent).add(group);
            }
        });

        modBus.addListener(new TestFrameworkPayloadInitialization(this)::onNetworkSetup);
        modBus.addListener((final RegisterGameTestsEvent event) -> event.register(GameTestRegistration.REGISTER_METHOD));

        if (FMLLoader.getDist().isClient()) {
            setupClient(this, modBus, container);
        }

        FrameworkCollectors.templatesWithAnnotation(container, structures::register);

        byStage.get(OnInit.Stage.AFTER_SETUP).forEach(cons -> cons.accept(this));
    }

    private static void setupClient(TestFrameworkImpl impl, IEventBus modBus, ModContainer container) {
        if (impl.client != null) impl.client.init(modBus, container);
        NeoForge.EVENT_BUS.addListener((final ClientPlayerNetworkEvent.LoggingIn logOut) -> {
            synchronized (impl.tests().enabled) {
                List.copyOf(impl.tests().enabled).forEach(impl.tests()::disable);
            }
            impl.tests().initialiseDefaultEnabledTests();
        });
    }

    @Override
    public List<Test> collectTests(final ModContainer container) {
        final List<Test> tests = new ArrayList<>();
        tests.addAll(FrameworkCollectors.Tests.eventTestMethodsWithAnnotation(container, TestHolder.class));
        tests.addAll(FrameworkCollectors.Tests.forMethodsWithAnnotation(container, TestHolder.class));
        tests.addAll(FrameworkCollectors.Tests.forGameTestMethodsWithAnnotation(container, TestHolder.class));
        tests.addAll(FrameworkCollectors.Tests.forClassesWithAnnotation(container, TestHolder.class));
        return tests;
    }

    @Override
    public IEventBus modEventBus() {
        return modBus;
    }

    @Override
    public ModContainer container() {
        return container;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public TestsImpl tests() {
        return tests;
    }

    @Override
    public void changeStatus(Test test, Test.Status newStatus, @Nullable Entity changer) {
        final Test.Status oldStatus = tests.getStatus(test.id());
        if (oldStatus.equals(newStatus)) return; // If the status is the same, don't waste power
        if (!tests.isEnabled(test.id())) return; // If the test is disabled, we don't care

        tests.setStatus(test.id(), newStatus);

        tests.globalListeners.forEach(listener -> listener.onStatusChange(this, test, oldStatus, newStatus, changer));
        test.listeners().forEach(listener -> listener.onStatusChange(this, test, oldStatus, newStatus, changer));

        logger.info("Status of test '{}' has had status changed to {}{}.", test.id(), newStatus, changer instanceof Player player ? " by " + player.getGameProfile().getName() : "");

        final ChangeStatusPayload packet = new ChangeStatusPayload(this, test.id(), newStatus);
        sendPacketIfOn(
                () -> PacketDistributor.ALL.noArg().send(packet),
                () -> PacketDistributor.SERVER.noArg().send(packet),
                null);
    }

    @Override
    public void setEnabled(Test test, boolean enabled, @Nullable Entity changer) {
        if (tests.isEnabled(test.id()) == enabled) return; // If the status is the same, don't waste power

        if (enabled) {
            tests.enable(test.id());
        } else {
            tests.disable(test.id());
        }

        logger.info("Test '{}' has been {}{}.", test.id(), enabled ? "enabled" : "disabled", changer instanceof Player player ? " by " + player.getGameProfile().getName() : "");

        if (enabled) {
            changeStatus(test, new Test.Status(Test.Result.NOT_PROCESSED, ""), changer);
        }

        final Consumer<TestListener> listenerConsumer = enabled ? listener -> listener.onEnabled(this, test, changer) : listener -> listener.onDisabled(this, test, changer);
        tests.globalListeners.forEach(listenerConsumer);
        test.listeners().forEach(listenerConsumer);

        final ChangeEnabledPayload packet = new ChangeEnabledPayload(TestFrameworkImpl.this, test.id(), enabled);
        sendPacketIfOn(
                () -> PacketDistributor.ALL.noArg().send(packet),
                () -> PacketDistributor.SERVER.noArg().send(packet),
                null);
    }

    @SuppressWarnings("SameParameterValue")
    private void sendPacketIfOn(@Nullable Runnable onServer, @Nullable Runnable remoteClient, @Nullable Runnable singlePlayer) {
        if (FMLLoader.getDist().isClient() && server != null) {
            if (singlePlayer != null) singlePlayer.run();
        } else if (FMLLoader.getDist().isClient()) {
            if (remoteClient != null && configuration.isEnabled(Feature.CLIENT_MODIFICATIONS)) remoteClient.run();
        } else if (FMLLoader.getDist().isDedicatedServer() && server != null) {
            if (onServer != null && configuration.isEnabled(Feature.CLIENT_SYNC)) onServer.run();
        }
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public final class TestsImpl implements MutableTests {
        private final Map<String, Test> tests = Collections.synchronizedMap(new LinkedHashMap<>());
        private final Map<String, Group> groups = Collections.synchronizedMap(new LinkedHashMap<>());
        private final Map<String, EventListenerGroupImpl> collectors = new HashMap<>();
        private final Set<String> enabled = Collections.synchronizedSet(new LinkedHashSet<>());
        private final Map<String, Test.Status> statuses = new ConcurrentHashMap<>();
        private Map<Mod.EventBusSubscriber.Bus, IEventBus> buses = Map.of();

        private final Set<TestListener> globalListeners = new HashSet<>();

        @Override
        public void addListener(TestListener listener) {
            globalListeners.add(listener);
        }

        @Override
        public Optional<Test> byId(String id) {
            return Optional.ofNullable(tests.get(id));
        }

        @Override
        public Group getOrCreateGroup(String id) {
            @Nullable
            Group group = groups.get(id);
            if (group != null) return group;
            group = addGroupToParents(new Group(id, new CopyOnWriteArrayList<>()));
            groups.put(id, group);
            return group;
        }

        @Override
        public Optional<Group> maybeGetGroup(String id) {
            return Optional.ofNullable(groups.get(id));
        }

        @Override
        public Collection<Group> allGroups() {
            return groups.values();
        }

        @Override
        public void enable(String id) {
            if (enabled.contains(id)) return;
            final EventListenerGroupImpl collector = collectors.computeIfAbsent(id, it -> new EventListenerGroupImpl());
            byId(id).ifPresent(test -> test.onEnabled(collector));
            collector.register(buses);
            enabled.add(id);
        }

        @Override
        public void disable(String id) {
            if (!enabled.contains(id)) return;
            byId(id).ifPresent(Test::onDisabled);
            Optional.ofNullable(collectors.get(id)).ifPresent(col -> col.unregister(buses));
            enabled.remove(id);
        }

        @Override
        public boolean isEnabled(String id) {
            return enabled.contains(id);
        }

        @Override
        public Test.Status getStatus(String testId) {
            return statuses.getOrDefault(testId, Test.Status.DEFAULT);
        }

        @Override
        public void setStatus(String testId, Test.Status status) {
            statuses.put(testId, status);
        }

        @Override
        public void register(Test test) {
            if (tests.containsKey(test.id())) {
                throw new UnsupportedOperationException("Duplicate test with ID: " + test.id());
            }

            tests.put(test.id(), test);
            if (test.groups().isEmpty()) {
                getOrCreateGroup("ungrouped").add(test);
            } else {
                test.groups().forEach(group -> getOrCreateGroup(group).add(test));
            }
            test.init(TestFrameworkImpl.this);
        }

        private Group addGroupToParents(Group group) {
            final List<String> splitOnDot = List.of(group.id().split("\\."));
            if (splitOnDot.size() >= 2) {
                final Group parent = getOrCreateGroup(String.join(".", splitOnDot.subList(0, splitOnDot.size() - 1)));
                parent.add(group);
            }
            return group;
        }

        private final Collection<Test> allView = Collections.unmodifiableCollection(tests.values());

        @Override
        public Collection<Test> all() {
            return allView;
        }

        @Override
        public Stream<Test> enabled() {
            return enabled.stream().flatMap(it -> byId(it).stream());
        }

        @Override
        public void initialiseDefaultEnabledTests() {
            enabled.clear();
            Predicate<Test> isEnabledByDefault = Test::enabledByDefault;

            final Set<String> enabledTests = new HashSet<>(configuration.enabledTests());
            final Set<Group> enabledGroups = new HashSet<>();
            final Iterator<String> etestsItr = enabledTests.iterator();
            while (etestsItr.hasNext()) {
                final String next = etestsItr.next();
                if (next.startsWith("g:")) {
                    enabledGroups.add(getOrCreateGroup(next.substring(2)));
                    etestsItr.remove();
                }
            }

            enabledGroups.addAll(groups.values().stream().filter(Group::isEnabledByDefault).toList());
            final Set<Test> groupTestsEnabledByDefault = enabledGroups.stream().flatMap(it -> it.resolveAll().stream()).collect(Collectors.toSet());

            isEnabledByDefault = isEnabledByDefault.or(it -> enabledTests.contains(it.id()));
            isEnabledByDefault = isEnabledByDefault.or(groupTestsEnabledByDefault::contains);

            for (final Test test : tests.values()) {
                if (isEnabledByDefault.test(test)) {
                    enable(test.id());
                }
                setStatus(test.id(), new Test.Status(Test.Result.NOT_PROCESSED, ""));
            }
        }
    }

    public static String capitaliseWords(String string, String splitOn) {
        return Stream.of(string.split(splitOn)).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

}
