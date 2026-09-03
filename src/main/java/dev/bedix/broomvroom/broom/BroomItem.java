package dev.bedix.broomvroom.broom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class BroomItem extends Item {
	public BroomItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		UUID itemOwner = BroomEntity.readOwner(stack);
		if (itemOwner != null && !itemOwner.equals(player.getUUID())) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.literal("To nie twoja miotła."));
			}
			return InteractionResult.FAIL;
		}

		BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		}

		Vec3 pos = hit.getLocation();
		BroomEntity broom = new BroomEntity(ModEntityTypes.BROOM, level);
		broom.snapTo(pos.x, pos.y, pos.z, player.getYRot(), 0.0f);
		if (!level.noCollision(broom, broom.getBoundingBox())) {
			return InteractionResult.FAIL;
		}
		if (!level.isClientSide()) {
			broom.setOwner(player);
			if (stack.has(DataComponents.CUSTOM_NAME)) {
				broom.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
				broom.setCustomNameVisible(true);
			}
			level.addFreshEntity(broom);
			stack.consume(1, player);
		}
		return InteractionResult.SUCCESS;
	}
}
