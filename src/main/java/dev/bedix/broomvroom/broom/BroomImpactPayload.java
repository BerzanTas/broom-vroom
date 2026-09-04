package dev.bedix.broomvroom.broom;

import dev.bedix.broomvroom.Broomvroom;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BroomImpactPayload(float damage) implements CustomPacketPayload {
	public static final Type<BroomImpactPayload> TYPE =
			new Type<>(Identifier.fromNamespaceAndPath(Broomvroom.MOD_ID, "impact"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BroomImpactPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT,
			BroomImpactPayload::damage,
			BroomImpactPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
