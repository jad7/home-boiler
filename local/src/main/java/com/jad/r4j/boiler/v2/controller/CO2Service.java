package com.jad.r4j.boiler.v2.controller;

import com.jad.r4j.boiler.config.Configuration;
import com.jad.r4j.boiler.impl.TaskProcessor;
import com.jad.r4j.boiler.impl.sensor.MHZ19;
import com.jad.r4j.boiler.utils.RingBufferTimeserial;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class CO2Service {
    private final RingBufferTimeserial timeserial;
    private final Configuration configuration;
    private final DisplayService displayService;
    private MHZ19 mhz19;

    @Inject
    public CO2Service(DisplayService displayService,
                      TaskProcessor taskProcessor,
                      @Named("config.co2.refresh.ms") Integer refreshMs,
                      Configuration configuration,
                      MHZ19 mhz19) {
        this.displayService = displayService;
        this.configuration = configuration;
        this.mhz19 = mhz19;
        timeserial = new RingBufferTimeserial(100);
        taskProcessor.scheduleRepeatable(this::loadData, refreshMs, TimeUnit.MILLISECONDS);
        class Ref {@Setter volatile Long val;};
        final Ref ref = new Ref();
        taskProcessor.scheduleRepeatable(() ->
                Optional.ofNullable(timeserial.avg(30, TimeUnit.SECONDS))
                .map(Math::round).ifPresent(ref::setVal), 10, TimeUnit.SECONDS);
        this.displayService.addStatic(() ->  "CO2 " + ref.val);

    }

    private void loadData() {
        try {
            int read = mhz19.read();
            timeserial.add(read);
            configuration.update(Configuration.CURRENT_CO2_STAT, Integer.class, read);
        } catch (Exception e) {
            log.error("Can not read CO2", e);
            displayService.showNotification("Err rEAD Co2");
        }
    }


}
