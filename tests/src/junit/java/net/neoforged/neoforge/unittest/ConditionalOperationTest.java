package net.neoforged.neoforge.unittest;

import static net.neoforged.neoforge.common.conditions.ICondition.IContext.TAGS_INVALID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.RegistryOps;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.conditions.ConditionalOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class ConditionalOperationTest {
    private static final String MOD_ID = "conditional_operation_test";
    private static final RegistryOps<JsonElement> OPS = ConditionalOperation.getOps(JsonOps.INSTANCE, (RegistryOps.RegistryInfoLookup) null, () -> TAGS_INVALID);

    @Test
    void playground() {
        Outer.CODEC.parse(OPS, JsonParser.parseString("""
                {
                    "string": "yes",
                    "a": {
                        "neoforge:conditional_operation_type": "neoforge:alternative",
                        "conditions": [{"type": "neoforge:false"}],
                        "value": "owoforge",
                        "or_else": "neoforge"
                    },
                    inner: {
                        "neoforge:conditional_operation_type": "neoforge:alternative",
                        "value": {},
                        "or_else": "neoforge"
                    }
                }
                """));
    }

    @Test
    @Deprecated
    void testOldFormat() { // in order to not be a breaking change this needs to work
        JsonElement toParse = JsonParser.parseString("""
                {
                    "neoforge:conditions": [{"type": "neoforge:false"}],
                    "string": "boop",
                    "a": 5,
                    "inner": {
                        "list": [],
                        "doubloons": 3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223176
                    }
                }
                """);
        Outer.CODEC.parse(OPS, toParse)
                .mapError(Assertions::fail)
                .ifSuccess(Assertions::assertNull);
    }

    @Test
    void testAlternative() {
        JsonElement toParse = JsonParser.parseString("""
                {
                    "string": {
                        "neoforge:conditional_operation_type": "neoforge:alternative",
                        "conditions": [{"type": "neoforge:false"}],
                        "value": "owoforge",
                        "or_else": "neoforge"
                    },
                    "a": 143,
                    "inner": {
                        "list": ["fox","momentum","boop"],
                        "doubloons": 77
                    }
                }
                """);
        Outer expected = new Outer("neoforge", 143, new Inner(Optional.of(List.of("fox", "momentum", "boop")), 77));
        Outer.CODEC.parse(OPS, toParse)
                .mapError(Assertions::fail)
                .ifSuccess(result -> Assertions.assertEquals(expected, result));
    }

    @Test
    void testOrNull() {
        JsonElement toParse = JsonParser.parseString("""
                {
                    "string": "or null test",
                    "a": 2,
                    "inner": {
                        "list": {
                            "neoforge:conditional_operation_type": "neoforge:or_null",
                            "conditions": [{"type": "neoforge:false"}],
                            "value": ["oh no"]
                        },
                        "doubloons": -1.2
                    }
                }
                """);
        Outer expected = new Outer("or null test", 2, new Inner(Optional.empty(), -1.2));
        Outer.CODEC.parse(OPS, toParse)
                .mapError(Assertions::fail)
                .ifSuccess(result -> Assertions.assertEquals(expected, result));
    }

    @Test
    void testListElement() {
        JsonElement toParse = JsonParser.parseString("""
                {
                    "string": "this is a string",
                    "a": -1,
                    "inner": {
                        "list": [
                            "fox",
                            {
                                "neoforge:conditional_operation_type": "neoforge:alternative",
                                "conditions": [{"type": "neoforge:true"}],
                                "value": "run",
                                "or_else": "walk"
                            },
                            "boop"
                        ],
                        "doubloons": 7.7
                    }
                }
                """);
        Outer expected = new Outer("this is a string", -1, new Inner(Optional.of(List.of("fox", "run", "boop")), 7.7));
        Outer.CODEC.parse(OPS, toParse)
                .mapError(Assertions::fail)
                .ifSuccess(result -> Assertions.assertEquals(expected, result));
    }

    @Test
    void testConditionCausingError() {
        JsonElement toParse = JsonParser.parseString("""
                {
                    "string": "Witty comment unavailable :(",
                    "a": 0,
                    "inner": {
                        "list": {
                            "neoforge:conditional_operation_type": "neoforge:alternative",
                            "conditions": [{"type": "neoforge:false"}],
                            "value": ["You", "Once there was magic"],
                            "or_else": {"Me": "But I killed it."}
                        },
                        "doubloons": 28
                    }
                }
                """);
        var result = Outer.CODEC.parse(OPS, toParse); // TODO: replace with actual error message check
        Assertions.assertThrows(IllegalStateException.class, result::getOrThrow, "Expected parsing to fail due to an object in place of a list, but it succeeded!");
    }

    @Test
    void testBadConditionsParse() {
        JsonElement toParse = JsonParser.parseString("""
                {
                    "string": {
                        "neoforge:conditional_operation_type": "neoforge:alternative",
                        "value": "owoforge",
                        "or_else": "neoforge"
                    },
                    "a": 8,
                    "inner": {
                        "list": [],
                        "doubloons": 0
                    }
                }
                """);
        var result = Outer.CODEC.parse(OPS, toParse); // TODO: replace with actual error message check
        Assertions.assertThrows(IllegalStateException.class, result::getOrThrow, "Expected parsing to fail due to missing conditions field, but it succeeded!");
    }

    @Mod(MOD_ID)
    public static class ConditionalOperationTestMod {}

    record Outer(String string, int a, Inner inner) {
        public static final Codec<Outer> CODEC = RecordCodecBuilder.<Outer>mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("string").forGetter(Outer::string),
                Codec.INT.fieldOf("a").forGetter(Outer::a),
                Inner.CODEC.fieldOf("inner").forGetter(Outer::inner)).apply(instance, Outer::new)).codec();
    }

    record Inner(Optional<List<String>> list, double doubloons) {
        public static final Codec<Inner> CODEC = RecordCodecBuilder.<Inner>mapCodec(instance -> instance.group(
                Codec.STRING.listOf().optionalFieldOf("list").forGetter(Inner::list),
                Codec.DOUBLE.fieldOf("doubloons").forGetter(Inner::doubloons)).apply(instance, Inner::new)).codec();
    }
}
