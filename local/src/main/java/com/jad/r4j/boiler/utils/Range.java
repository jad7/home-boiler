package com.jad.r4j.boiler.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class Range<C> {
    private final C min;
    private final C max;

    public C min() {
        return min;
    }

    public C max() {
        return max;
    }
}
