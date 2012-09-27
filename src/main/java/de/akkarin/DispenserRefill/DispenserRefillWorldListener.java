/**
 * 
 */
package de.akkarin.DispenserRefill;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.craftbukkit.block.CraftDispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;


/**
 * @author		Johannes Donath
 * @copyright		2012 Evil-Co.de <http://www.evil-co.de>
 * @package		de.akkarin.DispenserRefill
 */
public class DispenserRefillWorldListener implements Listener {
	
	/**
	 * Contains the current plugin instance.
	 */
	private final DispenserRefillPlugin plugin;
	
	/**
	 * Creates a new instance of type DispenserRefillWorldListener
	 * @param plugin
	 */
	public DispenserRefillWorldListener(DispenserRefillPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Register events.
	 */
	public void registerEvents() {
		final PluginManager pm = this.plugin.getServer().getPluginManager();
		pm.registerEvents(this, plugin);
	}
	
	/**
	 * Handles the dispense event.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDispense(BlockDispenseEvent event) {	
		if (event.isCancelled()) return;
		
		// get iterator
		Iterator<Location> it = this.plugin.dispenserList.iterator();
		
		while(it.hasNext()) {
			Location currentPosition = it.next();
			
			// check
			if (currentPosition.equals(event.getBlock().getLocation())) {
				// get dispenser
				CraftDispenser dispenser = new CraftDispenser(event.getBlock());
				
				// create item stack
				ItemStack newItemStack = event.getItem().clone();
				dispenser.getInventory().addItem(newItemStack);
				
				return; // Tony: Stop here. There is no other entry
			}
		}
		
		// debug logging
		// plugin.getLogger().finest("No dispenser found for position " + event.getBlock().getLocation().toString() + ".");
	}
	
	/**
	 * Handles the block break event.
	 * @param event
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		// get iterator
		Iterator<Location> it = this.plugin.dispenserList.iterator();
		
		while(it.hasNext()) {
			// get item
			Location currentPosition = it.next();
			
			// check
			if (currentPosition.equals(event.getBlock().getLocation())) {
				// remove item
				this.plugin.dispenserList.remove(currentPosition);
				
				// save database
				this.plugin.saveDatabase();
				
				// notify user
				this.plugin.getWorldEdit().wrapPlayer(event.getPlayer()).printError("You just destroyed an infinite dispenser.");
				
				// skip all other loops
				return;
			}
		}
	}
}
