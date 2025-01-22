package net.neoforged.neodev.utils.structure;

import org.gradle.api.Named;

public record MethodInfo(String name, String descriptor, int access) implements Named {
    @Override
    public String getName() {
        return name;
    }
}
