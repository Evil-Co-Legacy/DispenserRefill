package de.akkarin.DispenserRefill;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.logging.Level;

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
import de.akkarin.DispenserRefill.database.ContainerDatabaseException;
import de.akkarin.DispenserRefill.database.DatabaseMigrator;
import de.akkarin.DispenserRefill.database.DispenserDatabaseMigrationException;
import de.akkarin.DispenserRefill.database.YAMLDatabase;

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
	 * Manager for container lists.
	 */
	private YAMLDatabase database;
	
	/**
	 * Creates a new instance of type DispenserRefillPlugin
	 * @throws FileNotFoundException 
	 */
	public DispenserRefillPlugin() throws FileNotFoundException {
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
	 * Checks permissions and throws an exception if permission is not met.
	 * @param sender The sender to check the permission on.
	 * @param perm The permission to check the permission on.
	 * @throws CommandPermissionsException if {@code sender} doesn't have {@code perm}
	 */
	public void checkPermission(CommandSender sender, String perm) throws CommandPermissionsException {
		if (!this.hasPermission(sender, perm)) throw new CommandPermissionsException();
	}
	
	/**
	 * Returns the database manager.
	 */
	public YAMLDatabase getContainerDatabase() {
		return this.database;
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
	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
	 */
	public void onDisable() {
		this.saveDatabase();
		
		// add log item
		this.getLogger().info("DispenserRefill has been disabled.");
	}
	
	/**
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	public void onEnable() {
		// Set the proper command injector
		commands.setInjector(new SimpleInjector(this));
		
		// create database instance
		try {
			this.database = new YAMLDatabase(new File(this.getDataFolder(), "containers.yml"), this.getLogger(), this);
		} catch (FileNotFoundException ex) {
			this.getLogger().log(Level.SEVERE, "Cannot init database!", ex);
		}
			
		// Register command classes
		this.getLogger().finest("Hooking commands.");
		final CommandsManagerRegistration reg = new CommandsManagerRegistration(this, this.commands);
		reg.register(GeneralCommands.class);
		
		// need to create the plugins/Locker folder
		this.getLogger().finest("Creating data dirs.");
		this.getDataFolder().mkdirs();
		
		// init permissions
		PermissionsResolverManager.initialize(this);
		
		// load DB
		try {
			this.database.load();
		} catch (ContainerDatabaseException ex) {
			this.getLogger().log(Level.WARNING, "Cannot load container file. No data loaded: Creating new file on next save!", ex);
		}
		
		// migrate
		DatabaseMigrator migrator = new DatabaseMigrator(new File(this.getDataFolder(), "dispensers.dat"), this.getLogger(), this.database, this);
		try {
			migrator.migrate();
		} catch (NoSuchFileException e) {
			; // ignore (there is just no dispensers.dat)
		} catch (DispenserDatabaseMigrationException ex) {
			this.getLogger().log(Level.WARNING, "Cannot import old container database!", ex);
		}
		
		// register events
		(new DispenserRefillWorldListener(this)).registerEvents();
	}
	
	/**
	 * Saves the container database.
	 */
	public void saveDatabase() {
		try {
			this.database.save();
		} catch (ContainerDatabaseException ex) {
			this.getLogger().log(Level.SEVERE, "Cannot save container file!", ex);
		}
	}
}
