package com.xdlr.camera.util;

import java.util.Random;

public class IdUtil {
    public static String generateId() {
        StringBuffer id = new StringBuffer();
        for (int i = 0; i < 18; i++) {
            id.append(new Random().nextInt(10));
        }
        return id.toString();
    }
}
