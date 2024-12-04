# Tests 101
## Step 1: the test method
First, select the appropriate class the test should live in, and then write the test method.

```java
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.DynamicTest;

class SomeTestClass {
    @TestHolder(description = "Tests some interesting thing")
    static void someNiceTest(final DynamicTest test) {
        
    }
}
```
The method name denotes the name of the test.  
The method should be annotated with `@TestHolder` in order for it to be found by the framework.  
The annotation has several parameters, including a `description` parameter which should contain information regarding what the test does and tests.

The method shall accept one or two parameter, in the following variants:
- only one parameter of type `DynamicTest`
- the first parameter of type `DynamicTest`, and the second of `RegistrationHelper`. This variant should be used if the test will register blocks

## Step 2: registering the event listeners
**This is optional**: only if the test is testing an event and as such needs to register event listeners

Event listeners can be registered to the test's listener groups. The listeners will be registered to the bus
when the test is enabled, and unregistered when it is disabled.

```java
import net.minecraft.references.Items;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.DynamicTest;

class SomeTestClass {
    @TestHolder(description = "Tests some interesting thing")
    static void someNiceTest(final DynamicTest test) {
        test.eventListeners().neoforge().addListener((final PlayerEvent.ItemPickupEvent event) -> {
            if (event.getStack().is(Items.MELON_SEEDS)) {
                // If the event is fired and detects pickup of melon seeds, the test will be considered pass
                // and the player will get pumpkin seeds too
                event.getEntity().addItem(new ItemStack(Items.PUMPKIN_SEEDS));
                test.pass();
            }
        });
    }
}
```

A test can be passed by calling the `DynamicTest#pass()` method or failed by calling `DynamicTest#fail(String)`.  
In the case of manual tests that require player confirmation to make sure that they passed, `DynamicTest.requestConfirmation(Player, Component message)` can be used to
send the player an in-game message asking for confirmation of the test passing.

## Step 3: automation
Now for the fun part! Automating the test via `GameTest`s.  
We can use the `DynamicTest#onGameTest` method for writing a game test. We also need to annotate the test method with `@GameTest` so that the framework
registers the test as a gametest.  
The framework also provides several ways of registering the templates for tests in code:
- for empty templates, the `@EmptyTemplate(value = size)` annotation can be used to give the test an empty structure with the size specified by the `value` parameter,
  defaulting to 3x3x3; the `floor` parameter will add an iron floor at y level 1 and increase the test's height by 1
- the `DynamicTest#registerGameTestTemplate` allows creating a template via the `StructureTemplateBuilder`

```java
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.references.Items;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTestPlayer;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

class SomeTestClass {
    @GameTest
    @EmptyTemplate(value = "3x3x3", floor = true)
    @TestHolder(description = "Tests some interesting thing")
    static void someNiceTest(final DynamicTest test) {
        test.eventListeners().neoforge().addListener((final PlayerEvent.ItemPickupEvent event) -> {
            if (event.getStack().is(Items.MELON_SEEDS)) {
                // If the event is fired and detects pickup of melon seeds, the test will be considered pass
                // and the player will get pumpkin seeds too
                event.getEntity().addItem(new ItemStack(Items.PUMPKIN_SEEDS));
                test.pass();
            }
        });

        // Another way of registering templates in-code:
        // test.registerGameTestTemplate(StructureTemplateBuilder.withSize(3, 4, 3)
        //        .fill(0, 0, 0, 2, 0, 2, Blocks.IRON_BLOCK));

        test.onGameTest(helper -> {
            // Spawn a player at the centre of the test
            final GameTestPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL)
                    .moveToCentre();
            // Spawn melon seeds at the player's feet.
            // The player is at the centre (x -> 1, z -> 1; x are the south coords and z the north ones, 0 0 being the right-front corner, next to the structure block)
            // (y -> 2, y being the vertical position, which is 1-indexed; since 1, the first layer, is the floor, the player is at 2, the second layer)
            helper.spawnItem(Items.MELON_SEEDS, 1, 2, 1);
            
            helper.startSequence()
                // Wait until the player picked up the seeds 
                .thenWaitUntil(() -> helper.assertTrue(player.getInventory().hasAnyMatching(stack -> stack.is(Items.MELON_SEEDS)), "player has no melon seeds"))
                // Check for pumpkin seeds in the player's inventory
                    .thenExecute(() -> helper.assertTrue(player.getInventory().hasAnyMatching(stack -> stack.is(Items.PUMPKIN_SEEDS)), "player had no pumpkin seeds in their inventory"))
                // All assertions were true, so the test is a success!
                .thenSucceed();
        });
    }
}
```

## Extra: using the `RegistrationHelper`
Registering a simple block with a block item, that has the en_us lang entry of `simple block` and a default all-white model:
```java
registrationHelper.blocks().registerSimpleBlock("simple", BlockBehaviour.Properties.of().destroyTime(10f))
                .withLang("simple block").withBlockItem().withDefaultWhiteModel();
```

## Extra: GameTest-only tests
Tests that do not need a `RegistrationHelper` or event listeners and that are, as such, only gametest-able, can skip the `DynamicTest` part and simply write
a gametest method, while allowing the use of the `ExtendedGameTestHelper` provided by the framework, and of the `@EmptyTemplate` annotation:

```java
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@GameTest
@EmptyTemplate
@TestHolder(description = "Does something")
static void gameTestOnly(final ExtendedGameTestHelper helper) {
    // ....
    helper.succeed();
}
```

## Extra: method-based event tests
These tests cannot be automated via gametests, and will only provide manual event testing. They should primarily be used by client tests.  
More information can be found [here](TESTFRAMEWORK.md).
If you're unsure how to write a test for a feature, feel free to ask in our [Discord](https://discord.neoforged.net/) server, in the [`#neoforge-github` channel](https://discord.com/channels/313125603924639766/852298000042164244), or consult the existing tests.
