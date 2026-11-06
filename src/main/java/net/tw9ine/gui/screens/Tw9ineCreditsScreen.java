/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.screens;

import java.util.Arrays;
import java.util.List;
import net.tw9ine.Tw9ine;
import net.tw9ine.gui.GuiManager;
import net.tw9ine.gui.colors.Colors;
import net.tw9ine.rendering.shaders.Shader;
import net.tw9ine.rendering.utils.TextureBank;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class Tw9ineCreditsScreen extends Screen {
	protected static final Tw9inePanorama TW9INE_ROTATING_PANORAMA_RENDERER = new Tw9inePanorama();

	private static final List<String> CONTRIBUTORS = Arrays.asList("nayeer12");

	private int currentContributorIndex = 0;
	private float animationProgress = 0.0f;
	private static final long ANIMATION_DURATION = 200000;

	public Tw9ineCreditsScreen() {
		super(Component.nullToEmpty("tw9ine Credits"));
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);

		animationProgress += a / (ANIMATION_DURATION / 1000.0f);
		if (animationProgress >= 1.0f) {
			animationProgress = 0.0f;
			currentContributorIndex = (currentContributorIndex + 1) % CONTRIBUTORS.size();
		}

		int textHeight = font.lineHeight;
		int totalHeight = CONTRIBUTORS.size() * (textHeight + 10);
		int startY = height;
		int endY = -totalHeight;

		int baseY = (int) (startY + (endY - startY) * animationProgress);
		int logoHeight = 70;
		int logoY = baseY + 20;

		graphics.blit(RenderPipelines.GUI_TEXTURED, TextureBank.mainmenu_logo, (width - 185) / 2, logoY - 100,
				0, 0, 185, logoHeight, 185, 70, 185, 70);

		for (int i = 0; i < CONTRIBUTORS.size(); i++) {
			int textWidth = font.width(CONTRIBUTORS.get(i));
			int textX = (width - (textWidth * 2)) / 2;
			int textY = baseY + i * (textHeight + 10);
			float alpha = getFadeAlpha(textY);
			drawContributorName(graphics, CONTRIBUTORS.get(i), textX, textY, alpha);
		}
	}

	private float getFadeAlpha(int y) {
		float fadeHeight = height / 4.0f;
		float alpha = 1.0f;

		if (y < fadeHeight) {
			alpha = y / fadeHeight;
		} else if (y > height - fadeHeight) {
			alpha = (height - y) / fadeHeight;
		}

		return Math.max(0.0f, Math.min(1.0f, alpha));
	}

	private void drawContributorName(GuiGraphicsExtractor context, String contributor, int x, int y, float alpha) {
		Tw9ine.getInstance().render2D.drawString(contributor, (float) x, (float) y,
				Shader.solid(Colors.White),
				GuiManager.fontSetting.getValue().getRenderer());
	}

	@Override
	protected void extractPanorama(final GuiGraphicsExtractor graphics, final float a){
		try {
			TW9INE_ROTATING_PANORAMA_RENDERER.extractRenderState(graphics, this.width, this.height, true);
		} catch (IllegalStateException e) {
		}
	}
}
