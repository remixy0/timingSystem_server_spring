package org.example.controller.Security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

@Component
public class LoginAttemptService {

    static final int MAX_FAILURES = 10;
    static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int MAX_TRACKED = 50_000;

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final LongSupplier clockMillis;

    public LoginAttemptService() {
        this(System::currentTimeMillis);
    }

    LoginAttemptService(LongSupplier clockMillis) {
        this.clockMillis = clockMillis;
    }

    public long secondsUntilUnlocked(String username) {
        Attempts a = attempts.get(username);
        if (a == null) return 0;
        long now = clockMillis.getAsLong();
        long windowEnd = a.windowStart + WINDOW.toMillis();
        if (now >= windowEnd) {
            attempts.remove(username, a);
            return 0;
        }
        if (a.failures < MAX_FAILURES) return 0;
        return Math.max(1, (windowEnd - now + 999) / 1000);
    }

    public void loginFailed(String username) {
        long now = clockMillis.getAsLong();
        if (attempts.size() >= MAX_TRACKED) {
            attempts.values().removeIf(a -> now >= a.windowStart + WINDOW.toMillis());
        }
        attempts.compute(username, (k, a) -> {
            if (a == null || now >= a.windowStart + WINDOW.toMillis()) return new Attempts(now, 1);
            return new Attempts(a.windowStart, a.failures + 1);
        });
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    private record Attempts(long windowStart, int failures) {}
}
