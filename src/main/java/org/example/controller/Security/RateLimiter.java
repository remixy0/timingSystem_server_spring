package org.example.controller.Security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

public class RateLimiter {

    private static final int MAX_KEYS = 100_000;
    private static final long CLEANUP_INTERVAL_NANOS = Duration.ofMinutes(1).toNanos();

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final LongSupplier nanoClock;
    private final AtomicLong lastCleanup;

    public RateLimiter() {
        this(System::nanoTime);
    }

    public RateLimiter(LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
        this.lastCleanup = new AtomicLong(nanoClock.getAsLong());
    }


    public long tryConsume(String key, int capacity, Duration window) {
        long now = nanoClock.getAsLong();
        cleanupIfDue(now);

        Bucket bucket = buckets.get(key);
        if (bucket == null) {
            if (buckets.size() >= MAX_KEYS) {
                cleanup(now);
                if (buckets.size() >= MAX_KEYS) {
                    // Still full: someone is flooding us with new keys. Fail closed for new keys.
                    return Math.max(1, window.toSeconds());
                }
            }
            bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, window, now));
        }
        return bucket.tryConsume(now);
    }

    int size() {
        return buckets.size();
    }

    private void cleanupIfDue(long now) {
        long last = lastCleanup.get();
        if (now - last >= CLEANUP_INTERVAL_NANOS && lastCleanup.compareAndSet(last, now)) {
            cleanup(now);
        }
    }

    private void cleanup(long now) {
        buckets.entrySet().removeIf(e -> e.getValue().isFull(now));
    }

    private static final class Bucket {
        private final double capacity;
        private final double tokensPerNano;
        private double tokens;
        private long lastRefill;

        Bucket(int capacity, Duration window, long now) {
            this.capacity = capacity;
            this.tokensPerNano = capacity / (double) window.toNanos();
            this.tokens = capacity;
            this.lastRefill = now;
        }

        synchronized long tryConsume(long now) {
            refill(now);
            if (tokens >= 1) {
                tokens -= 1;
                return 0;
            }
            double missingNanos = (1 - tokens) / tokensPerNano;
            return Math.max(1, (long) Math.ceil(missingNanos / 1_000_000_000d));
        }

        synchronized boolean isFull(long now) {
            refill(now);
            return tokens >= capacity;
        }

        private void refill(long now) {
            long elapsed = now - lastRefill;
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + elapsed * tokensPerNano);
                lastRefill = now;
            }
        }
    }
}
