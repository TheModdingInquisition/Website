package org.moddinginquisition.web.common.util

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.http.HttpResponseException
import io.javalin.http.util.RateLimitUtil

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@CompileStatic
class McRateLimit {
    private static final Map<TimeUnit, McRateLimiter> LIMITERS = new ConcurrentHashMap<TimeUnit, McRateLimiter>()
    private static String getKey(Context context) {
        return context.header('mc-uuid') + context.method() + context.matchedPath()
    }

    private static final ScheduledExecutorService executor = {
        final f = RateLimitUtil.getDeclaredField('executor')
        f.setAccessible(true)
        return (ScheduledExecutorService) f.get(null)
    }.call()

    @Canonical
    static class McRateLimiter {
        TimeUnit timeUnit

        private final var timeUnitString = removeSuffix(timeUnit.toString().toLowerCase(Locale.ROOT), 's')
        private final Map<String, Integer> keyToRequestCount = new ConcurrentHashMap<String, Integer>().with {
            McRateLimit.executor.scheduleAtFixedRate(() -> it.clear(), /*delay=*/0,  /*period=*/1, this.timeUnit)
            it
        }

        private void incrementCounter(Context ctx, int requestLimit) {
            if (!ctx.header('mc-uuid')) {
                throw new HttpResponseException(HttpCode.BAD_REQUEST.status, "Missing required 'mc-uuid' header!")
            }
            final key = getKey(ctx)
            final cnt = keyToRequestCount.computeIfAbsent(key, s -> 0)
            if (cnt < requestLimit) {
                keyToRequestCount.put(key, cnt + 1)
            } else {
                throw new HttpResponseException(429, "Rate limit exceeded - Server allows $requestLimit requests per $timeUnitString.")
            }
        }
    }

    static void requestPerTimeUnit(Context context, int requestsNum, TimeUnit unit) {
        if (Boolean.getBoolean('org.moddinginquisition.web.inDev')) {
            return // In-dev shouldn't require an uuid
        }
        LIMITERS.computeIfAbsent(unit, k -> new McRateLimiter(unit)).incrementCounter(context, requestsNum)
    }

    private static String removeSuffix(String self, String suffix) {
        if (self.endsWith(suffix)) {
            return self.substring(0, self.length() - suffix.length())
        }
        return this
    }
}
