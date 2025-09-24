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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This task is used to generate targets for the finalizeSpawn MethodRedirector coremod.
 */
public abstract class GenerateFinalizeSpawnTargets extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getInput();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    @TaskAction
    public void exec() throws IOException {
        var visitor = new Visitor();
        AsmUtils.visitAllClasses(
                getInput().getAsFile().get(),
                visitor,
                ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        var classList = List.copyOf(visitor.matchedClasses);

        FileUtils.writeStringSafe(
                getOutput().getAsFile().get().toPath(),
                new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(classList),
                StandardCharsets.UTF_8);
    }

    static class Visitor extends ClassVisitor {
        final Set<String> matchedClasses = new TreeSet<>();
        String currentClass = null;

        protected Visitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            currentClass = name;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            // Ignore these classes as we special case them
            if (currentClass.equals("net/minecraft/world/level/BaseSpawner")
                    || currentClass.equals("net/minecraft/world/level/block/entity/trialspawner/TrialSpawner")) {
                return null;
            }

            return new MethodVisitor(api) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    if (opcode == Opcodes.INVOKEVIRTUAL
                            && name.equals("finalizeSpawn")
                            && descriptor.equals("(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;")) {
                        matchedClasses.add(currentClass.replace('/', '.'));
                    }
                }
            };
        }
    }
}
