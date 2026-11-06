/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.command.commands;

import java.util.List;

import net.tw9ine.Tw9ine;
import net.tw9ine.command.Command;
import net.tw9ine.managers.CommandManager;
import net.tw9ine.command.InvalidSyntaxException;
import net.tw9ine.module.modules.render.POV;
import net.minecraft.client.player.AbstractClientPlayer;

public class CmdPOV extends Command {

	public CmdPOV() {
		super("pov", "Allows the player to see through someone else's POV.", "[set, toggle] [value]");
	}

	@Override
	public void runCommand(String[] parameters) throws InvalidSyntaxException {
		if (parameters.length != 2)
			throw new InvalidSyntaxException(this);

		POV module = Tw9ine.getInstance().moduleManager.pov;

		switch (parameters[0]) {
		case "set":
			try {
				String player = parameters[1];
				CommandManager.sendChatMessage("Setting POV Player Name to " + player);
				module.setEntityPOV(player);
			} catch (Exception e) {
				CommandManager.sendChatMessage("Invalid value.");
			}
			break;
		case "toggle":
			String state = parameters[1].toLowerCase();
			if (state.equals("on")) {
				module.state.setValue(true);
				CommandManager.sendChatMessage("POV toggled ON");
			} else if (state.equals("off")) {
				module.state.setValue(false);
				CommandManager.sendChatMessage("POV toggled OFF");
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
		case "set":
			List<AbstractClientPlayer> players = mc.level.players();
			int numPlayers = players.size();
			String[] suggestions = new String[numPlayers];

			int i = 0;
			for (AbstractClientPlayer x : players)
				suggestions[i++] = x.getName().getString();

			return suggestions;
		default:
			return new String[] { "toggle", "set" };
		}
	}
}