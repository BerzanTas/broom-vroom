package dev.bedix.broomvroom.mixin.client;

import dev.bedix.broomvroom.broom.BroomEntity;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	@Inject(method = "aiStep", at = @At("TAIL"))
	private void broomvroom$sprintOnBroom(CallbackInfo ci) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		if (player.getVehicle() instanceof BroomEntity && player.input != null) {
			player.setSprinting(player.input.keyPresses.sprint());
		}
	}
}
