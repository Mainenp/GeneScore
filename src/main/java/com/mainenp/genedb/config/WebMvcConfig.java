package com.mainenp.genedb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 * Registers interceptors and configures web settings
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;
    
    /**
     * Register interceptors
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/import/**"); // Exclude import endpoints from rate limiting
    }
}
