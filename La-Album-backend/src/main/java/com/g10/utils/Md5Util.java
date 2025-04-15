package com.g10.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具类，用于密码加密
 */
public class Md5Util {
    
    /**
     * 默认的密码字符串组合，用来将字节转换成16进制表示的字符
     */
    protected static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    protected static MessageDigest messagedigest = null;
    
    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            System.err.println(Md5Util.class.getName() + "初始化失败，MessageDigest不支持MD5Util。");
            nsaex.printStackTrace();
        }
    }
    
    /**
     * 生成字符串的md5校验值
     *
     * @param s 待加密的字符串
     * @return 加密后的MD5字符串
     */
    public static String getMD5String(String s) {
        return getMD5String(s.getBytes());
    }
    
    /**
     * 判断字符串的md5校验码是否与一个已知的md5码相匹配
     *
     * @param password 要校验的字符串
     * @param md5PwdStr 已知的md5校验码
     * @return 校验结果
     */
    public static boolean checkPassword(String password, String md5PwdStr) {
        String s = getMD5String(password);
        return s.equals(md5PwdStr);
    }
    
    public static String getMD5String(byte[] bytes) {
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }
    
    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }
    
    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }
    
    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高4位的数字转换
        char c1 = hexDigits[bt & 0xf];// 取字节中低4位的数字转换
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }
} 