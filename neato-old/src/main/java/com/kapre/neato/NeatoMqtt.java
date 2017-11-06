package com.kapre.neato;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.kapre.neato.core.NeatoDistance;
import com.kapre.neato.core.NeatoPacket;
import com.kapre.neato.core.NeatoReader;
import com.kapre.neato.mqtt.MqqtClient;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

public class NeatoMqtt {

  private static Logger log = Logger.getLogger(NeatoMqtt.class.getName());
  
  private static final String BROKER_URL = "tcp://localhost:1883";
  private static final String CLIENT_ID = "NeatoApp";
  private static final String NEATO_SCAN_START = "neato/scan/start";
  private static final String NEATO_SCAN_STOP = "neato/scan/stop";
  private static final String SCAN360_TOPIC = "neato/packets/360";

  public void run(final double rotationSpeed) {
    AtomicBoolean shouldContinue = new AtomicBoolean(true);

    log.info("Initializing MQTT client.");
    MqqtClient pubSubClient = new MqqtClient(BROKER_URL, CLIENT_ID);

    log.info("Initializing serial connection.");
    Serial serial = SerialFactory.createInstance();
    serial.open(Serial.DEFAULT_COM_PORT, 115200);

    final NeatoReader neato = new NeatoReader(() -> serial.read());

    log.info("Adding shutdown handlers.");
    Runtime.getRuntime().addShutdownHook(newCleanupThread(serial, pubSubClient));

    log.info("Cleaning up old messages in MQTT queues.");
    pubSubClient.publish(NEATO_SCAN_START, null);
    pubSubClient.publish(NEATO_SCAN_STOP, null);
    
    pubSubClient.subscribe(NEATO_SCAN_START, (String message) -> {
      synchronized (this) {
        log.info("Received start event.");
        startLidarRotation(rotationSpeed);
        shouldContinue.set(true);
        notify();
      }
    });

    pubSubClient.subscribe(NEATO_SCAN_STOP, (String message) -> {
      log.info("Received stop event.");
      shouldContinue.set(false);
    });

    log.info("Starting main loop.");
    synchronized (this) {
      while (true) {
        try {
          wait();
          log.info("Starting acquisition.");
          neato.read360Scan((final List<NeatoPacket> packets) -> {
            // publish the scanned packets
            log.finest("Publishing scanned packets");
            pubSubClient.publish(SCAN360_TOPIC, packetsToString(packets));

            // check if we received 'stop' messages
            boolean scanContinue = shouldContinue.get();
            if (!scanContinue) {
              stopLidarRotation();
            }

            return scanContinue;
          });
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private void startLidarRotation(final double rotationSpeed) {
    try {
      log.info("Spinning LIDAR.");
      PiBlaster.setPwm(18, rotationSpeed);
      Thread.sleep(2000);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void stopLidarRotation() {
    try {
      log.info("Stopping LIDAR rotation.");
      PiBlaster.setPwm(18, 0);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String packetsToString(final List<NeatoPacket> packets) {
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

    return sb.toString();
  }

  private Thread newCleanupThread(Serial serial, MqqtClient client) {
    return new Thread() {
      public void run() {
        Objects.requireNonNull(serial);
        Objects.requireNonNull(client);

        log.info("Closing serial connection.");
        serial.close();

        try {
          if (client.isConnected()) {
            log.info("Disconnecting MQTT client.");
            client.disconnect();
          }

          log.info("Stopping LIDAR rotation.");
          PiBlaster.setPwm(18, 0);
          PiBlaster.releasePwm(18);

        } catch (IOException | RuntimeException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }
}
