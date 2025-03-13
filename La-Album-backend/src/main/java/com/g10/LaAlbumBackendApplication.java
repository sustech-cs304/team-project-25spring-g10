package com.g10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LaAlbumBackendApplication
{
    public static void main( String[] args )
    {
        System.out.println("🚀 La-Album 后端正在启动...");
        SpringApplication.run(LaAlbumBackendApplication.class, args);
        System.out.println("✅ La-Album 后端启动成功！访问: http://localhost:8080");
    }
}
