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
import lombok.extern.slf4j.Slf4j;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


//sudo systemctl start serial-getty@ttyAMA0.service
@Slf4j
@Singleton
public class MHZ19 implements AutoCloseable {
    public static final byte[] DATA = Functions.toBytes( 0xff, 1, 0x86, 0, 0, 0, 0, 0, 0x79);
    public static final byte[] CALIBRATE_Z = Functions.toBytes( 0xff, 1, 0x87, 0, 0, 0, 0, 0, 0x78);
    public static final byte[] CALIBRATE_SPAN = Functions.toBytes( 0xff, 1, 0x88, 0x07, 0, 0, 0, 0, 0xA0);
    //public static final byte[] DATA = new byte[]{-1, 1, -122, 0, 0, 0, 0, 0, 121};
    final Serial serial = SerialFactory.createInstance();

    @Inject
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
                    log.info("Have readed: {} Result bytes: {} ",  available, Arrays.toString(read));
                    if (read.length > 4
                            && read[0] == (byte)0xff
                            && read[1] == (byte)134
                            && checkSum(read) == read[read.length - 1]) {
                        return (((int)read[2]) & 255) << 8 & read[3];
                    }
                } else {
                    log.info("available only:" + available);

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

    public static void main(String[] args) throws Exception {
        influxDB();
    }



    private static void influxDB() {
        log.info("Application started");
        final String serverURL = "http://127.0.0.1:8086", username = "root", password = "root";
        try (final MHZ19 mhz19 = new MHZ19(new Lifecycle());
            final InfluxDB influxDB = InfluxDBFactory.connect(serverURL)) {
            log.info("Connected to DB version: {}", influxDB.version());

            String databaseName = "boiler";
            influxDB.setDatabase(databaseName);


            //influxDB.enableBatch(BatchOptions.DEFAULTS);

            while (!Thread.currentThread().isInterrupted()) {
                int read;
                do {
                    read = mhz19.read();
                    log.info("CO2: {}", read);
                } while (read <= 0);
                influxDB.write(Point.measurement("sensors")
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .tag("sensor", "co2")
                        .addField("value", read)
                        .build());
                TimeUnit.SECONDS.sleep(10);
            }


        } catch (Exception e) {
            log.error("Error", e);
        }
    }
    private static void singleRead() throws IOException {
        try (MHZ19 mhz19 = new MHZ19(new Lifecycle())) {
            int read;
            do {
                read = mhz19.read();
            } while(read <= 0);

            System.out.println("Result:" + read);
        }
    }
}
