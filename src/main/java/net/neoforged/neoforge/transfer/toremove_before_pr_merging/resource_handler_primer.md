# IResourceHandler Primer

Note... this is still in progress, but Nano had asked if there was one to get a quicker to digest understanding of the
new system. I'll try to provide some examples, but this will be more polished later.

## Example

Rough non-new modder friendly code example: (This needs to be broken into smaller bitesized pieces, but it should be
enough to help get you started to peruse.)

```java

import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;

public static void demonstration(IResourceHandler<FluidResource> handler) {
    //To do any form of manipulation, we must first open a transaction
    // This is new, so there are likely to be growing pains! Don't get too frustrated if it doesn't click at first! :)
    //To do this, we open it with a try-block, this handles things also known as AutoCloseable
    try (var transaction = TransactionManager.open(TransactionContext.ROOT)) {
        //What we've done, is opened a transaction with the parent set as `root`. 
        // Some things to note:
        //      - TransactionContext.ROOT currently is just a fancy 'null'. 
        //        It helps with debugging when trying to isolate where you've opened roots
        //        while still keeping the habit of using a "parent" when opening a transaction
        //      - Only one transaction may be open on a particular parent at a time.
        //      - Unless you call transaction.commit(), nothing should be expected to change with what interactions you did.
        //        This is useful when you realize that what ever you wanted can't work after some chain of events, you can just
        //        revert everything you did without!

        //Let's try to insert some water since our transaction is open
        var inserted = handler.insert(FluidResource.of(Fluids.WATER), 100, transaction);
        // Something to remember that while we have called `insert` and passed the transaction we now have a state we can observe to see what has changed.
        // we can now decide, did our changes match what we desired, and if so deterine if we want to commit.
        // We can commit by simply calling
        transaction.commit();

        //Once we leave this block, the transaction will auto-close since we are in a try-block.
        // If we decided we are superior to syntactic sugar, you COULD handle the open and close yourself, but it is not recommended.
    }

    //Now the block is closed, our changes should be fully set. We can no longer rollback.

    // If we wanted to chain some transactions for a more involved query:
    // I'll summarize what this does after, but try to see if it is clear enough to you first
    try (var transaction = TransactionManager.open()) {
        var value = 0;
        try (var innerCheck = transaction.open()) {
            value = ResourceHandlerUtil.extract(handler, FluidResource.of(Fluids.WATER), innerCheck);
            innerCheck.commit();
        }
        if (value >= 100)
            transaction.commit();
    }


    // So assuming you tried understanding the example first, and didn't accidentally skip here,
    // This is intended to try to extract fluid, but ONLY succeed fully if the amount that was extracted was at least 100.
    // The inner check is always committed, but the since it is part of a chain, it has to also have the success from the outer scope as well. 
    // Without that, it would revert. `Transaction` has a breakdown in the java doc of how this works. 

}
```

## Snapshots

In order for us to support this new system, we actually need to start thinking about our data a little more carefully.
Before, when you wanted to change something you'd just call `insert(execute)`; however, because we now have the ability
to roll back, the supporting data also needs to support that rollback. In our case that comes in the form of a
`SnapshotJournal`. This allows you to create a snapshot of a given type for each "checkpoint" you need, and properly
handle either reverting the data, or commit the final results. There are a number of ways to do this, but for now it may
be easiest to look at either the `SetChangedSnapshot` for a "notification" of when something commits we can run a user
provided runnable action (An example would be like `blockentity::setChanged`). Though also as an option looking at the
various implementations that exist within the templates package. I'd like to have a more clear example here, just
haven't had the documentation time yet.

The current fastest way to understand how a snapshot is implemented, is from the `Transaction` package java docs and
probably the template implementations, but when you want to go use one you've created, you pretty much always
`snapshot.update(transaction)` just before you modify your data in the `insert` or `extract`




