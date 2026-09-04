package dev.bedix.broomvroom.broom;

import dev.bedix.broomvroom.Broomvroom;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CallBroomPayload() implements CustomPacketPayload {
	public static final CallBroomPayload INSTANCE = new CallBroomPayload();
	public static final Type<CallBroomPayload> TYPE =
			new Type<>(Identifier.fromNamespaceAndPath(Broomvroom.MOD_ID, "call"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CallBroomPayload> CODEC =
			StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
