package com.kapre.neato;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.kapre.neato.core.NeatoDistance;

public class NeatoDistanceTest {

  @Test
  public void testDistanceFlags() {
    NeatoDistance neatoDistance = new NeatoDistance((char) 0x00, (char) 0x00, (char) 0x00, (char) 0x00);
    assertEquals(neatoDistance.isInvalidFlag(), false);
    assertEquals(neatoDistance.isStrengthWarning(), false);
  }
  
  @Test
  public void testDistanceFlags2() {
    NeatoDistance neatoDistance = new NeatoDistance((char) 0x00, (char) 0xf0, (char) 0x00, (char) 0x00);
    assertEquals(neatoDistance.isInvalidFlag(), true);
    assertEquals(neatoDistance.isStrengthWarning(), true);
  }
  
  @Test
  public void testDistanceFlags3() {
    NeatoDistance neatoDistance = new NeatoDistance((char) 0x00, (char) 0x80, (char) 0x00, (char) 0x00);
    assertEquals(neatoDistance.isInvalidFlag(), true);
    assertEquals(neatoDistance.isStrengthWarning(), false);
  }
  
  @Test
  public void testDistanceFlags4() {
    NeatoDistance neatoDistance = new NeatoDistance((char) 0x00, (char) 0x40, (char) 0x00, (char) 0x00);
    assertEquals(neatoDistance.isInvalidFlag(), false);
    assertEquals(neatoDistance.isStrengthWarning(), true);
  }
  
  @Test
  public void testDistanceValue() {
    NeatoDistance neatoDistance = new NeatoDistance((char) 0xff, (char) 0x3f, (char) 0x00, (char) 0x00);
    NeatoDistance neatoDistance2 = new NeatoDistance((char) 0xff, (char) 0xff, (char) 0x00, (char) 0x00);
    assertEquals(neatoDistance.getDistance(), neatoDistance2.getDistance());
    assertEquals(neatoDistance.getDistance(), 16383);
    
  }

}
