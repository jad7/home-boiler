package com.jad.r4j.boiler.dto;

public enum DisplayError {
   HTTP_REMOTE_UPLOAD(1);

   private int code;

   private DisplayError(int i) {
      this.code = i;
   }

   public int getCode() {
      return this.code;
   }
}
