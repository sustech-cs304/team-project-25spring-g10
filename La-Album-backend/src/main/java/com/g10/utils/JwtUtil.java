package com.g10.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.Map;

/**
 * JWT工具类，用于生成和解析Token
 */
public class JwtUtil {

    // JWT密钥，实际应用中应从配置文件读取
    private static final String KEY = "la-album-g10-secret";
    
    // Token过期时间（毫秒）
    private static final long EXPIRE_TIME = 1000 * 60 * 60; // 1小时
    
    /**
     * 生成token
     * @param claims 存储在token中的信息
     * @return 生成的token字符串
     */
    public static String genToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .sign(Algorithm.HMAC256(KEY));
    }
    
    /**
     * 解析token
     * @param token 待解析的token字符串
     * @return 解析出的信息
     */
    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }
} 