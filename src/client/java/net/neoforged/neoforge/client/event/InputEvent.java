/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.input.PreeditEvent;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Fired when an input is detected from the user's input devices.
 * See the various subclasses to listen for specific devices and inputs.
 *
 * @see InputEvent.MouseButton
 * @see MouseScrollingEvent
 * @see Key
 * @see Preedit
 * @see InteractionKeyMappingTriggered
 */
public abstract class InputEvent extends Event {
    @ApiStatus.Internal
    protected InputEvent() {}

    /**
     * Fired when a mouse button is pressed/released. Sub-events get fired {@link Pre before} and {@link Post after} this happens.
     *
     * <p>These events are fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @see <a href="https://www.glfw.org/docs/latest/input_guide.html#input_mouse_button" target="_top">the online GLFW documentation</a>
     * @see Pre
     * @see Post
     */
    public static abstract class MouseButton extends InputEvent {
        private final MouseButtonInfo mouseButtonInfo;
        private final int action;

        @ApiStatus.Internal
        protected MouseButton(MouseButtonInfo mouseButtonInfo, int action) {
            this.mouseButtonInfo = mouseButtonInfo;
            this.action = action;
        }

        public MouseButtonInfo getMouseButtonInfo() {
            return mouseButtonInfo;
        }

        /**
         * {@return the mouse button's input code}
         *
         * @see GLFW mouse constants starting with 'GLFW_MOUSE_BUTTON_'
         * @see <a href="https://www.glfw.org/docs/latest/group__buttons.html" target="_top">the online GLFW documentation</a>
         */
        public int getButton() {
            return this.mouseButtonInfo.button();
        }

        /**
         * {@return the mouse button's action}
         *
         * @see InputConstants#PRESS
         * @see InputConstants#RELEASE
         */
        public int getAction() {
            return this.action;
        }

        /**
         * {@return a bit field representing the active modifier keys}
         *
         * @see InputConstants#MOD_CONTROL CTRL modifier key bit
         * @see GLFW#GLFW_MOD_SHIFT SHIFT modifier key bit
         * @see GLFW#GLFW_MOD_ALT ALT modifier key bit
         * @see GLFW#GLFW_MOD_SUPER SUPER modifier key bit
         * @see GLFW#GLFW_KEY_CAPS_LOCK CAPS LOCK modifier key bit
         * @see GLFW#GLFW_KEY_NUM_LOCK NUM LOCK modifier key bit
         * @see <a href="https://www.glfw.org/docs/latest/group__mods.html" target="_top">the online GLFW documentation</a>
         */
        public int getModifiers() {
            return this.mouseButtonInfo.modifiers();
        }

        /**
         * Fired when a mouse button is pressed/released, <b>before</b> being processed by vanilla.
         *
         * <p>This event is {@linkplain ICancellableEvent cancellable}.
         * If the event is cancelled, then the mouse event will not be processed by vanilla (e.g. keymappings and screens) </p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
         * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
         *
         * @see <a href="https://www.glfw.org/docs/latest/input_guide.html#input_mouse_button" target="_top">the online GLFW documentation</a>
         */
        public static class Pre extends MouseButton implements ICancellableEvent {
            @ApiStatus.Internal
            public Pre(MouseButtonInfo mouseButtonInfo, int action) {
                super(mouseButtonInfo, action);
            }
        }

        /**
         * Fired when a mouse button is pressed/released, <b>after</b> processing.
         *
         * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
         * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
         *
         * @see <a href="https://www.glfw.org/docs/latest/input_guide.html#input_mouse_button" target="_top">the online GLFW documentation</a>
         */
        public static class Post extends MouseButton {
            @ApiStatus.Internal
            public Post(MouseButtonInfo mouseButtonInfo, int action) {
                super(mouseButtonInfo, action);
            }
        }
    }

    /**
     * Fired when a mouse scroll wheel is used outside of a screen and a player is loaded, <b>before</b> being
     * processed by vanilla.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}.
     * If the event is cancelled, then the mouse scroll event will not be processed further.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @see <a href="https://www.glfw.org/docs/latest/input_guide.html#input_mouse_button" target="_top">the online GLFW documentation</a>
     */
    public static class MouseScrollingEvent extends InputEvent implements ICancellableEvent {
        private final double scrollDeltaX;
        private final double scrollDeltaY;
        private final int accumulatedScrollX;
        private final int accumulatedScrollY;
        private final double mouseX;
        private final double mouseY;
        private final boolean leftDown;
        private final boolean middleDown;
        private final boolean rightDown;

