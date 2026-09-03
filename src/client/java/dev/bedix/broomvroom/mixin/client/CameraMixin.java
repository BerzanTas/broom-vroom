package dev.bedix.broomvroom.mixin.client;

import dev.bedix.broomvroom.broom.BroomEntity;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	@Final
	private Quaternionf rotation;

	@Shadow
	private Entity entity;

	@Shadow
	private Vector3f forwards;

	@Shadow
	private Vector3f up;

	@Shadow
	private Vector3f left;

	@Shadow
	private int matrixPropertiesDirty;

	@Shadow
	public abstract boolean isDetached();

	@Shadow
	public abstract Vec3 position();

	@Shadow
	protected abstract void setPosition(Vec3 pos);

	@Inject(method = "alignWithEntity(F)V", at = @At("TAIL"))
	private void broomvroom$bankOnBroom(float tickProgress, CallbackInfo ci) {
		if (this.isDetached() || this.entity == null) {
			return;
		}
		if (!(this.entity instanceof Player player) || !(player.getVehicle() instanceof BroomEntity broom)) {
			return;
		}
		float rollDeg = broom.getRoll(tickProgress);
		if (Math.abs(rollDeg) < 0.01f) {
			return;
		}
		float rollRad = -rollDeg * Mth.DEG_TO_RAD;
		Vec3 axis = Vec3.directionFromRotation(
				broom.getVisualPitch(tickProgress),
				broom.getVisualYaw(tickProgress)
		);
		Quaternionf bank = new Quaternionf().rotationAxis(
				rollRad,
				(float) axis.x,
				(float) axis.y,
				(float) axis.z
		);

		Vec3 cam = this.position();
		Vec3 pivot = new Vec3(
				Mth.lerp(tickProgress, broom.xOld, broom.getX()),
				Mth.lerp(tickProgress, broom.yOld, broom.getY()) + 0.28,
				Mth.lerp(tickProgress, broom.zOld, broom.getZ())
		);
		Vector3f rel = new Vector3f(
				(float) (cam.x - pivot.x),
				(float) (cam.y - pivot.y),
				(float) (cam.z - pivot.z)
		);
		bank.transform(rel);
		this.setPosition(new Vec3(pivot.x + rel.x, pivot.y + rel.y, pivot.z + rel.z));

		this.rotation.premul(bank);
		bank.transform(this.forwards);
		bank.transform(this.up);
		bank.transform(this.left);
		this.matrixPropertiesDirty |= 3;
	}
}
