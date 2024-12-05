package com.shuidun.sandbox_town_backend.utils;

public class MyMath {
    public static long safeMod(long a, long b) {
        if (b == 0) throw new IllegalArgumentException("Divisor cannot be zero");
        long result = a % b;
        return result < 0 ? result + b : result;
    }
}
