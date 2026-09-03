package dev.bedix.broomvroom.broom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class BroomEntity extends Entity {

	public static final double LANDING_DISTANCE = 1.0;
	private static final double MIN_GROUND_CLEARANCE = 0.65;
	private static final double CLEARANCE_EPSILON = 0.04;
	private static final float IDLE_SINK_STEP = 0.015f;
	private static final float IDLE_SINK_MAX = 0.125f;
	private static final double FOLLOW_START = 7.0;
	private static final double FOLLOW_MAX = 11.0;
	private static final double WANDER_RADIUS = 4.4;
	private static final double MAX_HOVER = 2.0;
	private static final double MIN_HOVER = 0.45;
	private static final String OWNER_TAG = "BroomOwner";
	private static final double STAY_RADIUS = 1.4;
	private static final double HAY_HITBOX_DISTANCE = 1.35;

	private UUID ownerUuid;
	private boolean staying;
	private Vec3 stayAnchor = Vec3.ZERO;
	private float prevYaw;
	private float roll;
	private float rollO;
	private float visualPitch;
	private float visualPitchO;
	private Vec3 wanderTarget = Vec3.ZERO;
	private int wanderTicks;
	private BroomHayEntity hay;

	public BroomEntity(EntityType<? extends BroomEntity> type, Level world) {
		super(type, world);
		setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
	}

	public float getRoll() {
		return this.roll;
	}

	public float getRoll(float tickProgress) {
		return Mth.lerp(tickProgress, this.rollO, this.roll);
	}

	public float getVisualPitch() {
		return this.visualPitch;
	}

	public float getVisualPitch(float tickProgress) {
		return Mth.lerp(tickProgress, this.visualPitchO, this.visualPitch);
	}

	public float getVisualYaw(float tickProgress) {
		if (getControllingPassenger() instanceof LivingEntity rider) {
			return rider.getViewYRot(tickProgress);
		}
		return getYRot(tickProgress);
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	void attachHay(BroomHayEntity hayPart) {
		this.hay = hayPart;
	}

	public Vec3 hayHitboxPos() {
		float yaw = getVisualYaw(1.0f);
		Vec3 back = Vec3.directionFromRotation(this.visualPitch, yaw).scale(-HAY_HITBOX_DISTANCE);
		return position().add(back);
	}

	private void updateHayHitbox() {
		if (!level().isClientSide()) {
			ensureHay();
		}
		if (this.hay != null && !this.hay.isRemoved()) {
			this.hay.followBroom();
		}
	}

	private void ensureHay() {
		if (isRemoved()) {
			return;
		}
		if (this.hay != null && !this.hay.isRemoved()) {
			return;
		}
		BroomHayEntity hayPart = new BroomHayEntity(ModEntityTypes.BROOM_HAY, level());
		hayPart.setParent(this);
		Vec3 pos = hayHitboxPos();
		hayPart.snapTo(pos.x, pos.y, pos.z, getYRot(), this.visualPitch);
		level().addFreshEntity(hayPart);
		this.hay = hayPart;
	}

	@Override
	public void onRemoval(RemovalReason reason) {
		super.onRemoval(reason);
		if (this.hay != null && !this.hay.isRemoved() && reason.shouldDestroy()) {
			this.hay.discard();
		}
		this.hay = null;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	public void setOwner(Player player) {
		this.ownerUuid = player.getUUID();
		if (getCustomName() == null) {
			setCustomName(player.getName());
			setCustomNameVisible(true);
		}
	}

	public boolean isOwnedBy(Player player) {
		return this.ownerUuid != null && this.ownerUuid.equals(player.getUUID());
	}

	public static void writeOwner(ItemStack stack, UUID uuid) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(OWNER_TAG, uuid.toString()));
	}

	public static UUID readOwner(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null || data.isEmpty()) {
			return null;
		}
		CompoundTag tag = data.copyTag();
		if (!tag.contains(OWNER_TAG)) {
			return null;
		}
		try {
			return UUID.fromString(tag.getStringOr(OWNER_TAG, ""));
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	private Player findOwnerPlayer() {
		if (this.ownerUuid == null || !(level() instanceof ServerLevel serverLevel)) {
			return null;
		}
		Player player = serverLevel.getPlayerInAnyDimension(this.ownerUuid);
		if (player == null || player.level() != level()) {
			return null;
		}
		return player;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		if (!getPassengers().isEmpty()) {
			return false;
		}
		if (passenger instanceof Player player) {
			return this.ownerUuid == null || isOwnedBy(player);
		}
		return false;
	}

	@Override
	public LivingEntity getControllingPassenger() {
		return getFirstPassenger() instanceof LivingEntity living ? living : null;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void propagateFallToPassengers(double fallDistance, float damageMultiplier, DamageSource source) {
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand, Vec3 hit) {
		if (!level().isClientSide() && this.ownerUuid == null) {
			setOwner(player);
		}
		if (this.ownerUuid != null && !isOwnedBy(player)) {
			if (!level().isClientSide()) {
				player.sendOverlayMessage(Component.literal("To nie twoja miotła."));
			}
			return InteractionResult.FAIL;
		}
		if (player.isSecondaryUseActive()) {
			if (!isVehicle() && !level().isClientSide()) {
				this.staying = !this.staying;
				if (this.staying) {
					this.stayAnchor = position();
					this.wanderTicks = 0;
					player.sendOverlayMessage(Component.literal("Miotła zostaje."));
				} else {
					player.sendOverlayMessage(Component.literal("Miotła idzie za tobą."));
				}
			}
			return InteractionResult.SUCCESS;
		}
		if (!level().isClientSide() && !isVehicle()) {
			player.startRiding(this);
		}
		return InteractionResult.SUCCESS;
	}

	public ItemStack createItemStack() {
		ItemStack stack = new ItemStack(ModItems.BROOM);
		if (this.ownerUuid != null) {
			writeOwner(stack, this.ownerUuid);
		}
		if (getCustomName() != null) {
			stack.set(DataComponents.CUSTOM_NAME, getCustomName());
		}
		return stack;
	}

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);
		this.staying = false;
		setNoGravity(true);
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		setNoGravity(true);
	}

	@Override
	public void tick() {
		this.rollO = this.roll;
		this.visualPitchO = this.visualPitch;
		super.tick();

		for (Entity passenger : getPassengers()) {
			passenger.resetFallDistance();
		}

		setCustomNameVisible(!isVehicle() && getCustomName() != null);

		Player rider = getControllingPassenger() instanceof Player player ? player : null;
		if (rider != null) {
			updateVisualOrientation(rider);
		} else {
			this.visualPitch = Mth.lerp(0.2f, this.visualPitch, 0.0f);
			this.roll = Mth.lerp(0.2f, this.roll, 0.0f);
		}

		if (!isLocalInstanceAuthoritative()) {
			updateHayHitbox();
			return;
		}

		if (rider == null) {
			tickCompanion();
			updateHayHitbox();
			return;
		}

		Vec3 motion;
		if (rider.zza > 0.0f) {
			motion = rider.getLookAngle().scale(0.55);
		} else {
			Vec3 current = getDeltaMovement();
			double height = heightAboveGround();
			if (height > MIN_GROUND_CLEARANCE + CLEARANCE_EPSILON) {
				double sink = Math.max(current.y - IDLE_SINK_STEP, -IDLE_SINK_MAX);
				motion = new Vec3(current.x * 0.92, sink, current.z * 0.92);
			} else {
				motion = new Vec3(current.x * 0.78, 0.0, current.z * 0.78);
			}
		}

		setDeltaMovement(motion);
		move(MoverType.SELF, getDeltaMovement());
		applyGroundClearance();
		updateHayHitbox();
	}

	private void updateVisualOrientation(Player player) {
		float yaw = player.getYRot();
		setYRot(yaw);

		boolean nearGround = isNearGround();
		boolean movingForward = player.zza > 0.0f;
		float targetPitch = (!nearGround && movingForward) ? Mth.clamp(player.getXRot(), -60.0f, 60.0f) : 0.0f;
		this.visualPitch = Mth.lerp(0.2f, this.visualPitch, targetPitch);
		setXRot(this.visualPitch);

		if (tickCount <= 1) {
			this.prevYaw = yaw;
		}
		float yawDelta = Mth.wrapDegrees(yaw - this.prevYaw);
		this.prevYaw = yaw;
		if (Math.abs(yawDelta) < 0.25f) {
			yawDelta = 0.0f;
		}
		float targetRoll = nearGround ? 0.0f : Mth.clamp(-yawDelta * 8.0f, -70.0f, 70.0f);
		this.roll = Mth.lerp(nearGround ? 0.28f : 0.22f, this.roll, targetRoll);
	}

	private boolean isNearGround() {
		if (onGround()) {
			return true;
		}
		double height = heightAboveGround();
		return Double.isFinite(height) && height <= 1.15;
	}

	private void tickCompanion() {
		setNoGravity(true);
		if (this.staying) {
			tickStay();
			return;
		}
		Player player = findOwnerPlayer();
		double localGround = groundYAt(getX(), getY(), getZ());

		if (player == null) {
			double idleY = Mth.clamp(getY() + Math.sin(tickCount * 0.08) * 0.02, localGround + MIN_HOVER, localGround + MAX_HOVER);
			nudgeToward(new Vec3(getX(), idleY, getZ()), 0.08, 0.12);
			return;
		}

		double distH = Math.sqrt(Mth.square(getX() - player.getX()) + Mth.square(getZ() - player.getZ()));
		double distY = Math.abs(getY() - player.getY());
		boolean chasing = distH > FOLLOW_START || distY > MAX_HOVER;

		double minY;
		double maxY;
		Vec3 dest;
		if (chasing) {
			minY = localGround + 0.2;
			maxY = player.getY() + MAX_HOVER + 8.0;
			dest = new Vec3(player.getX(), player.getY() + 0.8, player.getZ());
			this.wanderTicks = 0;
		} else {
			minY = Math.max(localGround + MIN_HOVER, player.getY() - 0.2);
			maxY = player.getY() + MAX_HOVER;
			if (this.wanderTicks <= 0) {
				double ang = this.random.nextDouble() * Math.PI * 2.0;
				double rad = this.random.nextDouble() * WANDER_RADIUS;
				double ty = player.getY() + 0.35 + this.random.nextDouble() * Math.max(0.2, MAX_HOVER - 0.35);
				this.wanderTarget = new Vec3(
						player.getX() + Math.cos(ang) * rad,
						ty,
						player.getZ() + Math.sin(ang) * rad
				);
				this.wanderTicks = 35 + this.random.nextInt(55);
			}
			this.wanderTicks--;
			double bob = Math.sin((tickCount + this.wanderTicks) * 0.11) * 0.18;
			dest = new Vec3(
					this.wanderTarget.x,
					Mth.clamp(this.wanderTarget.y + bob, minY, maxY),
					this.wanderTarget.z
			);
		}

		double dist = dest.subtract(position()).length();
		double maxSpeed = chasing ? Math.min(0.55, 0.16 + dist * 0.06) : 0.09;
		nudgeToward(dest, maxSpeed, chasing ? 0.55 : 0.12);

		double clampedY = Mth.clamp(getY(), minY, maxY);
		if (clampedY != getY()) {
			setPos(getX(), clampedY, getZ());
			setDeltaMovement(getDeltaMovement().x, 0.0, getDeltaMovement().z);
		}
		faceVelocity();
	}

	private void tickStay() {
		double groundY = groundYAt(getX(), getY(), getZ());
		double minY = Math.max(groundY + MIN_HOVER, this.stayAnchor.y - 0.5);
		double maxY = Math.min(groundY + MAX_HOVER, this.stayAnchor.y + 0.8);
		if (maxY < minY) {
			maxY = minY + 0.2;
		}
		if (this.wanderTicks <= 0) {
			double ang = this.random.nextDouble() * Math.PI * 2.0;
			double rad = this.random.nextDouble() * STAY_RADIUS;
			this.wanderTarget = new Vec3(
					this.stayAnchor.x + Math.cos(ang) * rad,
					Mth.clamp(this.stayAnchor.y + (this.random.nextDouble() - 0.5) * 0.6, minY, maxY),
					this.stayAnchor.z + Math.sin(ang) * rad
			);
			this.wanderTicks = 40 + this.random.nextInt(50);
		}
		this.wanderTicks--;
		double bob = Math.sin((tickCount + this.wanderTicks) * 0.13) * 0.12;
		nudgeToward(new Vec3(this.wanderTarget.x, Mth.clamp(this.wanderTarget.y + bob, minY, maxY), this.wanderTarget.z), 0.06, 0.1);
		faceVelocity();
	}

	private void faceVelocity() {
		Vec3 vel = getDeltaMovement();
		if (vel.horizontalDistanceSqr() > 0.0004) {
			float yaw = (float) (Mth.atan2(-vel.x, vel.z) * Mth.RAD_TO_DEG);
			setYRot(Mth.rotLerp(0.12f, getYRot(), yaw));
		}
	}

	private void nudgeToward(Vec3 dest, double maxSpeed, double cap) {
		Vec3 delta = dest.subtract(position());
		double dist = delta.length();
		Vec3 motion;
		if (dist < 0.04) {
			motion = getDeltaMovement().scale(0.7);
		} else {
			double speed = Math.min(maxSpeed, dist * 0.2);
			motion = delta.scale(speed / dist).add(getDeltaMovement().scale(0.35));
			if (motion.length() > cap) {
				motion = motion.normalize().scale(cap);
			}
		}
		setDeltaMovement(motion);
		move(MoverType.SELF, motion);
	}

	private double groundYAt(double x, double y, double z) {
		Vec3 from = new Vec3(x, y + 0.08, z);
		var hit = level().clip(new ClipContext(
				from,
				from.subtract(0.0, 64.0, 0.0),
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				this
		));
		if (hit.getType() != HitResult.Type.BLOCK) {
			return y - 1.0;
		}
		return hit.getLocation().y;
	}

	private double heightAboveGround() {
		Vec3 from = position().add(0.0, 0.08, 0.0);
		var hit = level().clip(new ClipContext(
				from,
				from.subtract(0.0, 24.0, 0.0),
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				this
		));
		if (hit.getType() != HitResult.Type.BLOCK) {
			return Double.POSITIVE_INFINITY;
		}
		return getY() - hit.getLocation().y;
	}

	private void applyGroundClearance() {
		double height = heightAboveGround();
		if (!Double.isFinite(height) || height >= MIN_GROUND_CLEARANCE) {
			return;
		}
		double targetY = getY() + (MIN_GROUND_CLEARANCE - height);
		setPos(getX(), targetY, getZ());
		this.yOld = targetY;
		Vec3 velocity = getDeltaMovement();
		if (velocity.y < 0.0) {
			setDeltaMovement(velocity.x, 0.0, velocity.z);
		}
	}

	@Override
	public void onPassengerTurned(Entity passenger) {
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		return new Vec3(0.0, 0.18, 0.0);
	}

	public boolean canLand() {
		if (onGround()) {
			return true;
		}

		Vec3 from = position();
		Vec3 to = from.subtract(0.0, LANDING_DISTANCE, 0.0);
		var hit = level().clip(new ClipContext(
				from,
				to,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				this
		));
		return hit.getType() == HitResult.Type.BLOCK;
	}

	@Override
	public boolean isAttackable() {
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (isVehicle()) {
			return false;
		}
		if (!(source.getEntity() instanceof Player player)) {
			return false;
		}
		if (this.ownerUuid != null && !isOwnedBy(player)) {
			player.sendOverlayMessage(Component.literal("To nie twoja miotła."));
			return false;
		}
		if (this.ownerUuid == null) {
			setOwner(player);
		}
		spawnAtLocation(level, createItemStack());
		discard();
		return true;
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		input.getString(OWNER_TAG).ifPresent(value -> {
			try {
				this.ownerUuid = UUID.fromString(value);
			} catch (IllegalArgumentException ignored) {
				this.ownerUuid = null;
			}
		});
		this.staying = input.getBooleanOr("Staying", false);
		if (this.staying) {
			this.stayAnchor = new Vec3(
					input.getDoubleOr("StayX", getX()),
					input.getDoubleOr("StayY", getY()),
					input.getDoubleOr("StayZ", getZ())
			);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		if (this.ownerUuid != null) {
			output.putString(OWNER_TAG, this.ownerUuid.toString());
		}
		output.putBoolean("Staying", this.staying);
		if (this.staying) {
			output.putDouble("StayX", this.stayAnchor.x);
			output.putDouble("StayY", this.stayAnchor.y);
			output.putDouble("StayZ", this.stayAnchor.z);
		}
	}
}
