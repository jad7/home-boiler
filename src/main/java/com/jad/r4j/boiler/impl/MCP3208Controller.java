package com.jad.r4j.boiler.impl;

import com.jad.r4j.boiler.config.Configuration;
import com.pi4j.gpio.extension.mcp.MCP3208Pin;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import lombok.Getter;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Provider;
import java.io.IOException;

/**

 * <p>
 * Developed by Grid Dynamics International, Inc. for the customer Art.com.
 * http://www.griddynamics.com
 * <p>
 * Classification level: Confidential
 * <p>
 * EXCEPT EXPRESSED BY WRITTEN WRITING, THIS CODE AND INFORMATION ARE PROVIDED "AS IS"
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * For information about the licensing and copyright of this document please
 * contact Grid Dynamics at info@griddynamics.com.
 *
 * @since 11/05/2018
 */
@Slf4j
public class MCP3208Controller {

    private final SpiDevice device;

    @Getter
    private double voltage;

    public MCP3208Controller(SpiChannel channel, int speed, SpiMode spiMode, double voltage) throws IOException {
        this.voltage = voltage;
        this.device = SpiFactory.getInstance(channel, speed, spiMode);
    }

    //111111011010
    //111000101010
    public Integer pinReaderSignal(Pin pin) {
        final int address = pin.getAddress();
        final byte[] result;
        try {
            /*result = new byte[] {1,0,0};
            if (1==2) {
                throw new IOException();
            }*/
            final byte[] writeBytes = {((byte) (6 + (address > 3 ? 1 : 0))), (byte) ((address & 3) << 6), (byte) 0};
            if (Configuration.debug) {
                logBinaryString("Data send    :", writeBytes);
            }
            result = device.write(writeBytes);
            if (Configuration.debug) {
                logBinaryString("Data received:", result);
            }
        } catch (IOException e) {
            log.error("Can not perform read data from SPI: {}", address, e);
            return -1;
        }
        if (result != null && result.length == 3) {
            return ((result[1] & 15) << 8) + (result[2] < 0 ? 128 + (result[2] & 127) : result[2]);
        }
        return -1;
    }

    private void logBinaryString(String prefix, byte[] arr) {
        StringBuilder sb = new StringBuilder(27);
        for (int i = 0; i < 3; i++) {
            sb.append(String.format("%8s", Integer.toBinaryString(arr[i] & 0xFF)).replace(' ', '0'));
            sb.append('|');

        }
        log.info(prefix + " " + sb.toString());
    }

    public Integer getMaxValue() {
        return 4096;
    }

    public interface MCP3208PinOutput extends Provider<Integer>{}
}
