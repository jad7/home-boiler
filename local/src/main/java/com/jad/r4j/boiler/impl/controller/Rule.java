package com.jad.r4j.boiler.impl.controller;

public interface Rule {
   boolean isAppliableRightNowForChanging(Type var1, State var2);

   void changeState(Type var1, State var2);
}
