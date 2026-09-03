package dev.bedix.broomvroom;

import dev.bedix.broomvroom.broom.BroomEntity;
import dev.bedix.broomvroom.broom.ModEntityTypes;
import dev.bedix.broomvroom.broom.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.damagesource.DamageTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Broomvroom implements ModInitializer {
    public static final String MOD_ID = "broomvroom";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity.getVehicle() instanceof BroomEntity)) {
                return true;
            }
            return !source.is(DamageTypes.FALL) && !source.is(DamageTypes.FLY_INTO_WALL);
        });

        ModEntityTypes.initialize();
        ModItems.initialize();
    }
}
