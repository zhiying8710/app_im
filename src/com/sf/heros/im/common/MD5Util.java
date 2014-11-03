package com.sf.heros.im.common;
import java.security.MessageDigest;

public class MD5Util {
//        private final static String SALT = "zhiying8710@hotmail.com";
//        private final static StringBuffer sb = new StringBuffer();
        private final static String[] hexDigits = {"0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

        public static String encodeByMD5(String originString, String salt) {
            if (originString != null) {
                try{
                    StringBuffer sb = new StringBuffer();
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    sb.append(salt).append(originString);
                    byte[] results = md.digest(sb.toString().getBytes());
                    return byteArrayToHexString(results);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }

        private static String byteArrayToHexString(byte[] b) {
            StringBuffer resultSb = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                resultSb.append(byteToHexString(b[i]));
            }
            return resultSb.toString();
        }

        private static String byteToHexString(byte b) {
            int n = b;
            if (n < 0)
                n = 256 + n;
            int d1 = n / 16;
            int d2 = n % 16;
            return hexDigits[d1] + hexDigits[d2];
        }

        public static String encodeByMD5WithoutSalt(String originString) {
             if (originString != null) {
                    try{
                        StringBuffer sb = new StringBuffer();
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        sb.append(originString);
                        byte[] results = md.digest(sb.toString().getBytes());
                        String resultString = byteArrayToHexString(results);
                        return resultString;
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
        }
}
