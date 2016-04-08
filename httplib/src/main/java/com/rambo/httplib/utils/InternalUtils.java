package com.rambo.httplib.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class InternalUtils {
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public InternalUtils() {
    }

    private static String convertToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for(int j = 0; j < bytes.length; ++j) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARS[v & 15];
        }

        return new String(hexChars);
    }

    public static String sha1Hash(String text) {
        String hash = null;

        try {
            MessageDigest e = MessageDigest.getInstance("SHA-1");
            byte[] bytes = text.getBytes("UTF-8");
            e.update(bytes, 0, bytes.length);
            hash = convertToHex(e.digest());
        } catch (NoSuchAlgorithmException var4) {
            var4.printStackTrace();
        } catch (UnsupportedEncodingException var5) {
            var5.printStackTrace();
        }

        return hash;
    }
}
