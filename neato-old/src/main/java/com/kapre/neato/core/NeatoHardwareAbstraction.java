package com.kapre.neato.core;

public interface NeatoHardwareAbstraction {
	/**
	 * An implementation of this should get a single character from the Neato device stream
	 * 
	 * @return character from stream
	 */
  char getNextByte();
}
