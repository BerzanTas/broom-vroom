package dev.bedix.broomvroom.broom;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class BroomHayEntity extends Entity {
	private static final EntityDataAccessor<Integer> PARENT_ID =
			SynchedEntityData.defineId(BroomHayEntity.class, EntityDataSerializers.INT);

	private BroomEntity parent;

	public BroomHayEntity(EntityType<? extends BroomHayEntity> type, Level world) {
		super(type, world);
		this.noPhysics = true;
		setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		entityData.define(PARENT_ID, -1);
	}

	public void setParent(BroomEntity broom) {
		this.parent = broom;
		entityData.set(PARENT_ID, broom.getId());
		broom.attachHay(this);
	}

	public BroomEntity getParent() {
		if (this.parent != null && !this.parent.isRemoved()) {
			return this.parent;
		}
		Entity found = level().getEntity(entityData.get(PARENT_ID));
		if (found instanceof BroomEntity broom) {
			this.parent = broom;
			broom.attachHay(this);
			return broom;
		}
		this.parent = null;
		return null;
	}

	public boolean isFor(BroomEntity broom) {
		return broom != null && getParent() == broom;
	}

	public void followBroom() {
		BroomEntity broom = getParent();
		if (broom == null) {
			return;
		}
		Vec3 pos = broom.hayHitboxPos();
		snapTo(pos.x, pos.y, pos.z, broom.getYRot(), broom.getVisualPitch());
		setOldPosAndRot();
		setDeltaMovement(Vec3.ZERO);
	}

	@Override
	public void tick() {
		super.tick();
		BroomEntity broom = getParent();
		if (broom == null || broom.isRemoved()) {
			if (!level().isClientSide()) {
				discard();
			}
			return;
		}
		followBroom();
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand, Vec3 hit) {
		BroomEntity broom = getParent();
		return broom != null ? broom.interact(player, hand, hit) : InteractionResult.PASS;
	}

	@Override
	public boolean isAttackable() {
		return true;
	}

	@Override
	public boolean skipAttackInteraction(Entity attacker) {
		BroomEntity broom = getParent();
		return broom != null && broom.skipAttackInteraction(attacker);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		BroomEntity broom = getParent();
		return broom != null && broom.hurtServer(level, source, damage);
	}

	@Override
	public boolean is(Entity entity) {
		BroomEntity broom = getParent();
		return super.is(entity) || (broom != null && broom.is(entity));
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}
}
