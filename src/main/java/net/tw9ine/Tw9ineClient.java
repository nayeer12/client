/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * A class to represent tw9ine and all of its functions.
 */
package net.tw9ine;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.tw9ine.api.IAddon;
import net.tw9ine.gui.GuiManager;
import net.tw9ine.gui.font.FontManager;
import net.tw9ine.managers.CombatManager;
import net.tw9ine.managers.CommandManager;
import net.tw9ine.managers.EntityManager;
import net.tw9ine.managers.EventManager;
import net.tw9ine.managers.ModuleManager;
import net.tw9ine.managers.SettingManager;
import net.tw9ine.managers.altmanager.AltManager;
import net.tw9ine.managers.macros.MacroManager;
import net.tw9ine.managers.proxymanager.ProxyManager;
import net.tw9ine.managers.rotation.RotationManager;
import net.tw9ine.mixin.interfaces.IMinecraft;
import net.tw9ine.module.Module;
import net.tw9ine.rendering.AbstractRenderer;
import net.tw9ine.rendering.Compositor;
import net.tw9ine.rendering.Renderer2D;
import net.tw9ine.rendering.Renderer3D;
import net.tw9ine.rendering.shaders.ShaderManager;
import net.tw9ine.settings.friends.FriendsList;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class Tw9ineClient {
	public static final KeyMapping.Category TW9INE_CATEGORY =
			new KeyMapping.Category(Identifier.fromNamespaceAndPath("tw9ine", "category"));

	public static Minecraft MC;
	public static IMinecraft IMC;

	// Systems
	public RotationManager rotationManager;
	public ModuleManager moduleManager;
	public CommandManager commandManager;
	public AltManager altManager;
	public ProxyManager proxyManager;
	public GuiManager guiManager;
	public FontManager fontManager;
	public CombatManager combatManager;
	public SettingManager settingManager;
	public FriendsList friendsList;
	public EventManager eventManager;
	public MacroManager macroManager;
	public EntityManager entityManager;
	public ShaderManager shaderManager;
	public Renderer2D render2D;
	public Renderer3D render3D;
	public Compositor compositor;

	public static List<IAddon> addons = new ArrayList<>();
	public static Logger LOGGER;

	/**
	 * Initializes tw9ine and creates sub-systems.
	 */
	public void Initialize() {
		// Gets instance of Minecraft
		MC = Minecraft.getInstance();
		IMC = (IMinecraft) MC;
		LOGGER = LogUtils.getLogger();
	}

	/**
	 * Initializes systems and loads any assets.
	 */
	public void loadAssets() {
		LOGGER.info("[tw9ine] Starting Client");
		eventManager = new EventManager();

		// Register any addons.
		LogUtils.getLogger().info("[tw9ine] Starting addon initialization");
		for (EntrypointContainer<IAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("tw9ine",
				IAddon.class)) {
			IAddon addon = entrypoint.getEntrypoint();

			try {
				LOGGER.info("[tw9ine] Initializing addon: " + addon.getName());
				addon.onInitialize();
				LOGGER.info("[tw9ine] Addon initialized: " + addon.getName());
			} catch (Throwable e) {
				LOGGER.error("Error initializing addon: " + addon.getName(), e);
			}

			addons.add(addon);
		}

		LOGGER.info("[tw9ine] Reading Settings");
		settingManager = new SettingManager();

		LOGGER.info("[tw9ine] Reading Friends List");
		friendsList = new FriendsList();

		LOGGER.info("[tw9ine] Initializing Commands");
		commandManager = new CommandManager(addons);

		LOGGER.info("[tw9ine] Initializing Font Manager");
		fontManager = new FontManager();
		fontManager.Initialize();

		LOGGER.info("[tw9ine] Initializing Combat Manager");
		combatManager = new CombatManager();

		LOGGER.info("[tw9ine] Initializing Entity Manager");
		entityManager = new EntityManager();

		LOGGER.info("[tw9ine] Initializing Macro Manager");
		macroManager = new MacroManager();

		LOGGER.info("[tw9ine] Initializing Shader Effects");
		shaderManager = new ShaderManager();
		
		LOGGER.info("[tw9ine] Initializing Renderers");
		compositor = new Compositor();
		render2D = new Renderer2D();
		render3D = new Renderer3D();

		LOGGER.info("[tw9ine] Initializing Rotation Manager");
		rotationManager = new RotationManager();
		
		LOGGER.info("[tw9ine] Initializing Modules");
		moduleManager = new ModuleManager(addons);
		
		LOGGER.info("[tw9ine] Initializing GUI");
		guiManager = new GuiManager();
		guiManager.Initialize();

		LOGGER.info("[tw9ine] Loading Settings");
		SettingManager.loadGlobalSettings();
		SettingManager.loadSettings();

		LOGGER.info("[tw9ine] Initializing Alt Manager");
		altManager = new AltManager();

		LOGGER.info("[tw9ine] Initializing Proxy Manager");
		proxyManager = new ProxyManager();

		LOGGER.info("[tw9ine] Registering Shutdown Hook");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				endClient();
			} catch (Exception e) {
				LOGGER.error("[tw9ine] Error during shutdown: ", e);
			}
		}));

		LOGGER.info("[tw9ine] Initialized and ready to play!");
	}

	/**
	 * Called when the client is shutting down. Saves persistent data.
	 */
	public void endClient() {
		LOGGER.info("[tw9ine] Shutting down");
		try {
			SettingManager.saveSettings();
			altManager.saveAlts();
			friendsList.save();
			macroManager.save();
			moduleManager.modules.forEach(Module::onDisable);
		} catch (Exception e) {
			LOGGER.error("[tw9ine] Error saving data", e);
		}

		try {
			if (render2D != null) render2D.close();
			if (render3D != null) render3D.close();
			if (compositor != null) compositor.close();
			AbstractRenderer.closeSharedResources();
		} catch (Exception e) {
			LOGGER.error("[tw9ine] Error releasing renderer resources", e);
		}
	}
}
