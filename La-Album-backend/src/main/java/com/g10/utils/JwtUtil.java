package com.g10.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static final String KEY = "la-album-g10-secret";
    private static final long EXPIRE_TIME = 1000 * 60 * 60; // 1小时

    public static String genToken(Map<String, Object> claims) {
        String token = JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .sign(Algorithm.HMAC256(KEY));
        // return "Bearer " + token;
        return token;
    }

    public static Map<String, Object> parseToken(String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return JWT.require(Algorithm.HMAC256(KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

    public static boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC256(KEY)).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public static boolean isTokenExpired(String token) {
        try {
            Date expiresAt = JWT.require(Algorithm.HMAC256(KEY))
                    .build()
                    .verify(token)
                    .getExpiresAt();
            return expiresAt.before(new Date());
        } catch (JWTVerificationException e) {
            return true; // 如果验证失败，也视为已过期
        }
    }
}
