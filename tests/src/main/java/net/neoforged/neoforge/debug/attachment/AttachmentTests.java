/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.attachment;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import com.mojang.serialization.Codec;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "attachment")
public class AttachmentTests { // Porting 1.20.5 nuke?
/*    @GameTest
@EmptyTemplate
@TestHolder(description = "Ensures that ItemStack attachments are written to and read from packets as we expect")
static void itemAttachmentSyncTest(final DynamicTest test, final RegistrationHelper reg) {
final var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("handler", () -> AttachmentType.serializable(() -> new ItemStackHandler(1)).build());

test.onGameTest(helper -> {
ItemStack stack = Items.APPLE.getDefaultInstance();
IItemHandler handler = stack.getData(attachmentType);
helper.assertTrue(handler != null, "ItemStack handler is null");
assert handler != null;

handler.insertItem(0, Items.DIAMOND.getDefaultInstance(), false);
helper.assertTrue(ItemStack.matches(Items.DIAMOND.getDefaultInstance(), handler.getStackInSlot(0)), "ItemStack handler did not accept the item");

FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

// Try without stack nbt
buf.writeItemWithLargeCount(stack);
ItemStack readStack = buf.readItemWithLargeCount();

helper.assertTrue(ItemStack.matches(stack, readStack), "ItemStack did not sync correctly without NBT");
helper.assertTrue(ItemStack.matches(Items.DIAMOND.getDefaultInstance(), readStack.getData(attachmentType).getStackInSlot(0)), "ItemStack handler did not sync the contained item");

// Try with empty nbt
stack.getOrCreateTag();
buf.writeItemWithLargeCount(stack);
readStack = buf.readItemWithLargeCount();

helper.assertTrue(ItemStack.matches(stack, readStack), "ItemStack did not sync correctly with NBT");
helper.assertTrue(ItemStack.matches(Items.DIAMOND.getDefaultInstance(), readStack.getData(attachmentType).getStackInSlot(0)), "ItemStack handler did not sync the contained item");

// Try with non-empty nbt
stack.getOrCreateTag().putString("test", "test");
buf.writeItemWithLargeCount(stack);
readStack = buf.readItemWithLargeCount();

helper.assertTrue(ItemStack.matches(stack, readStack), "ItemStack did not sync correctly with NBT");
helper.assertTrue(ItemStack.matches(Items.DIAMOND.getDefaultInstance(), readStack.getData(attachmentType).getStackInSlot(0)), "ItemStack handler did not sync the contained item");

helper.succeed();
});
}*/
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that chunk attachments can capture a reference to the containing LevelChunk.")
    static void chunkAttachmentReferenceTest(DynamicTest test, RegistrationHelper reg) {
        class ChunkMutableInt implements INBTSerializable<IntTag> {
            private final LevelChunk chunk;
            private int value;

            public ChunkMutableInt(LevelChunk chunk, int value) {
                this.chunk = chunk;
                this.value = value;
            }

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
                chunk.setUnsaved(true);
            }

            @Override
            public IntTag serializeNBT(HolderLookup.Provider provider) {
                return IntTag.valueOf(value);
            }

            @Override
            public void deserializeNBT(HolderLookup.Provider provider, IntTag nbt) {
                this.value = nbt.getAsInt();
            }
        }

        var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("chunk_mutable_int", () -> AttachmentType.serializable(chunk -> new ChunkMutableInt((LevelChunk) chunk, 0)).build());

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            event.getDispatcher()
                    .register(literal(test.id())
                            .then(literal("print_and_increment")
                                    .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                                    .executes(ctx -> {
                                        var chunk = ctx.getSource().getLevel().getChunkAt(BlockPos.containing(ctx.getSource().getPosition()));
                                        var attachment = chunk.getData(attachmentType);
                                        attachment.setValue(attachment.getValue() + 1);
                                        ctx.getSource().sendSuccess(() -> Component.literal("New attachment value: " + attachment.getValue()), false);
                                        return Command.SINGLE_SUCCESS;
                                    })));
        });

        test.onGameTest(helper -> {
            var player = helper.makeOpMockPlayer(Commands.LEVEL_OWNERS);
            var pos = helper.absolutePos(BlockPos.ZERO);
            player.setPos(pos.getCenter());

            helper.getLevel().getChunk(pos).removeData(attachmentType); // remove data to ensure that the test can run multiple times

            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), test.id() + " print_and_increment");
            helper.assertTrue(((LevelChunk) helper.getLevel().getChunk(pos)).getData(attachmentType).getValue() == 1,
                    "Chunk attachment value should have been 1");

            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), test.id() + " print_and_increment");
            helper.assertTrue(((LevelChunk) helper.getLevel().getChunk(pos)).getData(attachmentType).getValue() == 2,
                    "Chunk attachment value should have been 2");

            helper.succeed();
        });
    }

