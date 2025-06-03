# Preamble

Spelling errors, grammatical mistakes and documentation are some of the things we are still working on, but for the most
part.
It is enough to start getting this reviewed again.

This work started roughly around 1.20.4. Adrian got it started, but ultimately started slowing down when tests were
starting to be required by Neo. I (Soaryn) offered to help write the tests as well as proof the PR, and we finished
around
1.21.1. The main problem, is that this level of breaking change was then pushed back to 1.21.2, which by the time
released
neither Adrian, nor I had the time to work on porting it, especially with consensus that most modders should not update.
With 1.21.5, we actually finished it but opted to wait a little longer until the next major target is chosen.
Ultimately, there is no point in waiting, we are targeting this for 1.21.6.

The idea is simple, we need a developer to feel comfortable with the new system while providing something familiar to
what
we already have now. This is achievable by two core tenants:

- Familiar naming : Not naming things just because they sound cool, or "X modloader does Y", or trying to make the "most
  obvious name", but instead something that makes sense canonically and fits the current positive pattern.
- Intuitive logic (where possible) : Templates and the base interface should be easy to grasp. While the actual
  implementations of said templates may be daunting, the consumption of them should be easy.

While abstraction can cause problems with readability at an initial glance, it is not expected for any dev to fully
digest the entire api in a matter of moments, but rather a small sample set of it. Be aware of this when reviewing as
while yes this does have
a fair amount of abstraction, there were some core areas that required it that allow us to reuse code meaning that when
improving one area, typically every thing else cascades with said benefit.

## Required Components

`IResource`
: Provides a centralized point for documentation, control of whether the resource is empty, as well as constrains the
type to classes the implementer controls as they must be able to implement the interface. For instance, someone can't
mistakenly do `IResourceHandler<ItemStack>`. This would be objectively incorrect as the Itemstack is a mutable structure
which removes the builtin safety features of the new system.

`IResourceHandler<T:IResource>`
: The entire PR is designed around the changes focused in the new interface for handling resource. This was a hybrid of
`IItemHandler` & `IIFluidHandler`. While not everthing has a 1:1 migration, we now widely cover the usecases of the
respective handlers
in some way while providing a consistent method naming and handling. No longer will you need to worry if `IItemHandlers`
return the amount taken,
and `IFluidHandlers` return the remainder (that is backwards on purpose for making the point).

`Transactions`
: Until just recently, transactions were a hard stop "no". With the community more receptive of having transactions,
this will definitely make our lives easier in regard to handling multiple requests and inventories. As well as clean up
some of the "simulation" code paths.

`IItemCapabilityContexts`
: Formerly known as `IItemContext` in previous iterations of this PR. A way to inform how handlers, on an itemstack,
should behave or be referenced when inserting/extracting. This solves
the common problem of "I have 10 empty buckets but 4 buckets worth of lava, I should be able to quickly fill 4 buckets
and leave the
other 6 in place". The interface has a pretty decent doc for what it does that Adrian wrote, though it may be out of
date with transactions a bit.

Everything else from `IResourceStacks` to the templates are mostly optional. How we implement the vanilla blocks can
change quite a bit based on when they are written as well as who is the current author.
What we have now, should work for the most part, but there may be places that the transactions will need to be improved
or were missed. Primarily when dealing with snapshots. The api design was completed before transactions were given the
green-light, so I expect some hiccups along the way.
A lot of the utility written for this PR definitely helps with some abstraction
and code reuse throughout those implementations.

## Migrations

See `migration_paths.md` for a full list regarding `IItemHandler` & `IFluidHandler` deprecations. In short, know that
all previous methods, have an alternative or introduces a new method that wasn't previously possible.
This is one of the major benefits to a synergistic and generalized handler.

## IResource

There was some discussion of whether we should have an `IResource` or not. At the end of the day, providing a type
constraint for the `IResourceHandler` proved to be more positive.

- `isEmpty` can properly be inquired on the resource and thus allows the creator of said resource type to control what
  is "empty" for their resource. We also don't need to add a point of failure somewhere else attempting to delegate that
  check on the resource consumer side. Note, this system allows for a resource to be never empty as well. That is purely
  up to design spec.
- We can worry less about type misuse, given all `IResourceHandlers` will be handling a type of some `IResource`. While
  in most cases you will likely need to know the type going in, certain situations can be done with complete type
  agnostic design.
- Documentation as well as navigation is more centralized and thus faster to follow by having a root interface. If
  utilizing an external mod API that only provides a resource, you can accurately determine what the resource of that
  mod is by going to `IResource` and then looking at what classes implement it (In IDEA, it is just a little button on
  the left near the line numbers).
