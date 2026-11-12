package net.neoforged.neodev;

import com.google.gson.GsonBuilder;
import net.neoforged.neodev.utils.AsmUtils;
import net.neoforged.neodev.utils.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public abstract class GenerateSpawnPlacementWideningTargets extends DefaultTask {
    private static final String SPAWN_PREDICATE_DESCRIPTOR = "(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z";
    private static final String ALT_SPAWN_PREDICATE_DESCRIPTOR = "(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z";
    private static final String GENERIC_TYPE = "Lnet/minecraft/world/entity/EntityType;";
    private static final int ACCESS = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;

    @InputFile
    public abstract RegularFileProperty getInput();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    @TaskAction
    public void exec() throws IOException {
        var visitor = new Visitor();
        AsmUtils.visitAllClasses(getInput().getAsFile().get(), visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        if (!visitor.foundPredicate) {
            throw new IllegalStateException("Did not find SpawnPlacements.SpawnPredicate#test(), was it modified?");
        }

        FileUtils.writeStringSafe(
                getOutput().getAsFile().get().toPath(),
                new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(visitor.targets),
                StandardCharsets.UTF_8);
    }

    record Target(String methodName, String methodDescriptor, String parameterType) {
        Target(String methodName, String methodDescriptor) {
            this(methodName, methodDescriptor, GENERIC_TYPE);
        }
    }

    static class Visitor extends ClassVisitor {
        final Map<String, Target> targets = new TreeMap<>();
        String currentClass = null;
        boolean foundPredicate = false;

        protected Visitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            currentClass = name;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (currentClass.equals("net/minecraft/world/entity/SpawnPlacements$SpawnPredicate")) {
                if (name.equals("test") && descriptor.equals(SPAWN_PREDICATE_DESCRIPTOR)) {
                    foundPredicate = true;
                }
                return null;
            }
            if ((access & ACCESS) == ACCESS && !currentClass.equals("net/minecraft/world/entity/SpawnPlacements")) {
                // Some older classes use a slightly different descriptor
                if (descriptor.equals(SPAWN_PREDICATE_DESCRIPTOR) || descriptor.equals(ALT_SPAWN_PREDICATE_DESCRIPTOR)) {
                    // Exclude methods which already permit subclasses in the EntityType generics
                    if (!signature.startsWith("(Lnet/minecraft/world/entity/EntityType<+")) {
                        targets.put(currentClass, new Target(name, descriptor));
                    }
                }
            }
            return null;
        }
    }
}
