package com.g10.config;

import com.g10.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，配置拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录、注册接口和静态资源不需要拦截
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/api/users/login",
                        "/api/users/register",
                        "/static/**",
                        "/api/files/**"
                );
    }
}
