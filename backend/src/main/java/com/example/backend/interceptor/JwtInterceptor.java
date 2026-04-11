package com.example.backend.interceptor;

import com.example.backend.utils.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {//每次http请求都调用
        System.out.println("JwtInterceptor 开始执行，路径：" + request.getRequestURI());
        System.out.println("拦截器执行，路径：" + request.getRequestURI() + "，方法：" + request.getMethod());
        //放行预检请求（CorsConfig会处理CORS头）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        //验证Authorization头
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(7);
        System.out.println("token:" + token);//验证
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);
        System.out.println("role from token: " + role);
        request.setAttribute("userId", userId);//这也是一个map，所以这里先贴标签，到了controller里面直接解析就行了
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        return true;
    }
}