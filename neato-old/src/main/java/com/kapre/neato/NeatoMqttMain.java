package com.kapre.neato;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class NeatoMqttMain {
  
  private static Logger log = Logger.getLogger(NeatoMqttMain.class.getName());
  
  public static void main(String[] args) {
    if (args.length < 1) {
      log.severe("Need to specify speed");
      System.exit(0);
    }

    // Check pi blaster
    if (!PiBlaster.isAvailable()) {
      log.severe("Pi blaster is not available. Install or run it first.");
      System.exit(0);
    }
    
    NeatoMqtt neato = new NeatoMqtt();
    
    try {
      neato.run(Double.valueOf(args[0]));
    } catch (RuntimeException e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      log.severe(sw.toString());
    }
  }
}
