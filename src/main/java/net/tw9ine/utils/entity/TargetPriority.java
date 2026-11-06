/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.utils.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public enum TargetPriority {
	/** Nearest target. */
	CLOSEST,
	/** Target that takes the most damage. */
	HIGHEST_DAMAGE,
	/** Target with the least health (including absorption). */
	LOWEST_HEALTH,
	/** Target with the most health (including absorption). */
	MOST_HEALTH,
	/** Target nearest to the crosshair. */
	CROSSHAIR;

	/**
	 * Scores a candidate for this priority. Lower is better, so callers keep the
	 * candidate with the smallest score.
	 *
	 * @param target   Candidate entity.
	 * @param distSqr  Squared distance from the local player to the candidate.
	 * @param damage   Damage the candidate would take (ignored unless
	 *                 HIGHEST_DAMAGE).
	 */
	public double score(LivingEntity target, double distSqr, double damage) {
		return switch (this) {
		case CLOSEST -> distSqr;
		case HIGHEST_DAMAGE -> -damage;
		case LOWEST_HEALTH -> health(target);
		case MOST_HEALTH -> -health(target);
		case CROSSHAIR -> angleTo(target.getBoundingBox().getCenter());
		};
	}

	private static double health(LivingEntity entity) {
		return entity.getHealth() + entity.getAbsorptionAmount();
	}

	/** Angle in radians between the player's view direction and a point. */
	public static double angleTo(Vec3 point) {
		Minecraft mc = Minecraft.getInstance();
		Vec3 eye = mc.player.getEyePosition();
		Vec3 toPoint = point.subtract(eye).normalize();
		double cos = mc.player.getViewVector(1.0f).dot(toPoint);
		return Math.acos(Math.max(-1.0, Math.min(1.0, cos)));
	}
}
