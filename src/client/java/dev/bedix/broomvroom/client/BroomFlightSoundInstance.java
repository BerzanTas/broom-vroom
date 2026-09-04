package dev.bedix.broomvroom.client;

import dev.bedix.broomvroom.broom.BroomEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class BroomFlightSoundInstance extends AbstractTickableSoundInstance {
    private final LocalPlayer player;
    private int time;

    public BroomFlightSoundInstance(LocalPlayer player) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        this.time++;
        if (this.player.isRemoved()
                || !(this.player.getVehicle() instanceof BroomEntity)
                || this.player.zza <= 0.0f) {
            stop();
            return;
        }

        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();

        boolean turbo = this.player.isSprinting();
        float targetVolume = turbo ? 1.0f : 0.55f;
        float targetPitch = turbo ? 1.22f : 1.0f;
        this.volume = Mth.lerp(0.25f, this.volume, targetVolume);
        this.pitch = Mth.lerp(0.25f, this.pitch, targetPitch);

        if (this.time < 10) {
            this.volume = 0.0f;
        } else if (this.time < 25) {
            this.volume *= (this.time - 10) / 15.0f;
        }
    }
}
