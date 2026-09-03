package dev.bedix.broomvroom.mixin.client;

import dev.bedix.broomvroom.broom.BroomEntity;
import dev.bedix.broomvroom.client.BroomRideAccess;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	@Inject(
			method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
			at = @At("TAIL")
	)
	private void broomvroom$captureBroomRide(
			LivingEntity entity,
			LivingEntityRenderState state,
			float tickProgress,
			CallbackInfo ci
	) {
		if (state instanceof BroomRideAccess access) {
			if (entity.getVehicle() instanceof BroomEntity broom) {
				float yaw = broom.getVisualYaw(tickProgress);
				access.broomvroom$setRidingBroom(true);
				access.broomvroom$setBroomYaw(yaw);
				access.broomvroom$setBroomPitch(broom.getVisualPitch(tickProgress));
				access.broomvroom$setBroomRoll(broom.getRoll(tickProgress));
				state.bodyRot = yaw;
				state.yRot = 0.0f;
			} else {
				access.broomvroom$setRidingBroom(false);
				access.broomvroom$setBroomYaw(0.0f);
				access.broomvroom$setBroomPitch(0.0f);
				access.broomvroom$setBroomRoll(0.0f);
			}
		}
	}
}
