/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine;

import net.fabricmc.api.ModInitializer;

/**
 * Initializes and provides access to the tw9ine singleton.
 */
public class Tw9ine implements ModInitializer {
	private static Tw9ineClient INSTANCE;

	@Override
	public void onInitialize() {
		INSTANCE = new Tw9ineClient();
		INSTANCE.Initialize();
	}

	/**
	 * @return Singleton instance of Tw9ineClient.
	 */
	public static Tw9ineClient getInstance() {
		return INSTANCE;
	}
}
