package dev.bedix.broomvroom;

import dev.bedix.broomvroom.broom.BroomCall;
import dev.bedix.broomvroom.broom.BroomEntity;
import dev.bedix.broomvroom.broom.BroomImpactPayload;
import dev.bedix.broomvroom.broom.CallBroomPayload;
import dev.bedix.broomvroom.broom.ModEntityTypes;
import dev.bedix.broomvroom.broom.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Broomvroom implements ModInitializer {
    public static final String MOD_ID = "broomvroom";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(CallBroomPayload.TYPE, CallBroomPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(BroomImpactPayload.TYPE, BroomImpactPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CallBroomPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> BroomCall.call(context.player()));
        });
        ServerPlayNetworking.registerGlobalReceiver(BroomImpactPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                if (!(player.getVehicle() instanceof BroomEntity)) {
                    return;
                }
                float damage = Mth.clamp(payload.damage(), 0.0f, 16.0f);
                if (damage >= 1.0f) {
                    player.hurt(player.damageSources().flyIntoWall(), damage);
                }
            });
        });

        ModEntityTypes.initialize();
        ModItems.initialize();
    }
}
