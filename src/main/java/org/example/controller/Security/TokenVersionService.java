package org.example.controller.Security;

import org.example.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Makes JWTs revocable.
 *
 * Every user has a token version (users.token_version) that is written into each token they get.
 * A token is only accepted while its version still matches the user's current version, so
 * increasing the version ("log out on all devices", password change, account compromise)
 * instantly invalidates every token issued before. Tokens of deleted users stop working too.
 *
 * The version is cached for a few seconds so normal requests don't each hit the database.
 * The cache is cleared right away when this server changes the version.
 */
@Component
public class TokenVersionService {

    private static final long CACHE_MILLIS = 30_000;
    private static final int MAX_CACHE_ENTRIES = 10_000;

    private record Cached(Integer version, long expiresAt) {}

    private final UserRepository userRepository;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    public TokenVersionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** True if the user still exists and the token was issued with their current version. */
    public boolean isCurrent(String username, int tokenVersion) {
        Integer current = currentVersion(username);
        return current != null && current == tokenVersion;
    }

    /** Invalidates every token the user has. */
    public void revokeAll(String username) {
        userRepository.incrementTokenVersion(username);
        cache.remove(username);
    }

    /** Current version, or null if there is no such user. */
    private Integer currentVersion(String username) {
        long now = System.currentTimeMillis();
        Cached cached = cache.get(username);
        if (cached != null && cached.expiresAt() > now) return cached.version();

        Optional<Integer> fromDb = userRepository.findTokenVersionByUsername(username);
        if (cache.size() >= MAX_CACHE_ENTRIES) cache.clear();
        Integer version = fromDb.orElse(null);
        cache.put(username, new Cached(version, now + CACHE_MILLIS));
        return version;
    }
}
