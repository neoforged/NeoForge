package net.neoforged.neodev.utils;

import java.io.Serializable;
import java.util.function.Predicate;

@FunctionalInterface
public interface SerializablePredicate<T> extends Serializable, Predicate<T> {
    @Override
    boolean test(T value);
}
