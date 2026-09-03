package dev.bedix.broomvroom.mixin.client;

import dev.bedix.broomvroom.client.BroomRideAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
	@Shadow
	@Final
	public ModelPart head;

	@Shadow
	@Final
	public ModelPart body;

	@Shadow
	@Final
	public ModelPart rightArm;

	@Shadow
	@Final
	public ModelPart leftArm;

	@Shadow
	@Final
	public ModelPart rightLeg;

	@Shadow
	@Final
	public ModelPart leftLeg;

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
	private void broomvroom$poseOnBroom(HumanoidRenderState state, CallbackInfo ci) {
		if (!(state instanceof BroomRideAccess access) || !access.broomvroom$isRidingBroom()) {
			return;
		}
		this.head.yRot = 0.0f;
		this.body.yRot = 0.0f;
		this.rightArm.xRot = -0.85f;
		this.rightArm.yRot = 0.0f;
		this.rightArm.zRot = 0.18f;
		this.leftArm.xRot = -0.85f;
		this.leftArm.yRot = 0.0f;
		this.leftArm.zRot = -0.18f;
		this.rightLeg.xRot = -0.785f;
		this.rightLeg.yRot = 0.35f;
		this.rightLeg.zRot = 0.0f;
		this.leftLeg.xRot = -0.785f;
		this.leftLeg.yRot = -0.35f;
		this.leftLeg.zRot = 0.0f;
	}
}
