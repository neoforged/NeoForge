/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class RegistryBuilder<T>
{
    private static final int MAX_ID = Integer.MAX_VALUE - 1;

    private ResourceLocation registryName;
    private ResourceLocation optionalDefaultKey;
    private int minId = 0;
    private int maxId = MAX_ID;
    private List<IForgeRegistry.AddCallback<T>> addCallback = Lists.newArrayList();
    private List<IForgeRegistry.ClearCallback<T>> clearCallback = Lists.newArrayList();
    private List<IForgeRegistry.CreateCallback<T>> createCallback = Lists.newArrayList();
    private List<IForgeRegistry.ValidateCallback<T>> validateCallback = Lists.newArrayList();
    private List<IForgeRegistry.BakeCallback<T>> bakeCallback = Lists.newArrayList();
    private boolean saveToDisc = true;
    private boolean sync = true;
    private boolean allowOverrides = true;
    private boolean allowModifications = false;
    private boolean hasWrapper = false;
    private IForgeRegistry.MissingFactory<T> missingFactory;
    private Set<ResourceLocation> legacyNames = new HashSet<>();
    @Nullable
    private Function<T, Holder.Reference<T>> intrusiveHolderCallback = null;

    public RegistryBuilder<T> setName(ResourceLocation name)
    {
        this.registryName = name;
        return this;
    }

    public RegistryBuilder<T> setIDRange(int min, int max)
    {
        this.minId = Math.max(min, 0);
        this.maxId = Math.min(max, MAX_ID);
        return this;
    }

    public RegistryBuilder<T> setMaxID(int max)
    {
        return this.setIDRange(0, max);
    }

    public RegistryBuilder<T> setDefaultKey(ResourceLocation key)
    {
        this.optionalDefaultKey = key;
        return this;
    }

    @SuppressWarnings("unchecked")
    public RegistryBuilder<T> addCallback(Object inst)
    {
        if (inst instanceof IForgeRegistry.AddCallback)
            this.add((IForgeRegistry.AddCallback<T>)inst);
        if (inst instanceof IForgeRegistry.ClearCallback)
            this.add((IForgeRegistry.ClearCallback<T>)inst);
        if (inst instanceof IForgeRegistry.CreateCallback)
            this.add((IForgeRegistry.CreateCallback<T>)inst);
        if (inst instanceof IForgeRegistry.ValidateCallback)
            this.add((IForgeRegistry.ValidateCallback<T>)inst);
        if (inst instanceof IForgeRegistry.BakeCallback)
            this.add((IForgeRegistry.BakeCallback<T>)inst);
        if (inst instanceof IForgeRegistry.MissingFactory)
            this.set((IForgeRegistry.MissingFactory<T>)inst);
        return this;
    }

    public RegistryBuilder<T> add(IForgeRegistry.AddCallback<T> add)
    {
        this.addCallback.add(add);
        return this;
    }

    public RegistryBuilder<T> onAdd(IForgeRegistry.AddCallback<T> add)
    {
        return this.add(add);
    }

    public RegistryBuilder<T> add(IForgeRegistry.ClearCallback<T> clear)
    {
        this.clearCallback.add(clear);
        return this;
    }

    public RegistryBuilder<T> onClear(IForgeRegistry.ClearCallback<T> clear)
    {
        return this.add(clear);
    }

    public RegistryBuilder<T> add(IForgeRegistry.CreateCallback<T> create)
    {
        this.createCallback.add(create);
        return this;
    }

    public RegistryBuilder<T> onCreate(IForgeRegistry.CreateCallback<T> create)
    {
        return this.add(create);
    }

    public RegistryBuilder<T> add(IForgeRegistry.ValidateCallback<T> validate)
    {
        this.validateCallback.add(validate);
        return this;
    }

    public RegistryBuilder<T> onValidate(IForgeRegistry.ValidateCallback<T> validate)
    {
        return this.add(validate);
    }

    public RegistryBuilder<T> add(IForgeRegistry.BakeCallback<T> bake)
    {
        this.bakeCallback.add(bake);
        return this;
    }

    public RegistryBuilder<T> onBake(IForgeRegistry.BakeCallback<T> bake)
    {
        return this.add(bake);
    }

    public RegistryBuilder<T> set(IForgeRegistry.MissingFactory<T> missing)
    {
        this.missingFactory = missing;
        return this;
    }

    public RegistryBuilder<T> missing(IForgeRegistry.MissingFactory<T> missing)
    {
        return this.set(missing);
    }

    public RegistryBuilder<T> disableSaving()
    {
        this.saveToDisc = false;
        return this;
    }

    /**
     * Prevents the registry from being synced to clients.
     *
     * @return this
     */
    public RegistryBuilder<T> disableSync()
    {
        this.sync = false;
        return this;
    }

    public RegistryBuilder<T> disableOverrides()
    {
        this.allowOverrides = false;
        return this;
    }

    public RegistryBuilder<T> allowModification()
    {
        this.allowModifications = true;
        return this;
    }

    RegistryBuilder<T> hasWrapper()
    {
        this.hasWrapper = true;
        return this;
    }

    public RegistryBuilder<T> legacyName(String name)
    {
        return legacyName(new ResourceLocation(name));
    }

    public RegistryBuilder<T> legacyName(ResourceLocation name)
    {
        this.legacyNames.add(name);
        return this;
    }

    RegistryBuilder<T> intrusiveHolderCallback(Function<T, Holder.Reference<T>> intrusiveHolderCallback)
    {
        this.intrusiveHolderCallback = intrusiveHolderCallback;
        return this;
    }

    /**
     * Enables tags for this registry if not already.
     * All forge registries with wrappers inherently support tags.
     *
     * @return this builder
     * @see RegistryBuilder#hasWrapper()
     */
    public RegistryBuilder<T> hasTags()
    {
        // Tag system heavily relies on Registry<?> objects, so we need a wrapper for this registry to take advantage
        this.hasWrapper();
        return this;
    }

    /**
     * Modders: Use {@link NewRegistryEvent#create(RegistryBuilder)} instead
     */
    IForgeRegistry<T> create()
    {
        if (hasWrapper)
        {
            if (getDefault() == null)
                addCallback(new NamespacedWrapper.Factory<T>());
            else
                addCallback(new NamespacedDefaultedWrapper.Factory<T>());
        }
        return RegistryManager.ACTIVE.createRegistry(registryName, this);
    }

    @Nullable
    public IForgeRegistry.AddCallback<T> getAdd()
    {
        if (addCallback.isEmpty())
            return null;
        if (addCallback.size() == 1)
            return addCallback.get(0);

        return (owner, stage, id, key, obj, old) ->
        {
            for (IForgeRegistry.AddCallback<T> cb : this.addCallback)
                cb.onAdd(owner, stage, id, key, obj, old);
        };
    }

    @Nullable
    public IForgeRegistry.ClearCallback<T> getClear()
    {
        if (clearCallback.isEmpty())
            return null;
        if (clearCallback.size() == 1)
            return clearCallback.get(0);

        return (owner, stage) ->
        {
            for (IForgeRegistry.ClearCallback<T> cb : this.clearCallback)
                cb.onClear(owner, stage);
        };
    }

    @Nullable
    public IForgeRegistry.CreateCallback<T> getCreate()
    {
        if (createCallback.isEmpty())
            return null;
        if (createCallback.size() == 1)
            return createCallback.get(0);

        return (owner, stage) ->
        {
            for (IForgeRegistry.CreateCallback<T> cb : this.createCallback)
                cb.onCreate(owner, stage);
        };
    }

    @Nullable
    public IForgeRegistry.ValidateCallback<T> getValidate()
    {
        if (validateCallback.isEmpty())
            return null;
        if (validateCallback.size() == 1)
            return validateCallback.get(0);

        return (owner, stage, id, key, obj) ->
        {
            for (IForgeRegistry.ValidateCallback<T> cb : this.validateCallback)
                cb.onValidate(owner, stage, id, key, obj);
        };
    }

    @Nullable
    public IForgeRegistry.BakeCallback<T> getBake()
    {
        if (bakeCallback.isEmpty())
            return null;
        if (bakeCallback.size() == 1)
            return bakeCallback.get(0);

        return (owner, stage) ->
        {
            for (IForgeRegistry.BakeCallback<T> cb : this.bakeCallback)
                cb.onBake(owner, stage);
        };
    }

    @Nullable
    public ResourceLocation getDefault()
    {
        return this.optionalDefaultKey;
    }

    public int getMinId()
    {
        return minId;
    }

    public int getMaxId()
    {
        return maxId;
    }

    public boolean getAllowOverrides()
    {
        return allowOverrides;
    }

    public boolean getAllowModifications()
    {
        return allowModifications;
    }

    @Nullable
    public IForgeRegistry.MissingFactory<T> getMissingFactory()
    {
        return missingFactory;
    }

    public boolean getSaveToDisc()
    {
        return saveToDisc;
    }

    public boolean getSync()
    {
        return sync;
    }

    public Set<ResourceLocation> getLegacyNames()
    {
        return legacyNames;
    }

    Function<T, Holder.Reference<T>> getIntrusiveHolderCallback()
    {
        return this.intrusiveHolderCallback;
    }

    boolean getHasWrapper()
    {
        return this.hasWrapper;
    }
}
