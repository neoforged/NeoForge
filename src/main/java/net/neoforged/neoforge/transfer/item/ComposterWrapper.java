package net.neoforged.neoforge.transfer.item;

import com.google.common.collect.MapMaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotParticipant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ApiStatus.Internal
public class ComposterWrapper extends SnapshotParticipant<Float> {
    /**
     * To make sure multiple accesses to the same composter return the same wrapper,
     * we maintain a {@code (Level, BlockPos) -> Wrapper} cache.</li>
     */
    private record Location(Level level, BlockPos pos) {
        private BlockState getBlockState() {
            return level.getBlockState(pos);
        }
    }

    // Weak values to make sure wrappers are cleaned up after use, thread-safe.
    private static final Map<Location, ComposterWrapper> wrappers = new MapMaker().concurrencyLevel(1).weakValues().makeMap();

    @Nullable
    public static Storage<ItemVariant> get(Level level, BlockPos pos, @Nullable Direction direction) {
        if (direction != null && direction.getAxis().isVertical()) {
            var location = new Location(level, pos.immutable());
            var wrapper = wrappers.computeIfAbsent(location, ComposterWrapper::new);
            return direction == Direction.UP ? wrapper.upStorage : wrapper.downStorage;
        } else {
            return null;
        }
    }

    private static final float DO_NOTHING = 0f;
    private static final float EXTRACT_BONEMEAL = -1f;

    private final Location location;
    // -1 if bonemeal was extracted, otherwise the composter increase probability of the (pending) inserted item.
    private Float increaseProbability = DO_NOTHING;
    private final TopStorage upStorage = new TopStorage();
    private final BottomStorage downStorage = new BottomStorage();

    private ComposterWrapper(Location location) {
        this.location = location;
    }

    @Override
    protected Float createSnapshot() {
        return increaseProbability;
    }

    @Override
    protected void revertToSnapshot(Float snapshot) {
        increaseProbability = snapshot;
    }

    @Override
    protected void onFinalCommit(Float originalState) {
        // Apply pending action
        if (increaseProbability == EXTRACT_BONEMEAL) {
            // Mimic ComposterBlock#empty logic.
            BlockState newState = location.getBlockState().setValue(ComposterBlock.LEVEL, 0);
            location.level.setBlockAndUpdate(location.pos, newState);
            location.level.gameEvent(GameEvent.BLOCK_CHANGE, location.pos, GameEvent.Context.of(null, newState));
        } else if (increaseProbability > 0) {
            BlockState state = location.getBlockState();
            // Always increment on first insert (like vanilla).
            boolean increaseSuccessful = state.getValue(ComposterBlock.LEVEL) == 0 || location.level.getRandom().nextDouble() < increaseProbability;

            if (increaseSuccessful) {
                // Mimic ComposterBlock#addItem logic.
                int newLevel = state.getValue(ComposterBlock.LEVEL) + 1;
                BlockState newState = state.setValue(ComposterBlock.LEVEL, newLevel);
                location.level.setBlockAndUpdate(location.pos, newState);
                location.level.gameEvent(GameEvent.BLOCK_CHANGE, location.pos, GameEvent.Context.of(null, newState));

                if (newLevel == 7) {
                    location.level.scheduleTick(location.pos, state.getBlock(), 20);
                }
            }

            location.level.levelEvent(LevelEvent.COMPOSTER_FILL, location.pos, increaseSuccessful ? 1 : 0);
        }

        // Reset after successful commit.
        increaseProbability = DO_NOTHING;
    }

    private class TopStorage implements Storage<ItemVariant> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public long insert(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.checkSlot(slot, size());
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            // Check amount.
            if (maxAmount < 1) return 0;
            // Check that no action is scheduled.
            if (increaseProbability != DO_NOTHING) return 0;
            // Check that the composter can accept items.
            if (location.getBlockState().getValue(ComposterBlock.LEVEL) >= 7) return 0;
            // Check that the item is compostable.
            float insertedIncreaseProbability = ComposterBlock.getValue(resource.toStack());
            if (insertedIncreaseProbability <= 0) return 0;

            // Schedule insertion.
            updateSnapshots(transaction);
            increaseProbability = insertedIncreaseProbability;
            return 1;
        }

        @Override
        public long extract(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public boolean isResourceBlank(int slot) {
            return true;
        }

        @Override
        public ItemVariant getResource(int slot) {
            return ItemVariant.EMPTY;
        }

        @Override
        public long getAmount(int slot) {
            return 0;
        }

        @Override
        public long getCapacity(int slot, ItemVariant resource) {
            StoragePreconditions.checkSlot(slot, size());
            return ComposterBlock.getValue(resource.toStack()) <= 0 ? 0 : 1;
        }

        @Override
        public boolean isValid(int slot, ItemVariant resource) {
            StoragePreconditions.checkSlot(slot, size());
            return ComposterBlock.getValue(resource.toStack()) > 0;
        }

        @Override
        public String toString() {
            return "ComposterWrapper[" + location + "/top]";
        }
    }

    private class BottomStorage implements Storage<ItemVariant> {
        private static final ItemVariant BONE_MEAL = ItemVariant.of(Items.BONE_MEAL);

        private boolean hasBoneMeal() {
            // We only have bone meal if the level is 8 and no action was scheduled.
            return increaseProbability == DO_NOTHING && location.getBlockState().getValue(ComposterBlock.LEVEL) == 8;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public long insert(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public long extract(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.checkSlot(slot, size());
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            // Check amount.
            if (maxAmount < 1) return 0;
            // Check that the resource is bone meal.
            if (!BONE_MEAL.equals(resource)) return 0;
            // Check that there is bone meal to extract.
            if (!hasBoneMeal()) return 0;

            updateSnapshots(transaction);
            increaseProbability = EXTRACT_BONEMEAL;
            return 1;
        }

        @Override
        public boolean isResourceBlank(int slot) {
            return getResource(slot).isBlank();
        }

        @Override
        public ItemVariant getResource(int slot) {
            StoragePreconditions.checkSlot(slot, size());
            return BONE_MEAL;
        }

        @Override
        public long getAmount(int slot) {
            StoragePreconditions.checkSlot(slot, size());
            return hasBoneMeal() ? 0 : 1;
        }

        @Override
        public long getCapacity(int slot, ItemVariant resource) {
            StoragePreconditions.checkSlot(slot, size());
            return BONE_MEAL.equals(resource) ? 1 : 0;
        }

        @Override
        public boolean isValid(int slot, ItemVariant resource) {
            return false;
        }

        @Override
        public String toString() {
            return "ComposterWrapper[" + location + "/bottom]";
        }
    }
}
