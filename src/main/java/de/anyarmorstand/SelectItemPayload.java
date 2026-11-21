package de.anyarmorstand;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Client -> Server: "Setze dieses Item auf den Kopf dieses Armor Stands". */
public record SelectItemPayload(int entityId, Identifier itemId) implements CustomPayload {

	public static final CustomPayload.Id<SelectItemPayload> ID =
			new CustomPayload.Id<>(Identifier.of(AnyArmorStandMod.MOD_ID, "select_item"));

	public static final PacketCodec<RegistryByteBuf, SelectItemPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, SelectItemPayload::entityId,
			Identifier.PACKET_CODEC, SelectItemPayload::itemId,
			SelectItemPayload::new
	);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
