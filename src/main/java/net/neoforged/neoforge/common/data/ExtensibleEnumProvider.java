/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Primitives;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.fml.common.asm.enumextension.ReservedConstructor;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

public abstract class ExtensibleEnumProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final PackOutput packOutput;
    private final String modId;
    private final List<EnumEntryBuilder<?>> builders = new ArrayList<>();

    public ExtensibleEnumProvider(PackOutput packOutput, String modId) {
        this.packOutput = packOutput;
        this.modId = modId;
    }

    protected abstract void addEnumExtensions();

    protected final <T extends Enum<T> & IExtensibleEnum> EnumEntryBuilder<T> builder(Class<T> enumClass) {
        EnumEntryBuilder<T> builder = new EnumEntryBuilder<>(modId, enumClass);
        builders.add(builder);
        return builder;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput output) {
        addEnumExtensions();
        if (!builders.isEmpty()) {
            JsonObject json = new JsonObject();
            json.addProperty("modid", modId);
            JsonArray entryArr = new JsonArray(builders.size());
            for (EnumEntryBuilder<?> builder : builders) {
                entryArr.add(builder.build());
            }
            json.add("entries", entryArr);
            return DataProvider.saveStable(
                    output,
                    json,
                    packOutput.getOutputFolder().resolve("META-INF").resolve("enumextender.json"));
        }
        return CompletableFuture.allOf();
    }

    @Override
    public String getName() {
        return "enum_extensions";
    }

    public static class EnumEntryBuilder<T extends Enum<T> & IExtensibleEnum> {
        private static final Set<AccessFlag> FLAGS = Set.of(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL);

        private final String modId;
        private final Class<T> enumClass;
        private String fieldName;
        private Class<?>[] ctorParamTypes;
        private String ctorDescriptor;
        private List<Object> constParams;
        private Pair<Class<?>, String> lazyParams;

        private EnumEntryBuilder(String modId, Class<T> enumClass) {
            this.modId = modId;
            this.enumClass = enumClass;
        }

        public EnumEntryBuilder<T> fieldName(String fieldName) {
            if (!fieldName.toLowerCase(Locale.ROOT).startsWith(modId)) {
                fieldName = modId + "_" + fieldName;
                LOGGER.warn("Unprefixed enum field '{}' automatically prefixed with mod ID '{}'", fieldName, modId);
            }
            this.fieldName = fieldName;
            return this;
        }

        public EnumEntryBuilder<T> constructor(Class<?>... paramTypes) {
            Class<?>[] realParamTypes = new Class<?>[2 + paramTypes.length];
            realParamTypes[0] = String.class;
            realParamTypes[1] = int.class;
            System.arraycopy(paramTypes, 0, realParamTypes, 2, paramTypes.length);
            Constructor<?> ctor = ObfuscationReflectionHelper.findConstructor(enumClass, realParamTypes);
            if (ctor == null || ctor.isAnnotationPresent(ReservedConstructor.class)) {
                throw new IllegalArgumentException(String.format(
                        Locale.ROOT,
                        "Cannot locate valid constructor in enum '%s' with parameters '%s'",
                        enumClass.getName(),
                        Arrays.toString(paramTypes)));
            }
            this.ctorParamTypes = paramTypes;
            this.ctorDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Arrays.stream(paramTypes).map(Type::getType).toArray(Type[]::new));
            return this;
        }

        public EnumEntryBuilder<T> constParameters(Object... parameters) {
            Preconditions.checkState(lazyParams == null, "Cannot use both constant and lazy parameters");
            this.constParams = List.of(parameters);
            return this;
        }

        public EnumEntryBuilder<T> lazyParameters(Class<?> srcClass, String fieldName) {
            Preconditions.checkState(constParams == null, "Cannot use both constant and lazy parameters");
            Field field = ObfuscationReflectionHelper.findField(srcClass, fieldName);
            if (field == null) {
                throw new IllegalArgumentException(String.format(
                        Locale.ROOT,
                        "Cannot locate field '%s' in class '%s'",
                        fieldName,
                        srcClass.getName()));
            }
            if (!field.accessFlags().containsAll(FLAGS)) {
                throw new IllegalArgumentException(String.format(
                        Locale.ROOT,
                        "Parameter field '%s' in class '%s' must be public static final",
                        fieldName,
                        srcClass.getName()));
            }
            if (!List.class.isAssignableFrom(field.getType())) {
                throw new IllegalArgumentException(String.format(
                        Locale.ROOT,
                        "Parameter field '%s' in class '%s' must be of type List<Object>",
                        fieldName,
                        srcClass.getName()));
            }
            this.lazyParams = Pair.of(srcClass, fieldName);
            return this;
        }

        JsonObject build() {
            Preconditions.checkNotNull(fieldName, "Enum field name not set");
            Preconditions.checkNotNull(ctorDescriptor, "Constructor not set");
            Preconditions.checkState(constParams != null || lazyParams != null, "Parameters not set");

            JsonObject obj = new JsonObject();
            obj.addProperty("enum", enumClass.getName().replace('.', '/'));
            obj.addProperty("name", fieldName);
            obj.addProperty("constructor", ctorDescriptor);

            JsonElement params;
            if (constParams != null) {
                params = buildConstantParameters(ctorParamTypes, constParams);
            } else {
                JsonObject paramObj = new JsonObject();
                paramObj.addProperty("class", lazyParams.getFirst().getName().replace('.', '/'));
                paramObj.addProperty("field", lazyParams.getSecond());
                params = paramObj;
            }
            obj.add("parameters", params);

            return obj;
        }

        private static JsonArray buildConstantParameters(Class<?>[] ctorParamTypes, List<Object> constParams) {
            JsonArray paramArr = new JsonArray();
            for (int idx = 0; idx < constParams.size(); idx++) {
                Object param = constParams.get(idx);

                Class<?> type = ctorParamTypes[idx];
                boolean valid = true;
                if (type.isPrimitive()) {
                    if (param == null || !Primitives.wrap(type).isInstance(param)) {
                        valid = false;
                    }
                } else {
                    if (param != null && !type.isInstance(param)) {
                        valid = false;
                    }
                }
                if (!valid) {
                    throw new IllegalArgumentException(String.format(
                            Locale.ROOT,
                            "Type of parameter '%s' does not match expected type '%s'",
                            param,
                            type.getName()));
                }

                switch (param) {
                    case String string -> paramArr.add(string);
                    case Character ch -> paramArr.add(ch);
                    case Byte b -> paramArr.add(b);
                    case Short s -> paramArr.add(s);
                    case Integer i -> paramArr.add(i);
                    case Long l -> paramArr.add(l);
                    case Float f -> paramArr.add(f);
                    case Double d -> paramArr.add(d);
                    case Boolean bool -> paramArr.add(bool);
                    case null -> paramArr.add(JsonNull.INSTANCE);
                    default -> throw new IllegalArgumentException("Unsupported constant parameter: " + param);
                }
            }
            return paramArr;
        }
    }
}
