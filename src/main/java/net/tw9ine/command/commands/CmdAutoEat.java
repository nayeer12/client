/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.command.commands;

import net.tw9ine.Tw9ine;
import net.tw9ine.command.Command;
import net.tw9ine.managers.CommandManager;
import net.tw9ine.command.InvalidSyntaxException;
import net.tw9ine.module.modules.misc.AutoEat;

public class CmdAutoEat extends Command {

	public CmdAutoEat() {
		super("autoeat", "Automatically eats when the player is hungry.", "[toggle/set] [value]");
	}

	@Override
	public void runCommand(String[] parameters) throws InvalidSyntaxException {
		if (parameters.length != 2)
			throw new InvalidSyntaxException(this);

		AutoEat module = Tw9ine.getInstance().moduleManager.autoeat;

		switch (parameters[0]) {
		case "toggle":
			String state = parameters[1].toLowerCase();
			if (state.equals("on")) {
				module.state.setValue(true);
				CommandManager.sendChatMessage("AutoEat toggled ON");
			} else if (state.equals("off")) {
				module.state.setValue(false);
				CommandManager.sendChatMessage("AutoEat toggled OFF");
			} else {
				CommandManager.sendChatMessage("Invalid value. [ON/OFF]");
			}
			break;
		case "set":
			String setting = parameters[1].toLowerCase();
			if (setting.isEmpty()) {
				CommandManager.sendChatMessage("Please enter the number of hearts to set to.");
			} else {
				module.setHunger((int) Math.min(Double.parseDouble(setting) * 2, 20));
				CommandManager.sendChatMessage("AutoEat hunger set to " + setting + " hearts.");
			}
			break;
		default:
			throw new InvalidSyntaxException(this);
		}
	}

	@Override
	public String[] getAutocorrect(String previousParameter) {
		switch (previousParameter) {
		case "toggle":
			return new String[] { "on", "off" };
		case "set":
			return new String[] { "1", "2", "4", "6", "8" };
		default:
			return new String[] { "toggle", "set" };
		}
	}
}