        @ApiStatus.Internal
        public MouseScrollingEvent(double scrollDeltaX, double scrollDeltaY, Vector2ic accumulatedScroll, boolean leftDown, boolean middleDown, boolean rightDown, double mouseX, double mouseY) {
            this.scrollDeltaX = scrollDeltaX;
            this.scrollDeltaY = scrollDeltaY;
            this.accumulatedScrollX = accumulatedScroll.x();
            this.accumulatedScrollY = accumulatedScroll.y();
            this.leftDown = leftDown;
            this.middleDown = middleDown;
            this.rightDown = rightDown;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        /**
         * {@return the amount of change / delta of the mouse scroll on the X axis}
         */
        public double getScrollDeltaX() {
            return this.scrollDeltaX;
        }

        /**
         * {@return the amount of change / delta of the mouse scroll on the Y axis}
         */
        public double getScrollDeltaY() {
            return this.scrollDeltaY;
        }

        /// Returns the integral horizontal scroll amount accumulated since the last scroll action
        /// that resulted in a non-zero integral scroll amount.
        ///
        /// @return the accumulated integral horizontal scroll amount
        public int getAccumulatedScrollX() {
            return accumulatedScrollX;
        }

        /// Returns the integral vertical scroll amount accumulated since the last scroll action
        /// that resulted in a non-zero integral scroll amount.
        ///
        /// @return the accumulated integral vertical scroll amount
        public int getAccumulatedScrollY() {
            return accumulatedScrollY;
        }

        /**
         * {@return {@code true} if the left mouse button is pressed}
         */
        public boolean isLeftDown() {
            return this.leftDown;
        }

        /**
         * {@return {@code true} if the right mouse button is pressed}
         */
        public boolean isRightDown() {
            return this.rightDown;
        }

        /**
         * {@return {@code true} if the middle mouse button is pressed}
         */
        public boolean isMiddleDown() {
            return this.middleDown;
        }

        /**
         * {@return the X position of the mouse cursor}
         */
        public double getMouseX() {
            return this.mouseX;
        }

        /**
         * {@return the Y position of the mouse cursor}
         */
        public double getMouseY() {
            return this.mouseY;
        }
    }

    /**
     * Fired when a keyboard key input occurs, such as pressing, releasing, or repeating a key.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class Key extends InputEvent {
        private final KeyEvent keyEvent;
        private final int action;

        @ApiStatus.Internal
        public Key(KeyEvent keyEvent, int action) {
            this.keyEvent = keyEvent;
            this.action = action;
        }

        public KeyEvent getKeyEvent() {
            return keyEvent;
        }

        /**
         * {@return the {@code GLFW} (platform-agnostic) key code}
         *
         * @see InputConstants input constants starting with {@code KEY_}
         * @see GLFW key constants starting with {@code GLFW_KEY_}
         * @see <a href="https://www.glfw.org/docs/latest/group__keys.html" target="_top">the online GLFW documentation</a>
         */
        public int getKey() {
            return this.keyEvent.key();
        }

        /**
         * {@return the platform-specific scan code}
         * <p>
         * The scan code is unique for every key, regardless of whether it has a key code.
         * Scan codes are platform-specific but consistent over time, so keys will have different scan codes depending
         * on the platform but they are safe to save to disk as custom key bindings.
         *
         * @see InputConstants#getKey(KeyEvent)
         */
        public int getScanCode() {
            return this.keyEvent.scancode();
        }

        /**
         * {@return the mouse button's action}
         *
         * @see InputConstants#PRESS
         * @see InputConstants#RELEASE
         * @see InputConstants#REPEAT
         */
        public int getAction() {
            return this.action;
        }

        /**
         * {@return a bit field representing the active modifier keys}
         *
         * @see InputConstants#MOD_CONTROL CTRL modifier key bit
         * @see GLFW#GLFW_MOD_SHIFT SHIFT modifier key bit
         * @see GLFW#GLFW_MOD_ALT ALT modifier key bit
         * @see GLFW#GLFW_MOD_SUPER SUPER modifier key bit
         * @see GLFW#GLFW_KEY_CAPS_LOCK CAPS LOCK modifier key bit
         * @see GLFW#GLFW_KEY_NUM_LOCK NUM LOCK modifier key bit
         * @see <a href="https://www.glfw.org/docs/latest/group__mods.html" target="_top">the online GLFW documentation</a>
         */
        public int getModifiers() {
            return this.keyEvent.modifiers();
        }
    }

