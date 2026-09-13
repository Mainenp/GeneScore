package com.mainenp.genedb.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Interceptor
 * Implements simple rate limiting to prevent API abuse
 * Limits: 100 requests per minute per IP address
 */
@Slf4j
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long MINUTE_IN_MS = 60 * 1000;
    
    private final ConcurrentHashMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    
    /**
     * Pre-handle request - check rate limit
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler
     * @return true if request should proceed, false if rate limited
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                            Object handler) throws Exception {
        
        String clientIp = getClientIp(request);
        RequestCounter counter = requestCounters.computeIfAbsent(clientIp, 
                k -> new RequestCounter());
        
        // Check if counter has expired
        if (System.currentTimeMillis() - counter.getStartTime() > MINUTE_IN_MS) {
            counter.reset();
        }
        
        // Increment counter
        int currentCount = counter.increment();
        
        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for IP: {} ({}+ requests)", clientIp, currentCount);
            // Avoid servlet constant availability differences across servlet versions.
            response.setStatus(429);
            response.getWriter().write("{\"code\": 429, \"msg\": \"Too many requests\", \"data\": null}");
            return false;
        }
        
        return true;
    }
    
    /**
     * Get client IP address from request
     * @param request the HTTP request
     * @return client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Request counter helper class
     */
    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private long startTime = System.currentTimeMillis();
        
        int increment() {
            return count.incrementAndGet();
        }
        
        void reset() {
            count.set(1);
            startTime = System.currentTimeMillis();
        }
        
        long getStartTime() {
            return startTime;
        }
    }
}
