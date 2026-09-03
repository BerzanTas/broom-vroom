package dev.bedix.broomvroom.client;

public interface BroomRideAccess {
	void broomvroom$setRidingBroom(boolean riding);

	boolean broomvroom$isRidingBroom();

	void broomvroom$setBroomYaw(float yawDegrees);

	float broomvroom$getBroomYaw();

	void broomvroom$setBroomPitch(float pitchDegrees);

	float broomvroom$getBroomPitch();

	void broomvroom$setBroomRoll(float rollDegrees);

	float broomvroom$getBroomRoll();
}
