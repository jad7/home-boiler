package com.jad.r4j.boiler.impl.sensor;

import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Configuration;
import com.jad.r4j.boiler.config.Destroyable;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;

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
 * @since 11/08/2018
 */

@Slf4j
public class TM1637Python implements Destroyable {

    private final Process process;
    private final PrintStream output;

    public TM1637Python(File file) {
        try {
            final File err = new File(Paths.get("").toAbsolutePath().toFile(), "error.ptn");
            if (err.exists()) {
                err.delete();
            }
            err.createNewFile();
            final File out = new File(Paths.get("").toAbsolutePath().toFile(), "out.ptn");
            if (out.exists()) {
                out.delete();
            }
            out.createNewFile();
            process = new ProcessBuilder(Arrays.asList("python", file.getAbsolutePath()))
                    .redirectError(err)
                    .redirectOutput(ProcessBuilder.Redirect.to(out))
                    //.inheritIO()
                    .directory(file.getParentFile())
                    .start();
            output = new PrintStream(new BufferedOutputStream(process.getOutputStream()), true, "UTF-8");


            log.info("Python display ready");
        } catch (IOException e) {
            throw new RuntimeException("Can not start process", e);
        }
    }

    public void setLisa() {
        send("lisa");
    }

    public void setDigit(double value) {
        send("num " + (int) value + " " + ((int) (value * 100) - ((int) value * 100)));
    }

    private void send(String str) {
        if (!process.isAlive()) {
            log.error("Python dispay process is dead");
        }
        if (Configuration.debug) {
            log.info("Sent to display: \"{}\"", str);
        }
        output.println(str);
        output.flush();
    }


    public void setError(int code) {
        send("err " + code);
    }

    public void changed() {
        send("changed");
    }

    @Override
    public void destroy() {
        process.destroy();
    }
}
