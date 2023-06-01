package com.shuidun.sandbox_town_backend.utils;

public class NumUtils {
    public static int toInt(Object obj) {
        return (int) Math.round(Double.parseDouble(obj.toString()));
    }
}
