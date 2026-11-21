package de.anyarmorstand.client;

import de.anyarmorstand.AnyArmorStandMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

/** Client-Teil: Taste J öffnet das Menü, wenn du einen Armor Stand ansiehst. */
public class AnyArmorStandClient implements ClientModInitializer {

	private static KeyBinding openMenuKey;

	@Override
	public void onInitializeClient() {
		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.anyarmorstand.open_menu",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_J,
				KeyBinding.Category.create(Identifier.of(AnyArmorStandMod.MOD_ID, "main"))
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMenuKey.wasPressed()) {
				if (client.player == null || client.currentScreen != null) {
					continue;
				}
				if (client.crosshairTarget instanceof EntityHitResult hit
						&& hit.getEntity() instanceof ArmorStandEntity stand) {
					client.setScreen(new ArmorStandMenuScreen(stand.getId()));
				} else {
					client.player.sendMessage(Text.literal("Schau zuerst auf einen Armor Stand."), true);
				}
			}
		});
	}
}
