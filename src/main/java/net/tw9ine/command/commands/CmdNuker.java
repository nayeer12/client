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
import net.tw9ine.module.modules.world.Nuker;

public class CmdNuker extends Command {

	public CmdNuker() {
		super("nuker", "Destroys blocks within a certain distance of the player.", "[radius, toggle] [value]");
	}

	@Override
	public void runCommand(String[] parameters) throws InvalidSyntaxException {
		if (parameters.length != 2)
			throw new InvalidSyntaxException(this);

		Nuker module = Tw9ine.getInstance().moduleManager.nuker;

		switch (parameters[0]) {
		case "radius":
			try {
				int radius = Integer.parseInt(parameters[1]);
				if (radius > 10) {
					radius = 10;
				} else if (radius < 1) {
					radius = 1;
				}
				module.setRadius(radius);
				CommandManager.sendChatMessage("Nuker radius set to " + radius);

			} catch (Exception e) {
				CommandManager.sendChatMessage("Invalid value. [1-10]");
			}
			break;
		case "toggle":
			String state = parameters[1].toLowerCase();
			if (state.equals("on")) {
				module.state.setValue(true);
				CommandManager.sendChatMessage("Nuker toggled ON");
			} else if (state.equals("off")) {
				module.state.setValue(false);
				CommandManager.sendChatMessage("Nuker toggled OFF");
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
		case "radius":
			return new String[] { "1.0", "2.0", "5.0", "10.0" };
		default:
			return new String[] { "toggle" };
		}
	}
}