    /**
     * Fired when an {@linkplain PreeditEvent Input Method Editor (IME) preedit event} is submitted to a GUI input target.
     *
     * <p>An IME lets users enter text that is not directly available from their keyboard, such as Chinese, Japanese,
     * and Korean characters. While input is being composed, the IME supplies temporary preedit text that can change
     * until the user commits it.</p>
     *
     * See the two subclasses for listening before and after the normal handling.
     *
     * @see Preedit.Pre
     * @see Preedit.Post
     */
    public static abstract class Preedit extends InputEvent {
        private final GuiEventListener inputTarget;
        private final @Nullable PreeditEvent preeditEvent;

        @ApiStatus.Internal
        public Preedit(GuiEventListener inputTarget, @Nullable PreeditEvent preeditEvent) {
            this.inputTarget = inputTarget;
            this.preeditEvent = preeditEvent;
        }

        /**
         * {@return the GUI input target that will receive the preedit event}
         */
        public GuiEventListener getInputTarget() {
            return inputTarget;
        }

        /**
         * {@return the current preedit composition, or {@code null} if there is no active composition}
         */
        public @Nullable PreeditEvent getPreeditEvent() {
            return preeditEvent;
        }

        /**
         * Fired <b>before</b> the preedit event is handled by the input target.
         *
         * <p>This event is {@linkplain ICancellableEvent cancellable}.
         * If the event is cancelled, the input target's preedit handler will be bypassed
         * and the corresponding {@link Preedit.Post} will not be fired.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
         * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
         */
        public static class Pre extends Preedit implements ICancellableEvent {
            @ApiStatus.Internal
            public Pre(GuiEventListener inputTarget, @Nullable PreeditEvent preeditEvent) {
                super(inputTarget, preeditEvent);
            }
        }

        /**
         * Fired <b>after</b> the preedit event is handled, if not handled by the input target
         * and the corresponding {@link Preedit.Pre} is not cancelled.
         *
         * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
         * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
         */
        public static class Post extends Preedit {
            @ApiStatus.Internal
            public Post(GuiEventListener inputTarget, @Nullable PreeditEvent preeditEvent) {
                super(inputTarget, preeditEvent);
            }
        }
    }

    /**
     * Fired when a keymapping that by default involves clicking the mouse buttons is triggered.
     *
     * <p>The key bindings that trigger this event are:</p>
     * <ul>
     * <li><b>Use Item</b> - defaults to <em>left mouse click</em></li>
     * <li><b>Pick Block</b> - defaults to <em>middle mouse click</em></li>
     * <li><b>Attack</b> - defaults to <em>right mouse click</em></li>
     * </ul>
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}.
     * If this event is cancelled, then the keymapping's action is not processed further, and the hand will be swung
     * according to {@link #shouldSwingHand()}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class InteractionKeyMappingTriggered extends InputEvent implements ICancellableEvent {
        private final int button;
        private final KeyMapping keyMapping;
        private final InteractionHand hand;
        private boolean handSwing = true;

        @ApiStatus.Internal
        public InteractionKeyMappingTriggered(int button, KeyMapping keyMapping, InteractionHand hand) {
            this.button = button;
            this.keyMapping = keyMapping;
            this.hand = hand;
        }

        /**
         * Sets whether to swing the hand. This takes effect whether or not the event is cancelled.
         *
         * @param value whether to swing the hand
         */
        public void setSwingHand(boolean value) {
            handSwing = value;
        }

        /**
         * {@return whether to swing the hand; always takes effect, regardless of cancellation}
         */
        public boolean shouldSwingHand() {
            return handSwing;
        }

        /**
         * {@return the hand that caused the input}
         * <p>
         * The event will be called for both hands if this is a use item input regardless
         * of both event's cancellation.
         * Will always be {@link InteractionHand#MAIN_HAND} if this is an attack or pick block input.
         */
        public InteractionHand getHand() {
            return hand;
        }

        /**
         * {@return {@code true} if the mouse button is the left mouse button}
         */
        public boolean isAttack() {
            return button == 0;
        }

        /**
         * {@return {@code true} if the mouse button is the right mouse button}
         */
        public boolean isUseItem() {
            return button == 1;
        }

        /**
         * {@return {@code true} if the mouse button is the middle mouse button}
         */
        public boolean isPickBlock() {
            return button == 2;
        }

        /**
         * {@return the key mapping which triggered this event}
         */
        public KeyMapping getKeyMapping() {
            return keyMapping;
        }
    }
}
