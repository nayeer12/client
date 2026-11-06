package net.tw9ine.event.listeners;

import net.tw9ine.event.events.StartAttackEvent;

public interface StartAttackListener extends AbstractListener {
	void onStartAttack(StartAttackEvent event);
}
