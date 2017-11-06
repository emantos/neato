package com.kapre.neato.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqqtClient implements MqttCallback {
  
  private static Logger log = Logger.getLogger(MqqtClient.class.getName());
  
  private final int pubQos = 1;
  private final int subQos = 1;
  private final MemoryPersistence persistence = new MemoryPersistence();
  private MqttClient client = null; 
  private Map<String, MqttMessageHandler> handlers = new HashMap<String, MqttMessageHandler>();
  
  public MqqtClient(String brokerUrl, String clientId) {
    try {
      client = new MqttClient(brokerUrl, clientId, persistence);
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession(true);
      client.connect(connOpts);
      client.setCallback(this);
    } catch (MqttException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void publish(String topic, String msg) {
    MqttMessage message = null;
    if (msg != null) {
      message = new MqttMessage(msg.getBytes());
    } else {
      message = new MqttMessage();
    }
    
    message.setQos(pubQos);
    try {
      client.publish(topic, message);
    } catch (MqttException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void subscribe(String topic, MqttMessageHandler handler) {
    try {
      client.subscribe(topic, subQos);
      handlers.put(topic, handler);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public boolean isConnected() {
    return client.isConnected();
  }
  
  public void disconnect() {
    try {
      client.disconnect();
    } catch (MqttException e) {
      throw new RuntimeException(e);
    }
  }

  public void connectionLost(Throwable arg0) {
    log.finest("Function 'connectionLost' unimplemented.");
  }

  public void deliveryComplete(IMqttDeliveryToken arg0) {
    log.finest("Function 'deliveryComplete' unimplemented.");
  }

  public void messageArrived(String topic, MqttMessage msg) throws Exception {
    MqttMessageHandler handler = handlers.get(topic);
    if (handler != null) {
      handler.handle(new String(msg.getPayload()));
    }
  }
}
