package org.example.controller.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;


public class RateLimitFilter extends OncePerRequestFilter {

    record Rule(String name, String method, String path, boolean prefix, int capacity, Duration window) {
        boolean matches(String reqMethod, String reqPath) {
            if (method != null && !method.equalsIgnoreCase(reqMethod)) return false;
            return prefix ? reqPath.startsWith(path) : reqPath.equals(path);
        }
    }

    static final List<Rule> RULES = List.of(
            // Password guessing
            new Rule("login", "POST", "/api/login", false, 10, Duration.ofMinutes(1)),
            // Account creation sends an e-mail from our Gmail account
            new Rule("register", "POST", "/api/register", false, 5, Duration.ofHours(1)),
            // Re-sending sends an e-mail
            new Rule("resend", "POST", "/api/verify/resend", false, 3, Duration.ofMinutes(15)),
            // 6-digit codes: without a limit they can be brute-forced
            new Rule("verify", "GET", "/api/verify/", true, 20, Duration.ofMinutes(15)),
            // Coach request sends an e-mail to the coach
            new Rule("coach-request", "POST", "/api/coach", false, 10, Duration.ofHours(1)),
            // Everything else under /api (the iOS app uploads in batches, so this is generous)
            new Rule("api", null, "/api/", true, 300, Duration.ofMinutes(1))
    );

    private final RateLimiter limiter;

    public RateLimitFilter(RateLimiter limiter) {
        this.limiter = limiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // CORS preflight requests are cheap and must not use up the client's budget
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        Rule rule = findRule(request.getMethod(), path);
        if (rule == null) {
            chain.doFilter(request, response);
            return;
        }

        String key = rule.name() + ":" + request.getRemoteAddr();
        long waitSeconds = limiter.tryConsume(key, rule.capacity(), rule.window());
        if (waitSeconds > 0) {
            reject(response, waitSeconds);
            return;
        }

        chain.doFilter(request, response);
    }

    static Rule findRule(String method, String path) {
        for (Rule rule : RULES) {
            if (rule.matches(method, path)) return rule;
        }
        return null;
    }

    private static void reject(HttpServletResponse response, long waitSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(waitSeconds));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"message\":\"Too many requests. Please try again in " + humanWait(waitSeconds) + ".\"}");
    }

    static String humanWait(long seconds) {
        if (seconds < 60) return seconds + (seconds == 1 ? " second" : " seconds");
        long minutes = (seconds + 59) / 60;
        return minutes + (minutes == 1 ? " minute" : " minutes");
    }
}
