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

        // 对于拾取物详情页，特殊处理：允许未登录用户访问公共拾取物，但需要验证管理员身份
        if (request.getRequestURI().startsWith("/picked/detail/")) {
            System.out.println("处理拾取物详情页请求");
            // 验证Authorization头
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                System.out.println("token:" + token);//验证
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);
                    Integer role = jwtUtil.getRoleFromToken(token);
                    System.out.println("role from token: " + role);
                    request.setAttribute("userId", userId);//这也是一个map，所以这里先贴标签，到了controller里面直接解析就行了
                    request.setAttribute("username", username);//这里先从token来设置这三个值，是因为前端在操作时一般不需要输入这三个数据，再有就是可以统一管理加强保密
                    request.setAttribute("role", role);
                }
            }
            // 无论是否有token，都允许继续访问，由controller来处理权限
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
        request.setAttribute("username", username);//这里先从token来设置这三个值，是因为前端在操作时一般不需要输入这三个数据，再有就是可以统一管理加强保密
        request.setAttribute("role", role);
        return true;
    }
}