/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.components.widgets;

import static net.tw9ine.Tw9ineClient.MC;

import java.util.function.Consumer;

import net.tw9ine.Tw9ine;
import net.tw9ine.gui.GuiManager;
import net.tw9ine.gui.colors.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.tw9ine.rendering.Renderer2D;
import net.tw9ine.rendering.shaders.Shader;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class Tw9ineImageButtonWidget extends AbstractButton {
	private Consumer<Tw9ineImageButtonWidget> pressAction;
	private final Identifier image;
	private boolean background = true;
	private static final Shader IMAGE_SHADER = Shader.solid(Colors.White);

	public Tw9ineImageButtonWidget(int x, int y, int width, int height, Identifier image) {
		super(x, y, width, height, Component.empty());

		this.image = image;
	}

	public Tw9ineImageButtonWidget(int x, int y, int width, int height, Identifier image,
			boolean background) {
		super(x, y, width, height, Component.empty());

		this.image = image;
		this.background = background;
	}

	public void setPressAction(Consumer<Tw9ineImageButtonWidget> pressAction) {
		this.pressAction = pressAction;
	}

	@Override
	public void onPress(InputWithModifiers input) {
		if (pressAction != null) {
			pressAction.accept(this);
		}
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		if (background) {
			Shader bgEffect = isHovered() ? GuiManager.buttonHoverBackgroundColor.getValue()
					: GuiManager.buttonBackgroundColor.getValue();
			Shader bdEffect = GuiManager.buttonBorderColor.getValue();

			Renderer2D renderer = Tw9ine.getInstance().render2D;
			renderer.beginFrame(graphics, MC.getDeltaTracker());
			renderer.drawOutlinedRoundedBox(getX(), getY(), width, height,
					GuiManager.roundingRadius.getValue(), bdEffect, bgEffect);
			renderer.drawTexturedQuad(image, getX(), getY(), width, height, IMAGE_SHADER);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		
	}
}