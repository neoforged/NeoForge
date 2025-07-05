# Slice proposals

Packages without the `*` are indicating the just files within that package.
Packages with the `*` indicate recursively include the folders (if any) as well.

Fundamentally, this can be split into four core parts: `Infrastructure`, `Wrappers`, `Templates`, and `Tests`.
In theory, the wrappers and templates would be the heaviest parts with the wrappers doing the more heavy changing to internals.
Both `Infrastructure` and `Wrappers` should be seen as mandatory since they provide the foundational layer and the vanilla 
implementations.

## Infrastructure

Required elements of the transfer system and the resources that support it. We provide the base implementations
(Infinite, Void, Empty) to allow a smoother transition. These are not expected to need much review nor changing, so providing them
in this slice is likely more ideal given we don't need to waste time with some of the deprecations made.

- New
  - `net.neoforged.neoforge.transfer` (NOT Fluid util or ItemUtil due to Capabilities needing to change) 
  - `net.neoforged.neoforge.transfer.resources.*` 
  - `net.neoforged.neoforge.transfer.transaction.*` 
  - `net.neoforged.neoforge.transfer.handlers` 
  - `net.neoforged.neoforge.transfer.handlers.energy.*`
  - `net.neoforged.neoforge.transfer.handlers.resource.*` 
  - `net.neoforged.neoforge.transfer.handlers.templates.templates.contexts.*`
  - `net.neoforged.neoforge.transfer.handlers.templates.energy.InfiniteEnergyHandler`
  - `net.neoforged.neoforge.transfer.handlers.templates.energy.VoidEnergyHandler`
  - `net.neoforged.neoforge.transfer.handlers.templates.energy.EmptyEnergyHandler`
  - `net.neoforged.neoforge.transfer.handlers.templates.resources.InfiniteResourceHandler`
  - `net.neoforged.neoforge.transfer.handlers.templates.resources.VoidResourceHandler`
  - `net.neoforged.neoforge.transfer.handlers.templates.resources.EmptyResourceHandler`
- Changed
  - `accesstransformer.cfg`
  - `net.neoforged.neoforge.client.color.item.FluidContentsTint`
  - `net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel`
  - `net.neoforged.neoforge.common.crafting.SizedIngredient`
  - `net.neoforged.neoforge.common.extensions.IItemStackExtension`
- Patches
  - `ItemContainerContents.java.patch`
  - `BucketItem.java.patch`
  - `Item.java.patch`
  - `Fluid.java.patch`

## Wrappers

Vanilla handlers wrapped into the new transfer system. This is where we should replace the old capabilities with our own. 
These, unlike the templates are not expected to be implemented by a user, but rather consumed.

- New
  - `net.neoforged.neoforge.transfer.ItemUtil`
  - `net.neoforged.neoforge.transfer.FluidUtil`
  - `net.neoforged.neoforge.transfer.handlers.wrappers.*`
  - `net.neoforged.neoforge.common.extensions.IContainerExtension`
- Changed
  - `net.neoforged.neoforge.items.*`
  - `net.neoforged.neoforge.fluids.*`
  - `net.neoforged.neoforge.energy.*`
  - `net.neoforged.neoforge.transfer.handlers.wrappers.items.ContainerOrHandler`
  - `net.neoforged.neoforge.capabilities.Capabilities`
  - `net.neoforged.neoforge.capabilities.CapabilityHooks`
  - `net.neoforged.neoforge.common.NeoForgeMod`
  - `injected-interfaces.json`
- Patches
  - `BaseContainerBlockEntity.java.patch`
  - `ChiseledBookShelfBlockEntity.java.patch`
  - `HopperBlockEntity.java.patch`
  - `JukeboxBlockEntity.java.patch`
  - `CrafterBlock.java.patch`
  - `DropperBlock.java.patch`

## Templates

The rest of the PR (or in this case, assumingly `net.neoforged.neoforge.transfer.handlers.templates`) 
Provided templates for users to be able to look at as an example of how to implement their own handlers.
These SHOULD NOT be the "solve everyone's" solutions. These are purely examples.

## Tests

All tests from the original PR. While we likely want to do this as we go, this is likely the best way to handle the slicing to
bypass wasting time on removing the overlapping aspects of some of the tests. This should be roughly `30 files` changed