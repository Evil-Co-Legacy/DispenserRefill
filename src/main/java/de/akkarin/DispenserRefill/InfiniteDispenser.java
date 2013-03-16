/**
 * 
 */
package de.akkarin.DispenserRefill;

import org.bukkit.Location;

/**
 * @author		Johannes Donath
 * @copyright		2013 Evil-Co.de
 * @license		GNU Lesser Public License <http://www.gnu.org/licenses/lgpl.txt>
 */
public class InfiniteDispenser {
	
	/**
	 * Stores the dispenser cooldown.
	 */
	protected int cooldown;
	
	/**
	 * Stores the last dispenser execution time.
	 */
	protected long cooldownPeriod = -1;
	
	/**
	 * Stores the dispenser location.
	 */
	protected final Location location;
	
	/**
	 * Creates a new infinite dispenser.
	 * @param			location			The dispenser's location.
	 */
	public InfiniteDispenser(Location location) {
		this.location = location;
	}
	
	/**
	 * Creates a new infinite dispenser.
	 * @param			location			Block location.
	 * @param			cooldown			Cooldown period.
	 */
	public InfiniteDispenser(Location location, int cooldown) {
		this.location = location;
		this.cooldown = cooldown;
	}
	
	/**
	 * Returns the dispenser cooldown period.
	 * @return
	 */
	public int getCooldown() {
		return this.cooldown;
	}
	
	/**
	 * Returns the last dispenser execution time (-1 if there is no time).
	 * @return
	 */
	public long getCooldownPeriod() {
		return this.cooldownPeriod;
	}
	
	/**
	 * Returns the dispenser's position.
	 * @return
	 */
	public Location getLocation() {
		return this.location;
	}
	
	/**
	 * Checks whether there's a cooldown ongoing.
	 * @param			currentTime
	 * @return
	 */
	public boolean hasCooldownPeriod(long currentTime) {
		return (currentTime - this.cooldown < this.cooldownPeriod);
	}
	
	/**
	 * Sets a new cooldown time.
	 * @param cooldown
	 */
	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}
	
	/**
	 * Sets a new execution time.
	 * @param period
	 */
	public void setCooldownPeriod(long period) {
		this.cooldownPeriod = period;
	}
}
