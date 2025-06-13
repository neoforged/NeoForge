# Energy Handler Primer

## PR Reviewing Preamble (Delete this section after review phase)

Originally this was going to be a separate sibling PR to the Resource handler, but when mentioning to Orion that some
files, such as the `EnergyStorage` class, were notably missing (back from last June or so due to a mistake from Adrian's
original PR) and that I was working on reverting the deletion; he informed me I should just go ahead cut it and merge
the two PRs together so that we also handle Energy handling within this update. Considering the code was already done
from before, this was relatively trivial and only needed a few changes to allow it to work with transactions. However, I
do understand that this is a sudden and rapid change, so If this needs to be split into 2 PRs again, I (Soaryn) don't
mind, but I would then like request that I can write that PR (given the code & tests are already done). There are likely
points to address now that it has transactions that may have broken the implementations, but with the tests running on
at least the item component, they seem to be fine thus far!

To be clear, the mistake that Adrian had made was that he missed a few files when splitting back out the energy changes
he was working on, since I believe the original request was to minimize breaking changes. Up until this week, we had no
idea they were missing. Since this PR has already become a relatively abrupt and obvious breaking change, it doesn't
make as much sense now, so I agree with Orion we should proceed with making all the handler capabilities as consistent
as we can sooner rather than later. This will ideally minimize the number of breaking change sync points people would
need to go through and handle them all in one go as many claim they advise to do when it comes to migrating from one MC
version to another.

## Old Design

The original `IEnergyStorage` was designed by KingLemming around Minecraft 1.2.5, while this worked exceptionally well
for a decade (not exaggerating the time), some things have either felt inconsistent with the other capabilities and out
of date or not quite solving the problems we face today. To be clear before moving on, what Lemming made worked
exceptionally well, and he did a fantastic job! We would just like to provide a work flow that hopes to align your uses
together rather than having 3 separate unique handler systems that work slightly differently from the next.

As an example for what we could do:

```java
import net.neoforged.neoforge.energy.IEnergyStorage;

public static void energyStorageDemonstration(IEnergyStorage handler) {
    //Let's assume you have some energy handler already
    //to extract from it, we'd call extractEnergy with some amount and indicate if we want to not commit those changes
    var extracted = handler.extractEnergy(100, false);

    //to fill similarily we call receiveEnergy
    var inserted = handler.receiveEnergy(100, false);
}
```

While these methods fundamentally work, they have some notable points that could be improved. First the naming doesn't
necessarily need to have `Energy` in the method name anymore given these are no longer expected necessarily to exist on
the block entity implementation itself. This was something that was "how it was" up until 1.20.4 when neo had
Technician's rework of the capability system. Because of this, we should be able to call these now `extract` and
`insert` respectively.

The next point that this PR now introduces, is the idea of transactions. In the case of doing an inquiry of "do you have
this amount?" as a simulated check then extracting that amount if successful, this only really works with the single
question. But what if we want to do a slightly more involved question of, "if I take some amount out and send some
amount back, how
much would fit?". Because we can't predict what the handler would do on extraction + insertion, we also can't assume we'
d get correct information. We can solve this with a transaction chain as explained below.

## New Design

Based heavily on the Resource handler work already done, this essentially takes the `IResourceHandler`
and removes the `IResource` constrained parameter taking a single distinct method if overlapping. This also like
`IResourceHandler` makes use of `Transactions` which allow a more involved and complex inquiry chains to occur while
providing a way to effectively "undo" any actions performed.

In essence, this means your Resource handling and Energy handling will appear the same, and be consistent in use and
their returns. The main focused difference is that you are only mutating an `int` on an energy handler.

```java

import net.neoforged.neoforge.transfer.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;

public static void energyHandlerDemonstration(IEnergyHandler handler) {
    //Let's assume you have some energy handler already
    //to extract from it, we'd call extract with some amount and indicate if we want to not commit those changes
    // Since we have no other transaction running, we shall open one with ROOT (or null in reality) as the parent.
    try (var transaction = TransactionManager.open(TransactionContext.ROOT)) {
        // first we simulate an extract
        var extracted = handler.extract(100, transaction);
        // We can do some intermediate actions such as finding out how much energy is now in the handler
        var currentAmount = EnergyHandlerUtil.getAmount(handler);
        // insert energy back into 
        var inserted = handler.insert(100, transaction);
        // and if we had (for some arbitrary reason) been able to return at least 50 energy, we want to commit our changes. 
        if (inserted >= 50) {
            //without this line or having inserted over 50, then all of our previous steps would be expected to be reverted.
            transaction.commit();
        }
        //if we committed, leaving this try-block will keep all values that were set;
        //if we didn't commit, all values will be reverted to their last "valid" state or snapshot. 
        // These snapshots are expected to be handled by the handler itself.
    }
}
```

New to this design though is the concept of being able to have an indexable amount. So while most handlers will use
`ISingleEnergyHandler` the root interface actually has the ability to specify per slot uses and extractions should a
consumer desire this. This, while niche, allows mods to be able to provide access to specific energy buffers within
their handler falling in-line with the `IResourceHandler`. Any modder not wanting to use the index variants do have
options around this such as the `ISingleEnergyHandler` and utility methods in `EnergyHandlerUtil` to minimize the impact
for them. There should be no perceptual change if the desire is to use what "was", outside either the names or perhaps
where the method is.

## Conclusion

Hopefully, with the new system, this both allows and empowers your ideas to be able to create what you are wanting to
do, whether it is to take an `IEnergyHandler` and insert or extract energy, or even do some more advanced things like
doing a round-robin insert of power to each sub buffer. To see a full breakdown of a migration path for each method, see
the breakdown table (At the time of writing this that file is called `energy_migration.md` in the same package)

## Addtional note

There are some cases where you don't want to go through all the pizazz of setting up a transaction that you are just
going to commit anyway, so the current option is to wrap it in an `SimpleEnergyHandler`; however, I am also leaning 
towards the idea of having this also be something in the energy utils.
