/**
 * 
 */
package de.akkarin.DispenserRefill.commands;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;

import de.akkarin.DispenserRefill.DispenserRefillPlugin;


/**
 * @author		Johannes Donath
 * @copyright		2012 Evil-Co.de <http://www.evil-co.de>
 * @package		de.akkarin.DispenserRefill
 */
public class GeneralCommands {
	
	/**
	 * Contains the instance of refill plugin.
	 */
	private DispenserRefillPlugin plugin;
	
	/**
	 * Creates a new instance of type GeneralCommands.
	 * @param plugin
	 */
	public GeneralCommands(DispenserRefillPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * The autorefill command.
	 * @param args
	 * @param sender
	 * @throws CommandException
	 */
	@Command(aliases = {"dispenserrefill", "infinitedispenser", "autorefill"}, usage = "", desc = "Switches a dispenser inventory between infinite and normal.", flags = "s", max = 0)
	public void autorefill(CommandContext args, CommandSender sender) throws CommandException {
		// wrap player
		BukkitPlayer player = this.plugin.getWorldEdit().wrapPlayer((Player) sender);
		
		// check permissions
		this.plugin.checkPermission(sender, "dispenserrefill.general.dispenser");
		
		// get position
		WorldVector pos = player.getBlockTrace(300);
		
		if (pos != null) {
			// check for correct block
			if (pos.getWorld().getBlockType(pos) != Material.DISPENSER.getId()) throw new CommandException("You can make dispensers only make infinite!");
			
			// get current location
			Location dispenserPosition = new Location(this.plugin.getServer().getWorld(pos.getWorld().getName()), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
			
			Iterator<Location> it = this.plugin.dispenserList.iterator();
			
			while(it.hasNext()) {
				Location currentPosition = it.next();
				
				if (currentPosition.equals(dispenserPosition)) {
					// dispenser already infinite! turn back to normal inventory
					this.plugin.dispenserList.remove(currentPosition);
					
					// save database
					this.plugin.saveDatabase();
					
					// notify player
					player.print("The dispenser is now back in normal mode. It can now run out of contents.");
					return;
				}
			}
			
			// make dispenser infinite
			this.plugin.dispenserList.add(dispenserPosition);
			
			// save database
			this.plugin.saveDatabase();
			
			// notify player
			player.print("The dispenser is now infinite");
			return;
		} else
			throw new CommandException("No block in sight!");
	}
}
