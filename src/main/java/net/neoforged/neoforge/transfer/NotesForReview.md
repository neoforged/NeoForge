# Preamble

The idea is simple, we need the user to feel comfortable with the new system while providing a familiarity to what we
already have provided in the past. This is achievable by two core tenants:

- Familiar naming
- Intuitive logic

While abstraction can cause problems with readability at an initial glance, it is not expected for any dev to fully
digest the entire api, but rather a small sample set of it. Be aware of this when reviewing as while yes this does have
a fair amount of abstraction, there were some core areas that required it.

## Migrations

| From            | To                                |
|-----------------|-----------------------------------|
| `IItemHandler`  | `IResourceHandler<ItemResource>`  |
| `IFluidHandler` | `IResourceHandler<FluidResource>` |

## IResource

There was some discussion of whether or not we should have an `IResource`. At the end of the day, providing a type
constraint on the `IResourceHandler` proved to be more positive.

- `isEmpty` can properly be inquired on the resource and thus allows the creator of said resource type to control what
  is "empty" for their resource
- We need to worry less about type misuse given all IResourceHandlers will be handling a type of `IResource`.
- Documentation as well as navigation is more centralized and thus faster to follow by having a root interface. If
  utilizing an external mod API that only provides a resource, you can accurately determine what the resource of that
  mod is by looking at what all implements `IResource`.
- We alleviate misunderstandings when it comes to implementing as you cannot implement it as
  `IResourceHandler<ItemStack>`. This is not possible given `ItemStack` is not of type `IResource`
- `IResourceHandlers` become agnostic to the type allowing you to use them all the same if desired or needed. 

When implementing your own `IResource` you can either choose to make it a registered backing type like `ItemResource` or `FluidResource` or a complete custom solution like the examples in the tests.


## Templates

There are several different templates we can provide as well as the option to provide none at all.
- `ResourceContainer` - Designed originally for XyCraft. Only works as an attachment
- `ResourceStorageHandler` - Tries to match vanilla in terms of DataComponent use. Has both an attachment and component implementation for items and fluids.
- `Contexts` - The idea of ItemContexts is new, but may not be as valid with Transactions

The first notable optional templates are the resource containers such as `SimpleItemResourceContainer`.
As the documentation expresses, these were initially designed for XyCraft so they are a lot less theory crafted and
actually what is in production now just with `IItemHandler`. It is however, a lot to digest so while I don't mind them
not being part of neo, I do want to emphasize it is important we maintain the ability to be implemented this way.

Being able to call something like the following is incredible powerful when able to be resused this simply.

```java
SimpleFluidResourceContainer.from(fluids).
capacity(capacity).
onChange(this::markBlockEntityAsDirty).
build();
```
