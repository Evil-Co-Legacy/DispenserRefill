package de.akkarin.DispenserRefill;

import java.io.Serializable;

public class DispenserPosition implements Serializable {
	private static final long serialVersionUID = 2325066739818088575L;

	private int positionX = 0;
	private int positionY = 0;
	private int positionZ = 0;
	
	public DispenserPosition(int x, int y, int z) {
		this.positionX = x;
		this.positionY = y;
		this.positionZ = z;
	}
	
	public int getPositionX() {
		return this.positionX;
	}
	
	public int getPositionY() {
		return this.positionY;
	}
	
	public int getPositionZ() {
		return this.positionZ;
	}
	
	public boolean equals(int x, int y, int z) {
		return (x == this .positionX && z == this .positionZ && z == this .positionZ);
	}
	
	public boolean equals(DispenserPosition a) {
		return this.equals(a.getPositionX(), a.getPositionY(), a.getPositionZ());
	}
}
