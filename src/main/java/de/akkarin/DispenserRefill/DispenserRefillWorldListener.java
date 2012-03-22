/**
 * 
 */
package de.akkarin.DispenserRefill;

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
		
		for(int i = 0; i < this.plugin.dispenserList.size(); i++) {
			if (this.plugin.dispenserList.get(i).equals(event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())) {				
				// readd item to stack
				CraftDispenser dispenser = new CraftDispenser(event.getBlock());
				
				// create new item stack
				ItemStack newItemList = event.getItem().clone();
				dispenser.getInventory().addItem(newItemList);
			}
		}
	}
	
	/**
	 * Handles the block break event.
	 * @param event
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		for(int i = 0; i < this.plugin.dispenserList.size(); i++) {
			if (this.plugin.dispenserList.get(i).equals(event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())) {
				// remove block from list
				this.plugin.dispenserList.remove(i);
				
				// save database
				this.plugin.saveDatabase();
				
				// notify user
				this.plugin.getWorldEdit().wrapPlayer(event.getPlayer()).printError("The infinite dispenser at " + event.getBlock().getX() + "," + event.getBlock().getY() + "," + event.getBlock().getZ() + " was eaten ...");
			}
		}
	}
}
