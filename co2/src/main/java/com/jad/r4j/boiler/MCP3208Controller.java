package com.jad.r4j.boiler;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MCP3208Controller {
   private static final Logger log = LoggerFactory.getLogger(MCP3208Controller.class);
   private final SpiDevice device;
   private double voltage;

   public MCP3208Controller(SpiChannel channel, int speed, SpiMode spiMode, double voltage) throws IOException {
      this.voltage = voltage;
      this.device = SpiFactory.getInstance(channel, speed, spiMode);
   }

   public Integer pinReaderSignal(Pin pin) {
      int address = pin.getAddress();

      byte[] result;
      try {
         byte[] writeBytes = new byte[]{(byte)(6 + (address > 3 ? 1 : 0)), (byte)((address & 3) << 6), 0};


         result = this.device.write(writeBytes);

      } catch (IOException var5) {
         log.error((String)"Can not perform read data from SPI: {}", (Object)address, (Object)var5);
         return -1;
      }

      return result != null && result.length == 3 ? ((result[1] & 15) << 8) + (result[2] < 0 ? 128 + (result[2] & 127) : result[2]) : -1;
   }

   private void logBinaryString(String prefix, byte[] arr) {
      StringBuilder sb = new StringBuilder(27);

      for(int i = 0; i < 3; ++i) {
         sb.append(String.format("%8s", Integer.toBinaryString(arr[i] & 255)).replace(' ', '0'));
         sb.append('|');
      }

      log.info(prefix + " " + sb.toString());
   }

   public Integer getMaxValue() {
      return 4096;
   }

   public double getVoltage() {
      return this.voltage;
   }

}
