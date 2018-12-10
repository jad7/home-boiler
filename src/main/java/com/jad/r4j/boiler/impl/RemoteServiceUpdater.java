package com.jad.r4j.boiler.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Destroyable;
import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.dto.DisplayError;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Named;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
 * @since 11/10/2018
 */

@Slf4j
@Singleton
public class RemoteServiceUpdater implements Destroyable {

    private String host;
    private String path;
    private DynamicConfigurationHolder ch;
    private DisplayController displayController;
    private TaskProcessor taskProcessor;
    private SensorsProvider sensorsProvider;

    private volatile boolean error = false;

    @Inject
    public RemoteServiceUpdater(@Named("server.url") String host,
                                @Named("server.path") String path,
                                DynamicConfigurationHolder ch,
                                DisplayController displayController,
                                TaskProcessor taskProcessor,
                                SensorsProvider sensorsProvider) {
        this.host = host;
        this.path = path;
        this.ch = ch;

        this.displayController = displayController;
        this.taskProcessor = taskProcessor;
        this.sensorsProvider = sensorsProvider;
    }

    @Schedule(parameter = "server.update.interval.seconds", timeUnit = TimeUnit.SECONDS)
    public void update() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("manual", ch.isManual());
        jsonObject.put("state", ch.isAnyBodyAtHome() ? "AT_HOME" : "NOT_AT_HOME");
        jsonObject.put("tempTo", ch.isAnyBodyAtHome() ? ch.maxWhenAtHomeTemperature() : ch.maxWhenNotAtHomeTemperature());
        jsonObject.put("tempFrom", ch.isAnyBodyAtHome() ? ch.minWhenAtHomeTemperature() : ch.minWhenNotAtHomeTemperature());
        jsonObject.put("currentTemperature", sensorsProvider.getCurrentRoomTemperature());
        jsonObject.put("relayEnabled", !sensorsProvider.isBoilerOff());
        try {
            final HttpResponse<JsonNode> response = Unirest.post(host + path)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(jsonObject)
                    .asJson();
            final JSONArray array = response.getBody().getArray();
            for (int i = 0; i < array.length(); i++) {
                final JSONObject obj = (JSONObject)array.get(i);
                final String name = obj.getString("name");
                switch (name) {
                    case "state":
                        final String value = obj.getString("value");
                        if ("AT_HOME".equalsIgnoreCase(value)) {
                            setAtHomeManual(true);
                        } else if ("NOT_AT_HOME".equalsIgnoreCase(value)) {
                            setAtHomeManual(false);

                        }
                        break;
                    default:
                        log.warn("Not supported update action: {}", name);
                }
            }
            if (array.length() > 0) {
                taskProcessor.schedule(this::update, 0, TimeUnit.MILLISECONDS);
            }
            error =
                    false;
        } catch (Exception e) {
            log.error("Can not upload data to remote server", e);
            error = true;
            processException();
        }
    }

    private void setAtHomeManual(boolean val) {
        final UUID uuid = UUID.randomUUID();
        ch.setAnyBodyAtHomeManual(val, uuid);
        taskProcessor.schedule(() -> {
            final DynamicConfigurationHolder.Manual<Boolean> manual = ch.getAnyBodyAtHomeManual();
            if (manual.getUuid().equals(uuid)) {
                ch.clearManual();
            }
        }, ch.getManualDurationMinutes(), TimeUnit.MINUTES);
        displayController.showChanged();
    }

    /*
    @Data
public class Status {

    private boolean isManual;
    private State state;
    private Double tempFrom;
    private Double tempTo;
    private Double currentTemperature;
    private boolean relayEnabled;

    public enum State {
        AT_HOME,
        NOT_AT_HOME
    }
}
     */

    public static void main(String[] args) {
        System.out.println(Math.round(324.32434));
    }

    private void processException() {
        @AllArgsConstructor
        class Int {int i;}
        final Int count = new Int(10);
        final AtomicReference<Runnable> scheduleContainer = new AtomicReference<>();
        final Runnable schedule = () -> {
            displayController.showError(DisplayError.HTTP_REMOTE_UPLOAD);
            count.i--;
            if (count.i >= 0 && error) {
                taskProcessor.schedule(scheduleContainer.get(), 20, TimeUnit.SECONDS);
            }
        };
        scheduleContainer.set(schedule);
        taskProcessor.schedule(schedule, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void destroy() {
        try {
            Unirest.shutdown();
        } catch (IOException e) {
            log.warn("Can stop Unirest", e);
        }
    }




}
