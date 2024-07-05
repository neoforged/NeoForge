/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Parent of the two crop growth events.
 * 
 * @see CropGrowEvent.Pre
 * @see CropGrowEvent.Post
 */
public abstract class CropGrowEvent extends BlockEvent {
    public CropGrowEvent(Level level, BlockPos pos, BlockState state) {
        super(level, pos, state);
    }

    /**
     * Fired when any "growing age" blocks (for example cacti, chorus plants, or crops
     * in vanilla) attempt to advance to the next growth age state during a random tick.
     * <p>
     * This event is only fired on the logical server.
     */
    public static class Pre extends CropGrowEvent {
        private Result result = Result.DEFAULT;

        public Pre(Level level, BlockPos pos, BlockState state) {
            super(level, pos, state);
        }

        /**
         * Changes the result of this event.
         * 
         * @see {@link Result} for the possible states.
         */
        public void setResult(Result result) {
            this.result = result;
        }

        /**
         * {@return the result of this event, which controls if the click will be treated as handled}
         */
        public Result getResult() {
            return this.result;
        }

        public static enum Result {
            /**
             * Forces the event to make the crop grow.
             * <p>
             * {@link CropGrowEvent.Post} will be fired after the growth.
             */
            GROW,

            /**
             * The crop will use its own checks to determine if it should grow.
             * <p>
             * {@link CropGrowEvent.Post} will be fired after the growth.
             */
            DEFAULT,

            /**
             * Forces the event to prevent the crop from growing.
             */
            DO_NOT_GROW;
        }
    }

    /**
     * Fired when "growing age" blocks (for example cacti, chorus plants, or crops
     * in vanilla) have successfully grown. The block's original state is available,
     * in addition to its new state.
     * <p>
     * This event is only fired on the logical server.
     */
    public static class Post extends CropGrowEvent {
        private final BlockState originalState;

        public Post(Level level, BlockPos pos, BlockState original, BlockState state) {
            super(level, pos, state);
            originalState = original;
        }

        /**
         * {@return the original state of the crop before growing}
         */
        public BlockState getOriginalState() {
            return originalState;
        }

        /**
         * {@return the new state of the crop after growing}
         */
        @Override
        public BlockState getState() {
            return super.getState();
        }
    }
}
