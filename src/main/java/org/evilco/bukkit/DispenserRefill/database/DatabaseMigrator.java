/**
 * 
 */
package org.evilco.bukkit.DispenserRefill.database;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.NoSuchFileException;
import java.util.Scanner;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author		Johannes Donath
 * @copyright		2013 Evil-Co.de
 * @license		GNU Lesser Public License <http://www.gnu.org/licenses/lgpl.txt>
 */
public class DatabaseMigrator {
	
	/**
	 * Stores the new database instance.
	 */
	protected YAMLDatabase database;
	
	/**
	 * Stores the data file to parse.
	 */
	protected File dataFile;
	
	/**
	 * Stores the logger to use.
	 */
	protected Logger logger;
	
	/**
	 * Stores the parent plugin instance.
	 */
	protected JavaPlugin plugin;
	
	/**
	 * Constructs a new database migrator.
	 * @param file
	 * @param logger
	 * @param database
	 * @param plugin
	 */
	public DatabaseMigrator(File file, Logger logger, YAMLDatabase database, JavaPlugin plugin) {
		// store parameters
		this.dataFile = file;
		this.logger = logger;
		this.database = database;
		this.plugin = plugin;
	}
	
	/**
	 * Migrates an old data file into new YAML database API.
	 * @throws DispenserDatabaseMigrationException
	 * @throws NoSuchFileException
	 */
	public void migrate() throws DispenserDatabaseMigrationException, NoSuchFileException {
		// check for data file
		if (!this.dataFile.exists()) throw new NoSuchFileException("Cannot find file " + dataFile.getPath() + "!");
		
		// create scanner
		Scanner scanner = null;
		
		// start scanning
		try {
			// create scanner with input stream
			scanner = new Scanner(new FileInputStream(this.dataFile), "UTF-8");
			
			// loop through lines
			while(scanner.hasNextLine()) {
				// get unified line
				String line = scanner.nextLine().replace("\n", "").replace("\r", "");
				
				// skip empty lines
				if (line.equalsIgnoreCase("") || line.equalsIgnoreCase(" ")) continue;
				
				// split line
				String[] lineEx = line.split(";");
				
				// parse elements
				double x = Double.parseDouble(lineEx[0]);
				double y = Double.parseDouble(lineEx[1]);
				double z = Double.parseDouble(lineEx[2]);
				World world = this.plugin.getServer().getWorld(lineEx[3]);
				int cooldown = (lineEx.length >= 5 ? Integer.parseInt(lineEx[4]) : -1);
				
				// init variables
				Location location = new Location(world, x, y, z);
				boolean existingContainer = false;
				
				//db check
				for(InfiniteContainer container : this.database.getContainerList()) {
					if (container.getLocation().equals(location)) {
						existingContainer = true;
						break;
					}
				}
				
				// does this container already exist in our db?
				if (existingContainer) continue;
				
				// decode
				this.database.getContainerList().add(new InfiniteContainer(location, cooldown));
			}
			
			// store new database file
			this.database.save();
			
			// log
			this.logger.info("Imported " + this.database.getContainerList().size() + " containers from old data file.");
			this.logger.info("Deleting old data file ...");
			
			// remove file
			if (!this.dataFile.delete()) this.logger.warning("Cannot remove old data file. Please remove " + this.dataFile.getPath() + " manually!");
		} catch (Exception ex) {
			throw new DispenserDatabaseMigrationException(ex);
		} finally {
			if (scanner != null) scanner.close();
		}
	}
}
