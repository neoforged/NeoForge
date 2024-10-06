/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.coremods;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

final class CoremodUtils {
    private static final Gson GSON = new Gson();

    CoremodUtils() {}

    static <T> T loadResource(String filename, TypeToken<T> type) {
        var stream = NeoForgeCoreMod.class.getResourceAsStream(filename);
        if (stream == null) {
            throw new IllegalStateException("Missing resource: " + filename);
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, type);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read JSON resource " + filename);
        }
    }

    static <T> T loadResource(String filename, Class<T> type) {
        return loadResource(filename, TypeToken.get(type));
    }

    static FieldNode getFieldByName(ClassNode classNode, String fieldName) {
        FieldNode foundField = null;
        for (var fieldNode : classNode.fields) {
            if (Objects.equals(fieldNode.name, fieldName)) {
                if (foundField == null) {
                    foundField = fieldNode;
                } else {
                    throw new IllegalStateException("Found multiple fields with name " + fieldName + " in " + classNode.name);
                }
            }
        }
        if (foundField == null) {
            throw new IllegalStateException("No field with name " + fieldName + " found in class " + classNode.name);
        }
        return foundField;
    }

    static MethodNode getMethodByDescriptor(ClassNode classNode, @Nullable String methodName, String methodSignature) {
        MethodNode foundMethod = null;
        for (var methodNode : classNode.methods) {
            if (Objects.equals(methodNode.desc, methodSignature)
                    && (methodName == null || Objects.equals(methodNode.name, methodName))) {
                if (foundMethod == null) {
                    foundMethod = methodNode;
                } else {
                    throw new IllegalStateException("Found duplicate method with signature " + methodSignature + " in " + classNode.name);
                }
            }
        }

        if (foundMethod == null) {
            if (methodName != null) {
                throw new IllegalStateException("Unable to find method " + methodSignature + " with name " + methodName + " in " + classNode.name);
            } else {
                throw new IllegalStateException("Unable to find method " + methodSignature + " in " + classNode.name);
            }
        }
        return foundMethod;
    }
}
