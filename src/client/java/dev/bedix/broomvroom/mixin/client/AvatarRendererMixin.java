package dev.bedix.broomvroom.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.bedix.broomvroom.broom.BroomEntity;
import dev.bedix.broomvroom.client.BroomRideAccess;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	// LivingEntityRenderer does scale(-1,-1,1) then translate(0, -1.501, 0).
	// Hip joints are 12px up the standing mesh, so after that transform they sit
	// 0.751 above the entity origin (the feet). Pitch/roll must use that point
	// — the crotch on the broom — not the feet, or the legs swing through the stick.
	private static final float HIP_PIVOT_Y = 1.501f - (12.0f / 16.0f);
	@Inject(
			method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
			at = @At("TAIL")
	)
	private void broomvroom$lockToBroom(
			Avatar entity,
			AvatarRenderState state,
			float tickProgress,
			CallbackInfo ci
	) {
		if (!(state instanceof BroomRideAccess access)) {
			return;
		}
		if (entity.getVehicle() instanceof BroomEntity broom) {
			float yaw = broom.getVisualYaw(tickProgress);
			access.broomvroom$setRidingBroom(true);
			access.broomvroom$setBroomYaw(yaw);
			access.broomvroom$setBroomPitch(broom.getVisualPitch(tickProgress));
			access.broomvroom$setBroomRoll(broom.getRoll(tickProgress));
			state.bodyRot = yaw;
			state.yRot = 0.0f;
		}
	}

	@Inject(
			method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
			at = @At("TAIL")
	)
	private void broomvroom$alignWithBroom(
			AvatarRenderState state,
			PoseStack poseStack,
			float bodyRot,
			float scale,
			CallbackInfo ci
	) {
		if (!(state instanceof BroomRideAccess access) || !access.broomvroom$isRidingBroom()) {
			return;
		}
		poseStack.mulPose(Axis.YP.rotationDegrees(Mth.wrapDegrees(bodyRot - access.broomvroom$getBroomYaw())));
		poseStack.translate(0.0f, HIP_PIVOT_Y, 0.0f);
		poseStack.mulPose(Axis.XP.rotationDegrees(-access.broomvroom$getBroomPitch()));
		poseStack.mulPose(Axis.ZP.rotationDegrees(access.broomvroom$getBroomRoll()));
		poseStack.translate(0.0f, -HIP_PIVOT_Y, 0.0f);
	}
}
