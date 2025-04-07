package com.example.storage.filter;

import com.example.storage.config.FilterConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class MoveFilter implements Filter {

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if("MOVE".equals(req.getMethod())){
            chain.doFilter(request, response);
        }
        else {
            chain.doFilter(request, response);
        }
    }

}
