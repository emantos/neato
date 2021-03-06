package com.kapre.neato.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class NeatoReader {
  public static final int LOOK_FOR_START_MARKER = 0;
  public static final int ACQUIRE_REST_PACKET = LOOK_FOR_START_MARKER + 1;
  
  private NeatoHardwareAbstraction device = null;
  
  public NeatoReader(NeatoHardwareAbstraction device) {
    this.device = Objects.requireNonNull(device);
  }
  
  public void readPackets(NeatoPacketListener listener) {
    Objects.requireNonNull(listener);
    
    char[] packetBuffer = new char[22];
    int currentBufferLength = 0;
    int state = LOOK_FOR_START_MARKER;
    boolean continueAcquisition = true;
    while (continueAcquisition) {
      char ch = device.getNextByte();
      switch (state) {
        case LOOK_FOR_START_MARKER:
          if (ch == NeatoPacket.START) {
            packetBuffer[0] = ch;
            currentBufferLength = 1;
            state = ACQUIRE_REST_PACKET;
          }
          break;
        case ACQUIRE_REST_PACKET:
          packetBuffer[currentBufferLength] = ch;
          currentBufferLength += 1;
          if (currentBufferLength == NeatoPacket.PACKET_LENGTH) {
            state = LOOK_FOR_START_MARKER;
            continueAcquisition = listener.onComplete(new NeatoPacket(packetBuffer));
          }
          break;
      }
    }
  }
  
  public void read360Scan(NeatoScanListener listener) {
    final List<NeatoPacket> packets = new ArrayList<NeatoPacket>();
    readPackets((NeatoPacket packet) -> {
      if (packet.getIndex() == 0) {
        packets.add(packet);
      } else {
        if (!packets.isEmpty()) {
          packets.add(packet);
        }
      }
      if (packets.size() == 90) {
        boolean isContinue = listener.onComplete(packets);
        packets.clear();
        return isContinue;
      } else {
        return true;
      }
    });
  }
  
  public CompletableFuture<List<NeatoPacket>> read360Scan() {
	CompletableFuture<List<NeatoPacket>> future = new CompletableFuture<>();
	try {
	  new Thread(() -> {
		  read360Scan((final List<NeatoPacket> packets) -> {
			future.complete(packets);
			return false;
		  });
	  	}).start();
	} catch (Exception e) {
		future.completeExceptionally(e);
	}
	return future;
  }
}
