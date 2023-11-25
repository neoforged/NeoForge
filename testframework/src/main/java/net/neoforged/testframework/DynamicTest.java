package net.neoforged.testframework;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

/**
 * A special type of {@linkplain Test test} which may only be linked to one {@linkplain TestFramework framework} at a time. <br>
 * This type of test can have enabled/disabled listeners added dynamically, and is as such, used primarily by method-based tests.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface DynamicTest extends Test {
    /**
     * {@return the framework this test is linked to}
     */
    TestFramework framework();

    /**
     * {@return the status of this test}
     */
    default Status status() {
        return framework().tests().getStatus(id());
    }

    /**
     * Updates the status of the test.
     *
     * @param newStatus the new status
     * @param updater   the entity which updated the status
     */
    default void updateStatus(Status newStatus, @Nullable Entity updater) {
        framework().changeStatus(this, newStatus, updater);
    }

    /**
     * Marks this test as {@linkplain Result#PASSED passed}.
     */
    default void pass() {
        updateStatus(new Status(Result.PASSED, ""), null);
    }

    /**
     * Marks this test as {@linkplain Result#FAILED failed}.
     *
     * @param message additional information explaining why the test failed
     */
    default void fail(String message) {
        updateStatus(new Status(Result.FAILED, message), null);
    }

    /**
     * Registers a listener to run when this test is enabled.
     *
     * @param whenEnabled the listener
     */
    void whenEnabled(final EnabledListener whenEnabled);

    /**
     * {@return a listener group used to subscribe to events when the test is enabled}
     *
     * @see DynamicTest#whenEnabled(EnabledListener)
     */
    EventListenerGroup eventListeners();

    /**
     * Registers a listener to run when this test is disabled.
     *
     * @param whenDisabled the listener
     */
    void whenDisabled(final Runnable whenDisabled);

    /**
     * Registers a listener to run when the GameTest version of this test is run.
     *
     * @param consumer the listener
     */
    void onGameTest(final Consumer<GameTestHelper> consumer);

    /**
     * {@return if this test is currently running as a GameTest}
     */
    boolean isDuringGameTest();

    @FunctionalInterface
    interface EnabledListener {
        void onEnabled(final EventListenerGroup listeners);
    }
}
