package com.kapre.neato.core;

import java.util.Objects;

public class NeatoDistance {
  private static final int OFFSET_DATA_DISTANCE_LSB = 0;
  private static final int OFFSET_DATA_DISTANCE_MSB = OFFSET_DATA_DISTANCE_LSB + 1;
  private static final int OFFSET_DATA_SIGNAL_LSB = OFFSET_DATA_DISTANCE_MSB + 1;
  private static final int OFFSET_DATA_SIGNAL_MSB = OFFSET_DATA_SIGNAL_LSB + 1;
  
  private char[] data = new char[NeatoPacket.N_DATA_QUADS];
  
  public NeatoDistance(char... data) {
    this.data = Objects.requireNonNull(data);
  }
  
  public int getDistance() {
    return ((int) data[OFFSET_DATA_DISTANCE_LSB] | ((data[OFFSET_DATA_DISTANCE_MSB] & 0x3f) << 8));
  }
  
  public int signalStrength() {
    return data[OFFSET_DATA_SIGNAL_LSB] | (data[OFFSET_DATA_SIGNAL_MSB] << 8); 
  }
  
  public boolean isInvalidFlag() {
    return (data[OFFSET_DATA_DISTANCE_MSB] & 0x80) > 0;
  }
  
  public boolean isStrengthWarning() {
    return (data[OFFSET_DATA_DISTANCE_MSB] & 0x40) > 0;
  }

  @Override
  public String toString() {
    return "[distance=" + getDistance() + ", signal=" + signalStrength()
        + ", invalid=" + isInvalidFlag() + ", strength warning=" + isStrengthWarning() + "]";
  }
  
  
}
