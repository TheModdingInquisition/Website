package org.moddinginquisition.web.backend.auth

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import groovy.transform.CompileStatic
import io.javalin.http.Context
import io.javalin.http.HttpCode
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.moddinginquisition.web.backend.Configuration

import java.util.concurrent.TimeUnit

@CompileStatic
class AuthResolver implements Runnable {

    private static final Cache<String, String> BY_TOKEN = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build()

    static String getLogin(String token) throws AuthException {
        final fromCache = BY_TOKEN.getIfPresent token
        if (fromCache) return fromCache
        try {
            final login = new GitHubBuilder()
                    .withJwtToken(token)
                    .build()
                    .getMyself()
                    .getLogin()
            BY_TOKEN.put token, login
            return login
        } catch (IOException e) {
            throw new AuthException(e)
        }
    }

    private static final List<String> JANITORS = new ArrayList<>()
    static boolean isUserJanitor(String login) {
        JANITORS.contains(login)
    }

    static boolean isJanitor(Context context) throws IOException {
        final header = context.header('Authorization')
        if (header) {
            try {
                final isJanitor = isUserJanitor(getLogin(header))
                if (!isJanitor) {
                    context.status(HttpCode.FORBIDDEN)
                    context.result("{'err': 'This endpoint requires you to be a Janitor'}")
                }
                return isJanitor
            } catch (AuthException ignored) {
                context.status(HttpCode.UNAUTHORIZED)
                context.result("{'err': 'Invalid token provided'}")
                return false
            }
        }
        return false
    }

    private final GitHub gitHub
    private final Configuration.GitHub conf
    AuthResolver(GitHub gitHub, Configuration.GitHub conf) {
        this.gitHub = gitHub
        this.conf = conf
    }

    @Override
    void run() {
        JANITORS.clear()
        JANITORS.addAll(gitHub.getOrganization(conf.organization)
                .getTeamByName(conf.janitorsTeam)
                .getMembers()
                .collect {it.getLogin()})
    }
}
