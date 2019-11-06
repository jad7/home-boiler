package com.jad.r4j.boiler.impl.sensor;

import com.jad.r4j.boiler.config.ConfigurationParent;
import com.jad.r4j.boiler.config.Destroyable;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Paths;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TM1637Python implements Destroyable {
   private static final Logger log = LoggerFactory.getLogger(TM1637Python.class);
   private final Process process;
   private final PrintStream output;

   public TM1637Python(File file) {
      try {
         File err = new File(Paths.get("").toAbsolutePath().toFile(), "error.ptn");
         if (err.exists()) {
            err.delete();
         }

         err.createNewFile();
         File out = new File(Paths.get("").toAbsolutePath().toFile(), "out.ptn");
         if (out.exists()) {
            out.delete();
         }

         out.createNewFile();
         this.process = (new ProcessBuilder(Arrays.asList("python", file.getAbsolutePath()))).redirectError(err).redirectOutput(Redirect.to(out)).directory(file.getParentFile()).start();
         this.output = new PrintStream(new BufferedOutputStream(this.process.getOutputStream()), true, "UTF-8");
         log.info("Python display ready");
      } catch (IOException var4) {
         throw new RuntimeException("Can not start process", var4);
      }
   }

   public void setLisa() {
      this.send("lisa");
   }

   public void setDigit(double value) {
      this.send("num " + (int)value + " " + ((int)(value * 100.0D) - (int)value * 100));
   }

   private void send(String str) {
      if (!this.process.isAlive()) {
         log.error("Python dispay process is dead");
      }

      if (ConfigurationParent.debug) {
         log.info("Sent to display: \"{}\"", str);
      }

      this.output.println(str);
      this.output.flush();
   }

   public void setError(int code) {
      this.send("err " + code);
   }

   public void changed() {
      this.send("changed");
   }

   public void destroy() {
      this.process.destroy();
   }
}
