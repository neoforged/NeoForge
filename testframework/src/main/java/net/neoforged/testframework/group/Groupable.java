package net.neoforged.testframework.group;

import net.neoforged.testframework.Test;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
public interface Groupable {
    /**
     * Resolves all tests in this groupable element.
     * @return all tests
     */
    default List<Test> resolveAll() {
        return resolveAsStream().toList();
    }

    /**
     * Resolves all tests in this groupable element as a {@link Stream}.
     * @return all tests as a stream
     */
    Stream<Test> resolveAsStream();
}
