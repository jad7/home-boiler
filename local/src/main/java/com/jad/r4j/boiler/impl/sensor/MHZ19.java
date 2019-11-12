package com.jad.r4j.boiler.impl.sensor;


import com.jad.r4j.boiler.utils.Functions;
import com.jad.r4j.boiler.v2.controller.Lifecycle;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Singleton
public class MHZ19 implements AutoCloseable {
    public static final byte[] DATA = Functions.toBytes( 0xff, 1, 0x86, 0, 0, 0, 0, 0, 0x79);
    public static final byte[] CALIBRATE_Z = Functions.toBytes( 0xff, 1, 0x87, 0, 0, 0, 0, 0, 0x78);
    public static final byte[] CALIBRATE_SPAN = Functions.toBytes( 0xff, 1, 0x88, 0x07, 0, 0, 0, 0, 0xA0);
    //public static final byte[] DATA = new byte[]{-1, 1, -122, 0, 0, 0, 0, 0, 121};
    final Serial serial = SerialFactory.createInstance();

    public MHZ19(Lifecycle lifecycle) {
        SerialConfig serialConfig = new SerialConfig();
        serialConfig.baud(Baud._9600);
        serialConfig.parity(Parity.NONE);
        serialConfig.dataBits(DataBits._8);
        serialConfig.stopBits(StopBits._1);
        serialConfig.device("/dev/ttyAMA0");

        try {
            this.serial.open(serialConfig);
            lifecycle.addDestroyable(this::close);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    public int read() throws IOException {
        //System.out.println("Start");
        if (this.serial.isOpen()) {

            this.serial.write(DATA);
            //System.out.println(Arrays.toString(DATA));
            this.serial.flush();

            for(int count = 5; !Thread.currentThread().isInterrupted() && count > 0; --count) {
                int available = this.serial.available();
                if (available == 9) {
                    byte[] read = this.serial.read(available);
                    //System.out.println("Have readed:" + available);
                    //System.out.println("Result bytes:" + Arrays.toString(read));
                    if (read.length > 4
                            && read[0] == (byte)0xff
                            && read[1] == (byte)134
                            && checkSum(read) == read[read.length - 1]) {
                        return (((int)read[2]) & 255) << 8 & read[3];
                    }
                } else {
                    //System.out.println("available only:" + available);

                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException var4) {
                        var4.printStackTrace();
                    }
                }
            }
        }

        return -1;
    }

    @Override
    public void close() {
        if (serial.isOpen()) {
            try {
                this.serial.close();
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    private byte checkSum(byte[] packet) {
        int checksum = 0;
        for(int i = 1; i < 8; i++) {
            checksum += packet[i];
        }
        checksum = 0xff - checksum;
        checksum += 1;
        return (byte) checksum;
    }

    public static void main(String[] args) throws IOException {
        try (MHZ19 mhz19 = new MHZ19(new Lifecycle())) {
            int read;
            do {
                read = mhz19.read();
            } while(read <= 0);

            System.out.println("Result:" + read);
        }

    }
}
