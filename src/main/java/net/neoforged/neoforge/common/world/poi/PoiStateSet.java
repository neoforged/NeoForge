/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.poi;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class PoiStateSet implements Set<BlockState> {
	/** Contains possibly lazily-evaluated states. */
	private final Set<BlockState> backingSet;
	/** Actual elements are added to this through {@link #addCustomStates}. */
	private final Set<BlockState> ownElements = new ReferenceOpenHashSet<>();
	
	public PoiStateSet(Set<BlockState> states) {
		// `states` may contain lazily-evaluated elements, so don't make a defensive copy.
		this.backingSet = states;
	}
	
	@Override
	public int size() {
		return this.backingSet.size() + this.ownElements.size();
	}
	
	@Override
	public boolean isEmpty() {
		return this.backingSet.isEmpty() && this.ownElements.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return this.backingSet.contains(o) || this.ownElements.contains(o);
	}
	
	@SuppressWarnings("SuspiciousMethodCalls")
	@Override
	public boolean containsAll(Collection<?> c) {
		for (var el : c) {
			if (!this.backingSet.contains(el) && !this.ownElements.contains(el))
				return false;
		}
		return true;
	}
	
	@Override
	public Iterator<BlockState> iterator() {
		return Iterators.unmodifiableIterator(Iterators.concat(this.backingSet.iterator(), this.ownElements.iterator()));
	}
	
	private ArrayList<BlockState> toList() {
		ArrayList<BlockState> outList = new ArrayList<>(this.backingSet.size() + this.ownElements.size());
		outList.addAll(this.backingSet);
		outList.addAll(this.ownElements);
		return outList;
	}
	
	@Override
	public Object[] toArray() {
		return toList().toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return toList().toArray(a);
	}
	
	@Override
	public boolean add(BlockState state) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(Collection<? extends BlockState> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeIf(Predicate<? super BlockState> filter) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
	
	void addCustomStates(Set<BlockState> states) {
		this.backingSet.addAll(states);
	}
}

