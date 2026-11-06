/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.utils.discord;

import net.tw9ine.Tw9ineClient;
import net.tw9ine.gui.screens.*;
import net.tw9ine.gui.screens.alts.AddAltScreen;
import net.tw9ine.gui.screens.alts.AltScreen;
import net.tw9ine.gui.screens.alts.EditAltScreen;
import net.tw9ine.gui.screens.proxy.AddProxyScreen;
import net.tw9ine.gui.screens.proxy.EditProxyScreen;
import net.tw9ine.gui.screens.proxy.ProxyScreen;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

import static net.tw9ine.Tw9ineClient.MC;

public class RPCManager {
    public static boolean started;
    private static final Discord rpc = Discord.INSTANCE;
    public static DiscordRPC presence = new DiscordRPC();
    private static Thread thread;

    public void startRpc() {
        if (!started) {
            started = true;
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            rpc.Discord_Initialize("1497026345862168646", handlers, true, "");
            presence.startTimestamp = (System.currentTimeMillis() / 1000L);
            presence.largeImageText = "";
            rpc.Discord_UpdatePresence(presence);

            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    rpc.Discord_RunCallbacks();

                    presence.details = getDetails();

                    presence.state = "MC 26.3";

                    presence.smallImageText = "logged as - " + MC.getUser().getName();
                    presence.smallImageKey = "https://minotar.net/helm/" + MC.getUser().getName() + "/100.png";


                    presence.button_label_1 = "Download";
                    presence.button_url_1 = "https://github.com/nayeer12/client";

                    presence.largeImageKey = "https://raw.githubusercontent.com/nayeer12/client/main/src/main/resources/assets/tw9ine/icon.png";

                    rpc.Discord_UpdatePresence(presence);
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }, "TH-RPC-Handler");
            
            thread.start();
        }
    }

    /*
     Stops the Discord Rich Presence (RPC).
     */
    public void stopRpc() {
        if (started) {
            started = false;

            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }

            rpc.Discord_Shutdown();

            thread = null;
        }
    }

    private String getDetails() {
        String result = "";

        if (MC.gui.screen() instanceof TitleScreen || MC.gui.screen() instanceof MainMenuScreen) {
            result = "In Main menu";
        } else if (MC.gui.screen() instanceof JoinMultiplayerScreen || MC.gui.screen() instanceof ManageServerScreen) {
            result = "Picking a server";
        } else if (MC.getCurrentServer() != null) {
            result = "Playing on a server";
        } else if (MC.isLocalServer()) {
            result = "Playing singleplayer";
        } else if (MC.gui.screen() instanceof OptionsScreen) {
            result = "Editing options";
        } else if (MC.gui.screen() instanceof SelectWorldScreen) {
            result = "Selecting a world";
        } else if (MC.gui.screen() instanceof Tw9ineCreditsScreen) {
            result = "Watching tw9ine credits";
        } else if (MC.gui.screen() instanceof WinScreen) {
            result = "Watching credits";
        } else if (MC.gui.screen() instanceof CreateWorldScreen) {
            result = "Creating a world";
        } else if (MC.gui.screen() instanceof EditWorldScreen) {
            result = "Editing a world";
        } else if (MC.gui.screen() instanceof ProxyScreen) {
            result = "Choosing a proxy";
        } else if (MC.gui.screen() instanceof AltScreen) {
            result = "Choosing an alt";
        } else if (MC.gui.screen() instanceof AddProxyScreen) {
            result = "Creating a proxy";
        } else if (MC.gui.screen() instanceof EditProxyScreen) {
            result = "Editing a proxy";
        } else if (MC.gui.screen() instanceof AddAltScreen) {
            result = "Creating an alt";
        } else if (MC.gui.screen() instanceof EditAltScreen) {
            result = "Editing an alt";
        }
        return result;
    }
}
