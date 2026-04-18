package com.example.backend.config;

import com.example.backend.interceptor.JwtInterceptor;
import com.example.backend.utils.FileUploadUtil;
import com.example.backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer{//启动时立刻执行
    @Autowired
    private JwtInterceptor jwtInterceptor;
    @Autowired
    private FileUploadConfig fileUploadConfig;
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("=== WebConfig.addInterceptors 被调用 ===");
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/register",
                        "/user/login",
                        "/user/registerAdmin",
                        "/lost/list",
                        "/picked/list",
                        "/lost/detail/**",
                        "/comment/target",
                        "/upload/**",
                        "/uploads/**"
                );
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:"+ fileUploadConfig.getUploadDir());
    }
}
