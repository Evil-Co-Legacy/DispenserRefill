package de.akkarin.DispenserRefill;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.SimpleInjector;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import de.akkarin.DispenserRefill.commands.GeneralCommands;

/**
 * @author		Johannes Donath
 * @copyright		2012 Evil-Co.de <http://www.evil-co.de>
 * @package		de.akkarin.DispenserRefill
 */
public class DispenserRefillPlugin extends JavaPlugin implements Listener {
	
	/**
	 * Manager for commands. This automatically handles nested commands,
	 * permissions checking, and a number of other fancy command things.
	 * We just set it up and register commands against it.
	 */
	private final CommandsManager<CommandSender> commands;
	
	/**
	 * Contains a list of all dispensers
	 */
	public java.util.Vector<DispenserPosition> dispenserList = new java.util.Vector<DispenserPosition>();
	
	/**
	 * Creates a new instance of type DispenserRefillPlugin
	 */
	public DispenserRefillPlugin() {
		// create a local copy of this instance
		final DispenserRefillPlugin plugin = this;
		
		// init command manager
		this.commands = new CommandsManager<CommandSender>() {
			@Override
			public boolean hasPermission(CommandSender player, String perm) {
				return plugin.hasPermission(player, perm);
			}
		};
	}
	
	/**
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	public void onEnable() {
		// Set the proper command injector
		commands.setInjector(new SimpleInjector(this));
			
		// Register command classes
		this.getLogger().finest("Hooking commands.");
		final CommandsManagerRegistration reg = new CommandsManagerRegistration(this, this.commands);
		reg.register(GeneralCommands.class);
		
		// need to create the plugins/Locker folder
		this.getLogger().finest("Creating data dirs.");
		this.getDataFolder().mkdirs();
		
		// init permissions
		PermissionsResolverManager.initialize(this);
		
		// get database
		this.loadDatabase();
		
		// register events
		this.getLogger().finest("Registering events.");
		(new DispenserRefillWorldListener(this)).registerEvents();
	}
	
	/**
	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
	 */
	public void onDisable() {
		// save dispenser database
		this.saveDatabase();
		
		// add log item
		this.getLogger().info("DispenserRefill has been disabled.");
	}
	
	/**
	 * Handle a command.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try {
			this.commands.execute(cmd.getName(), args, sender, sender);
		} catch (CommandPermissionsException e) {
			sender.sendMessage(ChatColor.RED + "You don't have permission.");
		} catch (MissingNestedCommandException e) {
			sender.sendMessage(ChatColor.RED + e.getUsage());
		} catch (CommandUsageException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			sender.sendMessage(ChatColor.RED + e.getUsage());
		} catch (WrappedCommandException e) {
			if (e.getCause() instanceof NumberFormatException)
				sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
			else {
				sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
				e.printStackTrace();
			}
		} catch (CommandException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
        
		return true;
	}
	
	/**
	 * Checks permissions.
	 * @param sender The sender to check the permission on.
	 * @param perm The permission to check the permission on.
	 * @return whether {@code sender} has {@code perm}
	 */
	public boolean hasPermission(CommandSender sender, String perm) {
		if (sender.isOp()) return true;
		
		// Invoke the permissions resolver
		if (sender instanceof Player) {
			Player player = (Player) sender;
			return PermissionsResolverManager.getInstance().hasPermission(player.getWorld().getName(), player.getName(), perm);
		}

		return false;
	}
	
	/**
	 * Checks permissions and throws an exception if permission is not met.
	 * @param sender The sender to check the permission on.
	 * @param perm The permission to check the permission on.
	 * @throws CommandPermissionsException if {@code sender} doesn't have {@code perm}
	 */
	public void checkPermission(CommandSender sender, String perm) throws CommandPermissionsException {
		if (!this.hasPermission(sender, perm)) throw new CommandPermissionsException();
	}
	
	/**
	 * Loads the database
	 */
	@SuppressWarnings("unchecked")
	public void loadDatabase() {
		// load dispenser list from file
		if ((new File(this.getDataFolder(), "dispensers.dat")).exists()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(this.getDataFolder(), "dispensers.dat")));
				Object result = ois.readObject();
				this.dispenserList = (java.util.Vector<DispenserPosition>) result;
				
				this.getLogger().info("Loaded " + this.dispenserList.size() + " dispensers from database.");
			} catch (Exception ex) {
				this.getLogger().severe("Cannot load dispenser.dat!");
				ex.printStackTrace();
			}
		} else
			this.getLogger().finest("There's no dispensers.dat! Creating one on next save.");
	}
	
	/**
	 * Saves the database.
	 */
	public void saveDatabase() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(this.getDataFolder(), "dispensers.dat")));
			oos.writeObject(this.dispenserList);
			oos.flush();
			oos.close();
			
			this.getLogger().finest("Saved " + this.dispenserList.size() + " dispensers to database");
		} catch (Exception ex) {
			this.getLogger().severe("Cannot save dispensers.dat!");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Returns the WorldEdit instance
	 * Note: Use this only for optional WorldEdit hooks!
	 * @return
	 */
	public WorldEditPlugin getWorldEdit() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
		 
		// Tony: This one is for soft dependencies!
		if (plugin == null || !(plugin instanceof WorldEditPlugin))
			return null;
		
		return (WorldEditPlugin) plugin;
	}
}
