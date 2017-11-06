package com.kapre.neato.mqtt;

public interface MqttMessageHandler {
  void handle(String message);
}
