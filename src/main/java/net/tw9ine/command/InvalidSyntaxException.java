/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.command;

import net.tw9ine.managers.CommandManager;
import net.minecraft.ChatFormatting;
import java.io.Serial;

public class InvalidSyntaxException extends CommandException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidSyntaxException(Command cmd) {
        super(cmd);
    }

    @Override
    public void PrintToChat() {
        CommandManager.sendChatMessage("Invalid syntax! Correct usage: " + ChatFormatting.LIGHT_PURPLE + ".tw9ine " + cmd.getName() + " " + cmd.getSyntax() + ChatFormatting.RESET);
    }
}
