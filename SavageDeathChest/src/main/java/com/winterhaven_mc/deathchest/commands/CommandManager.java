package com.winterhaven_mc.deathchest.commands;


import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import com.winterhaven_mc.deathchest.messages.MessageId;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import com.winterhaven_mc.deathchest.storage.DataStoreFactory;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * A class that implements player commands for the plugin
 */
public final class CommandManager implements CommandExecutor, TabCompleter {

	private final PluginMain plugin;
	private final String pluginName;

	// constant List of subcommands
	private final static List<String> subcommands =
			Collections.unmodifiableList(new ArrayList<>(
					Arrays.asList("list","reload","status")));


	public CommandManager(final PluginMain plugin) {

		this.plugin = plugin;
		plugin.getCommand("deathchest").setExecutor(this);
		pluginName = "[" + this.plugin.getName() + "] ";
	}


	/**
	 * Tab completer for DeathChest
	 */
	@Override
	public final List<String> onTabComplete(final CommandSender sender,
											final Command command,
											final String alias,
											final String[] args) {

		// initialize return list
		final List<String> returnList = new ArrayList<>();

		// if first argument, return list of valid matching subcommands
		if (args.length == 1) {

			for (String subcommand : subcommands) {
				if (sender.hasPermission("deathchest." + subcommand)
						&& subcommand.startsWith(args[0].toLowerCase())) {
					returnList.add(subcommand);
				}
			}
		}

		else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("list")
					&& sender.hasPermission("deathchest.list.other")) {

				// get map of chest ownerUUID,name from all current chests
				Map<UUID,String> chestOwners = new HashMap<>();
				for (DeathChest deathChest : plugin.chestManager.getChestList()) {
					chestOwners.put(deathChest.getOwnerUUID(),
							plugin.getServer().getOfflinePlayer(deathChest.getOwnerUUID()).getName());
				}
				returnList.addAll(chestOwners.values());
			}
		}

		return returnList;
	}


	/**
	 * Command handler for DeathChest
	 */
	@Override
	public final boolean onCommand(final CommandSender sender,
								   final Command cmd,
								   final String label,
								   final String[] args) {

		String subcommand;

		// if no arguments passed, set subcommand to status
		if (args.length < 1) {
			subcommand = "status";
		}
		else {
			subcommand = args[0];
		}

		// status command
		if (subcommand.equalsIgnoreCase("status")) {
			return statusCommand(sender);
		}

		// reload command
		if (subcommand.equalsIgnoreCase("reload")) {
			return reloadCommand(sender);
		}

		// list command
		if (subcommand.equalsIgnoreCase("list")) {
			return listCommand(sender, args);
		}

		// return true to suppress display of bukkit command help
		return true;
	}


	/**
	 * Status command
	 * @param sender Command sender
	 * @return true if command executed without error, false to output help message
	 */
	private boolean statusCommand(final CommandSender sender) {

		if (!sender.hasPermission("deathchest.status")) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_STATUS_PERMISSION);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		}

		String versionString = this.plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + pluginName + ChatColor.AQUA + "Version: " 
				+ ChatColor.RESET + versionString);

		if (plugin.debug) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}

		sender.sendMessage(ChatColor.GREEN + "Language: "
				+ ChatColor.RESET + plugin.getConfig().getString("language"));

		sender.sendMessage(ChatColor.GREEN + "Storage Type: "
				+ ChatColor.RESET + plugin.dataStore.getName());

		int expireTime = plugin.getConfig().getInt("expire-time");
		if (expireTime == 0) { expireTime = -1;	}
		sender.sendMessage(ChatColor.GREEN + "Chest Expiration: "
				+ ChatColor.RESET + plugin.messageManager.getTimeString(TimeUnit.MINUTES.toMillis(expireTime)));

		sender.sendMessage(ChatColor.GREEN + "Protection Plugin Support:");
		
		int count = 0;
		for (ProtectionPlugin pp : ProtectionPlugin.values()) {
			
			if (pp.isInstalled()) {
				
				List<String> pluginSettings = new ArrayList<>();
				
				count++;
				String statusString = ChatColor.AQUA + "  " + pp.getPluginName() + ": ";
				
				if (plugin.getConfig().getBoolean("protection-plugins." + pp.getPluginName() + ".check-on-place")) {
					pluginSettings.add("check-on-place");
				}
				if (plugin.getConfig().getBoolean("protection-plugins." + pp.getPluginName() + ".check-on-access")) {
					pluginSettings.add("check-on-access");
				}
				statusString = statusString + ChatColor.RESET + pluginSettings.toString();
				sender.sendMessage(statusString);
			}
		}		
		if (count == 0) {
			sender.sendMessage(ChatColor.AQUA + "  [ NONE ENABLED ]");
		}
		
		sender.sendMessage(ChatColor.GREEN + "Enabled Worlds: " + ChatColor.RESET + 
				plugin.worldManager.getEnabledWorldNames().toString());

		sender.sendMessage(ChatColor.GREEN + "Replaceable Blocks: " + ChatColor.RESET +
				plugin.chestManager.replaceableBlocks.toString());

		return true;
	}


	/**
	 * reload command
	 * @param sender command sender
	 * @return always returns true, to prevent bukkit command help display
	 */
	private boolean reloadCommand(final CommandSender sender) {

		if (!sender.hasPermission("deathchest.reload")) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_RELOAD_PERMISSION);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		}

		// copy default config from jar if it doesn't exist
		plugin.saveDefaultConfig();
		
		// reload config file
		plugin.reloadConfig();
		
		// reload replaceable blocks
		plugin.chestManager.replaceableBlocks.reload();
		
		// update debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");
		
		// update enabledWorlds list
		plugin.worldManager.reload();
		
		// reload messages
		plugin.messageManager.reload();

		// reload sounds
		plugin.soundConfig.reload();

		// reload datastore if changed
		DataStoreFactory.reload();

		// send success message
		plugin.messageManager.sendMessage(sender, MessageId.COMMAND_SUCCESS_RELOAD);

		// return true to prevent bukkit command help display
		return true;
	}


	// > /deathchest list
	// > /deathchest list [page]
	// > /deathchest list [player]
	// > /deathchest list [player] [page]

	private boolean listCommand(final CommandSender sender, final String[] args) {

		// if command sender does not have permission to list death chests, output error message and return true
		if (!sender.hasPermission("deathchest.list")) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_LIST_PERMISSION);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		Player player = null;

		// cast sender to player
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		// argument limits
		int maxArgs = 3;

		if (args.length > maxArgs) {
			plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		OfflinePlayer targetPlayer = null;

		// get list of offline players
		OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();

		String passedPlayerName = "";

		int page = 1;

		if (args.length == 2) {

			passedPlayerName = args[1];

			// if second argument not a number, try to match player name
			try {
				page = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException e) {
				// if sender does not have list other permission, send message and return
				if (!sender.hasPermission("deathchest.list.other")) {
					plugin.messageManager.sendMessage(sender, MessageId.COMMAND_FAIL_LIST_OTHER_PERMISSION);
					plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
					return true;
				}
				for (OfflinePlayer offlinePlayer : offlinePlayers) {
					if (offlinePlayer.getName().equalsIgnoreCase(args[1])) {
						targetPlayer = offlinePlayer;
					}
				}
				if (targetPlayer == null && !passedPlayerName.equals("*")) {
					plugin.messageManager.sendMessage(sender, MessageId.LIST_PLAYER_NOT_FOUND);
					plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
					return true;
				}
			}
		}

		if (args.length == 3) {
			try {
				page = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				// third argument not a number, ignore
			}
		}

		page = Math.max(1, page);

		int itemsPerPage = plugin.getConfig().getInt("list-page-size");

		// get all records from chest manager
		final Collection<DeathChest> chestList = plugin.chestManager.getChestList();

		// create empty list of records
		List<DeathChest> displayRecords = new ArrayList<>();

		// add chests to displayRecords list
		for (DeathChest deathChest : chestList) {

			// if passed player name is wildcard, add chest to list
			if ((passedPlayerName.equals("*")) && sender.hasPermission("deathchest.list.other")) {
				displayRecords.add(deathChest);
			}

			// if passed player is valid player and matches chest owner, add chest to list
			else if (targetPlayer != null
					&& deathChest.getOwnerUUID().equals(targetPlayer.getUniqueId())
					&& sender.hasPermission("deathchest.list.other")) {
				displayRecords.add(deathChest);
			}
			// if message recipient is valid player and matches chest owner, add chest to list
			else if (player != null && player.getUniqueId().equals(deathChest.getOwnerUUID())) {
				displayRecords.add(deathChest);
			}

		}

		// if display list is empty, output list empty message and return
		if (displayRecords.isEmpty()) {
			plugin.messageManager.sendMessage(sender, MessageId.LIST_EMPTY);
			return true;
		}

		// sort displayRecords
		displayRecords.sort(Comparator.comparingLong(DeathChest::getExpirationTime));

		// get page count
		int pageCount = ((displayRecords.size() - 1) / itemsPerPage) + 1;
		if (page > pageCount) {
			page = pageCount;
		}
		int startIndex = ((page - 1) * itemsPerPage);
		int endIndex = Math.min((page*itemsPerPage),displayRecords.size());

		List<DeathChest> displayRange = displayRecords.subList(startIndex, endIndex);


		int listCount = startIndex;

		// display list header
		plugin.messageManager.sendMessage(sender, MessageId.LIST_HEADER, page, pageCount);

		for (DeathChest deathChest : displayRange) {

			// increment list counter
			listCount++;

			// if passedPlayerName is wildcard, display LIST_ITEM_ALL
			if (passedPlayerName.equals("*")) {
				plugin.messageManager.sendMessage(sender, MessageId.LIST_ITEM_ALL, deathChest, listCount);
			}
			else {
				plugin.messageManager.sendMessage(sender, MessageId.LIST_ITEM, deathChest, listCount);
			}
		}

		// display list footer
		plugin.messageManager.sendMessage(sender, MessageId.LIST_FOOTER, page, pageCount);
		return true;
	}

}
