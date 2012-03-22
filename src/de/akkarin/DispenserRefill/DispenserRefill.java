package de.akkarin.DispenserRefill;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.block.CraftDispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class DispenserRefill extends JavaPlugin implements Listener {
	
	java.util.Vector<DispenserPosition> dispenserList = new java.util.Vector<DispenserPosition>();
	
	WorldEditPlugin worldEdit;
	
	@SuppressWarnings("unchecked")
	public void onEnable() {
		// ad log item
		this.getLogger().info("DispenserRefill has been enabled.");
		
		// create data dir if needed
		if (!this.getDataFolder().exists()) if (!this.getDataFolder().mkdirs())
			this.getLogger().log(Level.SEVERE, "Cannot create data folder " + this.getDataFolder().getAbsolutePath() + "!");
		
		// load dispenser list from file
		if ((new File(this.getDataFolder(), "dispensers.dat")).exists()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(this.getDataFolder(), "dispensers.dat")));
				Object result = ois.readObject();
				this.dispenserList = (java.util.Vector<DispenserPosition>) result;
				
				this.getLogger().info("Loaded " + this.dispenserList.size() + " dispensers from database.");
			} catch (Exception ex) {
				this.getLogger().log(Level.WARNING, "Cannot load list of dispensers from database!");
				ex.printStackTrace();
			}
		} else
			this.getLogger().log(Level.WARNING, "Dispenser database does not exist!");
		
		this.worldEdit = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
		
		// register event handlers
		/* this.getServer().getPluginManager().reg
		this.getServer().getPluginManager().registerEvent(BlockDispenseEvent.class, this, this.onDispense, Event.Priority.Normal, this); */
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {
		// save dispenser database
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(this.getDataFolder(), "dispensers.dat")));
			oos.writeObject(this.dispenserList);
			oos.flush();
			oos.close();
			
			this.getLogger().info("Saved dispenser database");
		} catch (Exception ex) {
			this.getLogger().log(Level.SEVERE, "Cannot save the dispenser database!");
			ex.printStackTrace();
		}
		
		// add log item
		this.getLogger().info("DispenserRefill has been disabled.");
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDispense(BlockDispenseEvent event) {	
		if (event.isCancelled()) return;
		
		for(int i = 0; i < this.dispenserList.size(); i++) {
			if (this.dispenserList.get(i).equals(event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())) {				
				// readd item to stack
				CraftDispenser dispenser = new CraftDispenser(event.getBlock());
				
				// create new item stack
				ItemStack newItemList = event.getItem().clone();
				dispenser.getInventory().addItem(newItemList);
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		for(int i = 0; i < this.dispenserList.size(); i++) {
			if (this.dispenserList.get(i).equals(event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())) {
				// remove block from list
				this.dispenserList.remove(i);
				
				// notify user
				this.worldEdit.wrapPlayer(event.getPlayer()).printError("The infinite dispenser at " + event.getBlock().getX() + "," + event.getBlock().getY() + "," + event.getBlock().getZ() + " was eaten ...");
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("autorefill")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You must be a player!");
				return true;
			}
			
			// get player object
			BukkitPlayer player = this.worldEdit.wrapPlayer((Player) sender);
			
			// get position
			WorldVector pos = player.getBlockTrace(300);
			
			if (pos != null) {
				// check for correct block
				if (pos.getWorld().getBlockType(pos) != Material.DISPENSER.getId()) {
					player.printError("You can only make dispensers infinite!");
					return true;
				}
				
				// check for already infinite dispensers
				DispenserPosition dispenserPosition = new DispenserPosition(pos.getWorld().getName(), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
				
				if (args.length <= 0) {
					for(int i = 0; i < this.dispenserList.size(); i++) {
						if (this.dispenserList.get(i).equals(dispenserPosition)) {
							player.printError("This dispenser has already an infinite inventory!");
							return true;
						}
					}
					
					this.dispenserList.add(dispenserPosition);
					player.print("The dispenser at " + pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ() + " has now an infinite inventory.");
				} else if (args[0].equalsIgnoreCase("disable")) {
					// remove dispenser from list
					for(int i = 0; i < this.dispenserList.size(); i++) {
						if (this.dispenserList.get(i).equals(dispenserPosition)) {
							this.dispenserList.remove(i);
							player.print("The dispenser at " + pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ() + " is now back in normal mode.");
							return true;
						}
					}
					
					player.printError("This dispenser isn't in infinite mode!");
				} else
					return false;
			} else
				player.printError("No block in sight!");
			
			return true;
		}
		
		return false;
	}
}
