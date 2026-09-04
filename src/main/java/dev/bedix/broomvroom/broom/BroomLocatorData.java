package dev.bedix.broomvroom.broom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.bedix.broomvroom.Broomvroom;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BroomLocatorData extends SavedData {
	public record Entry(UUID broomId, ResourceKey<Level> dimension, double x, double y, double z) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				UUIDUtil.STRING_CODEC.fieldOf("broom").forGetter(Entry::broomId),
				ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Entry::dimension),
				Codec.DOUBLE.fieldOf("x").forGetter(Entry::x),
				Codec.DOUBLE.fieldOf("y").forGetter(Entry::y),
				Codec.DOUBLE.fieldOf("z").forGetter(Entry::z)
		).apply(instance, Entry::new));

		public Vec3 pos() {
			return new Vec3(this.x, this.y, this.z);
		}
	}

	public static final Codec<BroomLocatorData> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Entry.CODEC)
			.xmap(BroomLocatorData::new, data -> data.entries);

	public static final SavedDataType<BroomLocatorData> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(Broomvroom.MOD_ID, "broom_locators"),
			BroomLocatorData::new,
			CODEC,
			DataFixTypes.LEVEL
	);

	private final Map<UUID, Entry> entries;

	public BroomLocatorData() {
		this(Map.of());
	}

	public BroomLocatorData(Map<UUID, Entry> entries) {
		this.entries = new HashMap<>(entries);
	}

	public static BroomLocatorData get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public void remember(BroomEntity broom) {
		UUID owner = broom.getOwnerUuid();
		if (owner == null) {
			return;
		}
		this.entries.put(owner, new Entry(
				broom.getUUID(),
				broom.level().dimension(),
				broom.getX(),
				broom.getY(),
				broom.getZ()
		));
		setDirty();
	}

	public void forget(BroomEntity broom) {
		UUID owner = broom.getOwnerUuid();
		if (owner == null) {
			return;
		}
		Entry existing = this.entries.get(owner);
		if (existing != null && existing.broomId().equals(broom.getUUID())) {
			this.entries.remove(owner);
			setDirty();
		}
	}

	public Entry get(UUID owner) {
		return this.entries.get(owner);
	}
}
