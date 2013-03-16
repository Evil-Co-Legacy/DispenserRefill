/**
 * 
 */
package de.akkarin.DispenserRefill.database;

/**
 * @author		Johannes Donath
 * @copyright		2013 Evil-Co.de
 * @license		GNU Lesser Public License <http://www.gnu.org/licenses/lgpl.txt>
 */
public class DispenserDatabaseMigrationException extends ContainerDatabaseException {
	private static final long serialVersionUID = 1187975392599095807L;

	/**
	 * Constructs the exception.
	 * @param ex
	 */
	public DispenserDatabaseMigrationException(Exception ex) {
		super(ex);
	}
	
	/**
	 * Constructs the exception.
	 * @param message
	 */
	public DispenserDatabaseMigrationException(String message) {
		super(message);
	}
	
	/**
	 * Constructs the exception.
	 * @param message
	 * @param ex
	 */
	public DispenserDatabaseMigrationException(String message, Exception ex) {
		super(message, ex);
	}
	
}
