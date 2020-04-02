package com.jad.r4j.boiler;


import com.jad.r4j.boiler.utils.Functions;
import com.pi4j.gpio.extension.mcp.MCP3208Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.serial.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiMode;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


//sudo systemctl start serial-getty@ttyAMA0.service
@Slf4j
public class MHZ19 implements AutoCloseable {
    public static final byte[] DATA = Functions.toBytes( 0xff, 1, 0x86, 0, 0, 0, 0, 0, 0x79);
    public static final byte[] CALIBRATE_Z = Functions.toBytes( 0xff, 1, 0x87, 0, 0, 0, 0, 0, 0x78);
    public static final byte[] CALIBRATE_SPAN = Functions.toBytes( 0xff, 1, 0x88, 0x07, 0, 0, 0, 0, 0xA0);
    //public static final byte[] DATA = new byte[]{-1, 1, -122, 0, 0, 0, 0, 0, 121};
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

    public Integer read() throws IOException {
        //System.out.println("Start");
        //if (this.serial.isOpen()) {
        //while (!Thread.currentThread().isInterrupted()) {
            this.serial.write(DATA);
            //System.out.println(Arrays.toString(DATA));
            this.serial.flush();

            ByteBuffer byteBuffer = ByteBuffer.allocate(9);
            //for(int count = 5; !Thread.currentThread().isInterrupted() && count > 0; --count) {
            //    int available = this.serial.available();
            this.serial.read(9, byteBuffer);
            byte[] read = byteBuffer.array();
            //if (byteBuffer.position() >= 9) {
            //    byteBuffer.flip();
            //log.info("Have readed: {} Result bytes: {} ",  available, Arrays.toString(read));

            if (read.length > 4
                    && read[0] == (byte) 0xff
                    && read[1] == (byte) 134
                    && checkSum(read) == read[read.length - 1]) {
                return ((((int) read[2]) & 255) << 8) + Byte.toUnsignedInt(read[3]);
            }

                /*System.out.println("Data received:");
                for (byte b : read) {
                    System.out.print(Byte.toUnsignedInt(b) + " ");
                }
                System.out.println();*/

                /*} else {
                    log.info("available only:" + available);


                }*/
            //}
        //}
         //throw new RuntimeException("My runtime");
        //}

        return null;
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
        try {
            singleRead();
            //influxDB();

        } catch (Exception e) {
            e.printStackTrace();
            //log.error(e.getMessage(), e);
        }
    }



    private static void influxDB() {
        log.info("Application started");
        final String serverURL = "http://127.0.0.1:8086", username = "root", password = "root";
        try (final MHZ19 mhz19 = new MHZ19();
            final InfluxDB influxDB = InfluxDBFactory.connect(serverURL)) {
            log.info("Connected to DB version: {}", influxDB.version());

            String databaseName = "boiler";
            influxDB.setDatabase(databaseName);
            influxDB.setRetentionPolicy("two_days");


            //influxDB.enableBatch(BatchOptions.DEFAULTS);

            long lastTime = 0;
            while (!Thread.currentThread().isInterrupted()) {
                int read;
                do {
                    read = mhz19.read();
                    log.info("CO2: {}", read);
                } while (read <= 0);
                influxDB.write(Point.measurement("sensors")
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .tag("sensor ", "co2")
                        .addField("value", read)
                        .build());
                //influxDB.flush();
                long now = System.currentTimeMillis();
                TimeUnit.MILLISECONDS.sleep(Math.max(0, lastTime + 10_000 - now)); //10sec
                lastTime = now;
            }


        } catch (Exception e) {
            log.error("Error", e);
        }
    }
    private static void singleRead() throws IOException {
        try (MHZ19 mhz19 = new MHZ19()) {
            int read;
            do {
                read = mhz19.read();
            } while(read <= 0);

            System.out.println("Result:" + read);
        }
    }

    public Observable<SensorValue> getObservable(Scheduler scheduler) {
        return Observable.interval(0, 10, TimeUnit.SECONDS, scheduler)
                .map(v -> {
                    int retry = 10;
                    Integer read = null;
                    do {
                        read = read();
                        log.debug("CO2: {}", read);
                    } while ((read == null || read <= 0) && retry-- > 0);
                    return read;
                })
                .filter(Objects::nonNull)
                .map(val -> new SensorValue("co2", val.doubleValue()));

    }
}
