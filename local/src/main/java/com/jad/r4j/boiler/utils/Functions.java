package com.jad.r4j.boiler.utils;

import java.util.function.*;

public class Functions {
    public static <A, B> Function<A, B> supToFunc(Supplier<B> supplier) {
        return a -> supplier.get();
    }

    public static <P1, V> Supplier<V> curry(Function<P1, V> function, P1 p1) {
        return () -> function.apply(p1);
    }

    public static <P1, P2, V> Function<P2, V> curry1(BiFunction<P1, P2, V> function, P1 p1) {
        return (p2) -> function.apply(p1, p2);
    }


    public static <P1, P2, V> Function<P1, V> curry2F(BiFunction<P1, P2, V> function, P2 p2) {
        return (p1) -> function.apply(p1, p2);
    }

    public static <P1, P2> Consumer<P1> curry2C(BiConsumer<P1, P2> consumer, P2 p2) {
        return (p1) -> consumer.accept(p1, p2);
    }
}
