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
import net.tw9ine.utils.entity.FakePlayerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class POV extends Module implements TickListener {
	private FakePlayerEntity fakePlayer;
	private String povString = null;
	private Entity povEntity = null;

	private final boolean fakePlayerSpawned = false;

	public POV() {
		super("POV");
		setCategory(Category.of("Render"));
		setDescription("Allows the player to see someone else's point-of-view.");
	}

	@Override
	public void onDisable() {
		Minecraft.getInstance().setCameraEntity(MC.player);
		if (fakePlayer != null) {
			fakePlayer.despawn();
			MC.level.removeEntity(-3, null);
		}
		Tw9ine.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Tw9ine.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	public void setEntityPOV(String entity) {
		povString = entity;
	}

	public Entity getEntity() {
		return povEntity;
	}

	public Player getEntityAsPlayer() {
		if (povEntity instanceof Player) {
			return (Player) povEntity;
		} else {
			return null;
		}
	}

	@Override
	public void onTick(Pre event) {
		LocalPlayer player = MC.player;
		povEntity = null;
		for (Entity entity : MC.level.players()) {
			if (entity.getName().getString().equals(povString)) {
				povEntity = entity;
			}
		}
		if (Minecraft.getInstance().getCameraEntity() == povEntity) {
			if (!fakePlayerSpawned) {
				fakePlayer = new FakePlayerEntity();
				fakePlayer.restoreFrom(player);
				fakePlayer.yHeadRot = player.yHeadRot;
				MC.level.addEntity(fakePlayer);
			}
			fakePlayer.restoreFrom(player);
			fakePlayer.yHeadRot = player.yHeadRot;
		} else {
			if (fakePlayer != null) {
				fakePlayer.despawn();
				MC.level.removeEntity(-3, null);
			}

			if (povEntity == null) {
				Minecraft.getInstance().setCameraEntity(MC.player);
			} else {
				Minecraft.getInstance().setCameraEntity(povEntity);
			}
		}
	}

	@Override
	public void onTick(Post event) {

	}
}