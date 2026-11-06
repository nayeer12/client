/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.managers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.logging.LogUtils;

import net.tw9ine.api.IAddon;
import net.tw9ine.command.Command;
import net.tw9ine.command.InvalidSyntaxException;
import net.tw9ine.command.commands.*;
import net.tw9ine.settings.types.StringSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class CommandManager {
	private final Map<String, Command> commands = new HashMap<>();
	private final List<String> commandHistory = new ArrayList<>();

	public static StringSetting PREFIX = StringSetting.builder().id("tw9ine_prefix").displayName("Prefix")
			.defaultValue(".tw9ine").build();

	// Commands
	public final CmdAimbot aimbot = new CmdAimbot();
	public final CmdAutoEat autoeat = new CmdAutoEat();
	public final CmdChestESP chestesp = new CmdChestESP();
	public final CmdClickgui clickgui = new CmdClickgui();
	public final CmdEntityESP entityesp = new CmdEntityESP();
	public final CmdFastBreak fastbreak = new CmdFastBreak();
	public final CmdFly fly = new CmdFly();
	public final CmdFreecam freecam = new CmdFreecam();
	public final CmdFont font = new CmdFont();
	public final CmdFullbright fullbright = new CmdFullbright();
	public final CmdHelp help = new CmdHelp();
	public final CmdHud hud = new CmdHud();
	public final CmdItemESP itemesp = new CmdItemESP();
	public final CmdNoclip noclip = new CmdNoclip();
	public final CmdNoFall nofall = new CmdNoFall();
	public final CmdNoSlowdown noslowdown = new CmdNoSlowdown();
	public final CmdNuker nuker = new CmdNuker();
	public final CmdPOV pov = new CmdPOV();
	public final CmdSpam spam = new CmdSpam();
	public final CmdSprint sprint = new CmdSprint();
	public final CmdStep step = new CmdStep();
	public final CmdTileBreaker tilebreaker = new CmdTileBreaker();
	public final CmdTimer timer = new CmdTimer();
	public final CmdTP tp = new CmdTP();
	public final CmdTracer tracer = new CmdTracer();
	public final CmdXRay xray = new CmdXRay();
	public final CmdHistory history = new CmdHistory();

	public CommandManager(List<IAddon> addons) {
		SettingManager.registerSetting(PREFIX);

		for (Field field : getClass().getDeclaredFields()) {
			if (Command.class.isAssignableFrom(field.getType())) {
				try {
					Command cmd = (Command) field.get(this);
					commands.put(cmd.getName(), cmd);
				} catch (IllegalAccessException e) {
					LogUtils.getLogger().error("Error initializing tw9ine commands: " + e.getMessage());
				}
			}
		}

		addons.forEach(addon -> addon.commands().forEach(command -> {
			if (!commands.containsKey(command.getName())) {
				commands.put(command.getName(), command);
			} else {
				LogUtils.getLogger().warn("Warning: Duplicate command name \"" + command.getName()
						+ "\" from addon. This command will not be registered.");
			}
		}));
	}

	/** Gets the command object from a syntax. */
	public Command getCommandBySyntax(String syntax) {
		return commands.get(syntax);
	}

	/**
	 * Gets all of the Commands currently registered, including ones registered by
	 * addons.
	 */
	public Map<String, Command> getCommands() {
		return commands;
	}

	/**
	 * Gets the total number of registered Commands, including ones registered by
	 * addons
	 */
	public int getNumOfCommands() {
		return commands.size();
	}

	/** Runs a command. */
	public void command(String[] commandIn) {
		try {
			commandHistory.add(String.join(" ", commandIn));

			Command command = commands.get(commandIn[1]);
			if (command == null) {
				sendChatMessage("Invalid Command! Type " + ChatFormatting.LIGHT_PURPLE + ".tw9ine help" + ChatFormatting.RESET
						+ " for a list of commands.");
			} else {
				String[] parameterList = Arrays.copyOfRange(commandIn, 2, commandIn.length);
				command.runCommand(parameterList);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			sendChatMessage("Invalid Command! Type " + ChatFormatting.LIGHT_PURPLE + ".tw9ine help" + ChatFormatting.RESET
					+ " for a list of commands.");
		} catch (InvalidSyntaxException e) {
			e.PrintToChat();
		}
	}

	/** Returns the command history. */
	public List<String> getCommandHistory() {
		return commandHistory;
	}

	/** Prints a message into the Minecraft Chat. */
	public static void sendChatMessage(String message) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui != null) {
			mc.gui.hud.getChat().addClientSystemMessage(Component.nullToEmpty(ChatFormatting.DARK_GRAY + "[" + ChatFormatting.AQUA + "tw9ine"
					+ ChatFormatting.DARK_GRAY + "] " + ChatFormatting.RESET + message));
		}
	}
}
