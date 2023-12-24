package net.neoforged.neoforge.debug.entity;

import io.netty.buffer.Unpooled;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = EntityDataSerializerTest.GROUP)
public class EntityDataSerializerTest {
    public static final String GROUP = "level.entity.data_serializer";

    private static DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Byte>> testSerializer;

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if custom EntityDataSerializers are properly handled")
    static void customEntityDataSerializer(final DynamicTest test, final RegistrationHelper reg) {
        var testEntity = reg.entityTypes().registerType("serializer_test_entity", () -> EntityType.Builder.of(TestEntity::new, MobCategory.CREATURE)
                .sized(1, 1)).withRenderer(() -> TestEntityRenderer::new);

        var serializerReg = reg.registrar(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS);
        testSerializer = serializerReg.register("test_serializer", () -> EntityDataSerializer.simple((buf, b) -> buf.writeByte(b), FriendlyByteBuf::readByte));

        test.onGameTest(helper -> {
            var entity = helper.spawn(testEntity.get(), 1, 1, 1);
            var items = entity.getEntityData().packDirty();
            if (items == null) {
                helper.fail("Expected dirty entity data, got none");
                return;
            }
            var pkt = new ClientboundSetEntityDataPacket(entity.getId(), items);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            pkt.write(buf);
            helper.assertTrue(buf.readVarInt() == entity.getId(), "Entity ID didn't match"); // Drop entity ID
            buf.readByte(); // Drop item ID
            int expectedId = NeoForgeRegistries.ENTITY_DATA_SERIALIZERS.getId(testSerializer.get()) + CommonHooks.VANILLA_SERIALIZER_LIMIT;
            helper.assertTrue(buf.readVarInt() == expectedId, "Serializer ID didn't match");
            buf.readByte(); // Drop data
            buf.readByte(); // Drop EOF marker
            helper.assertTrue(buf.readableBytes() == 0, "Buffer not empty");

            helper.succeed();
        });
    }

    private static class TestEntity extends Entity {
        private static final EntityDataAccessor<Byte> DATA_TEST_VALUE = SynchedEntityData.defineId(TestEntity.class, testSerializer.value());

        public TestEntity(EntityType<? extends Entity> entityType, Level level) {
            super(entityType, level);
            entityData.set(DATA_TEST_VALUE, (byte) 1);
        }

        @Override
        protected void defineSynchedData() {
            entityData.define(DATA_TEST_VALUE, (byte) 0);
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag tag) {}

        @Override
        protected void addAdditionalSaveData(CompoundTag tag) {}
    }

    private static class TestEntityRenderer extends EntityRenderer<TestEntity> {
        public TestEntityRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(TestEntity entity) {
            return null;
        }
    }
}
