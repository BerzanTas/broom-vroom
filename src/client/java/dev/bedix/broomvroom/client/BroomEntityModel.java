package dev.bedix.broomvroom.client;

import dev.bedix.broomvroom.Broomvroom;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class BroomEntityModel extends EntityModel<BroomEntityRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(
			Identifier.fromNamespaceAndPath(Broomvroom.MOD_ID, "broom"),
			"main"
	);

	private static final float DEG15 = 15.0f * Mth.DEG_TO_RAD;
	private static final float DEG30 = 30.0f * Mth.DEG_TO_RAD;
	private static final float LEN15 = 0.95f;
	private static final float LEN30 = 0.88f;
	private static final float HANDLE_LEN = 5.5f;
	private static final float SEAT_LEN = 2.5f;
	private static final float REAR_LEN = 1.35f;
	private static final float HALF_MAIN = 0.5f;
	private static final float HALF_15 = 0.42f;
	private static final float HALF_30 = 0.32f;
	private static final float INSET = 0.14f;

	public BroomEntityModel(ModelPart root) {
		super(root, RenderTypes::entitySolid);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		float run = dz(LEN15, DEG15) + dz(LEN30, DEG30) + dz(LEN15, DEG15);
		float dip = -(dy(LEN15, DEG15) + dy(LEN30, DEG30) + dy(LEN15, DEG15));
		float seatStartZ = -SEAT_LEN * 0.5f;
		float handleEndZ = seatStartZ - run;
		float seatEndZ = seatStartZ + SEAT_LEN;
		float rearZ = seatEndZ + run;
		float shaftEndZ = rearZ + REAR_LEN;

		float y = 0.0f;
		float z = handleEndZ;
		addShaft(root, "handle", 0.0f, handleEndZ, 0.0f, HANDLE_LEN, HALF_MAIN, 0.0f, true);
		addShaft(root, "down_15a", y, z, DEG15, LEN15, HALF_15, INSET, false);
		y += dy(LEN15, DEG15);
		z += dz(LEN15, DEG15);
		addShaft(root, "down_30", y, z, DEG30, LEN30, HALF_30, INSET, false);
		y += dy(LEN30, DEG30);
		z += dz(LEN30, DEG30);
		addShaft(root, "down_15b", y, z, DEG15, LEN15, HALF_15, INSET, false);

		root.addOrReplaceChild(
				"seat",
				CubeListBuilder.create().texOffs(16, 0).addBox(-0.7f, -0.5f, 0.0f, 1.4f, 1.0f, SEAT_LEN),
				PartPose.offset(0.0f, -dip, seatStartZ)
		);

		y = -dip;
		z = seatEndZ;
		addShaft(root, "up_15a", y, z, -DEG15, LEN15, HALF_15, INSET, false);
		y += dy(LEN15, -DEG15);
		z += dz(LEN15, -DEG15);
		addShaft(root, "up_30", y, z, -DEG30, LEN30, HALF_30, INSET, false);
		y += dy(LEN30, -DEG30);
		z += dz(LEN30, -DEG30);
		addShaft(root, "up_15b", y, z, -DEG15, LEN15, HALF_15, INSET, false);

		addShaft(root, "rear", 0.0f, rearZ, 0.0f, REAR_LEN, HALF_MAIN, 0.0f, false);

		root.addOrReplaceChild(
				"hay_collar",
				CubeListBuilder.create().texOffs(32, 16).addBox(-0.9f, -0.9f, 0.0f, 1.8f, 1.8f, 1.1f),
				PartPose.offset(0.0f, 0.0f, shaftEndZ)
		);
		float ropeZ = shaftEndZ + 1.1f;
		root.addOrReplaceChild(
				"rope",
				CubeListBuilder.create().texOffs(48, 16).addBox(-1.1f, -1.1f, 0.0f, 2.2f, 2.2f, 0.8f),
				PartPose.offset(0.0f, 0.0f, ropeZ)
		);
		float tuftZ = ropeZ + 0.8f;
		root.addOrReplaceChild(
				"hay_center",
				CubeListBuilder.create().texOffs(32, 0).addBox(-0.85f, -0.85f, -0.2f, 1.7f, 1.7f, 4.4f),
				PartPose.offset(0.0f, 0.0f, tuftZ)
		);
		addHayTuft(root, "hay_up", tuftZ, -DEG30, 0.0f);
		addHayTuft(root, "hay_down", tuftZ, DEG30, 0.0f);
		addHayTuft(root, "hay_left", tuftZ, 0.0f, -DEG30);
		addHayTuft(root, "hay_right", tuftZ, 0.0f, DEG30);

		return LayerDefinition.create(mesh, 64, 64);
	}

	private static float dz(float length, float xRot) {
		return length * Mth.cos(xRot);
	}

	private static float dy(float length, float xRot) {
		return -length * Mth.sin(xRot);
	}

	private static void addShaft(
			PartDefinition root,
			String name,
			float y,
			float z,
			float xRot,
			float length,
			float half,
			float inset,
			boolean extendNegativeZ
	) {
		float z0 = extendNegativeZ ? -length : -inset;
		float zLen = extendNegativeZ ? length : length + inset * 2.0f;
		float size = half * 2.0f;
		root.addOrReplaceChild(
				name,
				CubeListBuilder.create().texOffs(0, 0).addBox(-half, -half, z0, size, size, zLen),
				PartPose.offsetAndRotation(0.0f, y, z, xRot, 0.0f, 0.0f)
		);
	}

	private static void addHayTuft(PartDefinition root, String name, float z, float xRot, float yRot) {
		root.addOrReplaceChild(
				name,
				CubeListBuilder.create().texOffs(32, 0).addBox(-0.6f, -0.6f, -0.2f, 1.2f, 1.2f, 5.0f),
				PartPose.offsetAndRotation(0.0f, 0.0f, z, xRot, yRot, 0.0f)
		);
	}
}
