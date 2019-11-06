package com.jad.r4j.boiler.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Destroyable;
import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.dto.DisplayError;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Named;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RemoteServiceUpdater implements Destroyable {
   private static final Logger log = LoggerFactory.getLogger(RemoteServiceUpdater.class);
   private String host;
   private String path;
   private DynamicConfigurationHolder ch;
   private DisplayController displayController;
   private TaskProcessor taskProcessor;
   private SensorsProvider sensorsProvider;
   private volatile boolean error = false;

   @Inject
   public RemoteServiceUpdater(@Named("server.url") String host, @Named("server.path") String path,
                               DynamicConfigurationHolder ch, DisplayController displayController,
                               TaskProcessor taskProcessor, SensorsProvider sensorsProvider) {
      this.host = host;
      this.path = path;
      this.ch = ch;
      this.displayController = displayController;
      this.taskProcessor = taskProcessor;
      this.sensorsProvider = sensorsProvider;
   }

   @Schedule(
      parameter = "server.update.interval.seconds",
      timeUnit = TimeUnit.SECONDS
   )
   public void update() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("manual", this.ch.isManual());
      jsonObject.put("state", (Object)(this.ch.isAnyBodyAtHome() ? "AT_HOME" : "NOT_AT_HOME"));
      jsonObject.put("tempTo", this.ch.isAnyBodyAtHome() ? this.ch.maxWhenAtHomeTemperature() : this.ch.maxWhenNotAtHomeTemperature());
      jsonObject.put("tempFrom", this.ch.isAnyBodyAtHome() ? this.ch.minWhenAtHomeTemperature() : this.ch.minWhenNotAtHomeTemperature());
      jsonObject.put("currentTemperature", this.sensorsProvider.getCurrentRoomTemperature());
      jsonObject.put("relayEnabled", !this.sensorsProvider.isBoilerOff());

      try {
         HttpResponse<JsonNode> response = Unirest.post(this.host + this.path).header("Content-Type", "application/json").header("Accept", "application/json").body(jsonObject).asJson();
         JSONArray array = (response.getBody()).getArray();
         int i = 0;

         while(i < array.length()) {
            JSONObject obj = (JSONObject)array.get(i);
            String name = obj.getString("name");
            byte var8 = -1;
            switch(name.hashCode()) {
            case 109757585:
               if (name.equals("state")) {
                  var8 = 0;
               }
            default:
               switch(var8) {
               case 0:
                  String value = obj.getString("value");
                  if ("AT_HOME".equalsIgnoreCase(value)) {
                     this.setAtHomeManual(true);
                  } else if ("NOT_AT_HOME".equalsIgnoreCase(value)) {
                     this.setAtHomeManual(false);
                  }
                  break;
               default:
                  log.warn((String)"Not supported update action: {}", (Object)name);
               }

               ++i;
            }
         }

         if (array.length() > 0) {
            this.taskProcessor.schedule(this::update, 0L, TimeUnit.MILLISECONDS);
         }

         this.error = false;
      } catch (Exception var10) {
         log.error((String)"Can not upload data to remote server", (Throwable)var10);
         this.error = true;
         this.processException();
      }

   }

   private void setAtHomeManual(boolean val) {
      UUID uuid = UUID.randomUUID();
      this.ch.setAnyBodyAtHomeManual(val, uuid);
      this.taskProcessor.schedule(() -> {
         DynamicConfigurationHolder.Manual<Boolean> manual = this.ch.getAnyBodyAtHomeManual();
         if (manual.getUuid().equals(uuid)) {
            this.ch.clearManual();
         }

      }, (long)this.ch.getManualDurationMinutes(), TimeUnit.MINUTES);
      this.displayController.showChanged();
   }

   public static void main(String[] args) {
      System.out.println(Math.round(324.32434D));
   }

   private void processException() {
      class Int {
         int i;

         public Int(int i) {
            this.i = i;
         }
      }

      Int count = new Int(10);
      AtomicReference<Runnable> scheduleContainer = new AtomicReference();
      Runnable schedule = () -> {
         this.displayController.showError(DisplayError.HTTP_REMOTE_UPLOAD);
         --count.i;
         if (count.i >= 0 && this.error) {
            this.taskProcessor.schedule((Runnable)scheduleContainer.get(), 20L, TimeUnit.SECONDS);
         }

      };
      scheduleContainer.set(schedule);
      this.taskProcessor.schedule(schedule, 0L, TimeUnit.MILLISECONDS);
   }

   public void destroy() {
      try {
         Unirest.shutdown();
      } catch (IOException var2) {
         log.warn((String)"Can stop Unirest", (Throwable)var2);
      }

   }
}
