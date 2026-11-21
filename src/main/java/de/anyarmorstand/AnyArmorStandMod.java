package de.anyarmorstand;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Server-Teil: Nimmt die Auswahl aus dem J-Menü entgegen, prüft sie und setzt
 * das Item auf den Kopf des Armor Stands.
 *
 * Kreativmodus: Item kostenlos. Überleben: Das Item muss im Inventar sein
 * und es wird eines davon verbraucht.
 */
public class AnyArmorStandMod implements ModInitializer {

	public static final String MOD_ID = "anyarmorstand";

	/** Maximale Entfernung (Blöcke) zwischen Spieler und Armor Stand. */
	private static final double MAX_DISTANCE_SQUARED = 6.0 * 6.0;

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(SelectItemPayload.ID, SelectItemPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SelectItemPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			if (player.isSpectator()) {
				return;
			}

			Entity target = null;
			for (ServerWorld world : context.server().getWorlds()) {
				target = world.getEntityById(payload.entityId());
				if (target != null) {
					break;
				}
			}
			if (!(target instanceof ArmorStandEntity stand) || stand.isMarker()) {
				return;
			}
			if (player.squaredDistanceTo(stand) > MAX_DISTANCE_SQUARED) {
				player.sendMessage(Text.literal("Der Armor Stand ist zu weit weg."), true);
				return;
			}
			if (!stand.canUseSlot(EquipmentSlot.HEAD)) {
				player.sendMessage(Text.literal("Dieser Slot des Armor Stands ist gesperrt."), true);
				return;
			}

			Identifier id = payload.itemId();
			if (!Registries.ITEM.containsId(id)) {
				return;
			}
			Item item = Registries.ITEM.get(id);
			if (item == Items.AIR) {
				return;
			}
			ItemStack toPlace = new ItemStack(item);
			// Nur Items, die das Menü anbietet (also ohne eigenen Ausrüstungs-Slot).
			if (toPlace.get(DataComponentTypes.EQUIPPABLE) != null) {
				return;
			}

			if (!player.isCreative() && !takeOneFromInventory(player, item)) {
				player.sendMessage(Text.literal("Du hast dieses Item nicht im Inventar."), true);
				return;
			}

			ItemStack previous = stand.getEquippedStack(EquipmentSlot.HEAD);
			stand.equipStack(EquipmentSlot.HEAD, toPlace);
			if (!previous.isEmpty()) {
				player.getInventory().offerOrDrop(previous);
			}
		});
	}

	private static boolean takeOneFromInventory(PlayerEntity player, Item item) {
		for (int slot = 0; slot < player.getInventory().size(); slot++) {
			ItemStack stack = player.getInventory().getStack(slot);
			if (stack.isOf(item)) {
				stack.decrement(1);
				return true;
			}
		}
		return false;
	}
}
