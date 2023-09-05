/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.conditions;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record WithConditions<A>(List<ICondition> conditions, A carrier) {
   
   public static class Builder<T> {
      private final List<ICondition> conditions = new ArrayList<>();
      private T carrier;
      
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
      
      public WithConditions<T> build() {
         Validate.notNull(this.carrier, "You need to supply a carrier to create a WithConditions");
         Validate.notEmpty(this.conditions, "You need to supply at least one condition to create a WithConditions");
         
         return new WithConditions<>(this.conditions, this.carrier);
      }
   }
}
