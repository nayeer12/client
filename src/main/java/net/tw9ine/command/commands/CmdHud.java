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

public class CmdHud extends Command {

    public CmdHud() {
        super("hud", "Allows you to customize the hud using commands.", "[toggle] [value]");
    }

    @Override
    public void runCommand(String[] parameters) throws InvalidSyntaxException {
        if (parameters.length != 2)
            throw new InvalidSyntaxException(this);

        switch (parameters[0]) {
            default:
                throw new InvalidSyntaxException(this);
        }
    }

    @Override
    public String[] getAutocorrect(String previousParameter) {
        switch (previousParameter) {
            case "toggle":
                return new String[]{"on", "off"};
            default:
                return new String[]{"toggle"};
        }
    }
}
