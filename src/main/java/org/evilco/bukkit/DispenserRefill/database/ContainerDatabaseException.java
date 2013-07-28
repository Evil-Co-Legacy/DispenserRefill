/**
 * 
 */
package org.evilco.bukkit.DispenserRefill.database;

/**
 * @author		Johannes Donath
 * @copyright		2013 Evil-Co.de
 * @license		GNU Lesser Public License <http://www.gnu.org/licenses/lgpl.txt>
 */
public class ContainerDatabaseException extends Exception {
	private static final long serialVersionUID = -3272576372870258789L;
	
	/**
	 * Constructs the exception.
	 * @param ex
	 */
	public ContainerDatabaseException(Exception ex) {
		super(ex);
	}
	
	/**
	 * Constructs the exception.
	 * @param message
	 */
	public ContainerDatabaseException(String message) {
		super(message);
	}
	
	/**
	 * Constructs the exception.
	 * @param message
	 * @param ex
	 */
	public ContainerDatabaseException(String message, Exception ex) {
		super(message, ex);
	}
}
