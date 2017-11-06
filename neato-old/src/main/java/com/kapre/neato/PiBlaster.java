package com.kapre.neato;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class PiBlaster {
  private static Path piBlasterPath = Paths.get("/dev", "pi-blaster"); 
  
  public static void setPwm(int pin, double value) throws IOException {
    StringBuffer buffer = new StringBuffer();
    buffer.append(pin).append("=").append(value).append("\n");
    Files.write(piBlasterPath, buffer.toString().getBytes(), StandardOpenOption.APPEND);
  }
  
  public static void releasePwm(int pin) throws IOException {
    StringBuffer buffer = new StringBuffer();
    buffer.append("release ").append(pin).append("\n");
    Files.write(piBlasterPath, buffer.toString().getBytes(), StandardOpenOption.APPEND);
  }
  
  public static boolean isAvailable() {
    return Files.exists(piBlasterPath, LinkOption.NOFOLLOW_LINKS);
  }
}
