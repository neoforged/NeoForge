package net.neoforged.neodev.utils.structure;

import org.gradle.api.Named;

public record FieldInfo(String name, ClassInfo type, int access) implements Named {
    @Override
    public String getName() {
        return name;
    }
}
