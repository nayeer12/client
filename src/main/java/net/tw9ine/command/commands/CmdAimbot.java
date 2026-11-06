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
import net.tw9ine.module.modules.combat.Aimbot;

public class CmdAimbot extends Command {

	public CmdAimbot() {
		super("aimbot", "Allows the player to see chest locations through ESP", "[toggle/mode] [value]");
	}

	@Override
	public void runCommand(String[] parameters) throws InvalidSyntaxException {
		if (parameters.length != 2)
			throw new InvalidSyntaxException(this);

		Aimbot module = Tw9ine.getInstance().moduleManager.aimbot;

		switch (parameters[0]) {
		case "toggle":
			String state = parameters[1].toLowerCase();
			if (state.equals("on")) {
				module.state.setValue(true);
				CommandManager.sendChatMessage("Aimbot toggled ON");
			} else if (state.equals("off")) {
				module.state.setValue(false);
				CommandManager.sendChatMessage("Aimbot toggled OFF");
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
