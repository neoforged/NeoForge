package net.neoforged.testframework;

import net.neoforged.testframework.gametest.GameTestData;
import net.neoforged.testframework.group.Groupable;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The base interface for tests in the TestFramework.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface Test extends Groupable {
    /**
     * {@return the ID of this test}
     */
    String id();

    /**
     * A list of the groups of this test. <br>
     * If this list is empty, the test will be only in the {@code ungrouped} group.
     *
     * @return the groups of this test
     */
    List<String> groups();

    /**
     * {@return if this test is enabled by default}
     */
    boolean enabledByDefault();

    /**
     * This method is called when this test is enabled.
     *
     * @param buses a collector for event listeners. Prefer using this listener instead of the casual
     *              {@link IEventBus#addListener(Consumer)} or {@link IEventBus#register(Object)},
     *              as the collector will automatically unregister listeners when the test is disabled
     */
    void onEnabled(EventListenerGroup buses);

    /**
     * This method is called when this test is disabled.
     */
    void onDisabled();

    /**
     * This method is called when the test is registered to a {@link TestFramework}.
     *
     * @param framework the framework the test has been registered to
     */
    void init(TestFramework framework);

    /**
     * {@return the visual information about the test}
     */
    Visuals visuals();

    /**
     * {@inheritDoc}
     */
    @Override
    default Stream<Test> resolveAsStream() {
        return Stream.of(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default List<Test> resolveAll() {
        return List.of(this);
    }

    /**
     * {@return the game test version of this test}
     */
    @Nullable
    default GameTestData asGameTest() {
        return null;
    }

    /**
     * {@return the listeners of this test}
     */
    default Collection<TestListener> listeners() {
        return List.of();
    }

    /**
     * A group of collectors by {@link Mod.EventBusSubscriber.Bus bus}.
     */
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    interface EventListenerGroup {
        /**
         * Gets the collector for a bus.
         *
         * @param bus the bus to get the collector for
         * @return the collector associated with the bus
         */
        EventListenerCollector getFor(Mod.EventBusSubscriber.Bus bus);

        default EventListenerCollector mod() {
            return getFor(Mod.EventBusSubscriber.Bus.MOD);
        }

        default EventListenerCollector forge() {
            return getFor(Mod.EventBusSubscriber.Bus.FORGE);
        }

        /**
         * A collector of event listeners which automatically unregisters listeners when a test is disabled.
         */
        @ParametersAreNonnullByDefault
        @MethodsReturnNonnullByDefault
        interface EventListenerCollector {
            /**
             * Register an instance object or a {@linkplain Class}, and add listeners for all {@link SubscribeEvent} annotated methods
             * found there. <br>
             * <p>
             * Depending on what is passed as an argument, different listener creation behaviour is performed.
             *
             * <dl>
             *     <dt>Object Instance</dt>
             *     <dd>Scanned for <em>non-static</em> methods annotated with {@link SubscribeEvent} and creates listeners for
             *     each method found.</dd>
             *     <dt>Class Instance</dt>
             *     <dd>Scanned for <em>static</em> methods annotated with {@link SubscribeEvent} and creates listeners for
             *     each method found.</dd>
             * </dl>
             *
             * @param object either a {@link Class} instance or an arbitrary object, for scanning and event listener creation
             */
            void register(Object object);

            /**
             * Unregisters all the listeners added through this collector.
             *
             * @param bus the bus to unregister from
             */
            void unregisterAll(IEventBus bus);

            /**
             * Registers all the listeners added through this collector.
             *
             * @param bus the bus to register
             */
            void registerAll(IEventBus bus);

            /**
             * Add a consumer listener with the specified {@link EventPriority} and potentially cancelled events. <br>
             * Use this method when one of the other methods fails to determine the concrete {@link Event} subclass that is
             * intended to be subscribed to.
             *
             * @param priority         the priority of the listener
             * @param receiveCancelled indicate if this listener should receive events that have been {@link net.neoforged.bus.api.ICancellableEvent} cancelled
             * @param eventType        the concrete {@link Event} subclass to subscribe to
             * @param consumer         callback to invoke when a matching event is received
             * @param <T>              the {@link Event} subclass to listen for
             */
            <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Class<T> eventType, Consumer<T> consumer);

            /**
             * Add a consumer listener with the specified {@link EventPriority} and potentially cancelled events.
             *
             * @param priority         the priority of the listener
             * @param receiveCancelled indicate if this listener should receive events that have been {@link net.neoforged.bus.api.ICancellableEvent} cancelled
             * @param consumer         callback to invoke when a matching event is received
             * @param <T>              the {@link Event} subclass to listen for
             */
            <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> consumer);

            /**
             * Add a consumer listener with default {@link EventPriority#NORMAL} and not receiving cancelled events.
             *
             * @param consumer callback to invoke when a matching event is received
             * @param <T>      the {@link Event} subclass to listen for
             */
            default <T extends Event> void addListener(Consumer<T> consumer) {
                addListener(EventPriority.NORMAL, false, consumer);
            }
        }
    }

    /**
     * Represents the status of a test.
     *
     * @param result  the result
     * @param message the message, providing additional context if the test failed
     */
    record Status(Result result, String message) {
        public static final Status DEFAULT = new Status(Result.NOT_PROCESSED, "");

        @Override
        public String toString() {
            if (message.isBlank()) {
                return "[result=" + result + "]";
            } else {
                return "[result=" + result + ",message=" + message + "]";
            }
        }
    }

    enum Result {
        PASSED(0x90ee90, "Passed"),
        FAILED(0xFfcccb, "Failed"),
        NOT_PROCESSED(0xA6A39E, "Not Processed");

        private final int colour;
        private final String humanReadable;

        Result(int colour, String humanReadable) {
            this.colour = colour;
            this.humanReadable = humanReadable;
        }

        public int getColour() {
            return this.colour;
        }

        public boolean passed() {
            return this == PASSED;
        }

        public boolean failed() {
            return this == FAILED;
        }

        public String asHumanReadable() {
            return humanReadable;
        }
    }

    /**
     * Used by GUIs in order to display helpful information about tests.
     *
     * @param title       the human-readable title of the test
     * @param description the description of the test
     */
    record Visuals(Component title, List<Component> description) {
    }
}
