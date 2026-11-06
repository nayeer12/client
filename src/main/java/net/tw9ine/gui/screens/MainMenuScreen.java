/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.screens;

import static net.tw9ine.Tw9ineClient.MC;
import com.mojang.realmsclient.RealmsMainScreen;
import org.jspecify.annotations.Nullable;
import net.tw9ine.Tw9ineClient;
import net.tw9ine.api.IAddon;
import net.tw9ine.gui.components.widgets.Tw9ineButtonWidget;
import net.tw9ine.gui.components.widgets.Tw9ineImageButtonWidget;
import net.tw9ine.gui.screens.addons.AddonScreen;
import net.tw9ine.rendering.utils.TextureBank;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class MainMenuScreen extends Screen {
	protected static final Tw9inePanorama TW9INE_ROTATING_PANORAMA_RENDERER = new Tw9inePanorama();

	final int LOGO_HEIGHT = Math.max(58, height / 12);
	final int BUTTON_WIDTH = Math.max(150, width / 6);
	final int BUTTON_HEIGHT = Math.max(25, height / 20);
	final int SPACING = Math.max(5, height / 100);

	int smallScreenHeightOffset = 0;

	public MainMenuScreen() {
		super(Component.nullToEmpty("tw9ine Main Menu"));
	}

	public void init() {
		super.init();

		if (height <= 650)
			smallScreenHeightOffset = 40;
		else
			smallScreenHeightOffset = 0;

		int columns = 2;
		int rows = 3;
		float widgetHeight = ((BUTTON_HEIGHT + SPACING) * rows);
		int startX = (int) ((width - (BUTTON_WIDTH * columns + SPACING * (columns - 1))) / 2.0f);
		int startY = (int) ((height - widgetHeight) / 2) + smallScreenHeightOffset;

		Tw9ineButtonWidget singleplayerButton = new Tw9ineButtonWidget(startX, startY, BUTTON_WIDTH, BUTTON_HEIGHT,
				Component.nullToEmpty("Singleplayer"));
		singleplayerButton.setPressAction(b -> minecraft.gui.setScreen(new SelectWorldScreen(this)));
		addRenderableWidget(singleplayerButton);

		Tw9ineButtonWidget multiplayerButton = new Tw9ineButtonWidget(startX + BUTTON_WIDTH + SPACING, startY, BUTTON_WIDTH,
				BUTTON_HEIGHT, Component.nullToEmpty("Multiplayer"));
		multiplayerButton.setPressAction(b -> minecraft.gui.setScreen(new JoinMultiplayerScreen(this)));
		addRenderableWidget(multiplayerButton);

		Tw9ineButtonWidget realmsButton = new Tw9ineButtonWidget(startX, startY + BUTTON_HEIGHT + SPACING, BUTTON_WIDTH,
				BUTTON_HEIGHT, Component.nullToEmpty("Realms"));
		realmsButton.setPressAction(b -> minecraft.gui.setScreen(new RealmsMainScreen(this)));
		addRenderableWidget(realmsButton);

		Tw9ineButtonWidget settingsButton = new Tw9ineButtonWidget(startX + BUTTON_WIDTH + SPACING,
				startY + BUTTON_HEIGHT + SPACING, BUTTON_WIDTH, BUTTON_HEIGHT, Component.nullToEmpty("Settings"));
		settingsButton.setPressAction(b -> minecraft.gui.setScreen(new OptionsScreen(this, MC.options)));
		addRenderableWidget(settingsButton);

		Tw9ineButtonWidget addonsButton = new Tw9ineButtonWidget(startX, startY + ((BUTTON_HEIGHT + SPACING) * 2),
				BUTTON_WIDTH, BUTTON_HEIGHT, Component.nullToEmpty("Addons"));
		addonsButton.setPressAction(b -> minecraft.gui.setScreen(new AddonScreen(this)));
		addRenderableWidget(addonsButton);

		Tw9ineButtonWidget quitButton = new Tw9ineButtonWidget(startX + BUTTON_WIDTH + SPACING,
				startY + ((BUTTON_HEIGHT + SPACING) * 2), BUTTON_WIDTH, BUTTON_HEIGHT, Component.nullToEmpty("Quit"));
		quitButton.setPressAction(b -> minecraft.stop());
		addRenderableWidget(quitButton);
		
		try {
			Class<?> modsScreen = this.getClass().getClassLoader().loadClass("com.terraformersmc.modmenu.gui.ModsScreen");
			
			Tw9ineButtonWidget modsButton = new Tw9ineButtonWidget(startX,
					startY + ((BUTTON_HEIGHT + SPACING) * 3), BUTTON_WIDTH, BUTTON_HEIGHT, Component.nullToEmpty("Mods"));
			
			modsButton.setPressAction(b -> {
				try {
					minecraft.gui.setScreen((@Nullable Screen) modsScreen.getConstructor(Screen.class).newInstance(this));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
			addRenderableWidget(modsButton);
		} catch (ClassNotFoundException e) {}

		Tw9ineImageButtonWidget creditsButton = new Tw9ineImageButtonWidget(width - 20 - 10, height - 20 - 10, 20, 20,
				TextureBank.tw9ine);
		creditsButton.setPressAction(b -> MC.gui.setScreen(new Tw9ineCreditsScreen()));
		addRenderableWidget(creditsButton);


	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
		super.extractRenderState(drawContext, mouseX, mouseY, delta);

		float widgetHeight = (BUTTON_HEIGHT + SPACING) * 5;
		int startX = (width - BUTTON_WIDTH) / 2;
		int startY = (int) ((height - widgetHeight) / 2 + smallScreenHeightOffset);

		int logoWidth = (int) (LOGO_HEIGHT * (719.0 / 270.0));
		int logoX = (width - logoWidth) / 2;
		int logoY = startY - LOGO_HEIGHT;
		drawContext.blit(RenderPipelines.GUI_TEXTURED, TextureBank.mainmenu_logo, logoX, logoY, 0, 0, logoWidth,
				LOGO_HEIGHT, 719, 270, 719, 270);

		drawContext.text(font, "tw9ine", 2, height - 10, 0xFF9CC8DC);

		if (Tw9ineClient.addons.isEmpty()) {
			String noAddonsText = "No addons loaded";
			int textWidth = font.width(noAddonsText);
			drawContext.text(font, noAddonsText, width - textWidth - 15, 10, 0xFFFFFFFF);
		} else {
			int yOffset = 10;
			for (IAddon addon : Tw9ineClient.addons) {
				String addonName = addon.getName();
				String byText = " by ";
				String author = addon.getAuthor();

				int addonNameWidth = font.width(addonName);
				int byTextWidth = font.width(byText);
				int authorWidth = font.width(author);

				drawContext.text(font, addonName,
						width - addonNameWidth - byTextWidth - authorWidth - 20, yOffset, 0xFF50C878);

				drawContext.text(font, byText, width - byTextWidth - authorWidth - 15, yOffset,
						0xFFFFFFFF);

				drawContext.text(font, author, width - authorWidth - 10, yOffset, 0xFFFF0000);

				yOffset += 10;
			}
		}
	}

	@Override
	public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		extractPanorama(graphics, a);
	}

	@Override
	protected void extractPanorama(final GuiGraphicsExtractor graphics, final float a){
		try {
			TW9INE_ROTATING_PANORAMA_RENDERER.extractRenderState(graphics, this.width, this.height, true);
		} catch (IllegalStateException e) {
		}
	}
}
