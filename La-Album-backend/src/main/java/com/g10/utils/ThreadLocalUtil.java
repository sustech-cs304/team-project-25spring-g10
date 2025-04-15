package com.g10.utils;

import java.util.Map;

/**
 * ThreadLocal工具类，用于在线程中存储用户信息
 */
@SuppressWarnings("all")
public class ThreadLocalUtil {
    
    // 提供ThreadLocal对象
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();
    
    /**
     * 获取ThreadLocal中存储的数据
     */
    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }
    
    /**
     * 向ThreadLocal中存储数据
     */
    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }
    
    /**
     * 清除ThreadLocal中的数据，防止内存泄漏
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }
} 