- We alleviate misunderstandings when it comes to implementing as you prevented developers from doing
  `IResourceHandler<ItemStack>`. This is not possible given `ItemStack` is not of type `IResource`; however, if we were
  to unbind the type, there would be a hindsight trial and error for everyone to go through unnecessarily. We should
  seek to mitigate this as best as we can.
- `IResourceHandlers` become agnostic to the type allowing you to use them all the same if desired or needed. The only
  thing we need to know of the resource is `isEmpty` typically in our extraction/insertion calls.

When implementing your own `IResource` you can either choose to make it a registered backing type like `ItemResource` or
`FluidResource` and use `IRegisterdResource`, or you can create a complete custom solution like the examples in the
tests.

For example, one could make an ElementResource:

```java
public enum TestElementResource implements IResource, StringRepresentable {
    NONE("none"),
    FIRE("fire"),
    WATER("water"),
    EARTH("earth"),
    AIR("air");

    private final String elementName;

    TestElementResource(String elementName) {
        this.elementName = elementName;
    }

    @Override
    public boolean isEmpty() {
        return this == NONE;
    }

    @Override
    public String getSerializedName() {
        return elementName;
    }

    public static final StringRepresentableCodec<TestElementResource> CODEC = StringRepresentable.fromEnum(TestElementResource::values);
    public static final StreamCodec<FriendlyByteBuf, TestElementResource> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(TestElementResource.class);
}
```

Here we elected to use an empty resource, but again, that is completely up to your own designs.

## Templates

There are several different templates we currently provide as well. These are optional for the most part to the PR, but
it may be wise to provide them for now.

- `ResourceContainer` - Designed originally for XyCraft. Only works as an attachment. This is actually one of the core
  tests in the PR.
- `ResourceStorageHandler` - Tries to match vanilla in terms of DataComponent use. Has both an attachment and component
  sub class implementation for items and fluids.
- `Contexts` - The idea of `IItemCapabilityContexts` is new, and as mentioned before help solve some problems of when
  Itemstacks that have a handler on them are stacked. It is the context passed in when getting the capability of an
  ItemStack, this is similar to how you'd pass in a direction when you get a block capability.

The first notable optional templates are the resource containers such as `SimpleItemResourceContainer`.
As the documentation expresses, these were initially designed for XyCraft so they are a lot less theory crafted and
actually what is in production now just with `IItemHandler` as the main backing feature. It is however, a lot to digest
so while I don't mind them
not being part of neo, I do want to emphasize it is important we maintain the ability to be implemented.

Being able to call something like the following is incredible powerful when able to be resused this simply.

```java
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.handlers.templates.container.IHandleIOBehaviour;
import net.neoforged.neoforge.transfer.handlers.templates.container.SimpleFluidResourceContainer;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;

private void test(NonNullList<MutableResourceStack<FluidResource>> fluids) {
    //Creates a list of MutableResourceStacks stored in a container for simple use.  
    var container = SimpleFluidResourceContainer.from(fluids)
            .capacity(someArbitraryCapacity)
            .onChange(this::markBlockEntityAsDirty)
            .build();

    // We can also create the handler with io behavior.
    var handler = container.asHandler(IHandleIOBehaviour.INSERT_ONLY);
}
```

Viola, we now have not only a container that has the data stored, but also a handler we can return with the express
ability to be inserted only.
As noted, these are intended to be data attachments and provide codecs to serialize said data how you need. The test has
a practical example

## Transactions

TransactionImpl was left primarily untouched from the original spec. The idea is that instead of "simulating" and
passing in either a boolean or a enum flag on what you want to perceive the action to do, you pass in a transaction, and
if you like the results, commit them.

```java
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

private static void example(IResourceHandler<FluidResource> handler) {
    try (var transaction = Transaction.open(TransactionContext.ROOT)) {
        var handled = handler.extract(Fluids.WATER.defaultResource(), 1000, transaction);
        if (handled == 1000)
            transaction.commit();
    }
}
```

In this example, we take some handler of type FluidResource, and try to extract 1000 water from it. If we succeed to do
exactly that, we commit allowing the actions to take place. If we decided to leave off `transaction.commit()` then after
the try block, the handler should "revert" to its state before the transaction was opened. This of course assumes the
implementation of the handler allows it to do so. Do note, regardless if you commit or not, the returned values from a
handler method call should be the same.

In the java documentation there is also this example:

```java
private static void test() {
    try (Transaction outerTransaction = Transaction.open(TransactionContext.ROOT)) {
        // (A) some transaction operations
        try (Transaction nestedTransaction = outerTransaction.open(outerTransaction)) {
            // (B) more operations
            nestedTransaction.commit();
            // Commit the changes that happened in this transaction.
            // This is a nested transaction, so changes will only be applied if the outer
            // transaction is committed too.
        }
        // (C) even more operations
        outerTransaction.commit();
        // This is an outer transaction: changes (A), (B) and (C) are applied.
    }
}

```

