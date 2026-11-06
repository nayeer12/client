/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.command.commands;

import net.tw9ine.command.Command;
import net.tw9ine.command.InvalidSyntaxException;

public class CmdTP extends Command {

    public CmdTP() {
        super("tp", "Teleports the player certain blocks away (Vanilla only)", "[x] [y] [z]");
    }

    @Override
    public void runCommand(String[] parameters) throws InvalidSyntaxException {
        if (parameters.length != 3)
            throw new InvalidSyntaxException(this);

        mc.player.setPos(Double.parseDouble(parameters[0]), Double.parseDouble(parameters[1]), Double.parseDouble(parameters[2]));
    }

    @Override
    public String[] getAutocorrect(String previousParameter) {
        return new String[]{"0 0 0"};
    }
}
