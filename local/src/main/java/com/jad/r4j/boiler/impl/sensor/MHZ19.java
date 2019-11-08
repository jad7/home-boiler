package com.jad.r4j.boiler.impl.sensor;


import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MHZ19 implements AutoCloseable {
    public static final byte[] DATA = new byte[]{-1, 1, -122, 0, 0, 0, 0, 0, 121};
    final Serial serial = SerialFactory.createInstance();

    public MHZ19() {
        SerialConfig serialConfig = new SerialConfig();
        serialConfig.baud(Baud._9600);
        serialConfig.parity(Parity.NONE);
        serialConfig.dataBits(DataBits._8);
        serialConfig.stopBits(StopBits._1);
        serialConfig.device("/dev/ttyAMA0");

        try {
            this.serial.open(serialConfig);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    public int read() throws IOException {
        System.out.println("Start");
        if (this.serial.isOpen()) {
            this.serial.write(DATA);
            System.out.println(Arrays.toString(DATA));
            this.serial.flush();

            for(int count = 5; !Thread.currentThread().isInterrupted() && count > 0; --count) {
                int available = this.serial.available();
                if (available == 9) {
                    byte[] read = this.serial.read(available);
                    System.out.println("Have readed:" + available);
                    System.out.println("Result bytes:" + Arrays.toString(read));
                    if (read.length > 4 && read[0] == 255 && read[1] == 134 && this.checkSum(read)) {
                        return (read[2] & 255) << 8 & read[3];
                    }
                } else {
                    System.out.println("available only:" + available);

                    try {
                        TimeUnit.MILLISECONDS.sleep(100L);
                    } catch (InterruptedException var4) {
                        var4.printStackTrace();
                    }
                }
            }
        }

        return -1;
    }

    public void close() {
        if (this.serial != null && this.serial.isOpen()) {
            try {
                this.serial.close();
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    private boolean checkSum(byte[] read) {
        System.out.println(Arrays.toString(read));
        return true;
    }

    public static void main(String[] args) throws IOException {
        try (MHZ19 mhz19 = new MHZ19()) {
            int read;
            do {
                read = mhz19.read();
            } while(read <= 0);

            System.out.println("Result:" + read);
        }

    }
}
