package com.wolf.minimq.hello.java.jna;

public class HelloJNA {
    public int abs(int i) {
        return CLibrary.INSTANCE.abs(i);
    }

    public static void main(String[] args) {
        CLibrary.INSTANCE.printf("Hello World\n");
    }
}
