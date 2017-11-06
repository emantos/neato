package com.kapre.neato.core;

import java.util.Arrays;
import java.util.Objects;

public class NeatoPacket {
  public static final char START = 0xFA;
  public static final int PACKET_LENGTH = 22;
  
  public static final int N_DATA_QUADS = 4;                // there are 4 groups of data elements
  public static final int N_ELEMENTS_PER_QUAD = 4;         // viz., 0=distance LSB; 1=distance MSB; 2=sig LSB; 3=sig MSB
  
  // Offsets to bytes within 'Packet'
  private static final int OFFSET_TO_START = 0;
  private static final int OFFSET_TO_INDEX = OFFSET_TO_START + 1;
  private static final int OFFSET_TO_SPEED_LSB = OFFSET_TO_INDEX + 1;
  private static final int OFFSET_TO_SPEED_MSB = OFFSET_TO_SPEED_LSB + 1;
  private static final int OFFSET_TO_4_DATA_READINGS = OFFSET_TO_SPEED_MSB + 1;
  private static final int OFFSET_TO_CRC_L = OFFSET_TO_4_DATA_READINGS + (N_DATA_QUADS * N_ELEMENTS_PER_QUAD);
  private static final int OFFSET_TO_CRC_M = OFFSET_TO_CRC_L + 1;
  
  private char[] data = null;
  
  public NeatoPacket(char[] data) {
    Objects.requireNonNull(data);
    this.data = Arrays.copyOf(data, PACKET_LENGTH);
  }

  public int getSpeedRph() {
    char lowByte = data[OFFSET_TO_SPEED_LSB];
    char highByte = data[OFFSET_TO_SPEED_MSB];
    
    return  ((int) lowByte) | (highByte << 8); 
  }
  
  public double getSpeed() {
    return (double) getSpeedRph() / 64.0;
  }
  
  public int getIndex() {
    return ((int) data[OFFSET_TO_INDEX]) - 160;
  }
  
  public NeatoDistance[] getDistance() {
    NeatoDistance[] distanceMeasurements = new NeatoDistance[4];
    
    for(int i=0; i < distanceMeasurements.length; i++) {
      distanceMeasurements[i] = 
          new NeatoDistance(data[OFFSET_TO_4_DATA_READINGS * i], 
              data[(OFFSET_TO_4_DATA_READINGS * i) + 1], 
              data[(OFFSET_TO_4_DATA_READINGS * i) + 2], 
              data[(OFFSET_TO_4_DATA_READINGS * i) + 3]);
    }
    
    return distanceMeasurements;
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getIndex())
           .append(" [");
    for(char ch : data) {
      builder.append(String.format("%02x ", (int) ch));
    }
    builder.append("]  speed = ")
           .append(getSpeed());
    return builder.toString();
  }
  
  public String toString2() {
    StringBuilder builder = new StringBuilder();
    builder.append(getIndex())
           .append(" [");
    for(NeatoDistance distance : getDistance()) {
      builder.append(distance.toString());
    }
    builder.append("]  speed = ")
           .append(getSpeed());
    return builder.toString();
  }
  
  public String toString3() {
    StringBuilder builder = new StringBuilder();
    builder.append(getIndex())
           .append(" [");
    for(char ch : data) {
      builder.append(String.format("%02x ", (int) ch));
    }
    builder.append("] [");
    for(NeatoDistance distance : getDistance()) {
      builder.append(distance.toString());
    }       
    builder.append("]")
           .append("  speed = ")
           .append(getSpeed());
    return builder.toString();
  }
  
}
