package com.drag.user.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectSerializer {

    public static String serialize(Serializable obj) {
        if (obj == null) return "";
        String str = "";
        try {
            ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
            objStream.writeObject(obj);
            objStream.close();
            str = encodeBytes(serialObj.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static Object deserialize(String str) {
        if (str == null || str.length() == 0) return null;
        Object obj = null;
        try {
            ByteArrayInputStream serialObj = new ByteArrayInputStream(decodeBytes(str));
            ObjectInputStream objStream = new ObjectInputStream(serialObj);
            obj = objStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    private static String encodeBytes(byte[] bytes) {
        StringBuilder strBuf = new StringBuilder();
        for (byte aByte : bytes) {
            strBuf.append((char) (((aByte >> 4) & 0xF) + ((int) 'a')));
            strBuf.append((char) (((aByte) & 0xF) + ((int) 'a')));
        }
        return strBuf.toString();
    }

    private static byte[] decodeBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length(); i += 2) {
            char c = str.charAt(i);
            bytes[i / 2] = (byte) ((c - 'a') << 4);
            c = str.charAt(i + 1);
            bytes[i / 2] += (c - 'a');
        }
        return bytes;
    }
}