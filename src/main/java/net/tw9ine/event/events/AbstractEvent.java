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

public abstract class AbstractEvent {
	boolean isCancelled;

	public AbstractEvent() {
		isCancelled = false;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void cancel() {
		isCancelled = true;
	}

	public void setCancelled(boolean state) {
		isCancelled = state;
	}

	public abstract void Fire(ArrayList<? extends AbstractListener> listeners);

	public abstract <T extends AbstractListener> Class<T> GetListenerClassType();
}
