package dev.bedix.broomvroom.broom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class BroomCall {
	private BroomCall() {
	}

	public static void call(ServerPlayer player) {
		if (player.getVehicle() instanceof BroomEntity) {
			return;
		}

		ServerLevel level = player.level();
		BroomEntity broom = findLoaded(player, level);
		if (broom == null) {
			broom = loadUnloaded(player, level);
		}
		if (broom == null) {
			player.sendOverlayMessage(Component.translatable("broomvroom.message.no_broom"));
			return;
		}

		if (!isInsideSimulation(player, broom.position())) {
			Vec3 border = borderPos(player, broom.position());
			float yaw = yawToward(border, player.position());
			broom.snapTo(border.x, border.y, border.z, yaw, 0.0f);
			broom.setDeltaMovement(Vec3.ZERO);
		}
		broom.startRecall();
		if (broom.distanceTo(player) < 2.0) {
			player.startRiding(broom);
		}
	}

	private static BroomEntity findLoaded(ServerPlayer player, ServerLevel level) {
		List<? extends BroomEntity> found = level.getEntities(
				EntityTypeTest.forClass(BroomEntity.class),
				broom -> broom.isOwnedBy(player) && !broom.isVehicle()
		);
		BroomEntity nearest = null;
		double best = Double.MAX_VALUE;
		for (BroomEntity broom : found) {
			double dist = broom.distanceToSqr(player);
			if (dist < best) {
				best = dist;
				nearest = broom;
			}
		}
		return nearest;
	}

	private static BroomEntity loadUnloaded(ServerPlayer player, ServerLevel level) {
		BroomLocatorData.Entry entry = BroomLocatorData.get(player.level().getServer()).get(player.getUUID());
		if (entry == null || !entry.dimension().equals(level.dimension())) {
			return null;
		}

		ChunkPos chunk = ChunkPos.containing(BlockPos.containing(entry.x(), entry.y(), entry.z()));
		level.getChunk(chunk.x(), chunk.z());

		Entity entity = level.getEntity(entry.broomId());
		if (entity instanceof BroomEntity broom && broom.isOwnedBy(player) && !broom.isVehicle()) {
			return broom;
		}

		AABB box = new AABB(
				chunk.getMinBlockX(),
				level.getMinY(),
				chunk.getMinBlockZ(),
				chunk.getMinBlockX() + 16,
				level.getMaxY() + 1,
				chunk.getMinBlockZ() + 16
		);
		List<BroomEntity> inChunk = level.getEntities(
				EntityTypeTest.forClass(BroomEntity.class),
				box,
				broom -> broom.isOwnedBy(player) && !broom.isVehicle()
		);
		return inChunk.isEmpty() ? null : inChunk.get(0);
	}

	private static boolean isInsideSimulation(ServerPlayer player, Vec3 pos) {
		int sim = Math.max(2, player.level().getServer().getPlayerList().getSimulationDistance() - 1);
		int dx = Mth.floor(pos.x) >> 4;
		int dz = Mth.floor(pos.z) >> 4;
		int px = player.chunkPosition().x();
		int pz = player.chunkPosition().z();
		return Math.max(Math.abs(dx - px), Math.abs(dz - pz)) <= sim;
	}

	private static Vec3 borderPos(ServerPlayer player, Vec3 lastPos) {
		int sim = Math.max(2, player.level().getServer().getPlayerList().getSimulationDistance() - 1);
		double radius = sim * 16.0 - 8.0;
		Vec3 horiz = new Vec3(lastPos.x - player.getX(), 0.0, lastPos.z - player.getZ());
		if (horiz.lengthSqr() < 1.0e-4) {
			horiz = Vec3.directionFromRotation(0.0f, player.getYRot());
		}
		horiz = horiz.normalize().scale(radius);
		return new Vec3(player.getX() + horiz.x, player.getY() + 0.8, player.getZ() + horiz.z);
	}

	private static float yawToward(Vec3 from, Vec3 to) {
		return (float) (Mth.atan2(-(to.x - from.x), to.z - from.z) * Mth.RAD_TO_DEG);
	}
}
