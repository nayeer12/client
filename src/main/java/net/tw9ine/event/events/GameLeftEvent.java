/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.event.events;

import java.util.ArrayList;

import net.tw9ine.event.listeners.AbstractListener;
import net.tw9ine.event.listeners.GameLeftListener;

public class GameLeftEvent extends AbstractEvent {

	@Override
	public void Fire(ArrayList<? extends AbstractListener> listeners) {
		for (AbstractListener listener : listeners) {
			GameLeftListener gameLeftListener = (GameLeftListener) listener;
			gameLeftListener.onGameLeft(this);
		}
	}

	@Override
	public Class<GameLeftListener> GetListenerClassType() {
		return GameLeftListener.class;
	}
}
