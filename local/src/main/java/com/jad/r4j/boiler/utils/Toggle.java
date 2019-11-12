package com.jad.r4j.boiler.utils;

import java.util.ArrayList;
import java.util.List;

public class Toggle<T> {

    private int index = 0;
    private List<T> list = new ArrayList<>();

    public void add(T elm) {
        list.add(elm);
    }

    public T next() {
        index = index % list.size();
        T res = list.get(index);
        index++;
        return res;
    }


    public boolean hasNext() {
        return !list.isEmpty();
    }
}
