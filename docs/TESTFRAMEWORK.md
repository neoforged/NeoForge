# The Test Framework
This guide will explain the MC Test Framework, how it works, and how to use it.
If you are a contributor looking to add a GameTest for your PR, [please see this guide instead](NEOGAMETESTS.md).
## What is the Framework?
The Framework is used in order to manage and process Minecraft in-game tests, and is designed to be used by NeoForge.
## How does the Framework work?
The Framwork manages tests, which can be enabled or disabled at runtime, using either an in-game command or a UI. Tests have a status which consist of a message and a `Result`, which can be either `PASSED`, `FAILED` or `NOT_PROCESSED`. The status of a test is changed by test itself when it meets certain conditions, or it encounteres exceptions.  
The status of tests is synchronized with clients, which means multiple players can simultaneously run tests on the same server.  
Tests can also be run without manual player interaction (like on a CI for example), by utilising the [GameTest integration](#gametest-integration).
## Creating a `TestFramework`
A `TestFramework` can be created during mod construction using `FrameworkConfiguration$Builder`:
```java
  final TestFrameworkInternal framework = FrameworkConfiguration.builder(new ResourceLocation("examplemod:tests")) // The ID of the framework. Used by logging, primarily
    .clientConfiguration(() -> ClientConfiguration.builder() // Client-side compatibility configuration. This is COMPLETLY optional, but it is recommended for ease of use.
      .toggleOverlayKey(GLFW.GLFW_KEY_J) // The key used to toggle the tests overlay
      .openManagerKey(GLFW.GLFW_KEY_N) // The key used to open the Test Manager screen
      .build())
     
    .allowClientModifications() // Allow OP'd clients to modify the status of tests, and to enable them
    .syncToClients() // Sync the status of tests to clients
  
    .build().create(); // Build and store the InternalTestFramework. We use the "internal" version because we want to access methods not usually exposed, like the init method
      
  // Initialise this framework, using the mod event bus of the currently loading mod, and the container of the currently loading mod.
  // The container is used for collecting annotations.
  // This method will collect and register tests, structure templates, group data, and will fire init listeners.
  framework.init(modBus, modContainer);

  // Register the commands of the framework under the `tests` top-level command.
  NeoForge.EVENT_BUS.addListener((final RegisterCommandsEvent event) -> {
    final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("tests");
    framework.registerCommands(node);
    event.getDispatcher().register(node);
  });
```

## Creating tests
Tests are represented by any object implementing `Test` which is registered using `TestFramework$Tests#register(Test)` or using by using a test collector.

The test framework, by default collects 3 different types of tests:
- [Basic Tests](#Basic-tests)
- [Method-based Tests](#Method-based-tests)
- [Method-based Event Tests](#Method-based-event-tests)

### Basic tests
Basic tests are represented by any class inherithing from `Test`, annotated with `TestHolder`, which has a no-arg public constructor. The collector will instantiate
the test, using reflection.  
**Note**: it is recommended that you extend from `AbstractTest`, as it will configure the test based on the parameters of the `TestHolder` annotation.

Example basic test using `AbstractTest` which passes when a player enters a section:
```java
@TestHolder(
    value = "entity_entering_section",
    description = "Tests if the EntityEvent.EnteringSection will be fired when a player moves to another chunk."
)
public class EnteringSectionEventTest extends AbstractTest {
  @Override
  public void onEnabled(@NotNull EventListenerGroup listeners) {
    logger().info("Basic test 'entity_entering_section' has been enabled!");
    listeners.getFor(Bus.NEOFORGE).addListener((final EntityEvent.EnteringSection event) -> {
      if (event.getEntity() instanceof Player) {
        pass();
      }
    });
  }
}
```

### Method-based tests
Method-based tests are represented by any *static* method, annotated with `TestHolder`, which has either exactly one parameter of type `DynamicTest`, or a parameter of type `DynamicTest` and `RegistrationHelper`, in this order.
The collector will invoke the method during `TestFramework#init`, allowing you to configure listeners on the test.  
**Note**: the method can have any access modifier, as it will be invoked with a trusted lookup!

Example method-based test which passes when a player enters a section:
```java
@TestHolder(
    value = "entity_entering_section",
    description = "Tests if the EntityEvent.EnteringSection will be fired when a player moves to another chunk."
)
static void entityEnteringSection(final DynamicTest test /*, final RegistrationHelper reg */) {
  test.framework().logger().info("Method-based 'entity_entering_section' test has been initialised!");
  test.whenEnabled(listeners -> {
    test.framework().logger().info("Method-based 'entity_entering_section' test has been enabled!");

    listeners.getFor(Bus.NEOFORGE).addListener((final EntityEvent.EnteringSection event) -> {
      if (event.getEntity() instanceof Player) {
        test.pass();
      }
    });
  });
}
```

### Method-based Event tests
Method-based event tests are represented by any *static* method, annotated with `TestHolder`, which accept exactly 2 parameters:
- the first parameter is of any type which is a subclass of `Event`, and represents the type of the event to listen for; If this parameter implements `IModBusEvent`, the listener will be registered to the `MOD` bus, otherwise it will be registered to the `FORGE` bus;
- the second parameter is of the `DynamicTest` type.

The collector will register the method as an event listener, and as such the method is invoked every time an event is fired.
**Because of this, the test may *only* be configured in the annotation!**  
**Note**: the method can have any access modifier, as it will be invoked with a trusted lookup!

Example method-based event test which passes when a player enters a section:
```java
@TestHolder(
    value = "entity_entering_section",
    description = "Tests if the EntityEvent.EnteringSection will be fired when a player moves to another chunk."
)
static void entityEnteringSection(final EntityEvent.EnteringSection event, final DynamicTest test) {
  test.framework().logger().info("Method-based 'entity_entering_section' event test listener has been fired!");
  if (event.getEntity() instanceof Player) {
    test.pass();
  }
}
```

## Annotations

### The `TestHolder` annotation
The `TestHolder` annotation has different parameters which are used to configure a test:
- `value: String` - the ID of the test; *required*;
- `groups: String[]` - the groups this test is in;
- `title: String` - the human-readable title of the test;
- `description: String[]` - a description of the test; this property is usually used in order to provide instructions on how to use the text;
- `enabledByDefault: boolean` - if the test is enabled by default;
- `side: Dist[]` - the sides that the test should be registered on; **Note**: this property should be used only when *absolutely needed* (like on method-based event tests listening for client-only events).

### The `ForEachTest` annotation
When applied on a class, the `ForEachTest` annotation will apply common configuration to all method-based tests in the class.
The parameters of `ForEachTest` are:
- `idPrefix: String` - a prefix to be added to the IDs of the child tests;
- `groups: String[]` - the groups to add to the child tests;
- `side: Dist[]` - the sides to load the child tests on.

Example usage:
```java
@ForEachTest(
    idPrefix = "entity_event_",
    groups = "events.entity"
)
public class EntityEvents {
  @TestHolder(id = "on_join_level") // The ID of the test will be 'entity_event_on_join_level', and it will be in the 'events.entity' group
  static void onJoinLevel(final EntityJoinLevelEvent event, final DynamicTest test) {}
}
```

### The `OnInit` annotation
Annotate a static method accepting exactly one parameter of `TestFrameworkInternal` (or parent interfaces) to
register that method as an on-init listener, which will be called in `TestFrameworkInternal#init`
The time when the listener will be called depends on the `value` (stage) given as an annotation parameter.

### The `TestGroup` annotation
Annotate a `String` field with this annotation in order to configure the group with the ID being the underlying value of the field.  
The parameters of the annotation:
- `name: String` - the human-readable name of the group; *required*;
- `enabledByDefault: boolean` - if the tests in the group are enabled by default;
- `parents: String[]` - the explicitly-declared parents of the group;

Example usage for configuring the `events.level_related` group:
```java
@TestGroup(name = "Level-Related Events", enabledByDefault = true, parents = "level_tests")
public static final String LEVEL_RELATED_EVENTS = "events.level_related";
```

### The `RegisterStructureTemplate` annotation
Annotate a static field containing either a `StructureTemplate`, a `Supplier` of `StructureTemplate`, or a `StructureTemplateBuilder` in order to automatically register that code-defined template.  
The parameters of the annotation:
- `value: String` - the ID of the template; *required*.

Example usage for registering a 5x5x5 empty structure template:
```java
@RegisterStructureTemplate("examplemod:empty_5x5")
public static final StructureTemplate EMPTY_5x5 = StructureTemplateBuilder.empty(5, 5, 5);
```

## GameTest Integration
The test system has integration with Mojang's GameTest. A test can supply the information required by the GameTest using `@Nullable Test#asGameTest`. The provided `GameTestData` will give the GameTest framework the data it requires. Note that the name of the GameTest is the test's ID, and if the provided `batchName` is null, then the batch of the test will be the ID of its first group.

Default `GameTestData` providers:
- for basic tests using `AbstractTest`, override `onGameTest` and annotate it with `GameTest` (the annotation will be used to configure the `GameTestData` as vanilla does). That method will be run when the GameTest version of the test is run;
- for method-based tests, annotate the test method with `GameTest` to provide the configuration, and use `DynamicTest#onGameTest` to provide the gametest functionality;
- method-based event tests do not support this functionality!

Example basic test with GameTest integration:
```java
@TestHolder("lever_test")
public class LeverTest extends AbstractTest {
  @Override
  @GameTest(template = "examplemod:lever_test_template")
  protected void onGameTest(GameTestHelper helper) {
    helper.startSequence()
      .thenExecute(() -> helper.pullLever(0, 2, 0))
      .thenIdle(1)
      .thenWaitUntil(0, () -> helper.assertBlockProperty(new BlockPos(0, 2, 0), LeverBlock.POWERED, true))
      .thenExecute(this::pass) // Pass the test if the lever is powered. If the assertion fails, the framework will make sure to fail the test.
      .thenSucceed();
  }
}
```

Example method-based test with GameTest integration:
```java
@TestHolder("lever_test")
@GameTest(template = "examplemod:lever_test_template")
static void leverTest(final DynamicTest test) {
  test.onGameTest(helper -> helper.startSequence()
    .thenExecute(() -> helper.pullLever(0, 2, 0))
    .thenIdle(1)
    .thenWaitUntil(0, () -> helper.assertBlockProperty(new BlockPos(0, 2, 0), LeverBlock.POWERED, true))
    .thenExecute(test::pass) // Pass the test if the lever is powered. If the assertion fails, the framework will make sure to fail the test.
    .thenSucceed());
}
```

In both examples above, you may register the template in-code, using:
```java
@RegisterStructureTemplate("examplemod:lever_test_template")
static final StructureTemplate LEVER_TEST_TEMPLATE = StructureTemplateBuilder.withSize(1, 2, 1)
  .placeFloorLever(0, 1, 0, false)
  .build();
```
or, if you want to use an empty template, you may use the `@EmptyTemplate` annotation, which accepts:
- a `value: String` - the size of the structure, in `LxHxW` format, defaulting to `3x3x3`;
- a `floor: boolean` - if `true`, the template will have an iron floor, as such increasing the height by one, defaulting to `false`.

For a guide on GameTests, see [this guide](NEOGAMETESTS.md).