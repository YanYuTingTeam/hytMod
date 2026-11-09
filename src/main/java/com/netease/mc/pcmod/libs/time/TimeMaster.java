package com.netease.mc.pcmod.libs.time;

public class TimeMaster {
    private static long time = System.currentTimeMillis();

    public static void refresh() {
        time = System.currentTimeMillis();
    }

    public static long time() {
        long now = System.currentTimeMillis();
        if (time == 0L || now > time) {
            time = now;
        }
        return time;
    }
}
