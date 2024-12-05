package com.wolf.minimq.hello.unsafe;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class HelloUnsafe {
    public static final Unsafe UNSAFE = getUnsafe();

    public static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception ignore) {

        }
        return null;
    }

    public static void main(String[] args) {
        Unsafe unsafe = getUnsafe();

        if (unsafe != null) {
            System.out.println("Successfully obtained Unsafe instance: " + unsafe);
        } else {
            System.out.println("Failed to obtain Unsafe instance.");
        }
    }
}
