package de.akkarin.DispenserRefill;

import java.io.Serializable;

public class DispenserPosition implements Serializable {
	private static final long serialVersionUID = 2325066739818088575L;

	private double positionX = 0;
	private double positionY = 0;
	private double positionZ = 0;
	
	private String world;
	
	public DispenserPosition(String world, double x, double y, double z) {
		this.world = world;
		this.positionX = x;
		this.positionY = y;
		this.positionZ = z;
	}
	
	public String getWorld() {
		return this.world;
	}
	
	public double getPositionX() {
		return this.positionX;
	}
	
	public double getPositionY() {
		return this.positionY;
	}
	
	public double getPositionZ() {
		return this.positionZ;
	}
	
	public boolean equals(String world, double x, double y, double z) {
		return (world == this.world && x == this.positionX && y == this.positionY && z == this.positionZ);
	}
	
	public boolean equals(DispenserPosition a) {
		return this.equals(a.getWorld(), a.getPositionX(), a.getPositionY(), a.getPositionZ());
	}
}
