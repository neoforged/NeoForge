package net.neoforged.testframework;

import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.group.Group;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.testframework.impl.TestFrameworkInternal;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Optional;

/**
 * The backend of the testing framework.
 *
 * @see FrameworkConfiguration#create()
 * @see TestFrameworkInternal
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface TestFramework {
    /**
     * {@return the ID of this framework instance}
     */
    ResourceLocation id();

    /**
     * {@return this framework's logger}
     *
     * @apiNote use this logger for logging inside tests. It is monitored in its own separate file in {@code logs/tests/}.
     */
    Logger logger();

    /**
     * {@return the Tests instance of this framework}
     */
    Tests tests();

    /**
     * Changes the status of a test.
     *
     * @param test      the test whose status to change
     * @param newStatus the status to change to
     * @param changer   the entity that changed the status of the test. Usually the player completing a test
     */
    void changeStatus(Test test, Test.Status newStatus, @Nullable Entity changer);

    /**
     * Enables or disables a test.
     *
     * @param test    the test to enable/disable
     * @param enabled {@code true} if to enable, {@code false} if to disable
     * @param changer the entity that changed the status of the test. Usually the player which runs the enable command
     */
    void setEnabled(Test test, boolean enabled, @Nullable Entity changer);

    /**
     * {@return the mod event bus linked to this framework}
     *
     * @apiNote this is set in {@link TestFrameworkInternal#init(IEventBus, ModContainer)}
     */
    IEventBus modEventBus();

    /**
     * Interface used for accessing a framework's tests.
     */
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    interface Tests {
        /**
         * Queries a test by its {@linkplain Test#id() ID}.
         *
         * @param id the ID of the test to query
         * @return the test, if present
         */
        Optional<Test> byId(String id);

        /**
         * Gets or creates a test {@linkplain Group group}.
         *
         * @param id the ID of the group
         * @return the old group, if one existed, or the new one if a group with that ID did not exist before.
         */
        Group getOrCreateGroup(String id);

        /**
         * {@return all the registered test groups}
         */
        Collection<Group> allGroups();

        /**
         * Enables a test. <br> <br>
         * <strong>This method only updates the local test.</strong> <br>
         * It will <i>not</i> update clients or the server. <br>
         * Prefer using {@link TestFramework#setEnabled(Test, boolean, Entity)} instead.
         *
         * @param id the ID of the test to enable
         */
        void enable(String id);

        /**
         * Disables a test. <br> <br>
         * <strong>This method only updates the local test.</strong> <br>
         * It will <i>not</i> update clients or the server. <br>
         * Prefer using {@link TestFramework#setEnabled(Test, boolean, Entity)} instead.
         *
         * @param id the ID of the test to disable
         */
        void disable(String id);

        /**
         * Checks if a test is enabled.
         *
         * @param id the ID of the test to check
         * @return if the test is enabled
         */
        boolean isEnabled(String id);

        Test.Status getStatus(String testId);

        /**
         * Registers a test to the framework. <br>
         * It is recommended you register tests during mod loading, for proper indexing.
         *
         * @param test the test to register
         */
        void register(Test test);

        /**
         * {@return an unmodifiable view of all the tests registered this this instance}
         */
        @UnmodifiableView
        Collection<Test> all();

        /**
         * Adds a global test listener.
         *
         * @param listener the listener
         */
        void addListener(TestListener listener);
    }

}
