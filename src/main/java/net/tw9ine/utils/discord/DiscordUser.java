/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.utils.discord;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class DiscordUser extends Structure {
    public String userId;
    public String username;
    @Deprecated
    public String discriminator;
    public String avatar;

    protected List<String> getFieldOrder() {
        return Arrays.asList("userId", "username", "discriminator", "avatar");
    }
}
