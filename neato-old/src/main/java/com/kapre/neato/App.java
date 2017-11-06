package com.kapre.neato;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.kapre.neato.core.NeatoDistance;
import com.kapre.neato.core.NeatoPacket;
import com.kapre.neato.core.NeatoReader;
import com.kapre.neato.mqtt.MqqtClient;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

/**
 * Hello world!
 *
 */
public class App {
  
  private static final String BROKER_URL = "tcp://localhost:1883";
  private static final String CLIENT_ID = "NeatoApp";
  private static final String SCAN360_TOPIC = "neato/packets/360";
  
  public static void main(String[] args) {

    try {
      if (args.length < 1) {
        System.out.println("Need to specify speed");
        System.exit(0);
      }

      // Check pi blaster
      if (!PiBlaster.isAvailable()) {
        System.out.println("Pi blaster is not available. Install or run it first.");
        System.exit(0);
      }

      System.out.println("Initializing broadcaster.");
      MqqtClient pubSubClient = new MqqtClient(BROKER_URL, CLIENT_ID);
      
      System.out.println("Initializing serial connection.");
      Serial serial = SerialFactory.createInstance();
      serial.open(Serial.DEFAULT_COM_PORT, 115200);
      
      System.out.println("Spinning LIDAR.");
      PiBlaster.setPwm(18, Double.valueOf(args[0]));
      Thread.sleep(2000);
      
      System.out.println("Adding shutdown handlers.");
      Runtime.getRuntime().addShutdownHook(newCleanupThread(serial, pubSubClient));

      System.out.println("Starting acquisition.");
      final NeatoReader neato = new NeatoReader(() -> serial.read());

      neato.read360Scan((final List<NeatoPacket> packets) -> {
        StringBuilder sb = new StringBuilder();
        packets.forEach((NeatoPacket a) -> {
          for (NeatoDistance d : a.getDistance()) {
            if (d.isInvalidFlag()) {
              sb.append("0 ");
            } else {
              sb.append(d.getDistance()).append(" ");
            }
          }
        });

        pubSubClient.publish(SCAN360_TOPIC, sb.toString());
        return true;
      });

    } catch (SerialPortException e) {
      e.printStackTrace();
    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (RuntimeException e1) {
      e1.printStackTrace();
    }
  }
  
  private static Thread newCleanupThread(Serial serial, MqqtClient client) {
    return new Thread() {
      public void run() {
        Objects.requireNonNull(serial);
        Objects.requireNonNull(client);
        
        System.out.println("\nClosing serial connection.");
        serial.close();

        try {
          if (client.isConnected()) {
            System.out.println("Disconnecting MQTT client.");
            client.disconnect();
          }
            
          System.out.println("Stopping LIDAR rotation.");
          PiBlaster.setPwm(18, 0);
          PiBlaster.releasePwm(18);

        } catch (IOException | RuntimeException e) {
          e.printStackTrace();
        }
      }
    };
  }
}
