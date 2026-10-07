package org.example.repository;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

/**
 * Holds things waiting for an e-mail link to be clicked (new accounts, coach requests),
 * keyed by a long random token that goes into the link.
 *
 * - Tokens are 32 random bytes from SecureRandom (43 URL-safe characters), so they can't be
 *   guessed or brute-forced, unlike the old 6-digit codes.
 * - Every entry expires after `ttl`; expired entries are ignored and cleaned up.
 * - At most `maxEntries` are kept, so a flood of sign-ups can't use up the server's memory.
 * - A token works once: taking it removes it.
 *
 * Stored in memory, so pending entries are lost when the server restarts (users can use "resend").
 */
public class PendingTokenStore<T> {

    private record Entry<T>(T value, long expiresAtMillis) {}

    /** A token together with the value stored under it. */
    public record Issued<T>(String token, T value) {}

    private static final int TOKEN_BYTES = 32;
    /** Real tokens are 43 characters; anything much longer is junk and isn't looked up. */
    private static final int MAX_TOKEN_LENGTH = 64;

    private final Map<String, Entry<T>> entries = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Duration ttl;
    private final int maxEntries;
    private final LongSupplier clockMillis;

    public PendingTokenStore(Duration ttl, int maxEntries) {
        this(ttl, maxEntries, System::currentTimeMillis);
    }

    PendingTokenStore(Duration ttl, int maxEntries, LongSupplier clockMillis) {
        this.ttl = ttl;
        this.maxEntries = maxEntries;
        this.clockMillis = clockMillis;
    }

    /** Stores `value` under a new token. Returns empty if the store is full. */
    public Optional<String> add(T value) {
        long now = clockMillis.getAsLong();
        if (entries.size() >= maxEntries) {
            removeExpired(now);
            if (entries.size() >= maxEntries) return Optional.empty();
        }
        Entry<T> entry = new Entry<>(value, now + ttl.toMillis());
        while (true) {
            String token = newToken();
            if (entries.putIfAbsent(token, entry) == null) return Optional.of(token);
        }
    }

    /** Removes and returns the value for `token`, or null if the token is unknown, used or expired. */
    public T take(String token) {
        if (token == null || token.isEmpty() || token.length() > MAX_TOKEN_LENGTH) return null;
        Entry<T> entry = entries.remove(token);
        if (entry == null || entry.expiresAtMillis() <= clockMillis.getAsLong()) return null;
        return entry.value();
    }

    /** First non-expired value matching `match`. */
    public Optional<T> find(Predicate<T> match) {
        long now = clockMillis.getAsLong();
        return entries.values().stream()
                .filter(e -> e.expiresAtMillis() > now && match.test(e.value()))
                .map(Entry::value)
                .findFirst();
    }

    public boolean contains(Predicate<T> match) {
        return find(match).isPresent();
    }

    /**
     * Finds a non-expired value matching `match`, removes its old token and stores it again under a
     * new token with a fresh expiry (used for "resend e-mail").
     */
    public Optional<Issued<T>> reissue(Predicate<T> match) {
        long now = clockMillis.getAsLong();
        for (Map.Entry<String, Entry<T>> e : entries.entrySet()) {
            Entry<T> entry = e.getValue();
            if (entry.expiresAtMillis() > now && match.test(entry.value())
                    && entries.remove(e.getKey(), entry)) {
                return add(entry.value()).map(token -> new Issued<>(token, entry.value()));
            }
        }
        return Optional.empty();
    }

    int size() {
        return entries.size();
    }

    private void removeExpired(long now) {
        entries.values().removeIf(e -> e.expiresAtMillis() <= now);
    }

    private String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
