package dev.bedix.broomvroom.mixin.client;

import dev.bedix.broomvroom.client.BroomRideAccess;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements BroomRideAccess {
	@Unique
	private boolean broomvroom$ridingBroom;

	@Override
	public void broomvroom$setRidingBroom(boolean riding) {
		this.broomvroom$ridingBroom = riding;
	}

	@Override
	public boolean broomvroom$isRidingBroom() {
		return this.broomvroom$ridingBroom;
	}

	@Unique
	private float broomvroom$broomYaw;

	@Override
	public void broomvroom$setBroomYaw(float yawDegrees) {
		this.broomvroom$broomYaw = yawDegrees;
	}

	@Override
	public float broomvroom$getBroomYaw() {
		return this.broomvroom$broomYaw;
	}

	@Unique
	private float broomvroom$broomPitch;

	@Override
	public void broomvroom$setBroomPitch(float pitchDegrees) {
		this.broomvroom$broomPitch = pitchDegrees;
	}

	@Override
	public float broomvroom$getBroomPitch() {
		return this.broomvroom$broomPitch;
	}

	@Unique
	private float broomvroom$broomRoll;

	@Override
	public void broomvroom$setBroomRoll(float rollDegrees) {
		this.broomvroom$broomRoll = rollDegrees;
	}

	@Override
	public float broomvroom$getBroomRoll() {
		return this.broomvroom$broomRoll;
	}
}
