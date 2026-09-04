package dev.bedix.broomvroom.client;

import dev.bedix.broomvroom.Broomvroom;
import dev.bedix.broomvroom.broom.BroomEntity;
import dev.bedix.broomvroom.broom.BroomImpactPayload;
import dev.bedix.broomvroom.broom.CallBroomPayload;
import dev.bedix.broomvroom.broom.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class BroomvroomClient implements ClientModInitializer {
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(Broomvroom.MOD_ID, "broom")
	);
	private static final KeyMapping CALL_KEY = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.broomvroom.recall", GLFW.GLFW_KEY_R, KEY_CATEGORY)
	);

	private SoundInstance flightSound;

	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(BroomEntityModel.LAYER, BroomEntityModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntityTypes.BROOM, BroomEntityRenderer::new);
		EntityRendererRegistry.register(ModEntityTypes.BROOM_HAY, BroomHayRenderer::new);
		BroomEntity.impactSender = damage -> ClientPlayNetworking.send(new BroomImpactPayload(damage));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			LocalPlayer player = client.player;
			if (player == null) {
				this.flightSound = null;
				return;
			}

			while (CALL_KEY.consumeClick()) {
				if (client.gui.screen() == null && client.getConnection() != null) {
					ClientPlayNetworking.send(CallBroomPayload.INSTANCE);
				}
			}

			boolean flying = player.getVehicle() instanceof BroomEntity && player.zza > 0.0f;
			SoundManager sounds = client.getSoundManager();
			boolean playing = this.flightSound != null && sounds.isActive(this.flightSound);

			if (flying && !playing) {
				this.flightSound = new BroomFlightSoundInstance(player);
				sounds.play(this.flightSound);
			} else if (!flying && !playing) {
				this.flightSound = null;
			}
		});
	}
}
