package com.kapre.neato.pi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

import com.kapre.neato.core.NeatoHardwareAbstraction;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

public class PiConnectedNeatoHardware implements NeatoHardwareAbstraction {

	private static Logger log = Logger.getLogger(PiConnectedNeatoHardware.class.getName());

	private static Path piBlasterPath = Paths.get("/dev", "pi-blaster");
	
	public static String DEFAULT_SERIAL_PORT = Serial.DEFAULT_COM_PORT;

	private int pwmPin;
	
	private String serialPort;

	Serial serial = SerialFactory.createInstance();

	public PiConnectedNeatoHardware(int pwmPin, String serialPort) {
		if (!isPiBlasterAvailable()) {
			throw new NoPiBlasterException();
		}

		this.pwmPin = pwmPin;
		this.serialPort = serialPort == null ? DEFAULT_SERIAL_PORT : serialPort;
	}
	
	@Override
	public boolean open() {
		serial.open(serialPort, 115200);
		return true;
	}


	@Override
	public char getNextByte() {
		return serial.read();
	}
	
	@Override
	public boolean close() {
		serial.close();
		
		try {
			Files.write(piBlasterPath, String.format("release %d\n", pwmPin).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new LidarSpinException("Cannot release PWM pin of lidar", e);
		}
		
		return true;
	}

	@Override
	public boolean spin(float speed) {
		if (speed < 0 || speed > 1) {
			throw new IllegalArgumentException("Speed can only be between 0 and 1");
		}
		
		log.info("Spinning LIDAR [PWM Pin: " + pwmPin  + "] [Speed: " + speed + "]");
		try {
			Files.write(piBlasterPath, String.format("%d=%f\n", pwmPin, speed).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new LidarSpinException("Cannot spin lidar.", e);
		}
		
		return true;
	}

	public static boolean isPiBlasterAvailable() {
		return Files.exists(piBlasterPath, LinkOption.NOFOLLOW_LINKS);
	}
}
