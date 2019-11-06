package com.jad.r4j.boiler.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Schedule {
   TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

   int value() default -1;

   String parameter() default "#param#";

   boolean startImmediately() default true;
}
