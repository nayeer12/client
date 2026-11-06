/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.module.modules.render;

import net.tw9ine.Tw9ine;
import net.tw9ine.event.events.TickEvent.Post;
import net.tw9ine.event.events.TickEvent.Pre;
import net.tw9ine.event.listeners.TickListener;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.FloatSetting;
import net.minecraft.client.OptionInstance;

public class Zoom extends Module implements TickListener {

	private Integer lastFov = null;

	private final FloatSetting zoomFactor = FloatSetting.builder().id("zoom_factor").displayName("Factor")
			.description("The zoom factor that the zoom will use.").defaultValue(2f).minValue(1f).maxValue(3.6f)
			.step(0.1f).build();

	public Zoom() {
		super("Zoom");
		setCategory(Category.of("Render"));
		setDescription("Zooms the players camera to see further.");
		addSetting(zoomFactor);
	}

	@Override
	public void onDisable() {
		if (lastFov != null) {
			MC.options.fov().set(Math.max(30, Math.min(110, lastFov)));
			Tw9ine.getInstance().eventManager.RemoveListener(TickListener.class, this);
		}
	}

	@Override
	public void onEnable() {
		lastFov = MC.options.fov().get();
		Tw9ine.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Pre event) {

	}

	@Override
	public void onTick(Post event) {
		OptionInstance<Integer> fov = MC.options.fov();
		int newZoom = (int) Math.max(30, Math.min(110, lastFov / zoomFactor.getValue()));
		fov.set(newZoom);
	}
}
