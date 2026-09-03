package dev.bedix.broomvroom.broom;

import dev.bedix.broomvroom.Broomvroom;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;

public class ModEntityTypes {
    public static final EntityType<BroomEntity> BROOM = register(
            "broom",
            EntityType.Builder.<BroomEntity>of(BroomEntity::new, MobCategory.MISC)
                    .sized(1.0f, 0.5f)
                    .passengerAttachments(new Vec3(0.0, 0.18, 0.0))
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .noLootTable()
    );

    public static final EntityType<BroomHayEntity> BROOM_HAY = register(
            "broom_hay",
            EntityType.Builder.<BroomHayEntity>of(BroomHayEntity::new, MobCategory.MISC)
                    .sized(1.0f, 0.5f)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .noSummon()
                    .noSave()
                    .fireImmune()
                    .noLootTable()
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Broomvroom.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void initialize() {
        Broomvroom.LOGGER.info("Registered entity types for {}", Broomvroom.MOD_ID);
    }
}
