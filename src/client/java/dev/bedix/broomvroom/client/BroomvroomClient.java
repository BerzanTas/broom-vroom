package dev.bedix.broomvroom.client;

import dev.bedix.broomvroom.broom.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public class BroomvroomClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(BroomEntityModel.LAYER, BroomEntityModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntityTypes.BROOM, BroomEntityRenderer::new);
		EntityRendererRegistry.register(ModEntityTypes.BROOM_HAY, BroomHayRenderer::new);
	}
}
