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
import net.tw9ine.module.modules.movement.NoSlowdown;

public class CmdNoSlowdown extends Command {

	public CmdNoSlowdown() {
		super("noslowdown", "Disables webs from slowing the player down", "[toggle] [value]");
	}

	@Override
	public void runCommand(String[] parameters) throws InvalidSyntaxException {
		if (parameters.length != 2)
			throw new InvalidSyntaxException(this);

		NoSlowdown module = Tw9ine.getInstance().moduleManager.noslowdown;

		switch (parameters[0]) {
		case "toggle":
			String state = parameters[1].toLowerCase();
			if (state.equals("on")) {
				module.state.setValue(true);
				CommandManager.sendChatMessage("NoSlowDown toggled ON");
			} else if (state.equals("off")) {
				module.state.setValue(false);
				CommandManager.sendChatMessage("NoSlowDown toggled OFF");
			} else {
				CommandManager.sendChatMessage("Invalid value. [ON/OFF]");
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
		default:
			return new String[] { "toggle" };
		}
	}

}