## Considered for Removal

ResourceHandlerSlot
: Unknown usecases thus far. People we inquired, had NO idea this even existed. I'm sure people use it, but is it
something we should maintain?

ResourceHandlerCopySlot
: Unknown usecases thus far. People we inquired, had NO idea this even existed. I'm sure people use it, but is it
something we should maintain?

StackCopySlot
: Unknown usecases thus far. People we inquired, had NO idea this even existed. I'm sure people use it, but is it
something we should maintain?

ResourceHandlerRecipeInput
: Seemingly niche usecase, and likely better left up to the implementer to handle recipe inputs as they need.

## Common Misunderstandings

allowsInertion/Extraction
: In essence, this method for the most part is optional, but to allow mods that provide things like pipes to run more
efficiently, we need someway to inquire if we were to insert or extract would be denied? We are wanting to pre-emptively
skip those in the future. The problem we see, is that some developers may assume this method is control logic and thing
that by returning false, they disallow inserting altogether. This is incorrect and as state in the java doc, it doesn't
control internal logic, it is only a hint.
Alternative names for this are `mayInsert`, `supportsInsert`,
`insertionPossible` (and the same for extraction)

AutoCloseable Try
: Something that is not common in Minecraft modding is the idea of AutoClosable implementations. In short, if something
implements AutoCloseable, when put in a `try-block` like the transactions above, once you leave the block, the object
will call `close()`. In the case of transactions this will be something new for many of you. Be careful with this. It is
**very** simple to accidentally misconfigure your resource handler logic by either closing manually, forgetting to
commit
when you wanted to or calling a method like open again with the wrong context.

## Misc

### Transactions

In theory transactions were suddenly recently grown in interest. If you are still hesitant on them, I can certainly
understand
your reluctance. After making this PR, I have found it becomes quicker and quicker the more you use it (like anything)
there are glaring points that become an easy mistake, but with any luck we can have the errors thrown be clear enough.
In some cases this won't always work out, and it will be something incredibly simple staring you in the face
(Me spending 20 minutes on why a transaction was already opened...twice woops) .

### Naming

While I'm certainly a fan of calm discussions for how and what methods/classes should be named there are only a few I'm
personally feeling need a rename or slight tweak to them. With the initial core tenant of keeping something familiar but
canonically correct, I feel (perhaps conceitedly so) that we managed to achieve it mostly. There are a few that wind up
being, while correct sounding, confusing due to the backing structure names. `ItemContextItemHandler` is one of those
examples. While it is true to its name, it can definitely be something easily able to lose a mental grasp on what it is
supposed to be.

If you have something specific you see, calmly discuss it, provide alternate names that you'd like to see, but
understand a lot of these names were made over the course of several months and deliberation with myself, Adrian, pup,
thiakill, my stream chat when initially designing this, and several members of ForgeCraft so it wasn't done necessarily
on a whim.

### Longs

To broach the topic `longs`, there are some who desire `longs` for `IResourceHandler` for returns and parameters;
however, from use there is so far very little positive, being able to in insert, send massive amounts of said resource,
but a lot of annoying and possibly detrimental side effects.
The first, is that while it doesn't seem like much having to constantly cast back and forth between `ints` and `longs`
is incredibly tedious, with a possible point accidentally either truncating a value or overflowing by accident.
Serialization now effectively needs to double for both disk and networking packets, and in the case of common mods,
these can get rather hefty quick. I am still waiting for a viable enough positive to convince me it is warranted let
alone needed, but "I want my block to receive 9 quintillion units" is just simply not convincing. If it is a matter of
wanting the player to be able to hoard a lot, then that is rather possible with this. It'd be trivial to have a storage
that can hold several billion of a resource while still allowing mods to read those. That being said, you have to take a
moment, and honestly evaluate: "Am I making a storage that can hold 9 quintillion for the player to fill, or am I just
wanting my mod used". A player will not feasibly be able to store 1 quintillion of an item without some drastic skewing
of mechanics which this would ultimately start lending itself towards. It isn't so much an argument of balance or the
typically thrown notion of "OP", but more how can one modder compatibly create with another, and by just skewing
everything to kingdom come, it doesn't really synergize well.

A fun fact, initially when creating this and its sibling PR, I had started going through making things longs, but the
more I used it, the more it became a mechanical burden to exchange between the two types. As well as providing again,
no clear benefit as to the actual results of that burden being imposed.