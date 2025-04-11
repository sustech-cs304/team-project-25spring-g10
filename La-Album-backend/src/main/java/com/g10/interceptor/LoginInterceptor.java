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
        // 获取请求头中的令牌
        String token = request.getHeader("Authorization");
        
        // 验证token
        try {
            // 从Redis中获取相同的token，检查是否失效
            // ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            // String redisToken = operations.get(token);
            // if (redisToken == null) {
            //     // token已经失效
            //     throw new RuntimeException("登录已过期，请重新登录");
            // }
            
            // 解析token，获取用户信息
            Map<String, Object> claims = JwtUtil.parseToken(token);
            
            // 把业务数据存储到ThreadLocal中
            ThreadLocalUtil.set(claims);
            
            // 放行
            return true;
        } catch (Exception e) {
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