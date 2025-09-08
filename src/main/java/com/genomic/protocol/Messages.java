package com.genomic.protocol;

public class Messages {
    public static String ok(String msg) {
        return "STATUS: OK\nmessage=" + msg + "\nEND\n";
    }

    public static String error(int code, String msg) {
        return "STATUS: ERROR\ncode=" + code + "\nmessage=" + msg + "\nEND\n";
    }
}
