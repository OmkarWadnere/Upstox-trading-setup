package com.upstox.production.nifty.utility;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {

    public static <T> List<T> iterableToList(Iterable<?> iterable, Class<T> type) {
        List<T> list = new ArrayList<>();
        if (iterable != null) {
            for (Object obj : iterable) {
                if (type.isInstance(obj)) {
                    list.add(type.cast(obj));
                } else {
                    throw new ClassCastException("Element is not of type " + type.getName());
                }
            }
        }
        return list;
    }
}