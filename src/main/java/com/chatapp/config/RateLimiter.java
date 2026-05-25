package com.chatapp.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter {

    private static final int MAX_MESSAGES_PER_MINUTE = 30;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, RateWindow> windows = new ConcurrentHashMap<>();

    public boolean isAllowed(String username) {
        long now = System.currentTimeMillis();
        RateWindow window = windows.compute(username, (key, existing) -> {
            if (existing == null || now - existing.startTime > WINDOW_MS) {
                return new RateWindow(now);
            }
            return existing;
        });

        return window.count.incrementAndGet() <= MAX_MESSAGES_PER_MINUTE;
    }

    private static class RateWindow {
        final long startTime;
        final AtomicInteger count;

        RateWindow(long startTime) {
            this.startTime = startTime;
            this.count = new AtomicInteger(0);
        }
    }
}

