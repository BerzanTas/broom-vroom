package dev.bedix.broomvroom.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.bedix.broomvroom.Broomvroom;
import dev.bedix.broomvroom.broom.BroomEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class BroomEntityRenderer extends EntityRenderer<BroomEntity, BroomEntityRenderState> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Broomvroom.MOD_ID, "textures/entity/broom.png");

    private final BroomEntityModel model;

    public BroomEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new BroomEntityModel(context.bakeLayer(BroomEntityModel.LAYER));
        this.shadowRadius = 0.4f;
    }

    @Override
    public BroomEntityRenderState createRenderState() {
        return new BroomEntityRenderState();
    }

    @Override
    public void extractRenderState(BroomEntity entity, BroomEntityRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        state.yRot = entity.getVisualYaw(tickProgress);
        state.xRot = entity.getVisualPitch(tickProgress);
        state.roll = entity.getRoll(tickProgress);
    }

    @Override
    public void submit(
            BroomEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.28f, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot + 180.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.roll));
        poseStack.translate(0.0f, 0.0f, -0.32f);
        poseStack.scale(2.25f, 2.25f, 2.25f);
        collector.submitModel(
                this.model,
                state,
                poseStack,
                TEXTURE,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                null
        );
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
