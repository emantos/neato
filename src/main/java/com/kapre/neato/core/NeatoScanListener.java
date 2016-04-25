package com.kapre.neato.core;

import java.util.List;

public interface NeatoScanListener {
  boolean onComplete(List<NeatoPacket> packets);
}
