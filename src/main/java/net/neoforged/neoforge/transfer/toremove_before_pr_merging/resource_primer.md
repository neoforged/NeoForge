# Resource Primer

## Builtin Resources

Provided in Minecraft / Neo are two main stack types: `ItemStack` and `FluidStack`. Both of these encode some backing
instance, a set of data component mutations, and amount. However, the problem that we have faced with say `IItemHandler`
or the like, is that these structures are mutable. Meaning that when you tried to `insert` say a stack of apples into
the handler, there was no guarantee that the handler would properly not attempt to mutate the stack passed in. The
solution to this, is for us to make an immutable version of these. Thus `IResource` which for the case of items & fluids
contain a final backing stack of its relevant type that should otherwise not be accessed.

## Custom Resources

When wanting to make your own resource, there are a couple things you should ask first:

- Is my resource ever empty? If so I should identify "how or perhaps why?" so that you can return it if needed.
- Will I need several instances or is there a relatively limited amount? If not, then you might consider making it an enum.

As an example, let's say you were making an element-like resource. Assuming we want to handle holding "nothing" in some
cases, we will need to be able to handle `isEmpty`. My design takes me to wanting Fire, Water, Earth, and Air (because
perhaps I watch too much Avatar the Last Airbender).

Because of the low amount of instances, I could structure this like an enum:

```java
public enum ElementResource implements IResource, StringRepresentable {
    NONE("none"),
    FIRE("fire"),
    WATER("water"),
    EARTH("earth"),
    AIR("air");

    private final String elementName;

    ElementResource(String elementName) {
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

In this case, we give each one a name to make use of the existing `StringRepresentableCodec` as well as solve an
identity crisis problem should we ever want to reorder this list. (Enum ordinal have been the bane of most game devs
when they go to make this sort of design due to ordering, so be careful if you choose not to have some backing id beside
the order).

Since we are an `IResouce` we have to implement a method called `isEmpty` and in this case we can just check to see if
the instance is `NONE`.

The last thing you need to do is make a capability token for your resource handler on blocks/items/etc. (Probably best
to only register what you need). For example for a block you'd do something like:

```java
public static final BlockCapability<IResourceHandler<ElementResource>, @Nullable Direction> BLOCK = BlockCapability.createSided(create("fluid_handler"), IResourceHandler.asClass());
```

And there you go! You now have a new element resource that can be handled by anyone using
`IResourceHandler<ElementResource>`!

### Note

Due to the way `IResourceHandler` was designed, very little is intended to be
different between resource types when it comes to accessing them via the handlers. It is important that if you have some
form of special handling of your resource that you will need to be rather clear on your api/documentation when exposing
your resource to other mods. Given the new system allows a much simpler use of a generic resource, you will likely need
to be more aware when you want to impose limitations on handling specific resource. One that comes to mind is something
akin to "This resource is too dangerous to transport". Without the other mod knowing about that limitation, they could
inadvertently bypass it. This isn't really a new problem, but it does become simpler to introduce now.


