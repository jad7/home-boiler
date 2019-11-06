package com.jad.r4j.boiler.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tuple<A, B> {
    private A a;
    private B b;
}
