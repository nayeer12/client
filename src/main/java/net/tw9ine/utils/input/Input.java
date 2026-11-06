/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.utils.input;

import static net.tw9ine.Tw9ineClient.MC;

public class Input {
	private static CursorStyle lastCursorStyle = CursorStyle.Default;

	public static void setCursorStyle(CursorStyle style) {

		if (lastCursorStyle != style) {
			MC.getWindow().selectCursor(style.getCursorType());
			lastCursorStyle = style;
		}
	}
}
