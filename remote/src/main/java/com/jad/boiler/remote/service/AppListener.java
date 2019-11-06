package com.jad.boiler.remote.service;

import java.time.LocalDateTime;
import java.util.Arrays;

import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@Data
public class AppListener implements SpringApplicationRunListener {
   public static LocalDateTime appStartTime;
   private SpringApplication application;
   private String[] args;

   public AppListener(SpringApplication application, String[] args) {
      this.application = application;
      this.args = args;
   }

   public void starting() {
   }

   public void environmentPrepared(ConfigurableEnvironment environment) {
   }

   public void contextPrepared(ConfigurableApplicationContext context) {
   }

   public void contextLoaded(ConfigurableApplicationContext context) {
   }

   public void started(ConfigurableApplicationContext configurableApplicationContext) {
      appStartTime = LocalDateTime.now();
   }

   public void running(ConfigurableApplicationContext configurableApplicationContext) {
   }

   public void failed(ConfigurableApplicationContext configurableApplicationContext, Throwable throwable) {
   }

}
