package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.Thread.sleep;

/**
 * 检查用户是否已经完成登录
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j

public class loginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        //定义不需要过滤的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
//                "/employee/page"
        };
        //1、获取本次请求的URI
        String requestUri = req.getRequestURI();

        log.info("拦截到请求:{}",requestUri);

        //2、判断本次请求是否需要处理
        boolean check = check(urls,requestUri);

        //3、如果不需要处理，则直接放行
        if(check){
            log.info("本次请求不需要处理:{}",requestUri);
            chain.doFilter(req,res);
            return;
        }

        //4-1、判断登录状态，如果已登录，则直接放行
        if(req.getSession().getAttribute("employee") != null){
            log.info("用户已登录:{}",req.getSession().getAttribute("employee"));

            Long empId = (Long) req.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            chain.doFilter(req,res);
            return;
        }

        //4-2、判断登录状态，如果已登录，则直接放行
        if(req.getSession().getAttribute("user") != null){
            log.info("用户已登录:{}",req.getSession().getAttribute("user"));

            Long userId = (Long) req.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            chain.doFilter(req,res);
            return;
        }

        //5、如果未登录则返回未登录结果,通过输出流方式向客户带页面响应数据
        log.info("用户未登录");
        res.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        //chain.doFilter(req,res);
        return;
    }

    /**
     * 路径匹配，检查本次请求是否放行
     * @param requestURI
     * @param urls
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for(String url : urls){
            boolean match = PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
