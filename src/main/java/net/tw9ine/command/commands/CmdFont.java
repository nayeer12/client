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
import net.tw9ine.Tw9ineClient;
import net.tw9ine.command.Command;
import net.tw9ine.gui.GuiManager;
import net.tw9ine.gui.font.UIFont;
import net.tw9ine.managers.CommandManager;
import net.tw9ine.command.InvalidSyntaxException;

public class CmdFont extends Command {

    public CmdFont() {
        super("font", "Sets the HUD font.", "[set] [value]");
    }

    @Override
    public void runCommand(String[] parameters) throws InvalidSyntaxException {
        if (parameters.length != 2)
            throw new InvalidSyntaxException(this);

        Tw9ineClient tw9ine = Tw9ine.getInstance();

        switch (parameters[0]) {
            case "set":
                try {
                    String font = parameters[1];
                    UIFont t = tw9ine.fontManager.fonts.get(font);
                    if (t != null) {
                    	GuiManager.fontSetting.setFontName(font);
                    }
                } catch (Exception e) {
                    CommandManager.sendChatMessage("Invalid value.");
                }
                break;
            default:
                throw new InvalidSyntaxException(this);
        }
    }

    @Override
    public String[] getAutocorrect(String previousParameter) {
        switch (previousParameter) {
            case "set":
                Tw9ineClient tw9ine = Tw9ine.getInstance();

                String[] suggestions = new String[tw9ine.fontManager.fonts.size()];

                int i = 0;
                for (String fontName : tw9ine.fontManager.fonts.keySet())
                    suggestions[i++] = fontName;

                return suggestions;
            default:
                return new String[]{"set"};
        }
    }
}