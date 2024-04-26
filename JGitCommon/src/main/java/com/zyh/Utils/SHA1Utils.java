package com.zyh.Utils;


public class SHA1Utils {

    public static String bytesToHex(byte[] data){
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int t = data[i];
            if(t < 0) t += 256;
            if(t < 16)  buf.append("0");
            buf.append(Integer.toHexString(t));
        }
        return buf.toString();
    }
}
