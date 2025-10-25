package net.neoforged.neodev.utils.structure;

import java.util.List;
import org.apache.commons.lang3.mutable.MutableInt;
import org.gradle.api.Named;

public record ClassInfo(String name, MutableInt access, List<ClassInfo> parents, List<MethodInfo> methods,
        List<FieldInfo> fields) implements Named {
    public void addMethod(String name, String desc, int access) {
        this.methods.add(new MethodInfo(name, desc, access));
    }

    public boolean hasSuperclass(String name) {
        for (ClassInfo parent : parents) {
            if (parent.hasSuperclass(name)) {
                return true;
            }
        }
        return this.name.equals(name);
    }

    @Override
    public String getName() {
        return name;
    }
}
