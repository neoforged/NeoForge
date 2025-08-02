package net.neoforged.neodev.extensionvalidation;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Parses the {@link CheckExtensions.MethodDesc} from an {@code @MethodDesc} annotation specifying the original method
 * or the exclusions in an {@code @ExtensionMethod} annotation. Certain parameters are automatically filled in if
 * omitted from the annotation:
 * <ul>
 *     <li>
 *         If the method name is unspecified, assume the method name matches the name of the method annotated with the
 *         {@code ExtensionMethod} containing the {@code MethodDesc} being visited and rely on later presence validation
 *     </li>
 *     <li>
 *         If the method descriptor is unspecified, assume the owning class only has one method with the specified
 *         (or assumed, see above) name and rely on {@link ResolvingVisitor} to fill it in
 *     </li>
 * </ul>
 */
final class MethodDescAnnotationVisitor extends AnnotationVisitor {
    private final String extMthName;
    private final Consumer<CheckExtensions.MethodDesc> descConsumer;
    private String owner;
    private String mthName;
    private final List<Type> descriptor = new ArrayList<>();

    MethodDescAnnotationVisitor(int api, String extMthName, Consumer<CheckExtensions.MethodDesc> descConsumer) {
        super(api);
        this.extMthName = extMthName;
        this.descConsumer = descConsumer;
    }

    @Override
    public void visit(String name, Object value) {
        switch (name) {
            case "owner" -> owner = ((Type) value).getInternalName();
            case "name" -> mthName = (String) value;
        }
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        if (!name.equals("descriptor")) {
            return null;
        }

        return new AnnotationVisitor(api) {
            @Override
            public void visit(String name, Object value) {
                descriptor.add((Type) value);
            }
        };
    }

    @Override
    public void visitEnd() {
        if (owner != null) {
            if (mthName == null) {
                mthName = extMthName;
            }
            String descriptorString = null;
            if (!descriptor.isEmpty()) {
                StringBuilder builder = new StringBuilder("(");
                List<Type> types = this.descriptor;
                for (int i = 1; i < types.size(); i++) {
                    builder.append(types.get(i).getDescriptor());
                }
                builder.append(")").append(descriptor.getFirst().getDescriptor());
                descriptorString = builder.toString();
            }
            descConsumer.accept(new CheckExtensions.MethodDesc(owner, mthName, descriptorString));
        }
    }

    static final class ArrayVisitor extends AnnotationVisitor {
        private final String extMthName;
        private final Consumer<CheckExtensions.MethodDesc> descConsumer;

        ArrayVisitor(int api, String extMthName, Consumer<CheckExtensions.MethodDesc> descConsumer) {
            super(api);
            this.extMthName = extMthName;
            this.descConsumer = descConsumer;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            if (name == null && descriptor.equals(CollectingVisitor.METHOD_DESC_ANNO)) {
                return new MethodDescAnnotationVisitor(api, extMthName, descConsumer);
            }
            return null;
        }
    }
}
