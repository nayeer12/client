/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.module.modules.misc;

import net.tw9ine.module.Category;
import net.tw9ine.module.Module;

public class AntiCactus extends Module {

	public AntiCactus() {
		super("AntiCactus");

		setCategory(Category.of("Misc"));
		setDescription("Prevents blocks from hurting you.");
	}

	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
	}

	@Override
	public void onToggle() {
	}
}