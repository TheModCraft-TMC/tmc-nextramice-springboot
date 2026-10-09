package net.themodcraft.nextramice.bff;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/** Refresh-aware, per-session synchronized access to server-side authorized clients. */
public final class NextramiceAuthorizedClientService {

    private final OAuth2AuthorizedClientManager manager;
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public NextramiceAuthorizedClientService(OAuth2AuthorizedClientManager manager) {
        this.manager = manager;
    }

    public OAuth2AuthorizedClient authorize(
            String registrationId,
            Authentication principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String sessionId = request.getSession().getId();
        String key = sessionId + ':' + registrationId;
        ReentrantLock lock = locks.get(key);
        if (lock == null) {
            ReentrantLock candidate = new ReentrantLock();
            ReentrantLock existing = locks.putIfAbsent(key, candidate);
            lock = existing == null ? candidate : existing;
        }
        lock.lock();
        try {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(registrationId)
                    .principal(principal)
                    .attribute(HttpServletRequest.class.getName(), request)
                    .attribute(HttpServletResponse.class.getName(), response)
                    .build();
            return Objects.requireNonNull(
                    manager.authorize(authorizeRequest),
                    "OAuth authorization failed for " + registrationId
            );
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                locks.remove(key, lock);
            }
        }
    }
}
