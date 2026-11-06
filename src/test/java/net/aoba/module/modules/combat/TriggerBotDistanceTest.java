package net.aoba.module.modules.combat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

class TriggerBotDistanceTest {
	private static final AABB BOX = new AABB(0, 0, 0, 1, 2, 1);

	@Test
	void pointInsideBoxHasZeroDistance() {
		assertThat(TriggerBot.distanceSqrToBox(new Vec3(0.5, 1, 0.5), BOX)).isZero();
	}

	@Test
	void pointInFrontOfFaceMeasuresToTheFace() {
		// 3 blocks in front of the x = 1 face.
		assertThat(TriggerBot.distanceSqrToBox(new Vec3(4, 1, 0.5), BOX)).isCloseTo(9.0, within(1e-9));
	}

	@Test
	void pointPastCornerMeasuresToTheCorner() {
		// Offset (1, 1, 1) from the corner (1, 2, 1).
		assertThat(TriggerBot.distanceSqrToBox(new Vec3(2, 3, 2), BOX)).isCloseTo(3.0, within(1e-9));
	}
}
