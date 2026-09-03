package dev.bedix.broomvroom.mixin;

import dev.bedix.broomvroom.broom.BroomEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "wantsToStopRiding", at = @At("RETURN"), cancellable = true)
    private void broomvroom$shiftDescends(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        Player player = (Player) (Object) this;
        if (player.getVehicle() instanceof BroomEntity broom && !broom.canLand()) {
            cir.setReturnValue(false);
        }
    }
}
