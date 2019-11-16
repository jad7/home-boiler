package com.jad.boiler.remote.dto.v1;

import lombok.Data;

import java.time.Duration;

@Data
public class Info {
   private Status status;
   private Duration lastUpdate;
   private Info.Alive alive;
   private boolean allActionsHasBeenApplied;
   private boolean inited;

   public enum Alive {
      GREEN,
      YELLOW,
      RED;
   }
}
