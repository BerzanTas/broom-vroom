package dev.bedix.broomvroom.client;

import dev.bedix.broomvroom.broom.BroomHayEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class BroomHayRenderer extends EntityRenderer<BroomHayEntity, EntityRenderState> {
	public BroomHayRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.0f;
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}

	@Override
	protected boolean shouldShowName(BroomHayEntity entity, double distance) {
		return false;
	}
}
