/**
 * 
 */
package de.akkarin.DispenserRefill;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Dispenser;
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
		Iterator<InfiniteDispenser> it = this.plugin.dispenserList.iterator();
		
		while(it.hasNext()) {
			InfiniteDispenser dispenser = it.next();
			Location currentPosition = dispenser.getLocation();
			
			// check
			if (currentPosition.equals(event.getBlock().getLocation())) {
				// stop if there's a cooldown period active
				if (dispenser.hasCooldownPeriod(currentPosition.getWorld().getFullTime())) return; // there are no other dispensers on this position
				
				// get dispenser
				Dispenser dispenserBlock = (Dispenser) event.getBlock().getState();
				
				// set off dispenser
				dispenser.setCooldownPeriod(currentPosition.getWorld().getFullTime());
				
				// create item stack
				ItemStack newItemStack = event.getItem().clone();
				dispenserBlock.getInventory().addItem(newItemStack);
				
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
		Iterator<InfiniteDispenser> it = this.plugin.dispenserList.iterator();
		
		while(it.hasNext()) {
			// get item
			InfiniteDispenser dispenser = it.next();
			Location currentPosition = dispenser.getLocation();
			
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
