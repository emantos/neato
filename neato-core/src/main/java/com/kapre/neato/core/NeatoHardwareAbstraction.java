package com.kapre.neato.core;

public interface NeatoHardwareAbstraction {
	/**
	 * Opens the device
	 * 
	 * @return if successful
	 */
	boolean open();
	
	/**
	 * An implementation of this should get a single character from the Neato
	 * device stream
	 * 
	 * @return character from stream
	 */
	char getNextByte();

	/**
	 * Closes the device
	 * 
	 * @return if successful
	 */
	boolean close();

	/**
	 * An implementation of this should spin the Neato LIDAR given speed.
	 * This number should only be between 0 and 1 where 1 is full speed and
	 * 0 is full stop.
	 * 
	 * @return if spinning the lidar was successful
	 */
	boolean spin(float speed);
}