/*    @GameTest
@EmptyTemplate
@TestHolder(description = "Ensures that ItemStack attachments can be specified in recipe outputs")
static void itemAttachmentRecipeOutput(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

var ingredient = new TestEnabledIngredient(
Ingredient.of(Items.GOLDEN_SHOVEL),
test.framework(),
test.id());
var outputStack = Suppliers.memoize(() -> {
var stack = new ItemStack(Items.GOLDEN_SHOVEL);
stack.setData(attachmentType, 1);
return stack;
});

var shapelessId = new ResourceLocation(reg.modId(), "test_shapeless");
var shapedId = new ResourceLocation(reg.modId(), "test_shaped");
var blastingId = new ResourceLocation(reg.modId(), "test_blasting");

reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
@Override
protected void buildRecipes(RecipeOutput recipeOutput) {
ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, outputStack.get())
.requires(ingredient)
.unlockedBy("has_shovel", has(Items.GOLDEN_SHOVEL))
.save(recipeOutput, shapelessId);

ShapedRecipeBuilder.shaped(RecipeCategory.MISC, outputStack.get())
.pattern("xx")
.pattern("xx")
.define('x', ingredient)
.unlockedBy("has_shovel", has(Items.GOLDEN_SHOVEL))
.save(recipeOutput, shapedId);

SimpleCookingRecipeBuilder.blasting(ingredient, RecipeCategory.MISC, outputStack.get(), 1.0f, 100)
.unlockedBy("has_shovel", has(Items.GOLDEN_SHOVEL))
.save(recipeOutput, blastingId);
}
});

test.onGameTest(helper -> helper
.startSequence()
.thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
.thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

.thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
.thenExecute(crafter -> crafter.setItem(0, Items.GOLDEN_SHOVEL.getDefaultInstance()))
.thenIdle(3)

.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> {
var chest = helper.requireBlockEntity(1, 2, 1, ChestBlockEntity.class);
var stack = chest.getItem(0);

helper.assertTrue(stack.getItem() == Items.GOLDEN_SHOVEL, "Expected golden shovel");
helper.assertTrue(stack.hasData(attachmentType), "Expected attachment");
helper.assertTrue(stack.getData(attachmentType) == 1, "Expected attachment value of 1");
})

.thenExecute(() -> {
// Just look at the output via the RecipeManager
var recipeManager = helper.getLevel().getRecipeManager();

for (var id : List.of(shapelessId, shapedId, blastingId)) {
var recipe = recipeManager.byKey(id).map(RecipeHolder::value).orElse(null);
if (recipe == null) {
helper.fail("No recipe " + id);
}

helper.assertTrue(ItemStack.matches(outputStack.get(), recipe.getResultItem(helper.getLevel().registryAccess())), "Recipe output should match stack.");
}
})

.thenSucceed());
}

@GameTest
@EmptyTemplate
@TestHolder(description = "Ensures that ItemStack attachments can be cloned in certain recipes")
static void itemAttachmentRecipeCopying(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

var smithingId = new ResourceLocation(reg.modId(), "test_smithing");

reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
@Override
protected void buildRecipes(RecipeOutput recipeOutput) {
SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(Items.STONE_SHOVEL), Ingredient.of(Items.GOLD_INGOT), RecipeCategory.MISC, Items.GOLDEN_SHOVEL)
.unlocks("has_shovel", has(Items.STONE_SHOVEL))
.save(recipeOutput, smithingId);
}
});

test.onGameTest(helper -> helper
.startSequence()
.thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
.thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

.thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
.thenExecute(crafter -> {
ItemStack shulkerBox = Items.SHULKER_BOX.getDefaultInstance();
shulkerBox.setData(attachmentType, 1);
crafter.setItem(0, shulkerBox);
})
.thenExecute(crafter -> crafter.setItem(1, Items.BLACK_DYE.getDefaultInstance()))
.thenIdle(3)

.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> {
var chest = helper.requireBlockEntity(1, 2, 1, ChestBlockEntity.class);
var stack = chest.getItem(0);

helper.assertTrue(stack.getItem() == Items.BLACK_SHULKER_BOX, "Expected black shulker box");
helper.assertTrue(stack.hasData(attachmentType), "Expected attachment");
helper.assertTrue(stack.getData(attachmentType) == 1, "Expected attachment value of 1");
})

.thenIdle(5) // Crafter cooldown
.thenExecute(crafter -> {
ItemStack writtenBook = Items.WRITTEN_BOOK.getDefaultInstance();
writtenBook.setData(attachmentType, 1);
writtenBook.addTagElement("author", StringTag.valueOf("NeoForge"));
writtenBook.addTagElement("title", StringTag.valueOf("How to copy attachments"));
crafter.setItem(0, writtenBook);
})
.thenExecute(crafter -> crafter.setItem(1, Items.WRITABLE_BOOK.getDefaultInstance()))
.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> {
var chest = helper.requireBlockEntity(1, 2, 1, ChestBlockEntity.class);
var stack = chest.getItem(1);

helper.assertTrue(stack.getItem() == Items.WRITTEN_BOOK, "Expected written book");
helper.assertTrue(stack.hasData(attachmentType), "Expected attachment");
helper.assertTrue(stack.getData(attachmentType) == 1, "Expected attachment value of 1");
})

.thenExecute(() -> {
// Just look at the output via the RecipeManager
var recipeManager = helper.getLevel().getRecipeManager();
Recipe<Container> smithingRecipe = (Recipe<Container>) recipeManager.byKey(smithingId).map(RecipeHolder::value).orElse(null);
if (smithingRecipe == null) {
helper.fail("No recipe " + smithingId);
}
ItemStack smithingInput = new ItemStack(Items.STONE_SHOVEL);
smithingInput.setData(attachmentType, 1);
Container smithingContainer = new SimpleContainer(
new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
smithingInput,
new ItemStack(Items.GOLD_INGOT));
var outputStack = new ItemStack(Items.GOLDEN_SHOVEL);
outputStack.setData(attachmentType, 1);
helper.assertTrue(ItemStack.matches(outputStack, smithingRecipe.assemble(smithingContainer, helper.getLevel().registryAccess())), "Recipe output should match stack.");
})

.thenSucceed());
}

@GameTest
@EmptyTemplate
@TestHolder(description = "Ensures that ItemStack.CODEC can handle attachments")
static void itemAttachmentCodec(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

test.onGameTest(helper -> {
ItemStack stack = Items.APPLE.getDefaultInstance();
stack.setData(attachmentType, 1);

var encodedNbt = Util.getOrThrow(ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack), IllegalStateException::new);
var decodedStack = Util.getOrThrow(ItemStack.CODEC.parse(NbtOps.INSTANCE, encodedNbt), IllegalStateException::new);

helper.assertTrue(decodedStack.hasData(attachmentType), "Decoded stack should have the data");
helper.assertTrue(ItemStack.matches(stack, decodedStack), "Stacks should match");

helper.succeed();
});
}

@GameTest
@EmptyTemplate
@TestHolder(description = "Ensures that attachments can opt-out of serializing default values")
static void itemAttachmentSkipSerialization(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT, i -> i != 0).build());

test.onGameTest(helper -> {
ItemStack stack = Items.APPLE.getDefaultInstance();
stack.setData(attachmentType, 1);
helper.assertTrue(stack.serializeAttachments() != null, "Stack should have serialized attachments");
stack.setData(attachmentType, 0);
helper.assertTrue(stack.serializeAttachments() == null, "None of the stack's attachments should be serialized");
helper.assertTrue(stack.hasData(attachmentType), "Stack should have attached data");

helper.succeed();
});
}

@GameTest
@EmptyTemplate
@TestHolder(description = "Ensures that removing attachments works")
static void itemAttachmentRemoval(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

test.onGameTest(helper -> {
ItemStack stack = Items.APPLE.getDefaultInstance();
stack.setData(attachmentType, 1);
helper.assertTrue(stack.hasData(attachmentType), "Stack should have attached data");
stack.removeData(attachmentType);
helper.assertFalse(stack.hasData(attachmentType), "Stack should not have attached data");

helper.succeed();
});
}

@GameTest
@EmptyTemplate
@TestHolder(description = "Ensures that the presence of non-serializable attachments can be checked")
static void itemAttachmentPresence(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).build());

test.onGameTest(helper -> {
ItemStack stack = Items.APPLE.getDefaultInstance();
stack.setData(attachmentType, 1);
helper.assertTrue(stack.hasData(attachmentType), "Stack should have attached data");
//Also check we can detect the presence if we don't know what types are attached
helper.assertTrue(stack.hasAttachments(), "Stack should have attached data");

helper.succeed();
});
}

@GameTest
@EmptyTemplate
@TestHolder(description = "Ensures that custom cloning behaviour works")
static void itemAttachmentCloning(final DynamicTest test, final RegistrationHelper reg) {
//Note: Modification of the data to create a copy is not intended and only used to verify that the data is cloned properly
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyHandler((holder, i) -> 2 * i).build());

test.onGameTest(helper -> {
ItemStack stack = Items.APPLE.getDefaultInstance();
stack.setData(attachmentType, 1);
helper.assertTrue(stack.getData(attachmentType) == 1, "Stack should have attached data");
stack = stack.copy();
helper.assertTrue(stack.getData(attachmentType) == 2, "Stack cloner should have cloned and modified the data");
helper.succeed();
});
}

@GameTest
@EmptyTemplate
@TestHolder(description = "Ensures that optional data can be queried")
static void itemAttachmentOptional(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).build());

test.onGameTest(helper -> {
ItemStack stack = Items.APPLE.getDefaultInstance();
Optional<Integer> optional = stack.getExistingData(attachmentType);
helper.assertTrue(optional.isEmpty(), "Optional should be empty");
stack.setData(attachmentType, 1);
optional = stack.getExistingData(attachmentType);
helper.assertTrue(optional.isPresent(), "Optional should have attached data");
helper.succeed();
});
}*/

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that player attachments are copied on respawn when appropriate.")
    static void playerAttachmentCopyOnRespawn(DynamicTest test, RegistrationHelper reg) {
        var lostOnDeathBoolean = reg.attachments()
                .register("lost_on_death_boolean", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
        var keptOnDeathBoolean = reg.attachments()
                .register("kept_on_death_boolean", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build());

        test.onGameTest(helper -> {
            var player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);
            player.setData(lostOnDeathBoolean, true);
            player.setData(keptOnDeathBoolean, true);

            var returningPlayer = player.getServer().getPlayerList().respawn(player, true);

            helper.assertTrue(returningPlayer.getData(lostOnDeathBoolean), "Lost-on-death attachment should have remained after end portal respawning.");
            helper.assertTrue(returningPlayer.getData(keptOnDeathBoolean), "Kept-on-death attachment should have remained after end portal respawning.");

            var respawnedPlayer = player.getServer().getPlayerList().respawn(returningPlayer, false);

            helper.assertFalse(respawnedPlayer.getData(lostOnDeathBoolean), "Lost-on-death attachment should not have remained after respawning.");
            helper.assertTrue(respawnedPlayer.getData(keptOnDeathBoolean), "Kept-on-death attachment should have remained after respawning.");

            helper.succeed();
        });
    }
}
