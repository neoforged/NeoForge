/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import java.util.List;

public final class ConditionalObject<A> extends WithConditions<A> {
    private final List<WithConditions<A>> alternatives;

    public ConditionalObject(List<ICondition> conditions, A carrier, List<WithConditions<A>> alternatives) {
        super(conditions, carrier);
        this.alternatives = alternatives;
    }

    public ConditionalObject(List<ICondition> conditions, A carrier, WithConditions<A>... alternatives) {
        this(conditions, carrier, List.of(alternatives));
    }

    public ConditionalObject(A carrier) {
        this(List.of(), carrier, List.of());
    }

    public List<WithConditions<A>> alternatives() {
        return alternatives;
    }
}
