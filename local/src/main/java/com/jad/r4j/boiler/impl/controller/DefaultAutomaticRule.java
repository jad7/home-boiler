package com.jad.r4j.boiler.impl.controller;

public class DefaultAutomaticRule implements Rule {
   public DefaultAutomaticRule() {
   }

   public boolean isAppliableRightNowForChanging(Type type, State state) {
      return true;
   }

   public void changeState(Type type, State state) {
      if (type == Type.BOILER) {
      }

   }
}
