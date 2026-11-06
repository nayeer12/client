/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.utils.discord.callbacks;

import com.sun.jna.Callback;
import net.tw9ine.utils.discord.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(DiscordUser p0);
}
