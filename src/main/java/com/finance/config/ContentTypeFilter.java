package com.finance.manager.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
@Order(1)
public class ContentTypeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            String method = httpRequest.getMethod();
            String contentType = httpRequest.getContentType();

            boolean isBodyMethod = "POST".equalsIgnoreCase(method)
                    || "PUT".equalsIgnoreCase(method)
                    || "PATCH".equalsIgnoreCase(method);

            boolean missingJson = contentType == null
                    || contentType.isBlank()
                    || !contentType.contains("application/json");

            if (isBodyMethod && missingJson) {
                chain.doFilter(new HttpServletRequestWrapper(httpRequest) {
                    @Override
                    public String getContentType() {
                        return "application/json";
                    }
                    @Override
                    public String getHeader(String name) {
                        if ("Content-Type".equalsIgnoreCase(name)) return "application/json";
                        return super.getHeader(name);
                    }
                    @Override
                    public Enumeration<String> getHeaders(String name) {
                        if ("Content-Type".equalsIgnoreCase(name))
                            return Collections.enumeration(List.of("application/json"));
                        return super.getHeaders(name);
                    }
                }, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}