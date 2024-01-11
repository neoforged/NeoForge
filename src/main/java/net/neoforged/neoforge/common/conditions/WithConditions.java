/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.Validate;

public sealed class WithConditions<A> permits ConditionalObject {
    private final List<ICondition> conditions;
    private final A carrier;

    public WithConditions(A carrier, ICondition... conditions) {
        this(List.of(conditions), carrier);
    }

    public WithConditions(A carrier) {
        this(List.of(), carrier);
    }

    public WithConditions(List<ICondition> conditions, A carrier) {
        this.conditions = conditions;
        this.carrier = carrier;
    }

    public List<ICondition> conditions() {
        return this.conditions;
    }

    public A carrier() {
        return this.carrier;
    }

    public static <A> Builder<A> builder(A carrier) {
        return new Builder<A>().withCarrier(carrier);
    }

    public static class Builder<T> {
        protected final List<ICondition> conditions = new ArrayList<>();
        protected T carrier;

        public Builder<T> addCondition(ICondition... condition) {
            this.conditions.addAll(List.of(condition));
            return this;
        }

        public Builder<T> addCondition(Collection<ICondition> conditions) {
            this.conditions.addAll(conditions);
            return this;
        }

        public Builder<T> withCarrier(T carrier) {
            this.carrier = carrier;
            return this;
        }

        protected void validate() {
            Validate.notNull(this.carrier, "You need to supply a carrier to create a WithConditions");
            Validate.notEmpty(this.conditions, "You need to supply at least one condition to create a WithConditions");
        }

        public WithConditions<T> build() {
            validate();
            return new WithConditions<>(this.conditions, this.carrier);
        }
    }
}
