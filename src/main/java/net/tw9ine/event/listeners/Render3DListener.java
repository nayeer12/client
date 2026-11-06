/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.event.listeners;

import net.tw9ine.event.events.Render3DEvent;

public interface Render3DListener extends AbstractListener {
	void onRender(Render3DEvent event);
}
