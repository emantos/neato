package com.kapre.neato.core;

public interface NeatoPacketListener {
  /**
   * 
   * @param packet contains the fully formed packet
   * @return should return whether reader should continue or not
   */
  boolean onComplete(NeatoPacket packet);
}
