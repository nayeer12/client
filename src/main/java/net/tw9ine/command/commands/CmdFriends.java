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
import net.tw9ine.settings.friends.Friend;
import net.tw9ine.settings.friends.FriendsList;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class CmdFriends extends Command {

    public CmdFriends() {
        super("friends", "Allows the player to add and remove friends (Who will be excluded from many hacks)", "[add/remove/list] [value]");
    }

    @Override
    public void runCommand(String[] parameters) throws InvalidSyntaxException {
        FriendsList friendsList = Tw9ine.getInstance().friendsList;
        Minecraft MC = Minecraft.getInstance();
        switch (parameters[0]) {
            case "add": {
                String playerName = parameters[1];
                try {
                    ServerPlayer entity = MC.getSingleplayerServer().getPlayerList().getPlayerByName(playerName);
                    if (entity != null) {
                        Tw9ine.getInstance().friendsList.addFriend(entity.getName().getString(), entity.getUUID());
                        CommandManager.sendChatMessage("Player " + playerName + " was added to the friends list.");
                    } else {
                        CommandManager.sendChatMessage("Player " + playerName + " could not be found.");
                    }
                } catch (Exception e) {
                    CommandManager.sendChatMessage("Player " + playerName + " could not be added. Unknown error occured.");
                    return;
                }
            }
            break;
            case "remove": {
                String playerName = parameters[1];
                ServerPlayer entity = MC.getSingleplayerServer().getPlayerList().getPlayerByName(playerName);
                if (entity != null) {
                    Tw9ine.getInstance().friendsList.removeFriend(entity.getUUID());
                    CommandManager.sendChatMessage("Player " + playerName + " was removed from the friends list.");
                } else {
                    CommandManager.sendChatMessage("Player " + playerName + " could not be found.");
                }
            }
            break;
            case "list":
                StringBuilder friends = new StringBuilder("Friends: ");
                for (Friend friend : friendsList.getFriends()) {
                    friends.append(friend.getUsername()).append(", ");
                }
                friends.substring(0, friends.length() - 2);
                CommandManager.sendChatMessage(friends.toString());
                break;
        }
    }

    @Override
    public String[] getAutocorrect(String previousParameter) {
        switch (previousParameter) {
            case "add":
                return mc.getSingleplayerServer().getPlayerNames();
            case "remove":
                return new String[]{"xray", "delete"};
            default:
                return new String[]{"add", "remove", "list"};
        }
    }
}
