package com.jad.boiler.remote.service;

import com.jad.boiler.remote.dto.v1.Action;
import com.jad.boiler.remote.dto.v1.Info;
import com.jad.boiler.remote.dto.v1.Status;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StatusService {
   @Value("${com.jad.boiler.remote.pi.heartbeat.interval.second}")
   private Integer heartbeatIntervaleSeconds;
   @Value("${com.jad.boiler.remote.pi.heartbeat.interval.max.error.seconds}")
   private Integer heartbeatIntervalMaxErrorSeconds;
   private BlockingQueue<Action> actions = new ArrayBlockingQueue<>(20);
   private AtomicReference<Status> status = new AtomicReference<>();
   private volatile LocalDateTime lastUpdate;

   public StatusService() {
   }

   public Info getInfo() {
      Info info = new Info();
      Status status = (Status)this.status.get();
      info.setStatus(status);
      info.setAllActionsHasBeenApplied(this.actions.isEmpty());
      if (status == null) {
         info.setInited(false);
         Info.Alive alive = this.getAlive(AppListener.appStartTime);
         info.setAlive(alive == Info.Alive.GREEN ? Info.Alive.YELLOW : alive);
      } else {
         info.setInited(true);
         info.setLastUpdate(Duration.between(LocalDateTime.now(), this.lastUpdate));
         info.setAlive(this.getAlive(this.lastUpdate));
      }

      return info;
   }

   private Info.Alive getAlive(LocalDateTime from) {
      LocalDateTime currentTime = LocalDateTime.now();
      Duration diff = Duration.between(currentTime, from);
      if (diff.getSeconds() <= (long)this.heartbeatIntervaleSeconds) {
         return Info.Alive.GREEN;
      } else {
         return diff.getSeconds() <= (long)(this.heartbeatIntervaleSeconds + this.heartbeatIntervalMaxErrorSeconds) ? Info.Alive.YELLOW : Info.Alive.RED;
      }
   }

   public Collection<Action> setState(Status status) {
      this.lastUpdate = LocalDateTime.now();
      this.status.set(status);
      List<Action> actions = new ArrayList<>();
      this.actions.drainTo(actions);
      return actions;
   }

   public void addActions(List<Action> actions) {
      if (actions != null && !actions.isEmpty()) {
         if (this.actions.remainingCapacity() < actions.size()) {
            throw new RuntimeException("Overflow actions. Rejected");
         }

         this.actions.addAll(actions);
      }

   }

   public static void main(String[] args) {
      System.out.println(System.currentTimeMillis());
      System.out.println(LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli());
   }
}
