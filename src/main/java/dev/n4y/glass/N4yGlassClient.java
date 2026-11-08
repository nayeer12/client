package dev.n4y.glass;

import com.mojang.blaze3d.platform.InputConstants;
import dev.n4y.glass.ui.screen.GlassMainScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada del cliente de n4y Client Glass Edition.
 * Solo registra accesos a la interfaz: no modifica ninguna mecánica del juego.
 */
public final class N4yGlassClient implements ClientModInitializer {
	public static final String MOD_ID = "n4y_glass";
	public static final Logger LOGGER = LoggerFactory.getLogger("n4y Glass");

	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(id("main"));
	private static KeyMapping openMenuKey;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitializeClient() {
		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.n4y_glass.open_menu",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMenuKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new GlassMainScreen(null));
				}
			}
		});

		// Botón de acceso en el menú principal de Minecraft.
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (screen instanceof TitleScreen) {
				Screens.getButtons(screen).add(Button.builder(
								Component.translatable("n4y_glass.button.open"),
								button -> Minecraft.getInstance().setScreen(new GlassMainScreen(screen)))
						.bounds(6, 6, 90, 20)
						.build());
			}
		});

		LOGGER.info("n4y Client Glass Edition cargado");
	}
}
