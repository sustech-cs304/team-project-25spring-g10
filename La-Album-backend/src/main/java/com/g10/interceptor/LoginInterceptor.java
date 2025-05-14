package com.g10.interceptor;

import com.g10.utils.JwtUtil;
import com.g10.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * 登录拦截器，用于验证用户登录状态
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        System.out.println("拦截请求: " + requestURI);
        
        // 获取请求头中的令牌
        String token = request.getHeader("Authorization");
        System.out.println("Token: " + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"));
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            System.out.println("Token为空，拒绝访问");
            response.setStatus(401);
            return false;
        }
        
        // 验证token
        try {
            // 先解析token获取用户信息
            Map<String, Object> claims = JwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get("id").toString());
            String username = (String) claims.get("username");
            System.out.println("解析Token成功 - 用户ID: " + userId + ", 用户名: " + username);
            
            // 从Redis中获取该用户的token
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String userKey = "user:" + userId;
            String redisToken = operations.get(userKey);
            System.out.println("Redis中的Token: " + (redisToken != null ? redisToken.substring(0, Math.min(redisToken.length(), 20)) + "..." : "null"));
            
            // 验证当前token是否为最新token
            if (redisToken == null) {
                System.out.println("Redis中不存在Token，可能已过期");
                throw new RuntimeException("登录已失效，请重新登录");
            }
            
            if (!token.equals(redisToken)) {
                System.out.println("Token不匹配，当前Token已失效");
                throw new RuntimeException("登录已失效，请重新登录");
            }
            
            // 把业务数据存储到ThreadLocal中
            ThreadLocalUtil.set(claims);
            System.out.println("验证通过，允许访问");
            
            // 放行
            return true;
        } catch (Exception e) {
            System.out.println("Token验证失败: " + e.getMessage());
            // HTTP响应状态码设为401
            response.setStatus(401);
            return false;
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据，防止内存泄漏
        ThreadLocalUtil.remove();
    }
} 