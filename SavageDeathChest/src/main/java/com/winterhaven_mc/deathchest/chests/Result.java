package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.util.ProtectionPlugin;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;


/**
 * A class that encapsulates fields to be returned
 * as the result of a search for a valid chest location
 */
final class Result {

	private final ResultCode resultCode;
	private final Location location;
	private final ProtectionPlugin protectionPlugin;
	private final Collection<ItemStack> remainingItems;


	/**
	 * Class constructor
	 *
	 * @param resultCode initial result code for result
	 */
	Result(final ResultCode resultCode) {
		this.resultCode = resultCode;
		this.location = null;
		this.protectionPlugin = null;
		this.remainingItems = new ArrayList<>();
	}


	/**
	 * Class constructor
	 *
	 * @param resultCode initial result code for result
	 * @param remainingItems player dropped items remaining
	 *
	 */
	Result(final ResultCode resultCode, final Collection<ItemStack> remainingItems) {
		this.resultCode = resultCode;
		this.location = null;
		this.protectionPlugin = null;
		this.remainingItems = remainingItems;
	}


	/**
	 * Class constructor
	 *
	 * @param resultCode initial result code for result
	 * @param location chest location
	 */
	Result(final ResultCode resultCode, final Location location) {
		this.resultCode = resultCode;
		this.location = location;
		this.protectionPlugin = null;
		this.remainingItems = new ArrayList<>();
	}


	/**
	 * Class constructor
	 *
	 * @param resultCode initial result code for result
	 * @param location chest location
	 * @param remainingItems player dropped items remaining
	 */
	Result(final ResultCode resultCode,
		   final Location location,
		   final Collection<ItemStack> remainingItems) {
		this.resultCode = resultCode;
		this.location = location;
		this.protectionPlugin = null;
		this.remainingItems = remainingItems;
	}


	/**
	 * Class constructor
	 *
	 * @param resultCode initial result code for result
	 * @param protectionPlugin plugin that prevented chest placement
	 */
	Result(final ResultCode resultCode, final ProtectionPlugin protectionPlugin) {
		this.resultCode = resultCode;
		this.location = null;
		this.protectionPlugin = protectionPlugin;
		this.remainingItems = new ArrayList<>();
	}


	/**
	 * Class constructor
	 *
	 * @param resultCode initial result code for result
	 * @param location chest location
	 * @param protectionPlugin plugin that prevented chest placement
	 * @param remainingItems player dropped items remaining
	 */
	Result(final ResultCode resultCode,
		   final Location location,
		   final ProtectionPlugin protectionPlugin,
		   final Collection<ItemStack> remainingItems) {
		this.resultCode = resultCode;
		this.location = location;
		this.protectionPlugin = protectionPlugin;
		this.remainingItems = remainingItems;
	}


	/**
	 * Getter method for resultCode
	 *
	 * @return ResultCode - the result code currently set for this result object
	 */
	final ResultCode getResultCode() {
		return resultCode;
	}


	/**
	 * Getter method for location
	 *
	 * @return Location - the location currently set for this result object
	 */
	final Location getLocation() {
		return location;
	}


	/**
	 * Getter method for protectionPlugin
	 *
	 * @return ProtectionPlugin - the protection plugin enum value currently set for this result object
	 */
	final ProtectionPlugin getProtectionPlugin() {
		return protectionPlugin;
	}


	/**
	 * Getter method for remainingItems
	 *
	 * @return Collection of ItemStack - the remaining items currently set for this result object
	 */
	final Collection<ItemStack> getRemainingItems() {
		return remainingItems;
	}

}
