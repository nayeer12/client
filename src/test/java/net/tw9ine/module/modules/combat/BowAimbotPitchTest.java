package net.tw9ine.module.modules.combat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BowAimbotPitchTest {
	@Test
	void levelTargetNeedsSlightUpwardAim() {
		float pitch = BowAimbot.solvePitch(20, 0, 1.0);
		// Negative pitch means looking up; a 20 block flat shot needs only a small lift.
		assertThat(pitch).isNegative().isGreaterThan(-10f);
	}

	@Test
	void fartherTargetsNeedMoreLift() {
		assertThat(BowAimbot.solvePitch(60, 0, 1.0)).isLessThan(BowAimbot.solvePitch(20, 0, 1.0));
	}

	@Test
	void weakerShotsNeedMoreLift() {
		assertThat(BowAimbot.solvePitch(20, 0, 0.5)).isLessThan(BowAimbot.solvePitch(20, 0, 1.0));
	}

	@Test
	void unreachableTargetReturnsNaN() {
		assertThat(BowAimbot.solvePitch(1000, 0, 1.0)).isNaN();
	}

	@Test
	void targetDirectlyAboveOrBelow() {
		assertThat(BowAimbot.solvePitch(0, 5, 1.0)).isEqualTo(-90f);
		assertThat(BowAimbot.solvePitch(0, -5, 1.0)).isEqualTo(90f);
	}
}
