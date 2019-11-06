package com.jad.r4j.boiler;

public class Sum {
   public Sum() {
   }

   public static void main(String[] args) {
      int v1 = 2147483647;
      boolean v2 = true;
      int plus1 = (new Sum()).plus(2147483647, 1);
      System.out.println(plus1);
      int plus2 = -2147483648;
      System.out.println(-2147483648);
      System.out.println(Integer.toBinaryString(plus1));
      System.out.println(Integer.toBinaryString(-2147483648));
      System.out.println("at=info method=GET path=\"/social/the-edit/women/shines?".matches(".*path=\"/social/.*"));
   }

   private int plus(int i1, int i2) {
      int result = 0;
      int iterationOn = -1;
      int currentPosition = 1;

      for(boolean next = false; iterationOn != 0; currentPosition <<= 1) {
         iterationOn >>>= 1;
         int b1 = i1 & currentPosition;
         int b2 = i2 & currentPosition;
         if ((b1 & b2) != 0) {
            if (next) {
               result |= currentPosition;
            } else {
               next = true;
            }
         } else if ((b1 | b2) != 0) {
            if (!next) {
               result |= currentPosition;
            }
         } else if (next) {
            next = false;
            result |= currentPosition;
         }
      }

      return result;
   }

   int inc(int i) {
      int next = 1;

      for(int s = -1; s != 0; next <<= 1) {
         s >>= 1;
         int i1 = i & next;
         if (i1 == 0) {
            return i | next;
         }
      }

      throw new IllegalStateException("IS inc");
   }
}
