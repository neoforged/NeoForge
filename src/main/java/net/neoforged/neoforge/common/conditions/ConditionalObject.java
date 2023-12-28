/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ConditionalObject<A> extends WithConditions<A> {
    private final List<WithConditions<A>> alternatives;

    public ConditionalObject(List<ICondition> conditions, A carrier, List<WithConditions<A>> alternatives) {
        super(conditions, carrier);
        this.alternatives = alternatives;

        Validate.isTrue(alternatives.stream().noneMatch(ConditionalObject.class::isInstance), "Alternatives cannot have alternatives");
    }

    @SafeVarargs
    public ConditionalObject(List<ICondition> conditions, A carrier, WithConditions<A>... alternatives) {
        this(conditions, carrier, List.of(alternatives));
    }

    public ConditionalObject(A carrier) {
        this(List.of(), carrier, List.of());
    }

    public List<WithConditions<A>> alternatives() {
        return alternatives;
    }

    public static <A> Builder<A> builder(A carrier) {
        return new Builder<A>().withCarrier(carrier);
    }

    public static class Builder<T> extends WithConditions.Builder<T> {
        private final List<WithConditions<T>> alternatives = new ArrayList<>();

        @Override
        public Builder<T> addCondition(ICondition... condition) {
            return (Builder<T>) super.addCondition(condition);
        }

        @Override
        public Builder<T> addCondition(Collection<ICondition> conditions) {
            return (Builder<T>) super.addCondition(conditions);
        }

        @Override
        public Builder<T> withCarrier(T carrier) {
            return (Builder<T>) super.withCarrier(carrier);
        }

        public Builder<T> addAlternative(T alternative, ICondition... conditions) {
            this.alternatives.add(new WithConditions<>(alternative, conditions));
            return this;
        }

        @Override
        public ConditionalObject<T> build() {
            validate();
            return new ConditionalObject<>(conditions, carrier, alternatives);
        }
    }
}
