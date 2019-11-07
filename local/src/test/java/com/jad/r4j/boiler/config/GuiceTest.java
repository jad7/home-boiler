package com.jad.r4j.boiler.config;

import com.google.inject.*;
import org.junit.Test;

public class GuiceTest {

    @Test
    public void doTest() {
        Injector injector = Guice.createInjector(new MyModule());
        MyClassB instance = injector.getInstance(MyClassB.class);
        System.out.println(instance.getI());
        System.out.println(instance.getI());
        System.out.println(injector.getInstance(MyClassB.class).getI());
    }

    public static class MyModule extends AbstractModule {

    }

    @Singleton
    public static class MyClassA {
        static int i = 0;

        public MyClassA() {
            i++;
        }
    }

    public static class MyClassB {
        static int i = 0;

        @Inject
        public MyClassB(MyClassA classA) {
            i++;
        }

        public String getI() {
            return "Ai:" + MyClassA.i + ", Bi:" + MyClassB.i;
        }
    }
}
