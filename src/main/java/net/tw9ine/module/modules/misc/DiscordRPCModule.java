/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.module.modules.misc;

import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.utils.discord.RPCManager;

public class DiscordRPCModule extends Module {

    public DiscordRPCModule() {
        super("DiscordRPC");

        setCategory(Category.of("Misc"));
        setDescription("Toggles Discord RPC On and Off");
    }

    @Override
    public void onDisable() {
        RPCManager rpcManager = new RPCManager();
        rpcManager.stopRpc();
        // Turns the Discord RPC Off
    }

    @Override
    public void onEnable() {
        RPCManager rpcManager = new RPCManager();
        rpcManager.startRpc();
        // Turns the Discord RPC On
    }

    @Override
    public void onToggle() { // OnEnable And Disable Handle
    }
}
